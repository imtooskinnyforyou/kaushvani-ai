package com.example

import com.example.data.voice.*
import com.example.ui.components.SupportedRegionalLanguages
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MultilingualVoiceCatalogTest {

    private val service = MultilingualVoiceCatalogService()

    @Test
    fun testAll13RequiredFieldsGeneratedInFallback() {
        val hindiLang = SupportedRegionalLanguages.first { it.code == "hi" }
        val transcript = "यह मिट्टी का सुराही है, गोरखपुर का टेराकोटा शिल्प। प्राकृतिक चिकनी माटी से बना है, रंग लाल गेरुआ है। ऊंचाई 25 सेमी और वजन लगभग 800 ग्राम। पानी को शीतल रखता है। गीले कपड़े से साफ करें।"

        val catalog = service.createDeterministicFallback(
            transcript = transcript,
            language = hindiLang,
            artisanState = "Uttar Pradesh",
            artisanCluster = "Gorakhpur",
            categoryHint = "Pottery"
        )

        // Verify all 13 required fields are populated and non-blank:
        // 1. Title
        assertNotNull(catalog.title)
        assertTrue("Title should not be blank", catalog.title.isNotBlank())

        // 2. Regional Title
        assertNotNull(catalog.regionalTitle)
        assertTrue("Regional title should not be blank", catalog.regionalTitle.isNotBlank())

        // 3. Description
        assertNotNull(catalog.description)
        assertTrue("Description should not be blank", catalog.description.isNotBlank())

        // 4. Craft Story
        assertNotNull(catalog.craftStory)
        assertTrue("Craft story should not be blank", catalog.craftStory.isNotBlank())

        // 5. Category
        assertNotNull(catalog.category)
        assertTrue("Category should not be blank", catalog.category.isNotBlank())

        // 6. Craft Type
        assertNotNull(catalog.craftType)
        assertTrue("Craft type should not be blank", catalog.craftType.isNotBlank())

        // 7. Material
        assertNotNull(catalog.material)
        assertTrue("Material should not be blank", catalog.material.isNotBlank())

        // 8. Color
        assertNotNull(catalog.color)
        assertTrue("Color should not be blank", catalog.color.isNotBlank())

        // 9. Dimensions
        assertNotNull(catalog.dimensions)
        assertTrue("Dimensions should not be blank", catalog.dimensions.isNotBlank())

        // 10. Weight
        assertNotNull(catalog.weight)
        assertTrue("Weight should not be blank", catalog.weight.isNotBlank())

        // 11. Care Instructions
        assertNotNull(catalog.careInstructions)
        assertTrue("Care instructions should not be blank", catalog.careInstructions.isNotBlank())

        // 12. Tags
        assertNotNull(catalog.tags)
        assertTrue("Tags list should not be empty", catalog.tags.isNotEmpty())

        // 13. Keywords
        assertNotNull(catalog.keywords)
        assertTrue("Keywords list should not be empty", catalog.keywords.isNotEmpty())
    }

    @Test
    fun testNeverInventGiStatusUnlessExplicitlyClaimed() {
        val marathiLang = SupportedRegionalLanguages.first { it.code == "mr" }
        // Artisan does NOT mention GI tag
        val ordinaryTranscript = "मी लाकडी खेळणी बनवतो, सागाच्या लाकडापासून बनवले आहे. नैसर्गिक रंग वापरले आहेत."

        val catalogOrdinary = service.createDeterministicFallback(
            transcript = ordinaryTranscript,
            language = marathiLang,
            artisanState = "Maharashtra",
            artisanCluster = "Sawantwadi",
            categoryHint = "Woodcraft"
        )

        // MUST be false according to strict integrity mandate!
        assertFalse(
            "GI status must NEVER be invented if not explicitly stated by artisan",
            catalogOrdinary.isGiTagged
        )
        assertEquals("", catalogOrdinary.giTagDetails)
        assertEquals("", catalogOrdinary.governmentCertification)

        // Artisan explicitly states GI Tag
        val giClaimTranscript = "ही अधिकृत भौगोलिक उपदर्शन GI Tag प्रमाणित पैठणी साडी आहे. सिल्क मार्क प्रमाणित."
        val catalogGi = service.createDeterministicFallback(
            transcript = giClaimTranscript,
            language = marathiLang,
            artisanState = "Maharashtra",
            artisanCluster = "Paithan",
            categoryHint = "Handloom & Textiles"
        )

        assertTrue(
            "GI status should be true when explicitly claimed in transcript",
            catalogGi.isGiTagged
        )
        assertTrue(catalogGi.governmentCertification.contains("Silk Mark") || catalogGi.isGiTagged)
    }

    @Test
    fun testJsonValidationSanitizesInvalidGIAndCredentials() {
        val rawJson = """
            {
                "title": "Terracotta Pitcher",
                "regional_title": "टेराकोटा घड़ा",
                "description": "Authentic terracotta water pot handmade on potter wheel.",
                "craft_story": "Generational potters from Gorakhpur preserving earthenware craft.",
                "category": "Pottery",
                "craft_type": "Terracotta Earthenware",
                "material": "River Clay",
                "color": "Terracotta Red",
                "dimensions": "28 x 20 x 20 cm",
                "weight": "1.1 kg",
                "care_instructions": "Wipe with soft cloth, avoid detergent",
                "tags": ["Pottery", "Handcrafted", "Earthenware"],
                "keywords": ["clay pot", "water pitcher", "natural earthenware"],
                "is_gi_tagged": true,
                "gi_tag_details": "GI-POT-12345 (Invented)",
                "government_certification": "Ministry of Textiles National Award 2022",
                "artisan_credentials": "Padma Shri Artisan"
            }
        """.trimIndent()

        // Artisan transcript had NO mention of GI or Padma Shri
        val rawTranscript = "यह सामान्य मिट्टी का घड़ा है। प्राकृतिक लाल मिट्टी से बना है।"
        val validated = service.validateAndSanitizeJson(
            rawJson = rawJson,
            rawTranscript = rawTranscript,
            language = SupportedRegionalLanguages.first { it.code == "hi" },
            artisanState = "Uttar Pradesh",
            artisanCluster = "Gorakhpur"
        )

        assertNotNull(validated)
        // Since the transcript did NOT mention GI tag, the validator must strip the hallucination
        assertFalse(
            "Validator must strip hallucinated GI status if not in transcript",
            validated.isGiTagged
        )
        assertEquals(
            "Validator must clear hallucinated GI details",
            "",
            validated.giTagDetails
        )
        assertEquals(
            "Validator must clear ungrounded government certification",
            "",
            validated.governmentCertification
        )
        assertEquals(
            "Validator must clear ungrounded artisan credentials",
            "",
            validated.artisanCredentials
        )
    }

    @Test
    fun testMultilingualSupportConfiguredLanguages() {
        val configuredLanguages = SupportedRegionalLanguages
        assertTrue("At least 8 Indian languages supported", configuredLanguages.size >= 8)

        val hindi = configuredLanguages.find { it.code == "hi" }
        assertNotNull(hindi)
        assertEquals("hi-IN", hindi?.localeTag)

        val marathi = configuredLanguages.find { it.code == "mr" }
        assertNotNull(marathi)
        assertEquals("mr-IN", marathi?.localeTag)

        val tamil = configuredLanguages.find { it.code == "ta" }
        assertNotNull(tamil)
        assertEquals("ta-IN", tamil?.localeTag)

        val bengali = configuredLanguages.find { it.code == "bn" }
        assertNotNull(bengali)
        assertEquals("bn-IN", bengali?.localeTag)

        val gujarati = configuredLanguages.find { it.code == "gu" }
        assertNotNull(gujarati)
        assertEquals("gu-IN", gujarati?.localeTag)
    }

    @Test
    fun testVoiceCatalogErrorModes() {
        val denied = VoiceCatalogError.MicrophoneDenied
        assertEquals("माइक्रोफ़ोन अनुमति आवश्यक है", denied.titleHindi)
        assertTrue(denied.canRetryAudio)
        assertTrue(denied.suggestManualInput)

        val timeout = VoiceCatalogError.SilenceTimeout
        assertEquals("कोई आवाज़ नहीं सुनाई दी", timeout.titleHindi)
        assertTrue(timeout.canRetryAudio)
        assertTrue(timeout.suggestManualInput)

        val network = VoiceCatalogError.NetworkFailure
        assertEquals("नेटवर्क कनेक्शन धीमा या बंद", network.titleHindi)
        assertTrue(network.canRetryAudio)

        val unsupported = VoiceCatalogError.UnsupportedLanguage("Tamil", "ta-IN")
        assertEquals("Tamil स्पीच इंजन अनुपलब्ध", unsupported.titleHindi)
        assertTrue(unsupported.suggestManualInput)
    }
}
