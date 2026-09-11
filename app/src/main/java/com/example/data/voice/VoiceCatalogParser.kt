package com.example.data.voice

import android.util.Log
import com.example.data.ai.GeminiMetadataService
import com.example.data.ai.model.ProductMetadataRequest
import com.example.data.repository.CraftTaxonomyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.regex.Pattern

/**
 * Structured product catalog fields parsed from artisan voice input.
 * These fields immediately auto-populate the product creation wizard and review screens.
 */
data class ParsedCatalogFields(
    val rawTranscript: String,
    val detectedLanguage: String,
    val detectedLanguageCode: String,
    val title: String,
    val regionalTitle: String,
    val category: String, // e.g., "Pottery", "Handloom", "Metalcraft", "Woodcraft", "Jewelry", "Paintings"
    val specificCraftType: String, // e.g., "Gorakhpur Terracotta", "Paithani Handloom Silk"
    val materialsUsed: String,
    val dimensions: String,
    val laborHours: Double,
    val rawMaterialCost: Double,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val color: String,
    val manufacturingTechnique: String,
    val regionOrigin: String,
    val description: String,
    val regionalDescription: String,
    val craftStory: String,
    val tags: List<String>,
    val isGiTagged: Boolean,
    val confidenceScore: Float
)

/**
 * Parser service that converts unstructured multilingual artisan voice transcripts
 * (Hindi, Marathi, English, Gujarati, Bengali, etc.) into structured e-commerce product catalog fields.
 *
 * Implements a dual-engine approach:
 * 1. High-speed, deterministic offline NLP rules engine (<50ms, regex & Indian craft taxonomy).
 * 2. Optional deep LLM augmentation via GeminiMetadataService when network and API credentials exist.
 */
class VoiceCatalogParser(
    private val geminiMetadataService: GeminiMetadataService? = null
) {

    companion object {
        private const val TAG = "VoiceCatalogParser"
    }

    /**
     * Parses the voice transcript and auto-populates product catalog fields.
     */
    suspend fun parseVoiceDescription(
        transcript: String,
        selectedLocaleTag: String = "hi-IN",
        currentCategory: String = "Pottery",
        artisanState: String = "Maharashtra",
        artisanCluster: String = "Paithan & Yeola"
    ): ParsedCatalogFields = withContext(Dispatchers.Default) {
        val trimmed = transcript.trim()
        if (trimmed.isBlank()) {
            return@withContext createEmptyFallback(currentCategory)
        }

        // Step 1: Detect Language (Marathi, Hindi, English, Bengali, Gujarati)
        val (detectedLang, detectedLangCode) = detectLanguage(trimmed, selectedLocaleTag)

        // Step 2: Extract Category & Specific Craft Type using Craft Taxonomy
        val craftMatch = detectCraftAndCategory(trimmed, currentCategory)

        // Step 3: Extract Labor Hours
        val extractedLaborHours = extractLaborHours(trimmed)

        // Step 4: Extract Raw Material Cost & Retail Price
        val (extractedRawCost, extractedRetailPrice) = extractCostsAndPricing(trimmed, extractedLaborHours)

        // Step 5: Extract Dimensions / Size
        val extractedDimensions = extractDimensions(trimmed, craftMatch.category)

        // Step 6: Extract Materials & Colors
        val extractedMaterials = extractMaterials(trimmed, craftMatch)
        val extractedColor = extractColor(trimmed, craftMatch)

        // Step 7: Calculate fair wholesale and retail pricing if not explicitly spoken
        val finalRawCost = if (extractedRawCost > 0) extractedRawCost else getDefaultRawCost(craftMatch.category)
        val finalLaborHours = if (extractedLaborHours > 0) extractedLaborHours else getDefaultLaborHours(craftMatch.category)
        
        val fairRetail = if (extractedRetailPrice > 0) {
            extractedRetailPrice
        } else {
            Math.round((finalRawCost + (finalLaborHours * 175.0) + 60.0) * 1.35 * 1.15).toDouble()
        }

        val fairWholesale = Math.round((finalRawCost + (finalLaborHours * 160.0) + 40.0) * 1.20).toDouble()

        // Step 8: Construct Professional Titles and Descriptions
        val titles = generateBilingualTitles(trimmed, craftMatch, extractedDimensions, detectedLang)
        val descriptions = generateBilingualDescriptions(
            trimmed = trimmed,
            craftMatch = craftMatch,
            materials = extractedMaterials,
            dimensions = extractedDimensions,
            detectedLang = detectedLang
        )

        val tags = generateCatalogTags(craftMatch, extractedMaterials, extractedDimensions)

        val localParsed = ParsedCatalogFields(
            rawTranscript = trimmed,
            detectedLanguage = detectedLang,
            detectedLanguageCode = detectedLangCode,
            title = titles.first,
            regionalTitle = titles.second,
            category = craftMatch.category,
            specificCraftType = craftMatch.specificCraftType,
            materialsUsed = extractedMaterials,
            dimensions = extractedDimensions,
            laborHours = finalLaborHours,
            rawMaterialCost = finalRawCost,
            retailPrice = fairRetail,
            wholesalePrice = fairWholesale,
            color = extractedColor,
            manufacturingTechnique = craftMatch.technique,
            regionOrigin = craftMatch.regionOrigin.ifBlank { "$artisanCluster, $artisanState" },
            description = descriptions.first,
            regionalDescription = descriptions.second,
            craftStory = craftMatch.story,
            tags = tags,
            isGiTagged = craftMatch.isGiTagged,
            confidenceScore = 0.94f
        )

        // Step 9: If Gemini Service is available and API key is set, attempt AI enrichment asynchronously
        try {
            if (geminiMetadataService != null) {
                val req = ProductMetadataRequest(
                    voiceDescription = trimmed,
                    category = craftMatch.category,
                    rawMaterialCost = finalRawCost,
                    laborHours = finalLaborHours,
                    targetLanguage = detectedLang
                )
                val aiResult = geminiMetadataService.generateMetadata(req)
                if (aiResult.isSuccess) {
                    val ai = aiResult.getOrThrow()
                    return@withContext localParsed.copy(
                        title = ai.title.ifBlank { localParsed.title },
                        regionalTitle = ai.regionalTitle.ifBlank { localParsed.regionalTitle },
                        description = ai.description.ifBlank { localParsed.description },
                        regionalDescription = ai.regionalDescription.ifBlank { localParsed.regionalDescription },
                        craftStory = ai.craftStory.ifBlank { localParsed.craftStory },
                        tags = if (ai.suggestedTags.isNotEmpty()) ai.suggestedTags else localParsed.tags,
                        confidenceScore = 0.98f
                    )
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Gemini enhancement skipped, using local parsed output: ${e.message}")
        }

        return@withContext localParsed
    }

    private fun detectLanguage(text: String, fallbackLocaleTag: String): Pair<String, String> {
        val lower = text.lowercase()
        // Marathi specific marker words
        if (lower.contains("आहे") || lower.contains("साडी") || lower.contains("दिवस") ||
            lower.contains("नक्षी") || lower.contains("विणली") || lower.contains("केले") ||
            lower.contains("खर्च आला") || lower.contains("किंमत") || lower.contains("तयार केली")
        ) {
            return Pair("Marathi", "mr")
        }

        // Bengali specific marker words
        if (lower.contains("হচ্ছে") || lower.contains("শাড়ি") || lower.contains("টাকা") || lower.contains("বানিয়েছি")) {
            return Pair("Bengali", "bn")
        }

        // Gujarati specific marker words
        if (lower.contains("છે") || lower.contains("રૂપિયા") || lower.contains("બનાવ્યું") || lower.contains("સાડી")) {
            return Pair("Gujarati", "gu")
        }

        // Hindi specific marker words
        if (lower.contains("है") || lower.contains("रुपये") || lower.contains("घंटे") ||
            lower.contains("बनाया") || lower.contains("मिट्टी") || lower.contains("लागत") || lower.contains("कीमत")
        ) {
            return Pair("Hindi", "hi")
        }

        // Check if text is predominantly Latin characters -> English
        val latinCharCount = text.count { it in 'a'..'z' || it in 'A'..'Z' }
        if (latinCharCount > text.length * 0.5) {
            return Pair("English", "en")
        }

        // Fallback to selected locale tag
        return when {
            fallbackLocaleTag.startsWith("mr") -> Pair("Marathi", "mr")
            fallbackLocaleTag.startsWith("hi") -> Pair("Hindi", "hi")
            fallbackLocaleTag.startsWith("gu") -> Pair("Gujarati", "gu")
            fallbackLocaleTag.startsWith("bn") -> Pair("Bengali", "bn")
            fallbackLocaleTag.startsWith("ta") -> Pair("Tamil", "ta")
            fallbackLocaleTag.startsWith("te") -> Pair("Telugu", "te")
            else -> Pair("Hindi", "hi")
        }
    }

    private data class CraftMatch(
        val category: String,
        val specificCraftType: String,
        val defaultMaterials: String,
        val technique: String,
        val regionOrigin: String,
        val story: String,
        val isGiTagged: Boolean
    )

    private fun detectCraftAndCategory(text: String, fallbackCategory: String): CraftMatch {
        val lower = text.lowercase()

        // 1. Paithani / Silk Handloom
        if (lower.contains("पैठणी") || lower.contains("paithani") || lower.contains("सिल्क साडी") || lower.contains("रेशमाची")) {
            return CraftMatch(
                category = "Handloom & Textiles",
                specificCraftType = "Paithani Handloom Silk Saree",
                defaultMaterials = "Pure Mulberry Silk, Fine Gold Zari Thread",
                technique = "Interlocking tapestry weave with pure gold zari pallu and traditional peacock (mor) motifs",
                regionOrigin = "Paithan & Yeola, Maharashtra",
                story = "Woven on pit looms since the Satavahana era, this heritage Paithani saree embodies centuries of royal Maharashtrian textile mastery.",
                isGiTagged = true
            )
        }

        // 2. Banarasi Brocade
        if (lower.contains("बनारसी") || lower.contains("banarasi") || lower.contains("brocade") || lower.contains("जरी")) {
            return CraftMatch(
                category = "Handloom & Textiles",
                specificCraftType = "Banarasi Brocade Silk",
                defaultMaterials = "Katan Silk Yarn, Metallic Zari Gold/Silver Thread",
                technique = "Intricate jacquard drawloom weaving with floral jaal and kalga motifs",
                regionOrigin = "Varanasi, Uttar Pradesh",
                story = "Refined over Mughal court traditions, Banarasi handloom weaving is a UNESCO recognized artisanal marvel of Varanasi ghats.",
                isGiTagged = true
            )
        }

        // 3. General Handloom / Saree / Weaving
        if (lower.contains("साड़ी") || lower.contains("साडी") || lower.contains("saree") || lower.contains("हथकरघा") ||
            lower.contains("हातमाग") || lower.contains("रेशम") || lower.contains("बुनाई") || lower.contains("handloom") || lower.contains("chanderi")
        ) {
            return CraftMatch(
                category = "Handloom & Textiles",
                specificCraftType = "Handcrafted Traditional Weave",
                defaultMaterials = "Hand-spun Cotton & Mulberry Silk Yarn",
                technique = "Manual shuttle pit-loom weaving with handcrafted border contrasts",
                regionOrigin = "India Heritage Handloom Cluster",
                story = "Crafted by generational master weavers keeping slow, eco-conscious traditional textile practices alive.",
                isGiTagged = true
            )
        }

        // 4. Terracotta / Pottery
        if (lower.contains("टेराकोटा") || lower.contains("terracotta") || lower.contains("मिट्टी") || lower.contains("कलश") ||
            lower.contains("घड़ा") || lower.contains("कुम्हार") || lower.contains("चाक") || lower.contains("pottery") || lower.contains("clay")
        ) {
            return CraftMatch(
                category = "Pottery & Clay",
                specificCraftType = "Gorakhpur Terracotta Claycraft",
                defaultMaterials = "Natural River Clay, Organic Mineral Dyes",
                technique = "Wheel-thrown and hand-molded clay with intricate hand-carved relief, fired in open wood kilns",
                regionOrigin = "Bhiti Rawat, Gorakhpur, Uttar Pradesh",
                story = "Gorakhpur terracotta is world-renowned for its warm red clay tone and zero-paint natural bake, recognized with an official GI Tag.",
                isGiTagged = true
            )
        }

        // 5. Dhokra / Bell Metal / Brass
        if (lower.contains("ढोकरा") || lower.contains("धोकरा") || lower.contains("dhokra") || lower.contains("dokra") ||
            lower.contains("पीतल") || lower.contains("कांसा") || lower.contains("तांबा") || lower.contains("brass") || lower.contains("bronze")
        ) {
            return CraftMatch(
                category = "Metalcraft",
                specificCraftType = "Bastar Dhokra Bell Metal",
                defaultMaterials = "Brass, Bell Metal, Natural Beeswax",
                technique = "Ancient 4,000-year-old lost-wax casting method (Cire Perdue)",
                regionOrigin = "Bastar, Chhattisgarh",
                story = "Practiced since the Indus Valley Civilization, each Dhokra sculpture is individually sculpted in beeswax and destroyed upon casting, making each piece entirely unique.",
                isGiTagged = true
            )
        }

        // 6. Woodcraft / Wooden Toys
        if (lower.contains("लकड़ी") || lower.contains("काष्ठ") || lower.contains("wood") || lower.contains("wooden") ||
            lower.contains("शीशम") || lower.contains("channapatna") || lower.contains("चन्नापटना") || lower.contains("सागौन")
        ) {
            return CraftMatch(
                category = "Woodcraft",
                specificCraftType = "Handcrafted Woodcraft & Lacquerware",
                defaultMaterials = "Seasoned Sheesham & Teak Wood, Organic Vegetable Lac",
                technique = "Hand-turned on traditional lathes and buffed with natural non-toxic lacquer",
                regionOrigin = "Saharanpur / Channapatna Heritage Wood Cluster",
                story = "Sustainably harvested timber transformed into tactile heirloom art pieces using zero chemical varnishes.",
                isGiTagged = true
            )
        }

        // 7. Madhubani / Folk Paintings
        if (lower.contains("मधुबनी") || lower.contains("madhubani") || lower.contains("वारली") || lower.contains("warli") ||
            lower.contains("पेंटिंग") || lower.contains("painting") || lower.contains("चित्र")
        ) {
            return CraftMatch(
                category = "Paintings & Folk Art",
                specificCraftType = "Madhubani Heritage Folk Art",
                defaultMaterials = "Handmade Rice Paper, Natural Plant Pigments, Bamboo Nib",
                technique = "Geometric line art and ritual motifs hand-drawn using twigs and natural dyes",
                regionOrigin = "Mithila, Bihar",
                story = "Originating during King Janaka's era, Mithila painting celebrates feminine cosmic energy, nature, and wedding folklore.",
                isGiTagged = true
            )
        }

        // 8. Jewelry & Beads
        if (lower.contains("आभूषण") || lower.contains("गहने") || lower.contains("jewelry") || lower.contains("चांदी") ||
            lower.contains("मोती") || lower.contains("necklace") || lower.contains("bangle")
        ) {
            return CraftMatch(
                category = "Jewelry & Accessories",
                specificCraftType = "Handcrafted Tribal & Heritage Jewelry",
                defaultMaterials = "Sterling Silver 925, Natural Stones, Brass Beads",
                technique = "Filigree wire-pulling, hand-hammering, and traditional bezel stone setting",
                regionOrigin = "Jaipur / Cuttack Artisan Cluster",
                story = "Meticulously handcrafted heirloom ornaments carrying tribal emblems of protection and prosperity.",
                isGiTagged = false
            )
        }

        // Fallback based on existing category
        return CraftMatch(
            category = fallbackCategory,
            specificCraftType = "Traditional Handcrafted $fallbackCategory",
            defaultMaterials = "Locally Sourced Organic Materials",
            technique = "Heritage manual artisan craftsmanship passed down through generations",
            regionOrigin = "Artisan Heritage Village Cluster",
            story = "Authentic handcrafted craftwork made by local artisans sustaining rural cottage industries.",
            isGiTagged = false
        )
    }

    private fun extractLaborHours(text: String): Double {
        val lower = text.lowercase()

        // Match days: e.g. "12 दिवस", "12 दिन", "2 days", "3 day", "दोन दिवस"
        val daysRegex = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:दिवस|दिन|days?|roj)", Pattern.CASE_INSENSITIVE)
        val daysMatcher = daysRegex.matcher(lower)
        if (daysMatcher.find()) {
            val days = daysMatcher.group(1)?.toDoubleOrNull() ?: 1.0
            return days * 8.0 // 8 hours per workday
        }

        // Match hours: e.g. "3 घंटे", "4 तास", "6 hours", "5 hrs"
        val hoursRegex = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:घंटे|घंटा|तास|hours?|hrs?)", Pattern.CASE_INSENSITIVE)
        val hoursMatcher = hoursRegex.matcher(lower)
        if (hoursMatcher.find()) {
            return hoursMatcher.group(1)?.toDoubleOrNull() ?: 4.0
        }

        // Match weeks: e.g. "2 हफ्ते", "1 आठवडा", "1 week"
        val weeksRegex = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:हफ्ते|हफ़्ते|आठवडा|weeks?)", Pattern.CASE_INSENSITIVE)
        val weeksMatcher = weeksRegex.matcher(lower)
        if (weeksMatcher.find()) {
            val weeks = weeksMatcher.group(1)?.toDoubleOrNull() ?: 1.0
            return weeks * 48.0
        }

        return 0.0
    }

    private fun extractCostsAndPricing(text: String, laborHours: Double): Pair<Double, Double> {
        val lower = text.lowercase()
        var rawCost = 0.0
        var retailPrice = 0.0

        // Match Raw Material Cost:
        // e.g. "खर्च 3500", "खर्च आला 3500", "लागत 200", "कच्चा माल 1500", "material cost 800", "cost 500"
        val rawCostRegex = Pattern.compile(
            "(?:खर्च(?:\\s+आला)?|लागत|कच्चा(?:\\s+माल)?|material(?:\\s+cost)?|raw(?:\\s+cost)?)\\s*(?:रुपये|रु|rs|inr|₹)?\\s*[:=]?\\s*(\\d+(?:\\.\\d+)?)",
            Pattern.CASE_INSENSITIVE
        )
        val rawCostMatcher = rawCostRegex.matcher(lower)
        if (rawCostMatcher.find()) {
            rawCost = rawCostMatcher.group(1)?.toDoubleOrNull() ?: 0.0
        }

        // Match Retail Price:
        // e.g. "किंमत 12000", "कीमत 1500", "मूल्य 2500", "दाम 450", "बिक्री 800", "price 12000", "retail 1500"
        val priceRegex = Pattern.compile(
            "(?:किंमत|कीमत|मूल्य|दाम|बिक्री(?:\\s+मूल्य)?|price|retail|selling(?:\\s+price)?)\\s*(?:रुपये|रु|rs|inr|₹)?\\s*[:=]?\\s*(\\d+(?:\\.\\d+)?)",
            Pattern.CASE_INSENSITIVE
        )
        val priceMatcher = priceRegex.matcher(lower)
        if (priceMatcher.find()) {
            retailPrice = priceMatcher.group(1)?.toDoubleOrNull() ?: 0.0
        }

        // If neither was matched by prefix, check for currency numbers like "₹ 1500" or "1500 रुपये"
        if (rawCost == 0.0 && retailPrice == 0.0) {
            val currencyRegex = Pattern.compile("(?:₹|rs\\.?|रु|रुपये)\\s*(\\d{2,6})|(\\d{2,6})\\s*(?:रुपये|रु)", Pattern.CASE_INSENSITIVE)
            val currencyMatcher = currencyRegex.matcher(lower)
            val foundNumbers = mutableListOf<Double>()
            while (currencyMatcher.find()) {
                val num1 = currencyMatcher.group(1)?.toDoubleOrNull()
                val num2 = currencyMatcher.group(2)?.toDoubleOrNull()
                val n = num1 ?: num2
                if (n != null && n > 20) {
                    foundNumbers.add(n)
                }
            }

            if (foundNumbers.size >= 2) {
                rawCost = minOf(foundNumbers[0], foundNumbers[1])
                retailPrice = maxOf(foundNumbers[0], foundNumbers[1])
            } else if (foundNumbers.size == 1) {
                // If only one price is mentioned, it's usually the retail selling price
                retailPrice = foundNumbers[0]
            }
        }

        return Pair(rawCost, retailPrice)
    }

    private fun extractDimensions(text: String, category: String): String {
        val lower = text.lowercase()

        // Match numbers with units: e.g. "12 इंच", "12 inch", "15 cm", "6 मीटर", "5.5 meters", "6 yards"
        val dimRegex = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:इंच|inch|inches|\"|सेंटीमीटर|cm|फीट|feet|ft|मीटर|meters|m|गज|yards)", Pattern.CASE_INSENSITIVE)
        val matcher = dimRegex.matcher(lower)
        if (matcher.find()) {
            return matcher.group(0)?.trim() ?: ""
        }

        // Category standard default dimensions
        return when {
            category.contains("Textile") || category.contains("Handloom") -> "6.2 meters (with blouse piece)"
            category.contains("Pottery") -> "12 x 10 inches (H x Dia)"
            category.contains("Metal") -> "8 x 6 inches (H x W)"
            category.contains("Wood") -> "10 x 4 x 4 inches"
            category.contains("Painting") -> "18 x 24 inches"
            else -> "Standard Handcrafted Dimensions"
        }
    }

    private fun extractMaterials(text: String, craftMatch: CraftMatch): String {
        val lower = text.lowercase()
        val detected = mutableListOf<String>()

        if (lower.contains("रेशम") || lower.contains("silk") || lower.contains("सिल्क")) detected.add("Pure Mulberry Silk")
        if (lower.contains("जरी") || lower.contains("zari")) detected.add("Gold & Silver Zari")
        if (lower.contains("मिट्टी") || lower.contains("clay") || lower.contains("टेराकोटा")) detected.add("Natural Riverbed Clay")
        if (lower.contains("पीतल") || lower.contains("brass")) detected.add("Solid Brass")
        if (lower.contains("कांसा") || lower.contains("bell metal")) detected.add("Bell Metal Alloy")
        if (lower.contains("लकड़ी") || lower.contains("wood") || lower.contains("शीशम")) detected.add("Seasoned Sheesham Wood")
        if (lower.contains("सूती") || lower.contains("cotton") || lower.contains("खादी")) detected.add("Organic Hand-spun Cotton")
        if (lower.contains("प्राकृतिक रंग") || lower.contains("natural dye")) detected.add("Eco-friendly Natural Vegetable Dyes")

        return if (detected.isNotEmpty()) {
            detected.joinToString(", ")
        } else {
            craftMatch.defaultMaterials
        }
    }

    private fun extractColor(text: String, craftMatch: CraftMatch): String {
        val lower = text.lowercase()
        val colors = mutableListOf<String>()

        if (lower.contains("लाल") || lower.contains("red")) colors.add("Crimson Red")
        if (lower.contains("पीला") || lower.contains("yellow")) colors.add("Haldi Yellow")
        if (lower.contains("नीला") || lower.contains("blue")) colors.add("Royal Blue")
        if (lower.contains("हरा") || lower.contains("green")) colors.add("Emerald Green")
        if (lower.contains("सुनहरा") || lower.contains("golden") || lower.contains("गोल्ड")) colors.add("Metallic Gold")
        if (lower.contains("काला") || lower.contains("black")) colors.add("Charcoal Black")
        if (lower.contains("मोर") || lower.contains("peacock")) colors.add("Peacock Mor-Pankhi Duo")

        return if (colors.isNotEmpty()) {
            colors.joinToString(" & ")
        } else {
            if (craftMatch.category.contains("Pottery")) "Terracotta Earthen Red" else "Traditional Festive Multi-tone"
        }
    }

    private fun generateBilingualTitles(
        trimmed: String,
        craftMatch: CraftMatch,
        dimensions: String,
        detectedLang: String
    ): Pair<String, String> {
        val dimText = if (dimensions.isNotBlank() && !dimensions.contains("Standard")) " ($dimensions)" else ""

        val englishTitle = when {
            craftMatch.specificCraftType.contains("Paithani") ->
                "Authentic Handwoven Paithani Silk Saree with Peacock Motifs"
            craftMatch.specificCraftType.contains("Terracotta") ->
                "Handcrafted Terracotta Earthen Kalash$dimText"
            craftMatch.specificCraftType.contains("Dhokra") ->
                "Traditional Bastar Dhokra Bell Metal Figurine$dimText"
            craftMatch.specificCraftType.contains("Banarasi") ->
                "Heritage Banarasi Brocade Silk Handloom Saree"
            else ->
                "Authentic Handcrafted ${craftMatch.specificCraftType}$dimText"
        }

        val regionalTitle = when (detectedLang) {
            "Marathi" -> when {
                craftMatch.specificCraftType.contains("Paithani") ->
                    "पारंपरिक अस्सल हातमाग पैठणी साडी (पारंपरिक मोराची नक्षी)"
                craftMatch.specificCraftType.contains("Terracotta") ->
                    "हातनिर्मित टेराकोटा मातीचा कलश$dimText"
                else ->
                    "अस्सल पारंपरिक हस्तकला ${craftMatch.specificCraftType}"
            }
            else -> when {
                craftMatch.specificCraftType.contains("Terracotta") ->
                    "हस्तनिर्मित टेराकोटा मिट्टी का कलश$dimText"
                craftMatch.specificCraftType.contains("Paithani") ->
                    "पारंपरिक हथकरघा पैठणी सिल्क साड़ी"
                craftMatch.specificCraftType.contains("Dhokra") ->
                    "पारंपरिक बस्तर ढोकरा पीतल कलाकृति"
                else ->
                    "पारंपरिक हस्तनिर्मित ${craftMatch.specificCraftType}"
            }
        }

        return Pair(englishTitle, regionalTitle)
    }

    private fun generateBilingualDescriptions(
        trimmed: String,
        craftMatch: CraftMatch,
        materials: String,
        dimensions: String,
        detectedLang: String
    ): Pair<String, String> {
        val englishDesc = """
            Exquisite ${craftMatch.specificCraftType} handcrafted by master artisans from ${craftMatch.regionOrigin}.
            Crafted using $materials with authentic ${craftMatch.technique}.
            Dimensions: $dimensions.
            ${craftMatch.story}
            Artisan voice note: "$trimmed"
        """.trimIndent()

        val regionalDesc = when (detectedLang) {
            "Marathi" -> """
                ${craftMatch.regionOrigin} येथील कारागिरांनी तयार केलेली अस्सल ${craftMatch.specificCraftType}.
                साहित्य: $materials.
                तपशील: $dimensions.
                ${craftMatch.technique} पद्धतीचा वापर करून हाताने घडवलेली कलाकृती.
                कारागिराचा प्रत्यक्ष ऑडिओ तपशील: "$trimmed"
            """.trimIndent()
            else -> """
                ${craftMatch.regionOrigin} के सिद्धहस्त कारीगरों द्वारा निर्मित उत्कृष्ट ${craftMatch.specificCraftType}।
                प्रयुक्त सामग्री: $materials।
                आकार व आयाम: $dimensions।
                ${craftMatch.technique} द्वारा पूर्णतः हस्तनिर्मित।
                कारीगर का रिकॉर्डेड वॉयस विवरण: "$trimmed"
            """.trimIndent()
        }

        return Pair(englishDesc, regionalDesc)
    }

    private fun generateCatalogTags(
        craftMatch: CraftMatch,
        materials: String,
        dimensions: String
    ): List<String> {
        val tags = mutableListOf(
            "Handcrafted",
            "Artisan Direct",
            "Slow Fashion",
            "Indian Heritage",
            "Eco-friendly"
        )
        if (craftMatch.isGiTagged) tags.add("GI Tag Certified")
        if (craftMatch.category.contains("Textile")) {
            tags.addAll(listOf("Handloom Mark", "Silk Mark", "Festive Wear", "Ethnic Saree"))
        }
        if (craftMatch.category.contains("Pottery")) {
            tags.addAll(listOf("Terracotta", "Clay Craft", "Kiln Fired", "Home Decor"))
        }
        if (craftMatch.category.contains("Metal")) {
            tags.addAll(listOf("Lost Wax Casting", "Dhokra Art", "Bell Metal", "Antique Finish"))
        }
        return tags.distinct()
    }

    private fun getDefaultRawCost(category: String): Double = when {
        category.contains("Textile") || category.contains("Handloom") -> 1200.0
        category.contains("Metal") -> 450.0
        category.contains("Pottery") -> 120.0
        category.contains("Wood") -> 280.0
        else -> 200.0
    }

    private fun getDefaultLaborHours(category: String): Double = when {
        category.contains("Textile") || category.contains("Handloom") -> 16.0
        category.contains("Metal") -> 8.0
        category.contains("Pottery") -> 4.0
        category.contains("Wood") -> 6.0
        else -> 5.0
    }

    private fun createEmptyFallback(category: String): ParsedCatalogFields {
        return ParsedCatalogFields(
            rawTranscript = "",
            detectedLanguage = "Hindi",
            detectedLanguageCode = "hi",
            title = "New Handcrafted $category",
            regionalTitle = "नया हस्तनिर्मित $category",
            category = category,
            specificCraftType = "Traditional $category",
            materialsUsed = "Natural Materials",
            dimensions = "Standard",
            laborHours = 4.0,
            rawMaterialCost = 150.0,
            retailPrice = 950.0,
            wholesalePrice = 650.0,
            color = "Natural",
            manufacturingTechnique = "Handcrafted",
            regionOrigin = "Artisan Cluster, India",
            description = "High quality handcrafted product made by rural Indian artisans.",
            regionalDescription = "ग्रामीण कारीगरों द्वारा हस्तनिर्मित उत्कृष्ट उत्पाद।",
            craftStory = "Preserving authentic Indian artisan traditions.",
            tags = listOf("Handcrafted", "Heritage", "Artisan Direct"),
            isGiTagged = false,
            confidenceScore = 0.5f
        )
    }
}
