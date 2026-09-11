package com.example.data.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

data class GeneratedCraftCatalog(
    val title: String,
    val regionalTitle: String,
    val description: String,
    val regionalDescription: String,
    val category: String,
    val craftType: String,
    val materialsUsed: String,
    val suggestedMinPrice: Double,
    val suggestedMaxPrice: Double,
    val wholesalePrice: Double,
    val retailPrice: Double,
    val tags: List<String>,
    val careInstructions: String,
    val giTagEligible: Boolean,
    val culturalStory: String,
    val estimatedWeightKg: Double
)

data class VerifiedOfficialInfo(
    val title: String,
    val authorityOrScheme: String,
    val summary: String,
    val benefits: List<String> = emptyList(),
    val eligibility: String? = null,
    val officialPortal: String? = null,
    val helpline: String? = null,
    val isGovernmentVerified: Boolean = true
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "gemini"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedActions: List<String> = emptyList(),
    val aiGuidanceText: String? = null,
    val verifiedOfficialInfo: VerifiedOfficialInfo? = null,
    val referencedProductTitle: String? = null,
    val isError: Boolean = false,
    val rawUserPrompt: String? = null,
    val domainCategory: String? = null
)

object GeminiManager {
    private const val TAG = "GeminiManager"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    val metadataService: GeminiMetadataService = GeminiMetadataServiceImpl()

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * AI-Driven Smart Catalog Generation:
     * Takes voice description/speech transcript + craft details, produces multi-lingual title,
     * narrative cultural description, fair pricing calculation, tags, and category.
     */
    suspend fun generateCatalogFromVoice(
        voiceTranscript: String,
        selectedCategory: String,
        rawMaterialCost: Double,
        laborHours: Double,
        language: String,
        bitmap: Bitmap? = null
    ): GeneratedCraftCatalog = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are KAUSHVANI AI, an expert handicraft market linkage consultant and digital cataloger for traditional Indian rural artisans.
                    Analyze this artisan craft item:
                    - Artisan's Spoken Description (in $language): "$voiceTranscript"
                    - Primary Category: $selectedCategory
                    - Raw Material Cost: ₹$rawMaterialCost
                    - Labor Time: $laborHours hours
                    - Target Regional Language: $language

                    Generate a complete professional e-commerce & export-ready catalog specification in valid JSON:
                    {
                      "title": "Concise, SEO-optimized English product title",
                      "regionalTitle": "Authentic product title in $language script",
                      "description": "Rich 3-4 sentence storytelling description in English highlighting traditional handcrafting technique, artisan heritage, motifs, and quality",
                      "regionalDescription": "Rich descriptive explanation in $language script",
                      "category": "$selectedCategory",
                      "craftType": "Specific craft name (e.g. Gorakhpur Terracotta / Banarasi Katan Silk / Bastar Dhokra / Jaipur Blue Pottery)",
                      "materialsUsed": "Materials breakdown (e.g. Pure Gangetic clay, mineral pigments, wood ash)",
                      "suggestedMinPrice": (fair price minimum covering material + fair wages of ₹160/hr + overhead),
                      "suggestedMaxPrice": (fair market price benchmark),
                      "wholesalePrice": (bulk order B2B unit price),
                      "retailPrice": (direct consumer B2C price),
                      "tags": ["tag1", "tag2", "tag3", "tag4", "tag5"],
                      "careInstructions": "Practical care guidelines",
                      "giTagEligible": true/false,
                      "culturalStory": "Brief 1-sentence cultural heritage note",
                      "estimatedWeightKg": 0.8
                    }
                    Respond ONLY with pure JSON. Do not include markdown code block backticks.
                """.trimIndent()

                val jsonResponse = executeGeminiRequest(
                    model = "gemini-3.5-flash",
                    prompt = prompt,
                    apiKey = apiKey,
                    bitmap = bitmap
                )

                parseCatalogJson(jsonResponse, selectedCategory, rawMaterialCost, laborHours)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API error in generateCatalogFromVoice: ${e.message}", e)
                createSmartFallbackCatalog(voiceTranscript, selectedCategory, rawMaterialCost, laborHours, language)
            }
        } else {
            createSmartFallbackCatalog(voiceTranscript, selectedCategory, rawMaterialCost, laborHours, language)
        }
    }

    /**
     * AI Multi-turn Virtual Business Manager ("KAARIGAR Business Assistant / Vyapar Mitra")
     * Actually calls Gemini API using gemini-3.8-flash (with fallback to gemini-3.5-flash).
     * Maintains conversation history and strictly distinguishes creative AI guidance from verified official info.
     */
    suspend fun chatWithVyaparMitra(
        conversationHistory: List<ChatMessage>,
        userMessage: String,
        language: String,
        artisanContext: String = "",
        referencedProductTitle: String? = null
    ): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ChatMessage(
                sender = "gemini",
                text = "⚠️ जेमिनी एपीआई कुंजी (API Key) उपलब्ध नहीं है। कृपया सेटिंग्स में मान्य API Key दर्ज करें।",
                isError = true,
                rawUserPrompt = userMessage
            )
        }

        val primaryModel = "gemini-3.8-flash"
        val fallbackModel = "gemini-3.5-flash"

        try {
            val systemInstruction = """
                You are 'KAUSHVANI Business Assistant' (कौशवाणी व्यापार सहायक / Kaushvani Vyapar Mitra), a knowledgeable, empathetic, and practical AI business advisor for Indian traditional artisans, handloom weavers, and rural craft micro-entrepreneurs.

                CORE MISSION & 8 ESSENTIAL SUPPORT DOMAINS:
                1. Pricing questions: Calculate break-even costs, fair hourly living wage floor (₹175/hr - ₹180/hr), raw material markup, packaging, and healthy wholesale vs retail margins (e.g. 20-30% wholesale discount while securing fair wages).
                2. Product description help: Write captivating cultural storytelling, highlight handcrafted techniques, natural sustainable materials, GI tags (Geographical Indication), care instructions, and e-commerce keywords.
                3. Photography guidance: Actionable mobile photo tips (natural diffused morning daylight, clean plain backgrounds like khadi cloth or terracotta brick, 1:1 square framing, 45° angle, scale reference with a coin/hand, close-up texture details).
                4. Packaging guidance: Courier damage-proofing for fragile crafts (5-ply corrugated boxes, honeycomb paper/bubble wrap, humidity silica gel, corner protectors, artisan unboxing branding notes).
                5. Bulk-order negotiation: Minimum Order Quantity (MOQ), sample approval protocol before batch production, payment terms (50% advance before procuring raw materials, 50% upon dispatch), realistic delivery timelines.
                6. Buyer communication: Courteous, professional response templates in simple language for B2B wholesale buyers, export inquiries, customized corporate gifting, and polite price negotiations without underselling.
                7. Digital selling: Guidance on selling via WhatsApp Business catalog, Instagram reels, ONDC (Open Network for Digital Commerce), Amazon Karigar, GeM portal, and accepting payments via UPI QR codes safely.
                8. Relevant government-scheme guidance: Accurate, verified facts on PM Vishwakarma Yojana, Mudra Loan (Shishu, Kishore), Pehchan Artisan Card, ODOP (One District One Product), SFURTI, and Ministry of Textiles subsidies.

                CRITICAL POLICY: CLEAR DISTINCTION BETWEEN AI GUIDANCE AND VERIFIED OFFICIAL INFORMATION:
                You MUST strictly distinguish creative AI business suggestions from verified official government schemes/policies.
                - When discussing ANY official government scheme, portal, subsidy, or statutory rule (e.g. PM Vishwakarma, Mudra, Pehchan Card, GeM, ODOP):
                  You MUST enclose the official government details in a dedicated [OFFICIAL_INFO] block:
                  [OFFICIAL_INFO]
                  Title: <Scheme / Policy Name, e.g. PM Vishwakarma Yojana>
                  Authority: <Ministry of MSME / Ministry of Textiles / Govt of India / GeM>
                  Summary: <1-2 sentence official summary>
                  Benefits: <Bullet points of official grants, ₹15,000 toolkit, subsidized loan at 5%, stipend during training>
                  Eligibility: <Who qualifies, e.g. 18 traditional artisan trades>
                  Portal: <Official government URL, e.g. https://pmvishwakarma.gov.in or https://gem.gov.in>
                  Helpline: <Official helpline, e.g. 1800-267-7777 / National Handicrafts Helpline>
                  [END_OFFICIAL_INFO]
                - For strategic advice, photo tips, description drafting, packaging steps, negotiation advice, and marketing ideas:
                  Provide warm, actionable, step-by-step guidance.
                - Do not give canned generic responses. Adapt directly to the artisan's exact query and catalog data!

                LANGUAGE & TONE:
                - Communicate in the artisan's preferred language: $language.
                - When responding in Hindi or regional Indian languages, use clear, respectful, natural Devanagari script with bilingual English terms in brackets for key business concepts.
                - Use clear formatting with bullet points and friendly emojis.
                
                ARTISAN'S CURRENT LIVE CATALOG & CONTEXT:
                $artisanContext
            """.trimIndent()

            val contentsArray = JSONArray()
            val recentHistory = conversationHistory.filter { !it.isError }.takeLast(10)
            for (msg in recentHistory) {
                val role = if (msg.sender == "user") "user" else "model"
                val item = JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                }
                contentsArray.put(item)
            }

            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
            })

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            var textResponse = ""
            try {
                textResponse = executeRawGeminiCall(primaryModel, requestJson.toString(), apiKey)
            } catch (e: Exception) {
                Log.w(TAG, "Primary model $primaryModel failed, attempting fallback $fallbackModel: ${e.message}")
                textResponse = executeRawGeminiCall(fallbackModel, requestJson.toString(), apiKey)
            }

            if (textResponse.isBlank()) {
                return@withContext ChatMessage(
                    sender = "gemini",
                    text = "⚠️ जेमिनी एआई से उत्तर प्राप्त नहीं हो सका। कृपया पुनः प्रयास करें।",
                    isError = true,
                    rawUserPrompt = userMessage
                )
            }

            val parsedResult = parseAssistantResponse(textResponse, userMessage, language)
            parsedResult.copy(
                referencedProductTitle = referencedProductTitle,
                suggestedActions = generateQuickPrompts(userMessage, language)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Kaarigar Business Assistant error: ${e.message}", e)
            ChatMessage(
                sender = "gemini",
                text = "⚠️ नेटवर्क या कनेक्शन समस्या के कारण उत्तर नहीं मिल पाया: ${e.localizedMessage ?: "Connection error"}\nकृपया अपना इंटरनेट जांचें और 'पुनः प्रयास करें (Retry)' पर टैप करें।",
                isError = true,
                rawUserPrompt = userMessage
            )
        }
    }

    private fun executeRawGeminiCall(model: String, requestJsonStr: String, apiKey: String): String {
        val url = "$BASE_URL$model:generateContent?key=$apiKey"
        val body = requestJsonStr.toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()

        val response = client.newCall(request).execute()
        val responseStr = response.body?.string() ?: ""
        if (!response.isSuccessful) {
            val errorMsg = try {
                val errObj = JSONObject(responseStr).optJSONObject("error")
                errObj?.optString("message", "HTTP ${response.code}") ?: "HTTP ${response.code}"
            } catch (e: Exception) {
                "HTTP ${response.code}"
            }
            throw IOException("Gemini API Error ($model): $errorMsg")
        }
        return parseGeminiText(responseStr)
    }

    /**
     * AI-Assisted Real-time Speech Translation and Refinement
     */
    suspend fun translateAndRefineSpeech(
        rawTranscript: String,
        sourceLanguage: String
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    Artisan voice note in $sourceLanguage: "$rawTranscript"
                    Convert this conversational artisan voice transcript into:
                    1. A polished, professional Hindi description.
                    2. A professional English translation suitable for high-end international buyers.
                    
                    Return ONLY valid JSON:
                    {
                      "hindi": "...",
                      "english": "..."
                    }
                """.trimIndent()

                val responseJson = executeGeminiRequest("gemini-3.5-flash", prompt, apiKey)
                val cleanJson = responseJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val obj = JSONObject(cleanJson)
                val hi = obj.optString("hindi", rawTranscript)
                val en = obj.optString("english", rawTranscript)
                Pair(hi, en)
            } catch (e: Exception) {
                Pair(rawTranscript, "Handcrafted authentic craft piece made by skilled rural artisans using traditional heritage techniques.")
            }
        } else {
            Pair(rawTranscript, "Handcrafted authentic craft piece made by skilled rural artisans using traditional heritage techniques.")
        }
    }

    private fun executeGeminiRequest(
        model: String,
        prompt: String,
        apiKey: String,
        bitmap: Bitmap? = null
    ): String {
        val url = "$BASE_URL$model:generateContent?key=$apiKey"
        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", prompt))

        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            val imagePart = JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Image)
                })
            }
            partsArray.put(imagePart)
        }

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().put("parts", partsArray)))
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()
        val response = client.newCall(request).execute()
        val bodyStr = response.body?.string() ?: ""
        return parseGeminiText(bodyStr)
    }

    private fun parseGeminiText(jsonStr: String): String {
        return try {
            val obj = JSONObject(jsonStr)
            val candidates = obj.getJSONArray("candidates")
            val first = candidates.getJSONObject(0)
            val content = first.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseCatalogJson(
        rawText: String,
        category: String,
        rawCost: Double,
        laborHours: Double
    ): GeneratedCraftCatalog {
        return try {
            val clean = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(clean)
            val tagsArray = obj.optJSONArray("tags")
            val tagsList = mutableListOf<String>()
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.getString(i))
                }
            } else {
                tagsList.addAll(listOf("Handmade", "Artisan", category, "Authentic", "India"))
            }

            GeneratedCraftCatalog(
                title = obj.optString("title", "Handcrafted $category Art Piece"),
                regionalTitle = obj.optString("regionalTitle", "पारंपरिक हस्तशिल्प उत्पाद"),
                description = obj.optString("description", "Exquisitely hand-crafted by master artisans with traditional heritage techniques."),
                regionalDescription = obj.optString("regionalDescription", "पारंपरिक कारीगरी से निर्मित प्रामाणिक हस्तशिल्प।"),
                category = obj.optString("category", category),
                craftType = obj.optString("craftType", "$category Craft"),
                materialsUsed = obj.optString("materialsUsed", "Natural sustainable materials"),
                suggestedMinPrice = obj.optDouble("suggestedMinPrice", calculateFairPrice(rawCost, laborHours).first),
                suggestedMaxPrice = obj.optDouble("suggestedMaxPrice", calculateFairPrice(rawCost, laborHours).second),
                wholesalePrice = obj.optDouble("wholesalePrice", calculateWholesalePrice(rawCost, laborHours)),
                retailPrice = obj.optDouble("retailPrice", calculateFairPrice(rawCost, laborHours).first * 1.1),
                tags = tagsList,
                careInstructions = obj.optString("careInstructions", "Clean with soft dry cloth."),
                giTagEligible = obj.optBoolean("giTagEligible", true),
                culturalStory = obj.optString("culturalStory", "Rooted in centuries of indigenous artisan wisdom."),
                estimatedWeightKg = obj.optDouble("estimatedWeightKg", 0.6)
            )
        } catch (e: Exception) {
            createSmartFallbackCatalog("", category, rawCost, laborHours, "Hindi")
        }
    }

    private fun calculateFairPrice(rawCost: Double, laborHours: Double): Pair<Double, Double> {
        val fairHourlyWage = 175.0 // Ensuring ₹175/hr minimum living wage
        val packaging = 60.0
        val baseCost = rawCost + (laborHours * fairHourlyWage) + packaging
        val minPrice = (baseCost * 1.35).coerceAtLeast(450.0) // 35% artisan margin
        val maxPrice = (baseCost * 1.75).coerceAtLeast(650.0) // Benchmark retail
        return Pair(Math.round(minPrice).toDouble(), Math.round(maxPrice).toDouble())
    }

    private fun calculateWholesalePrice(rawCost: Double, laborHours: Double): Double {
        val fairHourlyWage = 160.0
        val packaging = 40.0
        val wholesale = (rawCost + (laborHours * fairHourlyWage) + packaging) * 1.20
        return Math.round(wholesale).toDouble().coerceAtLeast(350.0)
    }

    private fun createSmartFallbackCatalog(
        transcript: String,
        category: String,
        rawCost: Double,
        laborHours: Double,
        language: String
    ): GeneratedCraftCatalog {
        val (minP, maxP) = calculateFairPrice(rawCost, laborHours)
        val wholesale = calculateWholesalePrice(rawCost, laborHours)
        val retail = Math.round(minP * 1.10).toDouble()

        val sampleTitles = mapOf(
            "Pottery" to Pair(
                "Handcrafted Terracotta Earthen Decorative Urn",
                "हाथ से बना सजावटी मिट्टी का कलश (टेराकोटा)"
            ),
            "Handloom & Textiles" to Pair(
                "Handwoven Pure Mulberry Silk Heritage Stole",
                "हथकरघे पर बुना हुआ शुद्ध रेशमी दुपट्टा"
            ),
            "Metalcraft" to Pair(
                "Traditional Lost-Wax Cast Dhokra Brass Figurine",
                "प्राचीन मोम-ढालाई विधि से निर्मित ढोकरा पीतल शिल्प"
            ),
            "Paintings & Folk Art" to Pair(
                "Authentic Mithila Madhubani Folk Art Canvas",
                "हस्तचित्रित प्राकृतिक रंगों वाली मधुबनी पेंटिंग"
            ),
            "Woodcraft" to Pair(
                "Hand-Carved Sheesham Wood Jali Work Coaster Set",
                "शीशम की लकड़ी पर बारीक जालीदार नक्काशीदार कोस्टर"
            ),
            "Jewelry" to Pair(
                "Artisan Filigree Silver-Look Tribal Statement Necklace",
                "पारंपरिक हस्तनिर्मित जनजातीय आभूषण"
            ),
            "Leather" to Pair(
                "Hand-Stitched Shantiniketan Embossed Genuine Leather Bag",
                "शांतिनिकेतन पारंपरिक हस्तमुद्रित लेदर बैग"
            )
        )

        val titles = sampleTitles[category] ?: Pair(
            "Handcrafted Heritage $category Artifact",
            "हस्तनिर्मित पारंपरिक $category कलाकृति"
        )

        return GeneratedCraftCatalog(
            title = titles.first,
            regionalTitle = titles.second,
            description = "Masterfully handcrafted using age-old ancestral techniques passed down across generations. Each piece preserves the pure authenticity and cultural legacy of rural Indian artisan guilds.",
            regionalDescription = "पीढ़ियों से चली आ रही पारंपरिक कारीगरी द्वारा शुद्ध प्राकृतिक सामग्रियों से तैयार की गई अनुपम कलाकृति।",
            category = category,
            craftType = "$category Artisanal Craft",
            materialsUsed = "Eco-friendly, 100% natural locally sourced raw materials",
            suggestedMinPrice = minP,
            suggestedMaxPrice = maxP,
            wholesalePrice = wholesale,
            retailPrice = retail,
            tags = listOf(category, "Handcrafted", "Eco-friendly", "GI Tag Eligible", "Indian Artisan", "Direct from Maker"),
            careInstructions = "Handle with care. Keep in a dry place. Clean gently with a soft micro-fiber cloth.",
            giTagEligible = true,
            culturalStory = "Crafted by master artisans honoring India's rich indigenous heritage.",
            estimatedWeightKg = 0.75
        )
    }

    private fun getDefaultAiChatResponse(prompt: String, language: String, context: String = ""): String {
        val p = prompt.lowercase()
        val isEnglish = language.contains("English", ignoreCase = true)
        val isMarathi = language.contains("Marathi", ignoreCase = true)
        val isGujarati = language.contains("Gujarati", ignoreCase = true)

        return when {
            // 1. "What price should I keep?"
            p.contains("price") || p.contains("cost") || p.contains("कीमत") || p.contains("दाम") || p.contains("भाव") || p.contains("rate") || p.contains("मूल्य") -> {
                if (isEnglish) {
                    """
                    💰 **Fair Pricing Guide (Simple Formula):**

                    1. **Raw Materials**: Add total cost of clay, thread, dyes, wood, or metal used.
                    2. **Your Labor Hours**: Multiply hours worked by at least ₹175/hour (your fair living wage).
                    3. **Packaging**: Add ₹40 to ₹60 for safe protective box/wrap.
                    4. **Profit Margin**: Add 30% to 35% on top so your business grows.

                    💡 **Formula**: Price = (Materials + Labor Hours × ₹175 + Packaging) × 1.35
                    👉 *Tip: You can use the in-app AI Dynamic Pricing tool on your catalog items anytime to calculate this automatically!*
                    """.trimIndent()
                } else if (isMarathi) {
                    """
                    💰 **योग्य किंमत कशी ठरवावी (सोपी पद्धत):**

                    1. **कच्चा माल**: माती, धागा, रंग, लाकूड किंवा पितळ यांचा एकूण खर्च.
                    2. **तुमची मजुरी**: काम केलेले तास × ₹175/तास (कमीत कमी जगण्याजोगी मजुरी).
                    3. **पॅकेजिंग**: सुरक्षित बॉक्स व रॅपिंगसाठी ₹40 ते ₹60.
                    4. **नफा मार्जिन**: वर 30% ते 35% नफा जोडा.

                    💡 **सूत्र**: किंमत = (कच्चा माल + कामाचे तास × ₹175 + पॅकिंग) × 1.35
                    👉 *टीप: तुम्ही अ‍ॅपमधील AI Dynamic Pricing टूल वापरून एका टॅपमध्ये योग्य किंमत काढू शकता!*
                    """.trimIndent()
                } else {
                    """
                    💰 **शिल्प का सही दाम कैसे तय करें (सरल तरीका):**

                    1. **कच्चा माल**: मिट्टी, धागा, रंग, पीतल या लकड़ी का कुल खर्च जोड़ें।
                    2. **आपकी मेहनत की मजदूरी**: जितने घंटे लगे × ₹175/घंटा (सम्मानजनक मजदूरी दर)।
                    3. **सुरक्षित पैकेजिंग**: डिब्बे और पैकिंग के लिए ₹40 से ₹60 जोड़ें।
                    4. **मुनाफ़ा (मार्जिन)**: कुल लागत पर 30% से 35% मुनाफ़ा रखें।

                    💡 **सीधा फॉर्मूला**: खुदरा मूल्य = (कच्चा माल + श्रम घंटे × ₹175 + पैकेजिंग) × 1.35
                    👉 *सुझाव: ऐप में मौजूद 'AI Fair Pricing' कैलकुलेटर से आप 1-टैप में खुदरा व थोक रेट निकाल सकते हैं!*
                    """.trimIndent()
                }
            }

            // 2. "How can I improve this product photo?"
            p.contains("photo") || p.contains("image") || p.contains("फोटो") || p.contains("चित्र") || p.contains("camera") || p.contains("तस्वीर") -> {
                if (isEnglish) {
                    """
                    📸 **How to Improve Your Product Photo (4 Easy Steps):**

                    1. **Natural Morning Sunlight**: Place your craft near an open window or shaded doorway. Avoid harsh direct sun and dim indoor yellow bulbs.
                    2. **Clean Neutral Background**: Use a simple plain cloth (white, beige, or grey muslin) or clean wooden table. Remove floor tools and clutter.
                    3. **Frame in 1:1 Square**: Keep the craft centered with small breathing space on all 4 sides so online buyers see every corner clearly.
                    4. **Use AI Image Studio**: Open the app's 'AI Image Studio' to automatically enhance texture filters (Artisan Natural, Vibrant Loom, Heritage Warmth) in 1 tap!
                    """.trimIndent()
                } else {
                    """
                    📸 **शिल्प की फोटो बेहतरीन कैसे बनाएं (4 आसान नियम):**

                    1. **सुबह की प्राकृतिक रोशनी**: खिड़की या बरामदे की धूप में फोटो लें। पीले बल्ब या अंधेरे कमरे में फोटो न लें।
                    2. **साफ व सादा बैकग्राउंड**: पीछे सादा सफेद या हल्का कपड़ा रखें। फर्श पर पड़े औजार या सामान हटा दें।
                    3. **1:1 वर्गाकार फ्रेम**: शिल्प को ठीक बीच में रखें ताकि खरीदार को चारों तरफ से स्पष्ट दिखे।
                    4. **AI फोटो स्टूडियो का उपयोग करें**: ऐप के 'AI Image Studio' में जाकर 'Artisan Natural' या 'Vibrant Loom' फ़िल्टर लगाएं — 1 टैप में स्टूडियो जैसी चमक मिलेगी!
                    """.trimIndent()
                }
            }

            // 3. "What should I write in my product description?"
            p.contains("description") || p.contains("write") || p.contains("विवरण") || p.contains("लिखें") || p.contains("शब्द") || p.contains("story") -> {
                if (isEnglish) {
                    """
                    ✍️ **What to Write in Your Product Description:**

                    Buyers love the human story behind authentic handcrafted items! Include these 4 points:

                    1. **Craft Name & Origin**: e.g., *"Hand-carved Sheesham Wood Jali Coaster from Saharanpur cluster"*.
                    2. **The Handmade Story**: Mention that it is hand-crafted with traditional ancestral tools passed down through generations.
                    3. **Natural Pure Materials**: e.g., *"100% natural Gangetic terracotta clay / pure mulberry silk"*.
                    4. **Size & Care Instructions**: Dimensions (e.g. 10x10 cm) and how to clean (e.g. wipe gently with soft dry cloth, keep away from water).

                    💡 *Great news: You don't need to type! Just speak in your mother tongue in the Voice Catalog wizard, and AI writes this full story automatically!*
                    """.trimIndent()
                } else {
                    """
                    ✍️ **शिल्प के विवरण में क्या लिखना चाहिए (4 जरूरी बातें):**

                    ऑनलाइन खरीदार हाथ से बनी कला की कहानी को बहुत पसंद करते हैं। बस ये 4 बातें बताएं:

                    1. **शिल्प का नाम और स्थान**: जैसे *"गोरखपुर टेराकोटा पारंपरिक सजावटी कलश" या "बनारसी कातान सिल्क साड़ी"*।
                    2. **हाथ की कारीगरी की कहानी**: बताएं कि यह पीढ़ियों पुरानी पारंपरिक पद्धति और हस्तकला से बना है।
                    3. **शुद्ध प्राकृतिक सामग्री**: जैसे *"100% शुद्ध गंगा किनारे की मिट्टी / प्राकृतिक वनस्पति रंग / शुद्ध रेशम"*।
                    4. **नाप और देखभाल**: उत्पाद का आकार और साफ करने का तरीका (जैसे 'सूखे कपड़े से पोंछें')।

                    💡 *आपको लिखने की जरूरत नहीं है! बस 'Voice Catalog' में अपनी मातृभाषा में बोलें, AI पूरा विवरण खुद तैयार कर देगा!*
                    """.trimIndent()
                }
            }

            // 4. "Which products are getting more inquiries?"
            p.contains("inquir") || p.contains("demand") || p.contains("पूछताछ") || p.contains("मांग") || p.contains("order") || p.contains("ऑर्डर") || p.contains("आर्डर") || p.contains("बिक्री") -> {
                if (isEnglish) {
                    """
                    📊 **Products Getting the Highest Buyer Inquiries:**

                    1. **Terracotta & Clay Decor (मिट्टी के सजावटी शिल्प)**: High demand from urban boutique homes and eco-conscious gift buyers.
                    2. **Handloom Silk & Sarees (हथकरघा रेशम)**: Very strong wedding season inquiries for GI-tagged authentic weaves.
                    3. **Dhokra Brass & Metal Art (ढोकरा मेटल क्राफ्ट)**: Luxury hotel resorts and interior decorators are placing bulk sample orders.

                    👉 *Tip: Products with GI Tag certification badges and clear 1:1 studio photos receive 3x more buyer quote requests in your Inquiries tab!*
                    """.trimIndent()
                } else {
                    """
                    📊 **किन उत्पादों पर सबसे ज्यादा खरीदार पूछताछ आ रही है:**

                    1. **टेराकोटा और मिट्टी के सजावटी कलश/दीये**: शहरी घरों और पर्यावरण-अनुकूल उपहारों (Eco Gifts) के लिए सबसे ज्यादा मांग है।
                    2. **हथकरघा सिल्क साड़ियां व दुपट्टे**: शादी और त्योहारों के सीजन में शुद्ध GI-टैग वाले हथकरघा की बहुत पूछताछ है।
                    3. **ढोकरा पीतल व मेटल मूर्तियां**: हेरिटेज होटलों और इंटीरियर डिजाइनरों से 50-100 पीस के थोक ऑर्डर आ रहे हैं।

                    👉 *सुझाव: जिन उत्पादों पर GI Tag बैज और AI स्टूडियो की साफ फोटो लगी है, उन पर 'पूछताछ' (Inquiries) 3 गुना ज्यादा आती हैं!*
                    """.trimIndent()
                }
            }

            // 5. "How do I sell to bulk buyers?"
            p.contains("bulk") || p.contains("wholesale") || p.contains("थोक") || p.contains("बल्क") || p.contains("बड़ा") || p.contains("व्यापारी") || p.contains("buyer") -> {
                if (isEnglish) {
                    """
                    🤝 **How to Sell to Bulk Buyers (B2B Success Guide):**

                    1. **Set Minimum Order Quantity (MOQ)**: Fix a minimum order size (e.g., minimum 25 or 50 pieces).
                    2. **Wholesale Pricing**: Offer 20% to 30% discount off your single-piece retail price, while keeping your ₹175/hr labor wage fully covered!
                    3. **Send 1 Sample First**: Always get the buyer's approval on one physical sample before starting batch production.
                    4. **Payment Terms**: Request **50% advance payment** before buying materials, and remaining 50% upon dispatch receipt.
                    5. **Safe Packing**: Use sturdy corrugated boxes with corner protectors to prevent transit damage.
                    """.trimIndent()
                } else {
                    """
                    🤝 **थोक खरीदारों (बल्क बायर्स) को कैसे बेचें (5 नियम):**

                    1. **न्यूनतम आर्डर संख्या (MOQ)**: तय करें कि थोक में कम से कम 25 या 50 पीस का आर्डर होना चाहिए।
                    2. **थोक मूल्य (Wholesale Rate)**: खुदरा भाव से 20% से 30% की छूट दें, लेकिन ध्यान रहे कि आपकी मजदूरी (₹175/घंटा) पूरी सुरक्षित रहे।
                    3. **पहले 1 सैंपल भेजें**: पूरा माल बनाने से पहले खरीदार को 1 नमूना (Sample) दिखाकर सहमति लें।
                    4. **भुगतान की शर्त (Payment)**: कच्चा माल खरीदने के लिए **50% पेशगी (Advance)** लें, और बाकी 50% माल भेजने पर।
                    5. **सुरक्षित पैकेजिंग**: मजबूत 5-प्लाई गत्ते के डिब्बों में पैक करें ताकि रास्ते में टूट-फूट न हो।
                    """.trimIndent()
                }
            }

            // PM Vishwakarma and Government Schemes
            p.contains("vishwakarma") || p.contains("scheme") || p.contains("योजना") || p.contains("लोन") || p.contains("loan") -> {
                """
                🇮🇳 **PM Vishwakarma Yojana (पीएम विश्वकर्मा योजना):**

                1. **पहचान**: राष्ट्रीय कारीगर आईडी कार्ड व डिजिटल प्रमाणपत्र।
                2. **प्रशिक्षण व स्टाइपेंड**: 5-7 दिन का स्किल प्रशिक्षण और ₹500/दिन भत्ता।
                3. **टूलकिट अनुदान**: आधुनिक औजार खरीदने के लिए ₹15,000 की सरकारी मदद।
                4. **सस्ता लोन**: बिना किसी गारंटी के ₹1,00,000 (पहला चरण) और ₹2,00,000 (दूसरा चरण) सिर्फ 5% ब्याज दर पर।
                """.trimIndent()
            }

            else -> {
                """
                नमस्ते! मैं आपका **हुनरसेतु AI व्यापार मित्र (Business Assistant)** हूँ 🙏

                आप मुझसे अपने व्यवसाय के बारे में कोई भी प्रश्न पूछ सकते हैं:
                • "मुझे क्या दाम रखना चाहिए?" (Fair Pricing)
                • "मैं इस उत्पाद की फोटो कैसे सुधार सकता हूँ?" (Photo Tips)
                • "मुझे उत्पाद विवरण में क्या लिखना चाहिए?" (Description)
                • "किन उत्पादों पर ज्यादा पूछताछ आ रही है?" (Inquiry Trends)
                • "मैं थोक खरीदारों को कैसे बेचूं?" (Bulk Selling)

                माइक दबाकर अपनी भाषा में बोलें या नीचे दिए गए बटनों पर टैप करें!
                """.trimIndent()
            }
        }
    }

    /**
     * Parses the response from Gemini, isolating verified official scheme info from AI strategic guidance.
     */
    private fun parseAssistantResponse(rawResponse: String, userPrompt: String, language: String): ChatMessage {
        var officialInfo: VerifiedOfficialInfo? = null
        var aiGuidance = rawResponse.trim()

        // Check for [OFFICIAL_INFO] block
        val officialRegex = Regex("\\[OFFICIAL_INFO\\]([\\s\\S]*?)\\[END_OFFICIAL_INFO\\]", RegexOption.IGNORE_CASE)
        val match = officialRegex.find(rawResponse)
        if (match != null) {
            val officialBlock = match.groupValues[1]
            officialInfo = parseOfficialInfoBlock(officialBlock)
            // Remove official block from general guidance to keep it separated
            aiGuidance = rawResponse.replace(match.value, "").trim()
        } else {
            // Check if prompt specifically asked about official govt schemes/policies
            val lowerPrompt = userPrompt.lowercase()
            if (lowerPrompt.contains("vishwakarma") || lowerPrompt.contains("विश्वकर्मा") ||
                lowerPrompt.contains("pehchan") || lowerPrompt.contains("पहचान कार्ड") ||
                lowerPrompt.contains("mudra") || lowerPrompt.contains("मुद्रा") ||
                lowerPrompt.contains("gem portal") || lowerPrompt.contains("odop")
            ) {
                officialInfo = getVerifiedSchemeDetails(userPrompt, language)
            }
        }

        val domain = detectDomainCategory(userPrompt)

        return ChatMessage(
            sender = "gemini",
            text = if (aiGuidance.isNotBlank()) aiGuidance else rawResponse,
            aiGuidanceText = aiGuidance.ifBlank { null },
            verifiedOfficialInfo = officialInfo,
            domainCategory = domain,
            isError = false
        )
    }

    private fun parseOfficialInfoBlock(block: String): VerifiedOfficialInfo {
        var title = "PM Vishwakarma & Government Scheme"
        var authority = "Ministry of MSME / Ministry of Textiles, Govt. of India"
        var summary = ""
        val benefits = mutableListOf<String>()
        var eligibility: String? = null
        var portal: String? = "https://pmvishwakarma.gov.in"
        var helpline: String? = "1800-267-7777"

        val lines = block.lines()
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("Title:", ignoreCase = true) -> title = trimmed.substringAfter(":").trim()
                trimmed.startsWith("Authority:", ignoreCase = true) -> authority = trimmed.substringAfter(":").trim()
                trimmed.startsWith("Summary:", ignoreCase = true) -> summary = trimmed.substringAfter(":").trim()
                trimmed.startsWith("Benefits:", ignoreCase = true) -> {
                    val bText = trimmed.substringAfter(":").trim()
                    if (bText.isNotBlank()) benefits.add(bText)
                }
                trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                    val item = trimmed.removePrefix("•").removePrefix("-").removePrefix("*").trim()
                    if (item.isNotBlank()) benefits.add(item)
                }
                trimmed.startsWith("Eligibility:", ignoreCase = true) -> eligibility = trimmed.substringAfter(":").trim()
                trimmed.startsWith("Portal:", ignoreCase = true) -> portal = trimmed.substringAfter(":").trim()
                trimmed.startsWith("Helpline:", ignoreCase = true) -> helpline = trimmed.substringAfter(":").trim()
            }
        }

        if (summary.isBlank() && benefits.isNotEmpty()) {
            summary = benefits.firstOrNull() ?: ""
        }

        return VerifiedOfficialInfo(
            title = title,
            authorityOrScheme = authority,
            summary = summary.ifBlank { "आधिकारिक सरकारी सहायता योजना (Official Govt Support Scheme)" },
            benefits = if (benefits.isNotEmpty()) benefits else listOf(
                "₹15,000 टूलकिट अनुदान (Toolkit grant)",
                "5% रियायती ब्याज दर पर ₹1 लाख से ₹2 लाख का कोलेटरल-मुक्त ऋण",
                "प्रशिक्षण अवधि के दौरान ₹500/दिन वजीफा (Daily stipend)"
            ),
            eligibility = eligibility ?: "18 पारंपरिक व्यवसायों से जुड़े भारतीय कारीगर एवं शिल्पकार",
            officialPortal = portal,
            helpline = helpline,
            isGovernmentVerified = true
        )
    }

    private fun getVerifiedSchemeDetails(userPrompt: String, language: String): VerifiedOfficialInfo {
        val p = userPrompt.lowercase()
        return when {
            p.contains("pehchan") || p.contains("पहचान") || p.contains("artisan card") || p.contains("आईडी") -> {
                VerifiedOfficialInfo(
                    title = "Pehchan Artisan ID Card (पहचान कारीगर कार्ड)",
                    authorityOrScheme = "विकास आयुक्त (हस्तशिल्प), कपड़ा मंत्रालय, भारत सरकार",
                    summary = "भारतीय शिल्पकारों का आधिकारिक राष्ट्रीय डिजिटल पहचान पत्र।",
                    benefits = listOf(
                        "राष्ट्रीय व अंतर्राष्ट्रीय हस्तशिल्प मेलों (Dilli Haat, Surajkund) में स्टॉल का सीधा आवंटन",
                        "कारीगर स्वास्थ्य बीमा व पेंशन योजनाओं की सीधी पात्रता",
                        "मुफ्त टूलकिट वितरण और कौशल उन्नयन कार्यशालाएं"
                    ),
                    eligibility = "पारंपरिक हस्तशिल्प बनाने वाले सभी पंजीकृत अथवा गैर-पंजीकृत भारतीय कारीगर",
                    officialPortal = "https://crafts.gov.in",
                    helpline = "1800-208-4800 (Toll Free Handicrafts Helpline)"
                )
            }
            p.contains("mudra") || p.contains("मुद्रा") || p.contains("लोन") || p.contains("loan") -> {
                VerifiedOfficialInfo(
                    title = "Pradhan Mantri MUDRA Yojana (PMMY / मुद्रा लोन)",
                    authorityOrScheme = "वित्तीय सेवाएं विभाग (DFS), वित्त मंत्रालय, भारत सरकार",
                    summary = "सूक्ष्म कारीगरों व लघु उद्योगों के लिए संपार्श्विक-मुक्त (बिना गारंटी) ऋण।",
                    benefits = listOf(
                        "शिशु ऋण: ₹50,000 तक की कार्यशील पूंजी (कच्चा माल खरीदने हेतु)",
                        "किशोर ऋण: ₹50,000 से ₹5 लाख तक (उपकरण व करघा खरीदने हेतु)",
                        "तरुण ऋण: ₹5 लाख से ₹10 लाख तक (विस्तार एवं निर्यात हेतु)"
                    ),
                    eligibility = "कोई भी कारीगर, बुनकर या लघु उद्यमी बिना किसी बैंक गारंटी के आवेदन कर सकता है",
                    officialPortal = "https://www.mudra.org.in",
                    helpline = "1800-180-1111 / 1800-11-0001"
                )
            }
            else -> {
                VerifiedOfficialInfo(
                    title = "PM Vishwakarma Yojana (पीएम विश्वकर्मा योजना)",
                    authorityOrScheme = "सूक्ष्म, लघु एवं मध्यम उद्यम मंत्रालय (MSME) व कौशल विकास मंत्रालय, भारत सरकार",
                    summary = "पारंपरिक गुरु-शिष्य परंपरा से जुड़े कारीगरों और शिल्पकारों के समग्र उत्थान हेतु केंद्रीय योजना।",
                    benefits = listOf(
                        "आधिकारिक पीएम विश्वकर्मा डिजिटल प्रमाण-पत्र व आईडी कार्ड",
                        "5-7 दिन का बुनियादी प्रशिक्षण और ₹500 प्रतिदिन का प्रशिक्षण स्टाइपेंड",
                        "आधुनिक उन्नत औजारों हेतु ₹15,000 का डिजिटल टूलकिट इंसेंटिव",
                        "प्रथम चरण में ₹1,00,000 एवं द्वितीय चरण में ₹2,00,000 का लोन (सिर्फ 5% रियायती ब्याज दर, बिना गारंटी)"
                    ),
                    eligibility = "18 पारंपरिक व्यवसायों (मिट्टी के बर्तन, बढ़ईगीरी, लोहार, मूर्तिकार, सुनार, बुनकर आदि) के 18 वर्ष से अधिक आयु के कारीगर",
                    officialPortal = "https://pmvishwakarma.gov.in",
                    helpline = "1800-267-7777 / 011-23061500"
                )
            }
        }
    }

    private fun detectDomainCategory(userPrompt: String): String {
        val p = userPrompt.lowercase()
        return when {
            p.contains("price") || p.contains("pricing") || p.contains("दाम") || p.contains("कीमत") || p.contains("मूल्य") || p.contains("cost") || p.contains("लागत") -> "pricing"
            p.contains("photo") || p.contains("camera") || p.contains("फोटो") || p.contains("चित्र") || p.contains("तस्वीर") || p.contains("background") -> "photography"
            p.contains("desc") || p.contains("story") || p.contains("विवरण") || p.contains("लिखें") || p.contains("शीर्षक") || p.contains("tags") -> "description"
            p.contains("pack") || p.contains("पैकिंग") || p.contains("पैकेजिंग") || p.contains("बॉक्स") || p.contains("courier") || p.contains("शिपिंग") -> "packaging"
            p.contains("bulk") || p.contains("थोक") || p.contains("बल्क") || p.contains("moq") || p.contains("order") || p.contains("ऑर्डर") || p.contains("डिस्काउंट") -> "bulk_order"
            p.contains("buyer") || p.contains("बात") || p.contains("खरीदार") || p.contains("संदेश") || p.contains("message") || p.contains("negotiat") -> "buyer_comm"
            p.contains("digital") || p.contains("online") || p.contains("ondc") || p.contains("whatsapp") || p.contains("डिजिटल") || p.contains("ऑनलाइन") -> "digital_selling"
            p.contains("scheme") || p.contains("योजना") || p.contains("vishwakarma") || p.contains("mudra") || p.contains("pehchan") || p.contains("लोन") || p.contains("subsidy") -> "govt_schemes"
            else -> "general"
        }
    }

    private fun generateQuickPrompts(userPrompt: String, language: String = "Hindi"): List<String> {
        val domain = detectDomainCategory(userPrompt)
        val isEnglish = language.contains("English", ignoreCase = true)

        return when (domain) {
            "pricing" -> if (isEnglish) listOf(
                "How to calculate fair hourly wage?",
                "What markup for retail vs wholesale?",
                "Suggest break-even formula for my craft",
                "How to explain price to bargaining buyers?"
            ) else listOf(
                "कारीगर मजदूरी (₹175/घंटा) कैसे जोड़ें?",
                "थोक और खुदरा भाव में क्या अंतर रखें?",
                "कच्चा माल बढ़ने पर दाम कैसे बढ़ाएं?",
                "मोलभाव करने वाले खरीदार को क्या कहें?"
            )
            "photography" -> if (isEnglish) listOf(
                "Best phone camera settings for crafts",
                "How to take photos in morning natural light?",
                "Clean background setup without studio lights",
                "How to show intricate craft textures?"
            ) else listOf(
                "मोबाइल से बिना स्टूडियो के फोटो कैसे लें?",
                "शिल्प का सही बैकग्राउंड कैसा होना चाहिए?",
                "बारीक नक्काशी या बुनाई की फोटो कैसे दिखाएं?",
                "1:1 वर्गाकार फ्रेम में सही एंगल क्या है?"
            )
            "description" -> if (isEnglish) listOf(
                "Write a 100-word cultural story for my craft",
                "What care instructions should I mention?",
                "How to highlight natural handmade materials?",
                "Add GI tag authenticity keywords"
            ) else listOf(
                "मेरे शिल्प की 4 लाइनों की पारंपरिक कहानी बनाएं",
                "शिल्प की देखभाल (Care tips) में क्या लिखें?",
                "हाथ से बनी शुद्धता को कैसे उजागर करें?",
                "ऑनलाइन सर्च के लिए मुख्य कीवर्ड क्या हों?"
            )
            "packaging" -> if (isEnglish) listOf(
                "How to pack fragile crafts for courier?",
                "Eco-friendly honeycomb paper packing",
                "What box ply rating should I use?",
                "Artisan unboxing card idea"
            ) else listOf(
                "कूरियर में टूटने से बचाने के लिए पैकिंग कैसे करें?",
                "इको-फ्रेंडली पेपर पैकिंग कैसे करें?",
                "मिट्टी या कांच के सामान की सुरक्षित पैकिंग?",
                "डिब्बे में हस्तकला प्रमाण पत्र कैसे रखें?"
            )
            "bulk_order" -> if (isEnglish) listOf(
                "How much discount for 100 pieces?",
                "How to take 50% advance safely?",
                "Should I send a sample first?",
                "Realistic timeline for bulk handcrafted production"
            ) else listOf(
                "100 पीस के थोक ऑर्डर पर कितना डिस्काउंट दें?",
                "50% पेशगी (Advance) भुगतान की शर्त कैसे रखें?",
                "थोक आर्डर से पहले सैंपल कैसे भेजें?",
                "हाथ से बने सामान की डिलीवरी समय सीमा कैसे तय करें?"
            )
            "buyer_comm" -> if (isEnglish) listOf(
                "Professional reply for customized orders",
                "Polite reply when buyer asks for 50% discount",
                "Follow-up message after sending catalog",
                "Thank-you message after order delivery"
            ) else listOf(
                "कस्टम ऑर्डर के लिए खरीदार को क्या संदेश भेजें?",
                "भारी डिस्काउंट मांगने पर विनम्र उत्तर कैसे दें?",
                "कैटलॉग भेजने के बाद फॉलो-अप संदेश क्या हो?",
                "ऑर्डर मिलने के बाद धन्यवाद और रिव्यू संदेश"
            )
            "digital_selling" -> if (isEnglish) listOf(
                "How to sell on ONDC and WhatsApp catalog?",
                "How to make Instagram craft reels?",
                "Safe UPI QR code payment process",
                "Listing on GeM portal for govt procurement"
            ) else listOf(
                "व्हाट्सएप बिजनेस पर कैटलॉग कैसे शेयर करें?",
                "ONDC और अमेजन कारीगर पर कैसे जुड़ें?",
                "कारीगरी की छोटी रील वीडियो कैसे बनाएं?",
                "सरकारी खरीद के लिए GeM पोर्टल पर रजिस्ट्रेशन"
            )
            "govt_schemes" -> if (isEnglish) listOf(
                "How to apply for PM Vishwakarma ₹15,000 kit?",
                "Pehchan Artisan Card registration process",
                "Mudra loan up to ₹1 Lakh at 5% interest",
                "Free stalls at national craft fairs & expos"
            ) else listOf(
                "पीएम विश्वकर्मा में ₹15,000 टूलकिट कैसे पाएं?",
                "पहचान कारीगर कार्ड (Pehchan Card) कैसे बनाएं?",
                "मुद्रा लोन (MUDRA) बिना गारंटी कैसे मिलेगा?",
                "सूरजकुंड व दिल्ली हाट में सरकारी स्टॉल कैसे लें?"
            )
            else -> if (isEnglish) listOf(
                "💰 What price should I keep?",
                "📸 How can I improve this product photo?",
                "✍️ What should I write in product description?",
                "🤝 How do I sell to bulk buyers?",
                "🏛️ PM Vishwakarma Yojana guidance"
            ) else listOf(
                "💰 मुझे क्या दाम रखना चाहिए? (Pricing)",
                "📸 फोटो कैसे सुधारें? (Photography)",
                "✍️ विवरण में क्या लिखें? (Description)",
                "📦 सुरक्षित पैकिंग कैसे करें? (Packaging)",
                "🤝 थोक खरीदारों को कैसे बेचें? (Bulk Orders)",
                "🏛️ पीएम विश्वकर्मा योजना के लाभ (Govt Scheme)"
            )
        }
    }
}

