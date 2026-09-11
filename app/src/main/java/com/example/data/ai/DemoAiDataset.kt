package com.example.data.ai

import com.example.data.ai.model.DynamicPricingRequest
import com.example.data.ai.model.DynamicPricingResponse
import com.example.data.ai.model.ProductMetadataRequest
import com.example.data.ai.model.ProductStructuredMetadata
import com.example.data.ai.model.TagCategoryItem

/**
 * Predefined deterministic demo datasets and mock responses for the Hackathon Demonstration.
 * Guarantees zero failures and reliable offline execution across all regional languages,
 * pricing engines, catalog generators, and chat assistants.
 */
object DemoAiDataset {

    // --- PREDEFINED MULTILINGUAL SAMPLES ---

    data class MultilingualSample(
        val languageName: String,
        val languageCode: String,
        val samplePromptText: String,
        val craftCategory: String,
        val craftType: String,
        val region: String,
        val rawMaterialCost: Double,
        val laborHours: Double,
        val hourlyRate: Double,
        val packagingCost: Double,
        val titleEn: String,
        val titleRegional: String,
        val descriptionEn: String,
        val descriptionRegional: String,
        val culturalStory: String,
        val tags: List<String>,
        val tagCategories: List<TagCategoryItem>,
        val dimensions: String,
        val careInstructions: String,
        val sampleImageResOrUrl: String
    )

    val PREDEFINED_SAMPLES = listOf(
        MultilingualSample(
            languageName = "मराठी (Marathi)",
            languageCode = "mr",
            samplePromptText = "हा अस्सल पैठणी रेशमी साडी आहे. पारंपरिक मोर नक्षी आणि शुद्ध जरी पल्लू. 4 दिवस हातमागावर विणले आहे.",
            craftCategory = "Handloom & Textiles",
            craftType = "Paithani Silk Saree",
            region = "Paithan, Maharashtra",
            rawMaterialCost = 3200.0,
            laborHours = 32.0,
            hourlyRate = 180.0,
            packagingCost = 150.0,
            titleEn = "Handwoven Pure Paithani Silk Saree with Real Zari Peacock Motif",
            titleRegional = "अस्सल पैठणी रेशमी साडी (पारंपरिक मोर नक्षी व शुद्ध जरी पल्लू)",
            descriptionEn = "Authentic Maharashtra Paithan handloom masterpiece woven with certified mulberry silk threads and intricate gold zari borders. Features traditional Mor-Butti (peacock motifs) on the pallu, taking 32 hours of painstaking loom work.",
            descriptionRegional = "महाराष्ट्राच्या पैठण येथील अस्सल हातमाग कलाकृती. शुद्ध रेशीम धाग्यांनी आणि सोन्याच्या जरीने विणलेली, पारंपरिक मोर-नक्षी असलेली ही साडी लग्नसमारंभ आणि सणांसाठी अत्यंत शुभ व टिकाऊ आहे.",
            culturalStory = "Paithani weaving is a 2000-year-old royal Maratha craft recognized under Geographical Indications (GI). Each saree is an heirloom piece passing through generations.",
            tags = listOf("Paithani", "Pure Silk", "GI Heritage Craft", "Handloom Tradition", "Maharashtra Heritage", "Zari Border", "Direct from Weaver"),
            tagCategories = listOf(
                TagCategoryItem("Craft & Technique", listOf("Interlocking Weft", "Handloom Jacquard", "Zari Weaving")),
                TagCategoryItem("Materials", listOf("Pure Mulberry Silk", "Zari Thread", "Eco-friendly Dyes")),
                TagCategoryItem("Aesthetics & Style", listOf("Royal Maratha", "Peacock Motifs", "Traditional Border")),
                TagCategoryItem("Occasion & Utility", listOf("Wedding Saree", "Festive Celebrations", "Heirloom Treasure")),
                TagCategoryItem("Heritage & Origin", listOf("Paithan, Maharashtra", "GI Heritage Craft", "Handloom Tradition"))
            ),
            dimensions = "5.5m Saree + 0.8m Blouse Piece",
            careInstructions = "Dry clean only. Store wrapped in pure muslin/cotton cloth. Avoid direct perfume spray on zari.",
            sampleImageResOrUrl = "sample_paithani_saree"
        ),
        MultilingualSample(
            languageName = "हिन्दी (Hindi)",
            languageCode = "hi",
            samplePromptText = "यह गोरखपुर की पारंपरिक टेराकोटा हाथी मूर्ति है। प्राकृतिक लाल मिट्टी से चाक पर बनाकर 800 डिग्री भट्टी में पकाई गई है। 2 दिन की मेहनत है।",
            craftCategory = "Terracotta & Pottery",
            craftType = "Gorakhpur Terracotta Craft",
            region = "Bhiti Rawat, Gorakhpur, UP",
            rawMaterialCost = 180.0,
            laborHours = 6.0,
            hourlyRate = 150.0,
            packagingCost = 60.0,
            titleEn = "Handmade Gorakhpur GI Terracotta Elephant Figurine with Bell Engravings",
            titleRegional = "गोरखपुर जीआई टैग्ड पारंपरिक टेराकोटा नक्काशीदार हाथी मूर्ति",
            descriptionEn = "Exquisite GI-tagged terracotta elephant crafted from all-natural alluvial clay of the Rapti river basin in Gorakhpur. Adorned with delicate hand-pinched ear ornaments, floral motifs, and traditional hanging bells.",
            descriptionRegional = "गोरखपुर की राप्ती नदी की शुद्ध दोमट मिट्टी से हस्तनिर्मित जीआई प्रमाणित टेराकोटा हाथी। पारंपरिक चाक और हाथों से महीन नक्काशी कर 800°C भट्ठी में पकाया गया। घर की सजावट और वास्तु के लिए उत्तम।",
            culturalStory = "Gorakhpur Terracotta holds a prestigious Geographical Indication (GI) tag. The artisans use zero synthetic chemicals and follow century-old furnace wood-firing techniques.",
            tags = listOf("Gorakhpur Terracotta", "GI Tagged", "Eco-friendly Clay", "Handmade Pottery", "Home Decor", "ODOP Certified"),
            tagCategories = listOf(
                TagCategoryItem("Craft & Technique", listOf("Wheel Throwing", "Hand Modeling", "Natural Pit Firing")),
                TagCategoryItem("Materials", listOf("100% Natural Alluvial Clay", "Natural Oxide Glaze", "Chemical Free")),
                TagCategoryItem("Aesthetics & Style", listOf("Rustic Terracotta", "Folk Indian", "Ornate Engraving")),
                TagCategoryItem("Occasion & Utility", listOf("Living Room Decor", "Festive Gifting", "Vastu Enhancer")),
                TagCategoryItem("Heritage & Origin", listOf("Gorakhpur, Uttar Pradesh", "GI Tag Eligible", "ODOP Product"))
            ),
            dimensions = "22cm x 15cm x 28cm",
            careInstructions = "Wipe with a soft dry cloth. Keep indoors. Do not submerge in standing water for prolonged periods.",
            sampleImageResOrUrl = "sample_terracotta_elephant"
        ),
        MultilingualSample(
            languageName = "English",
            languageCode = "en",
            samplePromptText = "Authentic Dhokra bell-metal brass tribal sculpture of Dancing Goddess made using 4000-year-old lost-wax lost-wax casting technique. 14 hours manual labor.",
            craftCategory = "Metal Craft & Dhokra",
            craftType = "Lost-Wax Brass Dhokra",
            region = "Bastar, Chhattisgarh",
            rawMaterialCost = 450.0,
            laborHours = 14.0,
            hourlyRate = 160.0,
            packagingCost = 80.0,
            titleEn = "Bastar Lost-Wax Cast Brass Dhokra Tribal Figurine",
            titleRegional = "बस्तर लॉस्ट-वैक्स ब्रास डोकरा पारंपरिक आदिवासी मूर्ति",
            descriptionEn = "Handcrafted by Bastar master metal artisans using the ancient 4000-year-old Harappan lost-wax brass casting method. Each piece has an organic rustic patina and non-repeatable individual wire casting pattern.",
            descriptionRegional = "बस्तर के जनजातीय शिल्पकारों द्वारा प्राचीन लॉस्ट-वैक्स पद्धति से निर्मित पीतल डोकरा कलाकृति। हर मूर्ति अपने आप में एक अनोखा और अद्वितीय शिल्प है।",
            culturalStory = "Dhokra is an ancient tribal metal technique dating back to Mohenjo-daro's famous Dancing Girl sculpture. Bastar artisans craft each mold individually from beeswax, clay, and riverbed mud.",
            tags = listOf("Dhokra Art", "Lost Wax Casting", "Bastar Craft", "Brass Sculpture", "Tribal Folk Art", "Collector Edition"),
            tagCategories = listOf(
                TagCategoryItem("Craft & Technique", listOf("Cire Perdue (Lost Wax)", "Clay Core Molding", "Metal Chasing")),
                TagCategoryItem("Materials", listOf("Brass Alloy", "Beeswax", "River Clay Core")),
                TagCategoryItem("Aesthetics & Style", listOf("Tribal Rustic", "Primitive Folk", "Antique Brass")),
                TagCategoryItem("Occasion & Utility", listOf("Art Collector Piece", "Premium Decor", "Corporate Heritage Gift")),
                TagCategoryItem("Heritage & Origin", listOf("Bastar, Chhattisgarh", "Tribal Guild Certified", "GI Eligible"))
            ),
            dimensions = "18cm x 10cm x 24cm",
            careInstructions = "Dust with micro-fiber cloth. Apply thin coat of coconut oil or brass wax annually to maintain antique luster.",
            sampleImageResOrUrl = "sample_dhokra_brass"
        ),
        MultilingualSample(
            languageName = "বাংলা (Bengali)",
            languageCode = "bn",
            samplePromptText = "এটি প্রাকৃতিক খড় ও বাঁশ দিয়ে হাতে বোনা সুন্দরবন শিতলপাটি মাদুর। গ্রীষ্মে দারুণ আরামদায়ক ও ১০০% পরিবেশবান্ধব।",
            craftCategory = "Natural Fiber & Cane",
            craftType = "Shitalpati Handwoven Mat",
            region = "Cooch Behar, West Bengal",
            rawMaterialCost = 280.0,
            laborHours = 10.0,
            hourlyRate = 140.0,
            packagingCost = 40.0,
            titleEn = "Eco-Friendly Organic Cane Murta Shitalpati Cooling Mat",
            titleRegional = "হাতে বোনা শীতলপাটি মাদুর (প্রাকৃতিক ক্যানে ফাইবার)",
            descriptionEn = "Handwoven from green Murta cane reeds by rural Bengal artisans. Naturally cool to the touch and breathable, providing sustainable thermal comfort during warm seasons.",
            descriptionRegional = "পশ্চিমবঙ্গের ঐতিহ্যবাহী মুর্তা বেতের শীতলপাটি। প্রাকৃতিক শীতলতা ও সম্পূর্ণ পরিবেশবান্ধব গৃহসজ্জা।",
            culturalStory = "Recognized on UNESCO's Representative List of Intangible Cultural Heritage, Shitalpati weaving requires immense dexterity to slice cane into silk-like thin strips.",
            tags = listOf("Shitalpati", "UNESCO Heritage", "Murta Cane", "Eco-friendly", "Natural Cooling", "Bengal Craft"),
            tagCategories = listOf(
                TagCategoryItem("Craft & Technique", listOf("Fine Cane Slitting", "Plaited Weave", "Edge Binding")),
                TagCategoryItem("Materials", listOf("Murta Reed (Schumannianthus)", "Cotton Binding")),
                TagCategoryItem("Aesthetics & Style", listOf("Minimalist Cane", "Organic Earthy")),
                TagCategoryItem("Occasion & Utility", listOf("Meditation Mat", "Living Room Accent", "Eco Living")),
                TagCategoryItem("Heritage & Origin", listOf("Cooch Behar, West Bengal", "UNESCO Recognized"))
            ),
            dimensions = "180cm x 120cm",
            careInstructions = "Roll loosely when storing. Clean with damp cloth and dry completely in indirect shade.",
            sampleImageResOrUrl = "sample_shitalpati_mat"
        ),
        MultilingualSample(
            languageName = "தமிழ் (Tamil)",
            languageCode = "ta",
            samplePromptText = "தஞ்சாவூர் பாரம்பரிய 22k தங்க இலை வேலைப்பாடு கொண்ட கிருஷ்ணர் ஓவியம். தேக்கு மர சட்டகம் கொண்டது.",
            craftCategory = "Paintings & Folk Art",
            craftType = "Thanjavur Gold Leaf Painting",
            region = "Thanjavur, Tamil Nadu",
            rawMaterialCost = 1800.0,
            laborHours = 20.0,
            hourlyRate = 200.0,
            packagingCost = 250.0,
            titleEn = "Tanjore 22K Gold Foil Handpainted Lord Krishna Artwork with Teakwood Frame",
            titleRegional = "தஞ்சாவூர் 22K தங்க இலை கைவினை கிருஷ்ணர் ஓவியம்",
            descriptionEn = "Sacred Thanjavur art created on water-resistant plywood with Arabic gum gesso work, adorned with authentic 22-carat gold leaves and Jaipur glass stones.",
            descriptionRegional = "பாரம்பரிய தஞ்சாவூர் 22 காரட் தங்க இலை மற்றும் ஜெய்ப்பூர் கற்களால் கைவண்ணம் தீட்டப்பட்ட தெய்வீக கிருஷ்ணர் ஓவியம். பிரீமியம் தேக்கு மர சட்டகம் கொண்டது.",
            culturalStory = "Originating in the 16th century Maratha-Nayaka era, Tanjore painting is famous for its three-dimensional embossing and non-fading pure gold luster.",
            tags = listOf("Tanjore Painting", "22K Gold Foil", "GI Heritage Craft", "Teakwood Frame", "Tamil Nadu Art", "Pooja Decor"),
            tagCategories = listOf(
                TagCategoryItem("Craft & Technique", listOf("Gesso Relief Work", "22k Gold Gilding", "Natural Gem Setting")),
                TagCategoryItem("Materials", listOf("22K Gold Foil", "Teakwood Frame", "Jaipur Gemstones")),
                TagCategoryItem("Aesthetics & Style", listOf("South Indian Temple", "Classical Devotional")),
                TagCategoryItem("Occasion & Utility", listOf("Pooja Mandir", "Housewarming Gift", "Luxury Sacred Art")),
                TagCategoryItem("Heritage & Origin", listOf("Thanjavur, Tamil Nadu", "GI Heritage Region"))
            ),
            dimensions = "30cm x 40cm (Frame included)",
            careInstructions = "Do not clean frame with water. Use soft dry cotton swab. Keep protected behind clear glass.",
            sampleImageResOrUrl = "sample_tanjore_painting"
        )
    )

    // --- DEMO DYNAMIC PRICING DATASET ---

    fun getDemoPricingResponse(request: DynamicPricingRequest): DynamicPricingResponse {
        val baseMaterial = if (request.materialCost > 0) request.materialCost else 350.0
        val baseHours = if (request.laborHours > 0) request.laborHours else 8.0
        val wageRate = when (request.skillTier) {
            com.example.data.ai.model.ArtisanSkillTier.MASTER -> 300.0
            com.example.data.ai.model.ArtisanSkillTier.SKILLED -> 175.0
            com.example.data.ai.model.ArtisanSkillTier.APPRENTICE -> 120.0
        }
        val laborCost = Math.round(baseHours * wageRate).toDouble()
        val packagingCost = if (request.packagingCost > 0) request.packagingCost else 60.0
        val baseFairCost = baseMaterial + laborCost + packagingCost

        val fairMin = Math.round(baseFairCost * 1.35).toDouble().coerceAtLeast(450.0)
        val fairMax = Math.round(baseFairCost * 1.70).toDouble().coerceAtLeast(650.0)
        val retailPrice = Math.round(fairMin * 1.15).toDouble()
        val wholesalePrice = Math.round(baseFairCost * 1.20).toDouble()
        val exportPrice = Math.round(fairMax * 1.65).toDouble()

        val costSlices = listOf(
            com.example.data.ai.model.CostSliceItem(
                label = "Raw Materials",
                hindiLabel = "कच्चा माल",
                amount = baseMaterial,
                percentage = ((baseMaterial / baseFairCost) * 100).toFloat(),
                colorHex = 0xFFD97706
            ),
            com.example.data.ai.model.CostSliceItem(
                label = "Artisan Labor (${baseHours}h @ ₹${wageRate}/h)",
                hindiLabel = "कारीगर पारिश्रमिक",
                amount = laborCost,
                percentage = ((laborCost / baseFairCost) * 100).toFloat(),
                colorHex = 0xFFC05621
            ),
            com.example.data.ai.model.CostSliceItem(
                label = "Packaging & Safe Transit",
                hindiLabel = "पैकेजिंग एवं सुरक्षा",
                amount = packagingCost,
                percentage = ((packagingCost / baseFairCost) * 100).toFloat(),
                colorHex = 0xFF4A5568
            )
        )

        val tagList = listOf(
            com.example.data.ai.model.TagPriceImpact(
                tag = "GI Tag Certified",
                percentageBoost = 15,
                justification = "Geographical Indication adds verified origin authenticity and premium buyer trust."
            ),
            com.example.data.ai.model.TagPriceImpact(
                tag = "100% Handcrafted",
                percentageBoost = 10,
                justification = "Authentic handwork without mass molding commands higher artisan valuation."
            )
        )

        return DynamicPricingResponse(
            minFairPrice = fairMin,
            maxFairPrice = fairMax,
            optimalRetailPrice = retailPrice,
            wholesalePrice = wholesalePrice,
            exportPrice = exportPrice,
            suggestedMinOrderQty = 10,
            tagImpacts = tagList,
            costBreakdown = costSlices,
            benchmarkComparison = "Similar handmade ${request.category} crafts retail between ₹${Math.round(fairMin * 0.95)} and ₹${Math.round(fairMax * 1.25)} across artisan stores and online showcases.",
            marketInsights = "Calculated using Fair Wage Algorithm (₹$wageRate/hr labor + ₹$baseMaterial materials + ₹$packagingCost packaging). Guarantees an ethical profit margin of 35% above living wages.",
            regionalMarketInsights = "उचित मजदूरी आधारित गणना: ₹$wageRate/घंटा श्रम + ₹$baseMaterial कच्चा माल + ₹$packagingCost सुरक्षित पैकिंग। 35% का शुद्ध लाभ सुनिश्चित।",
            negotiationTip = "For orders over 20 pieces, offer wholesale price of ₹$wholesalePrice while securing a 50% advance deposit.",
            regionalNegotiationTip = "20 पीस से अधिक के ऑर्डर पर थोक दर ₹$wholesalePrice लागू करें और 50% अग्रिम भुगतान अवश्य लें।",
            isAiGenerated = true,
            confidenceScore = 0.96f
        )
    }

    // --- DEMO VYAPAR MITRA CONVERSATION RESPONSES ---

    fun getDemoChatResponse(userQuery: String, languageCode: String): Pair<String, List<String>> {
        val q = userQuery.lowercase()
        return when {
            q.contains("price") || q.contains("कीमत") || q.contains("भाव") || q.contains("दर") -> {
                val text = if (languageCode == "mr") {
                    "तुमच्या उत्पादनांसाठी योग्य दर ठरवताना: कच्चा माल + (कामाचे तास × ₹१६०-२२०/तास मजुरी) + पॅकेजिंग खर्चावर ३५% नफा मिळायला हवा. यामुळे कारागिराला न्याय्य मोबदला मिळतो आणि बाजारपेठेत स्पर्धात्मकता टिकते."
                } else if (languageCode == "hi") {
                    "अपने उत्पाद की सही कीमत तय करने के लिए: कच्चा माल + (श्रम के घंटे × ₹160-220/घंटा मजदूरी) + पैकेजिंग लागत पर कम से कम 35% लाभ जोड़ें। इससे आपको उचित मेहनताना मिलेगा और बाजार में अच्छी बिक्री होगी।"
                } else {
                    "To determine a fair price: calculate Raw Materials + (Crafting Hours × ₹160-220/hr Fair Wage) + Packaging, plus a 35% artisan markup. This protects your margins while competing effectively in retail markets."
                }
                val actions = listOf("कैटलॉग में कीमत अपडेट करें", "थोक दर (Wholesale) जानें", "कैटलॉग शेयर करें")
                Pair(text, actions)
            }
            q.contains("order") || q.contains("inquiry") || q.contains("पूछताछ") || q.contains("ऑर्डर") -> {
                val text = if (languageCode == "hi") {
                    "आपके पास वर्तमान में 3 सक्रिय थोक पूछताछ (B2B Inquiries) हैं! मुंबई और दिल्ली के बुटीक खरीदारों ने 20 से 50 पीस के ऑर्डर के लिए कोटेशन मांगा है। 'पूछताछ' टैब में जाकर तुरंत जवाब भेजें।"
                } else {
                    "You have 3 active wholesale B2B inquiries waiting in your queue! Buyers from Mumbai and Bangalore have requested quotations for 20-50 units. Check your Inquiries tab to respond."
                }
                val actions = listOf("नई पूछताछ देखें (View Inquiries)", "कोटेशन तैयार करें (Generate Quote)", "कॉल पर बात करें")
                Pair(text, actions)
            }
            else -> {
                val text = if (languageCode == "hi") {
                    "नमस्ते! मैं आपका 'कौशवाणी व्यापार मित्र' (KAUSHVANI AI Business Assistant) हूँ। मैं उत्पाद विवरण तैयार करने, उचित मूल्य निर्धारण (Fair Pricing), और थोक पूछताछ प्रबंधित करने में आपकी सहायता कर सकता हूँ।"
                } else if (languageCode == "mr") {
                    "नमस्कार! मी तुमचा 'कौशवाणी व्यापार मित्र' (KAUSHVANI AI) आहे. उत्पादनांचे स्मार्ट कॅटलॉग बनवणे, योग्य भाव ठरवणे आणि खरेदीदारांशी व्यवहार करण्यात मी तुम्हाला मदत करू शकतो."
                } else {
                    "Hello! I am your 'KAUSHVANI Vyapar Mitra' AI assistant. I can help you with smart voice cataloging, dynamic fair-wage pricing, and B2B quotation responses."
                }
                val actions = listOf("नया शिल्प जोड़ें (Add Craft)", "उचित मूल्य गणना (Price Calculator)", "व्यापार मार्गदर्शन (B2B Tips)")
                Pair(text, actions)
            }
        }
    }

    fun getDemoMetadataResponse(request: ProductMetadataRequest): ProductStructuredMetadata {
        val matchingSample = PREDEFINED_SAMPLES.find {
            request.voiceDescription.contains(it.languageName.take(4), ignoreCase = true) ||
            request.category.contains(it.craftCategory, ignoreCase = true) ||
            request.targetLanguage.contains(it.languageName, ignoreCase = true)
        } ?: PREDEFINED_SAMPLES.first()

        val baseCost = matchingSample.rawMaterialCost + (matchingSample.laborHours * matchingSample.hourlyRate) + matchingSample.packagingCost
        val minPrice = Math.round(baseCost * 1.35).toDouble()
        val maxPrice = Math.round(baseCost * 1.70).toDouble()
        val wholesale = Math.round(baseCost * 1.20).toDouble()
        val retail = Math.round(minPrice * 1.15).toDouble()

        return ProductStructuredMetadata(
            title = matchingSample.titleEn,
            regionalTitle = matchingSample.titleRegional,
            shortDescription = matchingSample.titleEn,
            description = matchingSample.descriptionEn,
            regionalDescription = matchingSample.descriptionRegional,
            seoKeywords = matchingSample.tags,
            suggestedTags = matchingSample.tags,
            craftStory = matchingSample.culturalStory,
            productName = matchingSample.craftType,
            category = matchingSample.craftCategory,
            specificCraftType = matchingSample.craftType,
            materialsUsed = "Natural Artisan Materials",
            color = "Traditional Authentic",
            dimensionsEstimate = matchingSample.dimensions,
            manufacturingTechnique = "Handcrafted",
            regionOrigin = matchingSample.region,
            artisanStory = matchingSample.culturalStory,
            keywords = matchingSample.tags,
            allTags = matchingSample.tags,
            detectedLanguage = matchingSample.languageName,
            detectedLanguageCode = matchingSample.languageCode,
            tagCategories = matchingSample.tagCategories,
            suggestedMinPrice = minPrice,
            suggestedMaxPrice = maxPrice,
            wholesalePrice = wholesale,
            retailPrice = retail,
            hourlyWageRate = matchingSample.hourlyRate,
            packagingCost = matchingSample.packagingCost,
            careInstructions = matchingSample.careInstructions
        )
    }
}
