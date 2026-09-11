package com.example.data.repository

import com.example.data.model.CraftDisciplineItem
import com.example.data.model.CraftSubCategory
import com.example.data.model.IndustryStandardCraftCategory
import com.example.data.model.SelectedCategoryMapping

/**
 * Standard Indian & Global Handicrafts Taxonomy Repository.
 * Encapsulates standard classifications aligned with Ministry of Textiles,
 * GI Registry India, EPCH Export Standards, and ONDC Handicrafts schema.
 */
object CraftTaxonomyRepository {

    val allTaxonomyCategories: List<IndustryStandardCraftCategory> by lazy {
        listOf(
            // 1. Handloom, Apparel & Textiles
            IndustryStandardCraftCategory(
                id = "textiles",
                name = "Handloom & Textiles",
                hindiName = "वस्त्र एवं हथकरघा",
                iconEmoji = "🧶",
                description = "Woven silks, cottons, heritage drapes, block prints, and natural fibers",
                hindiDescription = "प्राकृतिक रेशे, शुद्ध रेशम, हथकरघा बुनाई, ब्लॉक प्रिंट और पारंपरिक परिधान",
                hsnChapter = "HSN Ch. 50, 52, 62",
                subCategories = listOf(
                    CraftSubCategory(
                        id = "sarees_drapes",
                        parentCategoryId = "textiles",
                        name = "Sarees & Traditional Drapes",
                        hindiName = "साड़ियां एवं पारंपरिक परिधान",
                        description = "Heritage handloom sarees, pit-loom weaves, and gold zari borders",
                        hsnPrefix = "5007 / 5208",
                        ondcTaxonomy = "Fashion > Ethnic Wear > Sarees",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "paithani_silk",
                                parentCategoryId = "textiles",
                                subCategoryId = "sarees_drapes",
                                name = "Paithani Silk Handloom",
                                hindiName = "पैठणी रेशम हथकरघा",
                                region = "Paithan & Yeola",
                                state = "Maharashtra",
                                isGiTagged = true,
                                hsnCode = "HSN 5007.20 (Woven Silk)",
                                ondcCategory = "Fashion > Ethnic Wear > Silk Sarees",
                                epchCode = "EPCH-TEX-041",
                                materials = listOf("Pure Mulberry Silk", "Fine Zari Thread", "Natural Dyes"),
                                technique = "Interlocking tapestry weave with pure gold zari pallu and peacock motifs",
                                standardCertifications = listOf("Silk Mark", "Handloom Mark", "GI Tag Certified"),
                                suggestedRetailBenchmark = "₹6,500 - ₹45,000"
                            ),
                            CraftDisciplineItem(
                                id = "banarasi_brocade",
                                parentCategoryId = "textiles",
                                subCategoryId = "sarees_drapes",
                                name = "Banarasi Brocade Silk",
                                hindiName = "बनारसी ब्रोकेड रेशम",
                                region = "Varanasi",
                                state = "Uttar Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 5007.20 (Silk Brocade)",
                                ondcCategory = "Fashion > Ethnic Wear > Bridal Sarees",
                                epchCode = "EPCH-TEX-042",
                                materials = listOf("Katan Silk", "Zari Gold/Silver Thread", "Resham"),
                                technique = "Jacquard drawloom brocade with intricate kalga and floral bel motifs",
                                standardCertifications = listOf("Silk Mark", "Handloom Mark", "GI Tag Certified"),
                                suggestedRetailBenchmark = "₹8,000 - ₹60,000"
                            ),
                            CraftDisciplineItem(
                                id = "kanjeevaram_silk",
                                parentCategoryId = "textiles",
                                subCategoryId = "sarees_drapes",
                                name = "Kanjeevaram Temple Silk",
                                hindiName = "कांचीवरम टेम्पल सिल्क",
                                region = "Kanchipuram",
                                state = "Tamil Nadu",
                                isGiTagged = true,
                                hsnCode = "HSN 5007.20 (Woven Silk)",
                                ondcCategory = "Fashion > Ethnic Wear > Silk Sarees",
                                epchCode = "EPCH-TEX-043",
                                materials = listOf("Mulberry Silk", "Silver/Gold Zari", "Rice Starch"),
                                technique = "Korvai three-shuttle weaving with heavy temple border contrasts",
                                standardCertifications = listOf("Silk Mark", "GI Tag Certified"),
                                suggestedRetailBenchmark = "₹9,500 - ₹75,000"
                            ),
                            CraftDisciplineItem(
                                id = "chanderi_weave",
                                parentCategoryId = "textiles",
                                subCategoryId = "sarees_drapes",
                                name = "Chanderi Silk-Cotton",
                                hindiName = "चंदेरी सिल्क-कॉटन बुनाई",
                                region = "Chanderi, Ashoknagar",
                                state = "Madhya Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 5007.90 (Mixed Fibers)",
                                ondcCategory = "Fashion > Ethnic Wear > Festive Sarees",
                                epchCode = "EPCH-TEX-044",
                                materials = listOf("Raw Silk", "Cotton Yarn", "Zari Bootis"),
                                technique = "Sheer gossamer weave with needle-embroidered golden bootis",
                                standardCertifications = listOf("Handloom Mark", "GI Tag Certified"),
                                suggestedRetailBenchmark = "₹3,500 - ₹18,000"
                            ),
                            CraftDisciplineItem(
                                id = "pochampally_ikat",
                                parentCategoryId = "textiles",
                                subCategoryId = "sarees_drapes",
                                name = "Pochampally Double Ikat",
                                hindiName = "पोचमपल्ली डबल इकत",
                                region = "Bhoodan Pochampally",
                                state = "Telangana",
                                isGiTagged = true,
                                hsnCode = "HSN 5208.51 (Ikat Weave)",
                                ondcCategory = "Fashion > Ethnic Wear > Handloom Sarees",
                                epchCode = "EPCH-TEX-045",
                                materials = listOf("Cotton Yarn", "Natural Vegetable Dyes", "Silk"),
                                technique = "Resist-dyed double ikat warp and weft alignment technique",
                                standardCertifications = listOf("Handloom Mark", "GI Tag Certified"),
                                suggestedRetailBenchmark = "₹4,200 - ₹22,000"
                            )
                        )
                    ),
                    CraftSubCategory(
                        id = "stoles_shawls",
                        parentCategoryId = "textiles",
                        name = "Stoles, Shawls & Dupattas",
                        hindiName = "शॉल, स्टोल एवं दुपट्टे",
                        description = "Handcrafted wool, pashmina, silk stoles and embroidered dupattas",
                        hsnPrefix = "6214",
                        ondcTaxonomy = "Fashion > Accessories > Shawls & Stoles",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "kashmir_pashmina",
                                parentCategoryId = "textiles",
                                subCategoryId = "stoles_shawls",
                                name = "Kashmir Pashmina Shawl",
                                hindiName = "कश्मीरी पश्मीना शॉल",
                                region = "Srinagar & Budgam",
                                state = "Jammu & Kashmir",
                                isGiTagged = true,
                                hsnCode = "HSN 6214.20 (Wool & Pashmina)",
                                ondcCategory = "Fashion > Luxury Accessories > Pashmina",
                                epchCode = "EPCH-TEX-051",
                                materials = listOf("Changthangi Goat Pashm", "Natural Vegetable Dyes"),
                                technique = "Spun on wooden yinder wheel and woven on traditional handlooms with Sozni embroidery",
                                standardCertifications = listOf("GI Tag Certified", "Pashmina Mark"),
                                suggestedRetailBenchmark = "₹12,000 - ₹95,000"
                            ),
                            CraftDisciplineItem(
                                id = "phulkari_dupatta",
                                parentCategoryId = "textiles",
                                subCategoryId = "stoles_shawls",
                                name = "Punjab Phulkari Dupatta",
                                hindiName = "पंजाबी फुलकारी दुपट्टा",
                                region = "Patiala & Amritsar",
                                state = "Punjab",
                                isGiTagged = true,
                                hsnCode = "HSN 5810.92 (Hand Embroidery)",
                                ondcCategory = "Fashion > Ethnic Wear > Dupattas",
                                epchCode = "EPCH-TEX-052",
                                materials = listOf("Khaddar Cotton Cloth", "Untwisted Pat Silk Thread"),
                                technique = "Counted thread darning stitch worked from reverse of fabric",
                                standardCertifications = listOf("GI Tag Certified", "Handloom Mark"),
                                suggestedRetailBenchmark = "₹1,800 - ₹9,500"
                            ),
                            CraftDisciplineItem(
                                id = "kullu_shawl",
                                parentCategoryId = "textiles",
                                subCategoryId = "stoles_shawls",
                                name = "Kullu Woolen Shawl",
                                hindiName = "कुल्लू ऊनी शॉल",
                                region = "Kullu Valley",
                                state = "Himachal Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 6214.20 (Woolen Shawls)",
                                ondcCategory = "Fashion > Winterwear > Handcrafted Shawls",
                                epchCode = "EPCH-TEX-053",
                                materials = listOf("Merino Wool", "Local Desi Sheep Wool"),
                                technique = "Fly-shuttle frame loom with vibrant geometric patterned borders",
                                standardCertifications = listOf("GI Tag Certified", "Woolmark"),
                                suggestedRetailBenchmark = "₹1,500 - ₹8,000"
                            )
                        )
                    ),
                    CraftSubCategory(
                        id = "home_furnishing",
                        parentCategoryId = "textiles",
                        name = "Home Furnishings & Quilts",
                        hindiName = "गृह सज्जा, रज़ाइयां व कुशन",
                        description = "Handblock printed bedcovers, Jaipuri razai, kantha quilts, and cushion covers",
                        hsnPrefix = "6304 / 9404",
                        ondcTaxonomy = "Home & Living > Furnishings > Bedding & Quilts",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "jaipuri_razai",
                                parentCategoryId = "textiles",
                                subCategoryId = "home_furnishing",
                                name = "Jaipuri Blockprint Razai",
                                hindiName = "जयपुरी ब्लॉकप्रिंट रज़ाई",
                                region = "Sanganer & Bagru",
                                state = "Rajasthan",
                                isGiTagged = true,
                                hsnCode = "HSN 9404.90 (Handcrafted Quilts)",
                                ondcCategory = "Home & Living > Bedding > Handmade Quilts",
                                epchCode = "EPCH-TEX-061",
                                materials = listOf("Mulmul Muslin Cotton", "Carded Pure Cotton Fill", "Natural Dyes"),
                                technique = "Teakwood block hand-stamping with fine cotton fluffed filling and hand-tagai",
                                standardCertifications = listOf("GI Tag Certified", "Craftmark"),
                                suggestedRetailBenchmark = "₹1,800 - ₹6,500"
                            ),
                            CraftDisciplineItem(
                                id = "kalamkari_tapestry",
                                parentCategoryId = "textiles",
                                subCategoryId = "home_furnishing",
                                name = "Kalamkari Handpainted Tapestry",
                                hindiName = "कलमकारी हाथ से चित्रित टेपेस्ट्री",
                                region = "Srikalahasti & Machilipatnam",
                                state = "Andhra Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 6304.92 (Decorative Wall Art)",
                                ondcCategory = "Home & Living > Wall Decor > Textile Art",
                                epchCode = "EPCH-TEX-062",
                                materials = listOf("Unbleached Cotton Fabric", "Bamboo Reed Pen (Kalam)", "Natural Plant Dyes"),
                                technique = "Freehand pen drawing with natural mordants and river-washing cycles",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹2,200 - ₹14,000"
                            )
                        )
                    )
                )
            ),

            // 2. Pottery, Ceramics & Terracotta
            IndustryStandardCraftCategory(
                id = "pottery",
                name = "Pottery & Terracotta",
                hindiName = "मिट्टी एवं टेराकोटा शिल्प",
                iconEmoji = "🏺",
                description = "Hand-thrown clayware, terracotta sculptures, Jaipur blue pottery, and glazed ceramic art",
                hindiDescription = "चाक पर गढ़ी मिट्टी, टेराकोटा मूर्तियां, नीली मृद्भांड कला और पारंपरिक बर्तन",
                hsnChapter = "HSN Ch. 69",
                subCategories = listOf(
                    CraftSubCategory(
                        id = "sculptural_terracotta",
                        parentCategoryId = "pottery",
                        name = "Sculptures & Decorative Terracotta",
                        hindiName = "मूर्तियां एवं सजावटी टेराकोटा",
                        description = "Traditional clay horses, ornate kalash, figurines and wall relief plaques",
                        hsnPrefix = "6913",
                        ondcTaxonomy = "Home & Living > Decor > Terracotta Figurines",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "gorakhpur_terracotta",
                                parentCategoryId = "pottery",
                                subCategoryId = "sculptural_terracotta",
                                name = "Gorakhpur Terracotta Kalash & Elephants",
                                hindiName = "गोरखपुर टेराकोटा कलश व हाथी",
                                region = "Aurangabad & Gorakhpur",
                                state = "Uttar Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 6913.90 (Statuettes & Ornaments of Earth)",
                                ondcCategory = "Home & Living > Handmade Sculptures > Terracotta",
                                epchCode = "EPCH-CER-012",
                                materials = listOf("Pond clay (Kabis)", "Natural Soda", "Earthen Wood Kiln"),
                                technique = "Hand-embossed floral relief ornamentation without synthetic glazes; baked in open-pit kilns",
                                standardCertifications = listOf("GI Tag Certified", "ODOP UP"),
                                suggestedRetailBenchmark = "₹850 - ₹9,500"
                            ),
                            CraftDisciplineItem(
                                id = "bankura_horse",
                                parentCategoryId = "pottery",
                                subCategoryId = "sculptural_terracotta",
                                name = "Bankura Panchmura Terracotta Horse",
                                hindiName = "बांकुड़ा पंचमुड़ा टेराकोटा घोड़ा",
                                region = "Panchmura, Bankura",
                                state = "West Bengal",
                                isGiTagged = true,
                                hsnCode = "HSN 6913.90 (Terracotta Statues)",
                                ondcCategory = "Home & Living > Decor > Heritage Sculptures",
                                epchCode = "EPCH-CER-013",
                                materials = listOf("Alluvial River Clay", "Organic Ochre Slip"),
                                technique = "Wheel-turned modular body parts joined and carved by master potters",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹600 - ₹7,500"
                            ),
                            CraftDisciplineItem(
                                id = "molela_clay_plaque",
                                parentCategoryId = "pottery",
                                subCategoryId = "sculptural_terracotta",
                                name = "Molela Clay Murals & Votive Plaques",
                                hindiName = "मोलेला मिट्टी की भित्ति पट्टिकाएं",
                                region = "Molela, Rajsamand",
                                state = "Rajasthan",
                                isGiTagged = true,
                                hsnCode = "HSN 6913.90 (Terracotta Relief Wall Tiles)",
                                ondcCategory = "Home & Living > Wall Art > Terracotta Relief",
                                epchCode = "EPCH-CER-014",
                                materials = listOf("Local River Clay", "Donkey Dung binder", "Natural Mineral Pigments"),
                                technique = "Hand-molded hollow relief deities and folk story plaques",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹1,400 - ₹16,000"
                            )
                        )
                    ),
                    CraftSubCategory(
                        id = "glazed_ceramics",
                        parentCategoryId = "pottery",
                        name = "Glazed Ceramics & Blue Pottery",
                        hindiName = "ग्लेज़्ड सिरेमिक्स व ब्लू पॉटरी",
                        description = "Clay-free quartz pottery, studio ceramics, vases and handpainted tiles",
                        hsnPrefix = "6912 / 6913",
                        ondcTaxonomy = "Home & Living > Kitchen & Dining > Decorative Pottery",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "jaipur_blue_pottery",
                                parentCategoryId = "pottery",
                                subCategoryId = "glazed_ceramics",
                                name = "Jaipur Blue Pottery",
                                hindiName = "जयपुर ब्लू पॉटरी",
                                region = "Jaipur & Kot Jewar",
                                state = "Rajasthan",
                                isGiTagged = true,
                                hsnCode = "HSN 6912.00 (Ceramic Tableware & Decor)",
                                ondcCategory = "Home & Living > Dining & Decor > Blue Pottery",
                                epchCode = "EPCH-CER-021",
                                materials = listOf("Quartz Powder", "Fuller's Earth (Multani Mitti)", "Gum", "Cobalt & Copper Oxides"),
                                technique = "No-clay dough molded in plaster casts, brush painted with cobalt blue and turquoise glazes",
                                standardCertifications = listOf("GI Tag Certified", "Craftmark"),
                                suggestedRetailBenchmark = "₹450 - ₹5,800"
                            ),
                            CraftDisciplineItem(
                                id = "khurja_ceramics",
                                parentCategoryId = "pottery",
                                subCategoryId = "glazed_ceramics",
                                name = "Khurja Studio Ceramics",
                                hindiName = "खुर्जा सिरेमिक शिल्प",
                                region = "Khurja, Bulandshahr",
                                state = "Uttar Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 6912.00 (Ceramic Tableware)",
                                ondcCategory = "Home & Living > Kitchen & Dining > Crockery",
                                epchCode = "EPCH-CER-022",
                                materials = listOf("China Clay (Kaolin)", "Feldspar", "Food-grade Glaze"),
                                technique = "High-temperature kiln fired stoneware with hand-embossed floral glazing",
                                standardCertifications = listOf("GI Tag Certified", "ODOP UP"),
                                suggestedRetailBenchmark = "₹350 - ₹3,800"
                            )
                        )
                    ),
                    CraftSubCategory(
                        id = "traditional_cookware",
                        parentCategoryId = "pottery",
                        name = "Earthen Cookware & Kitchenware",
                        hindiName = "पारंपरिक मिट्टी के बर्तन व कुकवेयर",
                        description = "Black pottery, unglazed curd pots, handis, water bottles, and clay tawas",
                        hsnPrefix = "6912",
                        ondcTaxonomy = "Kitchen & Dining > Cookware > Clay Pots",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "nizamabad_black_pottery",
                                parentCategoryId = "pottery",
                                subCategoryId = "traditional_cookware",
                                name = "Nizamabad Black Pottery with Silver Inlay",
                                hindiName = "निजामाबाद काली मिट्टी शिल्प",
                                region = "Nizamabad, Azamgarh",
                                state = "Uttar Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 6912.00 (Tableware of Earthenware)",
                                ondcCategory = "Kitchen & Dining > Tableware > Heritage Clayware",
                                epchCode = "EPCH-CER-031",
                                materials = listOf("Local Black Silt Clay", "Mustard Oil", "Lead/Zinc Silver Luster Wash"),
                                technique = "Smoke reduction kiln firing with silver amalgam etched engraving",
                                standardCertifications = listOf("GI Tag Certified", "ODOP UP"),
                                suggestedRetailBenchmark = "₹500 - ₹4,500"
                            )
                        )
                    )
                )
            ),

            // 3. Metalcraft & Metalware
            IndustryStandardCraftCategory(
                id = "metalcraft",
                name = "Metalcraft & Dhokra",
                hindiName = "धातु शिल्प एवं ढलाई",
                iconEmoji = "🪙",
                description = "Lost-wax bell metal, engraved Moradabad brassware, Bidriware, and bronze sculptures",
                hindiDescription = "ढोकरा मोम ढलाई, नक्काशीदार पीतल के बर्तन, बिदरीवेयर और कांस्य मूर्तियां",
                hsnChapter = "HSN Ch. 74, 83",
                subCategories = listOf(
                    CraftSubCategory(
                        id = "lost_wax_casting",
                        parentCategoryId = "metalcraft",
                        name = "Lost-Wax Bell Metal & Dhokra",
                        hindiName = "ढोकरा एवं मोम ढलाई शिल्प",
                        description = "Tribal bell metal figurines, candle stands, tribal jewel boxes, and musicians",
                        hsnPrefix = "7418 / 8306",
                        ondcTaxonomy = "Home & Living > Decor > Metal Figurines",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "bastar_dhokra",
                                parentCategoryId = "metalcraft",
                                subCategoryId = "lost_wax_casting",
                                name = "Bastar Dhokra Bell Metal",
                                hindiName = "बस्तर ढोकरा कांस्य शिल्प",
                                region = "Bastar & Kondagaon",
                                state = "Chhattisgarh",
                                isGiTagged = true,
                                hsnCode = "HSN 8306.29 (Statuettes of Base Metal)",
                                ondcCategory = "Home & Living > Decor > Tribal Metal Art",
                                epchCode = "EPCH-MET-011",
                                materials = listOf("Brass/Bronze Alloy", "Beeswax", "Red Clay", "Charcoal Fire"),
                                technique = "4,000-year-old cire perdue (lost wax) non-ferrous metal hollow casting",
                                standardCertifications = listOf("GI Tag Certified", "Tribal Craft India"),
                                suggestedRetailBenchmark = "₹1,200 - ₹18,000"
                            ),
                            CraftDisciplineItem(
                                id = "swamimalai_bronze",
                                parentCategoryId = "metalcraft",
                                subCategoryId = "lost_wax_casting",
                                name = "Swamimalai Bronze Sacred Icons",
                                hindiName = "स्वामिमलाई कांस्य देव मूर्तियां",
                                region = "Swamimalai, Thanjavur",
                                state = "Tamil Nadu",
                                isGiTagged = true,
                                hsnCode = "HSN 8306.29 (Sacred Metal Sculptures)",
                                ondcCategory = "Home & Living > Spiritual & Sacred > Bronze Idols",
                                epchCode = "EPCH-MET-012",
                                materials = listOf("Panchaloha (5 Sacred Metals: Copper, Brass, Lead, Silver, Gold)", "Clay from Cauvery River"),
                                technique = "Shilpa Shastra proportional casting with hand chasing and engraving",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹4,500 - ₹1,20,000"
                            )
                        )
                    ),
                    CraftSubCategory(
                        id = "brassware_metalware",
                        parentCategoryId = "metalcraft",
                        name = "Brassware, Copper & Bidriware",
                        hindiName = "पीतल, तांबा एवं बिदरीवेयर",
                        description = "Hammered copper utensils, Moradabad etched brass, and silver-inlaid Bidri",
                        hsnPrefix = "7418 / 7419",
                        ondcTaxonomy = "Home & Living > Kitchen & Dining > Brass & Copper",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "moradabad_brass",
                                parentCategoryId = "metalcraft",
                                subCategoryId = "brassware_metalware",
                                name = "Moradabad Engraved Brassware",
                                hindiName = "मुरादाबाद नक्काशीदार पीतल",
                                region = "Moradabad",
                                state = "Uttar Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 7418.10 (Table & Kitchenware of Brass)",
                                ondcCategory = "Home & Living > Kitchen & Dining > Brassware",
                                epchCode = "EPCH-MET-021",
                                materials = listOf("Sheet Brass", "Chisels (Kalam)", "Natural Lac Inlay"),
                                technique = "Naqqaashi fine hand-chasing with lac filling and polishing",
                                standardCertifications = listOf("GI Tag Certified", "ODOP UP"),
                                suggestedRetailBenchmark = "₹850 - ₹12,000"
                            ),
                            CraftDisciplineItem(
                                id = "bidar_bidriware",
                                parentCategoryId = "metalcraft",
                                subCategoryId = "brassware_metalware",
                                name = "Bidar Bidriware with Pure Silver Wire",
                                hindiName = "बीदर बिदरीवेयर चांदी तारकशी",
                                region = "Bidar",
                                state = "Karnataka",
                                isGiTagged = true,
                                hsnCode = "HSN 8306.29 (Inlaid Metal Ornaments)",
                                ondcCategory = "Home & Living > Luxury Decor > Bidriware",
                                epchCode = "EPCH-MET-022",
                                materials = listOf("Zinc-Copper Alloy (16:1)", "Pure Silver Wire/Sheet", "Bidar Fort Soil & Ammonium Chloride"),
                                technique = "Alloy casting, engraving, pure silver wire inlaying, and black oxidation with ancient soil",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹1,500 - ₹28,000"
                            )
                        )
                    )
                )
            ),

            // 4. Woodcraft & Marquetry
            IndustryStandardCraftCategory(
                id = "woodcraft",
                name = "Woodcraft & Carving",
                hindiName = "काष्ठ कला एवं नक्काशी",
                iconEmoji = "🪵",
                description = "Channapatna natural lacquer toys, Saharanpur carved teak, and wood marquetry",
                hindiDescription = "चन्नापट्टना प्राकृतिक खिलौने, सहारनपुर लकड़ी नक्काशी और काष्ठ इनले कला",
                hsnChapter = "HSN Ch. 44, 95",
                subCategories = listOf(
                    CraftSubCategory(
                        id = "traditional_toys",
                        parentCategoryId = "woodcraft",
                        name = "Traditional Toys & Lacquerware",
                        hindiName = "पारंपरिक खिलौने व लैकरवेयर",
                        description = "Safe non-toxic organic wooden toys, spinning tops, puzzles and figurines",
                        hsnPrefix = "9503",
                        ondcTaxonomy = "Toys & Games > Traditional Wooden Toys",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "channapatna_toys",
                                parentCategoryId = "woodcraft",
                                subCategoryId = "traditional_toys",
                                name = "Channapatna Lacquer Wooden Toys",
                                hindiName = "चन्नापट्टना लकड़ी के खिलौने",
                                region = "Channapatna, Ramanagara",
                                state = "Karnataka",
                                isGiTagged = true,
                                hsnCode = "HSN 9503.00 (Wooden Toys with Natural Dyes)",
                                ondcCategory = "Toys & Games > Eco-friendly Wooden Toys",
                                epchCode = "EPCH-WOO-011",
                                materials = listOf("Wrightia Tinctoria (Ivory Wood)", "Natural Lac", "Turmeric/Indigo/Kumkum Pigments"),
                                technique = "Turned-wood lathe turning polished with talc and agave leaves for natural shine",
                                standardCertifications = listOf("GI Tag Certified", "EN-71 Child Safe", "BIS Certified"),
                                suggestedRetailBenchmark = "₹350 - ₹3,500"
                            ),
                            CraftDisciplineItem(
                                id = "kondapalli_toys",
                                parentCategoryId = "woodcraft",
                                subCategoryId = "traditional_toys",
                                name = "Kondapalli Painted Wooden Toys",
                                hindiName = "कोंडापल्ली चित्रित खिलौने",
                                region = "Kondapalli, Krishna",
                                state = "Andhra Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 9503.00 (Painted Toys)",
                                ondcCategory = "Toys & Games > Heritage Collectibles",
                                epchCode = "EPCH-WOO-012",
                                materials = listOf("Tella Poniki Wood (Soft Wood)", "Makku Tamarind Paste", "Natural Oil Colors"),
                                technique = "Hand-carved individual limbs joined with tamarind paste and hand-painted",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹450 - ₹4,800"
                            )
                        )
                    ),
                    CraftSubCategory(
                        id = "carved_decor_furniture",
                        parentCategoryId = "woodcraft",
                        name = "Carved Decor, Jaali & Panels",
                        hindiName = "नक्काशीदार सजावट, जाली व फर्नीचर",
                        description = "Saharanpur teak openwork jaali, walnut wood carving, and sheesham tableware",
                        hsnPrefix = "4420",
                        ondcTaxonomy = "Home & Living > Furniture & Decor > Carved Wood",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "saharanpur_carving",
                                parentCategoryId = "woodcraft",
                                subCategoryId = "carved_decor_furniture",
                                name = "Saharanpur Handcarved Woodcraft & Jaali",
                                hindiName = "सहारनपुर लकड़ी नक्काशी व जाली",
                                region = "Saharanpur",
                                state = "Uttar Pradesh",
                                isGiTagged = true,
                                hsnCode = "HSN 4420.10 (Statuettes and Ornaments of Wood)",
                                ondcCategory = "Home & Living > Decor > Carved Wooden Panels",
                                epchCode = "EPCH-WOO-021",
                                materials = listOf("Sheesham Wood (Indian Rosewood)", "Teakwood", "Natural Wax Polish"),
                                technique = "Intricate floral jaali pierced carving and brass wire inlay work",
                                standardCertifications = listOf("GI Tag Certified", "ODOP UP"),
                                suggestedRetailBenchmark = "₹650 - ₹16,000"
                            ),
                            CraftDisciplineItem(
                                id = "kashmir_walnut_carving",
                                parentCategoryId = "woodcraft",
                                subCategoryId = "carved_decor_furniture",
                                name = "Kashmir Walnut Wood Carving",
                                hindiName = "कश्मीरी अखरोट की लकड़ी की नक्काशी",
                                region = "Srinagar & Anantnag",
                                state = "Jammu & Kashmir",
                                isGiTagged = true,
                                hsnCode = "HSN 4420.90 (Wood Marquetry & Caskets)",
                                ondcCategory = "Home & Living > Luxury Decor > Walnut Wood",
                                epchCode = "EPCH-WOO-022",
                                materials = listOf("Seasoned Walnut Wood (Juglans Regia)", "Agate Stone Polish"),
                                technique = "Deep undercut relief carving of chinar leaves, dragons, and rosettes",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹1,800 - ₹45,000"
                            )
                        )
                    )
                )
            ),

            // 5. Paintings, Folk & Tribal Art
            IndustryStandardCraftCategory(
                id = "paintings",
                name = "Paintings & Folk Art",
                hindiName = "पारंपरिक चित्रकला एवं लोक कला",
                iconEmoji = "🎨",
                description = "Madhubani, Pattachitra, Warli, Pichwai, Gond art, and Tanjore gold foil paintings",
                hindiDescription = "मधुबनी, पट्टचित्र, वारली, पिछवाई, गोंड कला और तंजावुर स्वर्ण चित्रकला",
                hsnChapter = "HSN Ch. 97",
                subCategories = listOf(
                    CraftSubCategory(
                        id = "folk_cloth_canvas",
                        parentCategoryId = "paintings",
                        name = "Folk & Heritage Canvas Paintings",
                        hindiName = "पारंपरिक लोक चित्रकला एवं पट",
                        description = "Madhubani, Pattachitra, Pichwai, and Kalamkari scroll paintings on cotton or tussar silk",
                        hsnPrefix = "9701",
                        ondcTaxonomy = "Home & Living > Wall Art > Traditional Paintings",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "madhubani_painting",
                                parentCategoryId = "paintings",
                                subCategoryId = "folk_cloth_canvas",
                                name = "Mithila Madhubani Painting",
                                hindiName = "मिथिला मधुबनी चित्रकला",
                                region = "Madhubani, Darbhanga",
                                state = "Bihar",
                                isGiTagged = true,
                                hsnCode = "HSN 9701.10 (Paintings Executed Entirely by Hand)",
                                ondcCategory = "Home & Living > Wall Decor > Mithila Art",
                                epchCode = "EPCH-ART-001",
                                materials = listOf("Handmade Rice Paper / Tussar Silk", "Bamboo Twigs & Nibs", "Natural Plant/Mineral Dyes"),
                                technique = "Double line outlining with Kachni (fine hatching) and Bharni (color fill) folk narrative",
                                standardCertifications = listOf("GI Tag Certified", "Craftmark"),
                                suggestedRetailBenchmark = "₹1,200 - ₹35,000"
                            ),
                            CraftDisciplineItem(
                                id = "odisha_pattachitra",
                                parentCategoryId = "paintings",
                                subCategoryId = "folk_cloth_canvas",
                                name = "Raghurajpur Pattachitra on Palm Leaf",
                                hindiName = "रघुराजपुर पट्टचित्र एवं तालपत्र",
                                region = "Raghurajpur, Puri",
                                state = "Odisha",
                                isGiTagged = true,
                                hsnCode = "HSN 9701.10 (Handmade Paintings on Palm/Cloth)",
                                ondcCategory = "Home & Living > Wall Decor > Pattachitra",
                                epchCode = "EPCH-ART-002",
                                materials = listOf("Treated Cotton Cloth with Chalk & Tamarind Gum", "Palm Leaves", "Conch Shell White Pigment"),
                                technique = "Intricate floral borders and Jagannath mythological stories with fine squirrel hair brushes",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹1,500 - ₹48,000"
                            ),
                            CraftDisciplineItem(
                                id = "nathdwara_pichwai",
                                parentCategoryId = "paintings",
                                subCategoryId = "folk_cloth_canvas",
                                name = "Nathdwara Pichwai Painting",
                                hindiName = "नाथद्वारा पिछवाई चित्रकला",
                                region = "Nathdwara, Rajsamand",
                                state = "Rajasthan",
                                isGiTagged = true,
                                hsnCode = "HSN 9701.10 (Handpainted Textile Cloth)",
                                ondcCategory = "Home & Living > Sacred Art > Pichwai",
                                epchCode = "EPCH-ART-003",
                                materials = listOf("Pure Cotton Cloth", "Pure Gold Foil / Silver Paint", "Organic Stone Pigments"),
                                technique = "Detailed depiction of Shrinathji lila, lotus ponds, and sacred cows with gold accents",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹3,500 - ₹65,000"
                            )
                        )
                    ),
                    CraftSubCategory(
                        id = "tribal_wall_art",
                        parentCategoryId = "paintings",
                        name = "Tribal Art & Gold Leaf Works",
                        hindiName = "जनजातीय कला एवं स्वर्ण पत्र चित्र",
                        description = "Warli geometric tribal murals, Gond dot art, and Tanjore gold foil devotional panels",
                        hsnPrefix = "9701",
                        ondcTaxonomy = "Home & Living > Wall Decor > Tribal Art",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "warli_painting",
                                parentCategoryId = "paintings",
                                subCategoryId = "tribal_wall_art",
                                name = "Warli Tribal Art",
                                hindiName = "वारली जनजातीय चित्रकला",
                                region = "Dahanu & Palghar",
                                state = "Maharashtra",
                                isGiTagged = true,
                                hsnCode = "HSN 9701.10 (Tribal Hand Art)",
                                ondcCategory = "Home & Living > Wall Decor > Warli Art",
                                epchCode = "EPCH-ART-011",
                                materials = listOf("Earthen Mud/Cow Dung Canvas Base", "Rice Paste White Pigment", "Chewed Bamboo Twig Brush"),
                                technique = "Geometric circles, triangles, and squares representing harmony of nature and tarpa dance",
                                standardCertifications = listOf("GI Tag Certified", "Tribal India"),
                                suggestedRetailBenchmark = "₹800 - ₹12,000"
                            ),
                            CraftDisciplineItem(
                                id = "tanjore_gold_painting",
                                parentCategoryId = "paintings",
                                subCategoryId = "tribal_wall_art",
                                name = "Thanjavur Tanjore Gold Foil Painting",
                                hindiName = "तंजावुर स्वर्ण पत्र चित्रकला",
                                region = "Thanjavur",
                                state = "Tamil Nadu",
                                isGiTagged = true,
                                hsnCode = "HSN 9701.10 (Gold Embossed Art)",
                                ondcCategory = "Home & Living > Luxury Art > Tanjore Paintings",
                                epchCode = "EPCH-ART-012",
                                materials = listOf("Teakwood Board", "22-Karat Pure Gold Foil", "Semi-Precious Jaipur Stones", "Chalk Gesso"),
                                technique = "Embossed gesso relief layered with pure 22k gold leaf and vivid stone inlays",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹6,000 - ₹1,50,000"
                            )
                        )
                    )
                )
            ),

            // 6. Jewelry, Filigree & Beadwork
            IndustryStandardCraftCategory(
                id = "jewelry",
                name = "Jewelry & Filigree",
                hindiName = "पारंपरिक आभूषण एवं तारकशी",
                iconEmoji = "💍",
                description = "Cuttack silver filigree (Tarakasi), Meenakari enameling, terracotta & tribal jewelry",
                hindiDescription = "कटक चांदी तारकशी, जयपुर मीनाकारी, टेराकोटा एवं जनजातीय आभूषण",
                hsnChapter = "HSN Ch. 71",
                subCategories = listOf(
                    CraftSubCategory(
                        id = "silver_filigree",
                        parentCategoryId = "jewelry",
                        name = "Silver Filigree (Tarakasi)",
                        hindiName = "चांदी तारकशी आभूषण",
                        description = "Fine gossamer wire work in pure silver: earrings, brooches, konark wheels and sindoor boxes",
                        hsnPrefix = "7113",
                        ondcTaxonomy = "Jewelry > Silver Jewelry > Filigree",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "cuttack_tarakasi",
                                parentCategoryId = "jewelry",
                                subCategoryId = "silver_filigree",
                                name = "Cuttack Tarakasi Silver Filigree",
                                hindiName = "कटक तारकशी चांदी आभूषण",
                                region = "Cuttack",
                                state = "Odisha",
                                isGiTagged = true,
                                hsnCode = "HSN 7113.11 (Silver Jewelry of Filigree)",
                                ondcCategory = "Jewelry > Fine Silver > Tarakasi",
                                epchCode = "EPCH-JEW-001",
                                materials = listOf("92.5% Sterling Silver", "Thin Silver Wires (0.1mm)", "Borax"),
                                technique = "Drawing thin silver wires twisted and soldered into intricate lacework motifs",
                                standardCertifications = listOf("GI Tag Certified", "BIS Hallmarked 925"),
                                suggestedRetailBenchmark = "₹1,800 - ₹32,000"
                            )
                        )
                    ),
                    CraftSubCategory(
                        id = "enamel_meenakari",
                        parentCategoryId = "jewelry",
                        name = "Meenakari, Kundan & Terracotta",
                        hindiName = "मीनाकारी, कुंदन व टेराकोटा आभूषण",
                        description = "Enamel jewelry, hand-painted terracotta earrings, lac bangles, and tribal beads",
                        hsnPrefix = "7117",
                        ondcTaxonomy = "Jewelry > Fashion & Artisanal Jewelry",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "jaipur_meenakari",
                                parentCategoryId = "jewelry",
                                subCategoryId = "enamel_meenakari",
                                name = "Jaipur Meenakari Enameling",
                                hindiName = "जयपुर मीनाकारी आभूषण",
                                region = "Jaipur",
                                state = "Rajasthan",
                                isGiTagged = true,
                                hsnCode = "HSN 7117.19 (Enamelled Imitation & Base Metal Jewelry)",
                                ondcCategory = "Jewelry > Ethnic Jewelry > Meenakari",
                                epchCode = "EPCH-JEW-011",
                                materials = listOf("Brass/Silver Base", "Mineral Glass Enamel Colors", "Kiln Heat"),
                                technique = "Engraved metal hollows filled with powdered colored glass and fired sequentially",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹650 - ₹12,000"
                            ),
                            CraftDisciplineItem(
                                id = "artisan_terracotta_jewelry",
                                parentCategoryId = "jewelry",
                                subCategoryId = "enamel_meenakari",
                                name = "Eco-friendly Terracotta Clay Jewelry",
                                hindiName = "पर्यावरण-अनुकूल टेराकोटा आभूषण",
                                region = "Kolkata & Cuddalore",
                                state = "West Bengal & Tamil Nadu",
                                isGiTagged = false,
                                hsnCode = "HSN 7117.90 (Other Imitation Jewelry of Earthen Materials)",
                                ondcCategory = "Jewelry > Sustainable Fashion > Terracotta",
                                epchCode = "EPCH-JEW-012",
                                materials = listOf("Filtered Earthen Clay", "Non-toxic Acrylic/Vegetable Paints", "Silk Cords"),
                                technique = "Hand-sculpted miniature beads and pendants kiln-baked and hand-painted",
                                standardCertifications = listOf("Craftmark"),
                                suggestedRetailBenchmark = "₹300 - ₹2,500"
                            )
                        )
                    )
                )
            ),

            // 7. Cane, Bamboo & Natural Fiber
            IndustryStandardCraftCategory(
                id = "cane_bamboo",
                name = "Cane, Bamboo & Grass",
                hindiName = "प्राकृतिक रेशा, बेंत एवं बांस",
                iconEmoji = "🎋",
                description = "Tripura bamboo lamps, Assam cane baskets, Moonj grass mats, and golden jute crafts",
                hindiDescription = "त्रिपुरा बांस लैंप, असम बेंत डलिया, मूंज घास शिल्प और सुनहरा जूट उत्पाद",
                hsnChapter = "HSN Ch. 46, 53",
                subCategories = listOf(
                    CraftSubCategory(
                        id = "bamboo_lighting_decor",
                        parentCategoryId = "cane_bamboo",
                        name = "Bamboo Lighting & Home Decor",
                        hindiName = "बांस के लैंप व सजावटी शिल्प",
                        description = "Hand-woven bamboo lampshades, pendant lights, floor planters and partitions",
                        hsnPrefix = "4602 / 9405",
                        ondcTaxonomy = "Home & Living > Lighting > Bamboo Lamps",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "tripura_bamboo_lighting",
                                parentCategoryId = "cane_bamboo",
                                subCategoryId = "bamboo_lighting_decor",
                                name = "Tripura Handwoven Bamboo Lampshades",
                                hindiName = "त्रिपुरा बांस के लैंपशेड व डेकोर",
                                region = "Agartala & Khowai",
                                state = "Tripura",
                                isGiTagged = true,
                                hsnCode = "HSN 9405.10 (Lamps & Lighting Fittings of Bamboo)",
                                ondcCategory = "Home & Living > Lighting > Eco-friendly Lamps",
                                epchCode = "EPCH-NAT-001",
                                materials = listOf("Muli & Barak Seasoned Bamboo", "Smoked Varnish", "Natural Cane Splints"),
                                technique = "Fine bamboo splint weaving treated for insect resistance and natural finish",
                                standardCertifications = listOf("GI Tag Certified", "Green Craft"),
                                suggestedRetailBenchmark = "₹750 - ₹6,500"
                            )
                        )
                    ),
                    CraftSubCategory(
                        id = "natural_grass_baskets",
                        parentCategoryId = "cane_bamboo",
                        name = "Natural Grass, Jute & Basketry",
                        hindiName = "सिक्का घास, जूट एवं टोकरी शिल्प",
                        description = "Sikki golden grass boxes, Sabai grass storage baskets, and jute tote bags",
                        hsnPrefix = "4602",
                        ondcTaxonomy = "Home & Living > Storage & Organization > Baskets",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "bihar_sikki_grass",
                                parentCategoryId = "cane_bamboo",
                                subCategoryId = "natural_grass_baskets",
                                name = "Bihar Sikki Golden Grass Craft",
                                hindiName = "बिहार सिक्की गोल्डन ग्रास शिल्प",
                                region = "Madhubani & Sitamarhi",
                                state = "Bihar",
                                isGiTagged = true,
                                hsnCode = "HSN 4602.19 (Basketwork of Vegetable Materials)",
                                ondcCategory = "Home & Living > Organizers > Sikki Baskets",
                                epchCode = "EPCH-NAT-011",
                                materials = listOf("Sikki Grass (Golden Reed)", "Munj Grass Core", "Takua Needle"),
                                technique = "Coiled basketry using special takua needle stitching golden grass casing",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹450 - ₹4,200"
                            )
                        )
                    )
                )
            ),

            // 8. Leathercraft & Mojaris
            IndustryStandardCraftCategory(
                id = "leathercraft",
                name = "Leathercraft & Mojaris",
                hindiName = "चर्म शिल्प एवं जूती",
                iconEmoji = "👞",
                description = "Authentic Kolhapuri chappals, Jodhpur embroidered mojaris, and Shantiniketan embossed bags",
                hindiDescription = "कोल्हापुरी चप्पल, जोधपुरी मोजरी और शांतिनिकेतन एम्बोस्ड लेदर बैग",
                hsnChapter = "HSN Ch. 42, 64",
                subCategories = listOf(
                    CraftSubCategory(
                        id = "traditional_footwear",
                        parentCategoryId = "leathercraft",
                        name = "Handcrafted Footwear & Chappals",
                        hindiName = "पारंपरिक हस्तनिर्मित जूते व चप्पल",
                        description = "Vegetable-tanned leather chappals, braided straps, and silk-embroidered juttis",
                        hsnPrefix = "6403",
                        ondcTaxonomy = "Footwear > Ethnic Footwear > Mojaris & Chappals",
                        disciplines = listOf(
                            CraftDisciplineItem(
                                id = "kolhapuri_chappal",
                                parentCategoryId = "leathercraft",
                                subCategoryId = "traditional_footwear",
                                name = "Kolhapuri Hand-braided Leather Chappal",
                                hindiName = "कोल्हापुरी हाथ से बुनी चप्पल",
                                region = "Kolhapur & Athani",
                                state = "Maharashtra & Karnataka",
                                isGiTagged = true,
                                hsnCode = "HSN 6403.20 (Footwear with Outer Soles of Leather)",
                                ondcCategory = "Footwear > Men & Women > Kolhapuri Chappals",
                                epchCode = "EPCH-LEA-001",
                                materials = listOf("Vegetable Tanned Buffalo/Cow Hide", "Babool Bark Extract", "Cotton/Silk Thread"),
                                technique = "100% glue-less stitch construction with hand-braided kanthi straps",
                                standardCertifications = listOf("GI Tag Certified", "Leather Sector Skill Council"),
                                suggestedRetailBenchmark = "₹1,200 - ₹6,500"
                            ),
                            CraftDisciplineItem(
                                id = "jodhpur_mojari",
                                parentCategoryId = "leathercraft",
                                subCategoryId = "traditional_footwear",
                                name = "Jodhpur Embroidered Leather Mojari",
                                hindiName = "जोधपुरी कशीदाकारी मोजरी",
                                region = "Jodhpur & Jalore",
                                state = "Rajasthan",
                                isGiTagged = true,
                                hsnCode = "HSN 6403.59 (Ethnic Footwear)",
                                ondcCategory = "Footwear > Ethnic Footwear > Mojaris",
                                epchCode = "EPCH-LEA-002",
                                materials = listOf("Soft Goat/Camel Leather", "Zari & Silk Thread Embroidery", "Brass Sequins"),
                                technique = "Curved-toe hand stitching with dense silk thread geometric embroidery",
                                standardCertifications = listOf("GI Tag Certified"),
                                suggestedRetailBenchmark = "₹850 - ₹4,800"
                            )
                        )
                    )
                )
            )
        )
    }

    /**
     * Finds matching category, subcategory, and craft discipline by name or ID
     */
    fun findDisciplineById(disciplineId: String): CraftDisciplineItem? {
        for (cat in allTaxonomyCategories) {
            for (sub in cat.subCategories) {
                for (disc in sub.disciplines) {
                    if (disc.id.equals(disciplineId, ignoreCase = true)) {
                        return disc
                    }
                }
            }
        }
        return null
    }

    /**
     * Search taxonomy across all tiers (Categories, Subcategories, Disciplines, HSN codes, and GI clusters)
     */
    fun searchTaxonomy(query: String): List<SelectedCategoryMapping> {
        if (query.isBlank()) return getDefaultPopularMappings()

        val cleanQuery = query.trim().lowercase()
        val results = mutableListOf<SelectedCategoryMapping>()

        for (cat in allTaxonomyCategories) {
            for (sub in cat.subCategories) {
                for (disc in sub.disciplines) {
                    val matches = disc.name.lowercase().contains(cleanQuery) ||
                            disc.hindiName.lowercase().contains(cleanQuery) ||
                            disc.region.lowercase().contains(cleanQuery) ||
                            disc.state.lowercase().contains(cleanQuery) ||
                            disc.hsnCode.lowercase().contains(cleanQuery) ||
                            disc.technique.lowercase().contains(cleanQuery) ||
                            cat.name.lowercase().contains(cleanQuery) ||
                            cat.hindiName.lowercase().contains(cleanQuery) ||
                            sub.name.lowercase().contains(cleanQuery) ||
                            sub.hindiName.lowercase().contains(cleanQuery) ||
                            (cleanQuery == "gi" && disc.isGiTagged)

                    if (matches) {
                        results.add(
                            SelectedCategoryMapping(
                                categoryId = cat.id,
                                categoryName = cat.name,
                                categoryHindiName = cat.hindiName,
                                subCategoryId = sub.id,
                                subCategoryName = sub.name,
                                subCategoryHindiName = sub.hindiName,
                                disciplineId = disc.id,
                                disciplineName = disc.name,
                                disciplineHindiName = disc.hindiName,
                                region = "${disc.region}, ${disc.state}",
                                isGiTagged = disc.isGiTagged,
                                hsnCode = disc.hsnCode,
                                ondcCategory = disc.ondcCategory,
                                materials = disc.materials,
                                technique = disc.technique,
                                certifications = disc.standardCertifications
                            )
                        )
                    }
                }
            }
        }
        return results
    }

    /**
     * Curated list of high-volume popular Indian handicrafts for quick 1-tap chip selection
     */
    fun getDefaultPopularMappings(): List<SelectedCategoryMapping> {
        val popularIds = listOf(
            "gorakhpur_terracotta",
            "paithani_silk",
            "bastar_dhokra",
            "channapatna_toys",
            "madhubani_painting",
            "jaipur_blue_pottery",
            "cuttack_tarakasi",
            "kolhapuri_chappal",
            "tripura_bamboo_lighting"
        )
        return popularIds.mapNotNull { getMappingForDisciplineId(it) }
    }

    fun getMappingForDisciplineId(disciplineId: String): SelectedCategoryMapping? {
        for (cat in allTaxonomyCategories) {
            for (sub in cat.subCategories) {
                for (disc in sub.disciplines) {
                    if (disc.id == disciplineId) {
                        return SelectedCategoryMapping(
                            categoryId = cat.id,
                            categoryName = cat.name,
                            categoryHindiName = cat.hindiName,
                            subCategoryId = sub.id,
                            subCategoryName = sub.name,
                            subCategoryHindiName = sub.hindiName,
                            disciplineId = disc.id,
                            disciplineName = disc.name,
                            disciplineHindiName = disc.hindiName,
                            region = "${disc.region}, ${disc.state}",
                            isGiTagged = disc.isGiTagged,
                            hsnCode = disc.hsnCode,
                            ondcCategory = disc.ondcCategory,
                            materials = disc.materials,
                            technique = disc.technique,
                            certifications = disc.standardCertifications
                        )
                    }
                }
            }
        }
        return null
    }

    /**
     * Map old simple string (e.g. "Pottery", "Handloom & Textiles", "Metalcraft") to complete hierarchy
     */
    fun mapSimpleCategoryToTaxonomy(simpleCategoryName: String): SelectedCategoryMapping {
        val matchedCat = allTaxonomyCategories.find {
            it.name.contains(simpleCategoryName, ignoreCase = true) ||
                    simpleCategoryName.contains(it.name, ignoreCase = true) ||
                    it.hindiName.contains(simpleCategoryName, ignoreCase = true)
        } ?: allTaxonomyCategories.first()

        val sub = matchedCat.subCategories.firstOrNull() ?: allTaxonomyCategories.first().subCategories.first()
        val disc = sub.disciplines.firstOrNull() ?: sub.disciplines.first()

        return SelectedCategoryMapping(
            categoryId = matchedCat.id,
            categoryName = matchedCat.name,
            categoryHindiName = matchedCat.hindiName,
            subCategoryId = sub.id,
            subCategoryName = sub.name,
            subCategoryHindiName = sub.hindiName,
            disciplineId = disc.id,
            disciplineName = disc.name,
            disciplineHindiName = disc.hindiName,
            region = "${disc.region}, ${disc.state}",
            isGiTagged = disc.isGiTagged,
            hsnCode = disc.hsnCode,
            ondcCategory = disc.ondcCategory,
            materials = disc.materials,
            technique = disc.technique,
            certifications = disc.standardCertifications
        )
    }
}
