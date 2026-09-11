package com.example

import com.example.data.ai.DemoAiDataset
import com.example.data.ai.model.DynamicPricingRequest
import com.example.data.ai.model.ProductMetadataRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DemoAiModeTest {

    @Test
    fun testDemoMultilingualSamplesDatasetIntegrity() {
        val samples = DemoAiDataset.PREDEFINED_SAMPLES
        assertTrue("Must have at least 4 predefined multilingual samples", samples.size >= 4)
        
        val marathiSample = samples.find { it.languageCode == "mr" }
        assertNotNull("Marathi sample must exist", marathiSample)
        assertTrue(marathiSample!!.samplePromptText.contains("पैठणी"))
        assertTrue(marathiSample.rawMaterialCost > 0)
        assertTrue(marathiSample.laborHours > 0)
        assertTrue(marathiSample.tags.isNotEmpty())

        val hindiSample = samples.find { it.languageCode == "hi" }
        assertNotNull("Hindi sample must exist", hindiSample)
        assertTrue(hindiSample!!.samplePromptText.contains("टेराकोटा"))

        val englishSample = samples.find { it.languageCode == "en" }
        assertNotNull("English sample must exist", englishSample)
        assertTrue(englishSample!!.samplePromptText.contains("Dhokra") || englishSample.craftType.contains("Dhokra"))
    }

    @Test
    fun testDemoDynamicPricingResponseDeterministic() {
        val request = DynamicPricingRequest(
            materialCost = 180.0,
            laborHours = 6.0,
            category = "Pottery",
            craftTags = listOf("GI Tag", "Handmade")
        )
        val response = DemoAiDataset.getDemoPricingResponse(request)
        assertNotNull(response)
        assertTrue(response.minFairPrice > 0)
        assertTrue(response.maxFairPrice >= response.minFairPrice)
        assertTrue(response.optimalRetailPrice >= response.minFairPrice)
        assertTrue(response.wholesalePrice > 0)
        assertTrue(response.isAiGenerated)
        assertEquals(0.96f, response.confidenceScore, 0.01f)
    }

    @Test
    fun testDemoChatResponseDeterministic() {
        val (text, suggestedActions) = DemoAiDataset.getDemoChatResponse(
            userQuery = "What price should I charge for my craft?",
            languageCode = "en"
        )
        assertNotNull(text)
        assertTrue("Response text should explain pricing in demo mode", text.contains("price", ignoreCase = true) || text.contains("cost", ignoreCase = true) || text.contains("wage", ignoreCase = true))
        assertTrue(suggestedActions.isNotEmpty())
    }

    @Test
    fun testDemoMetadataServiceFallbackDeterministic() {
        val request = ProductMetadataRequest(
            voiceDescription = "हा पैठणी साडी आहे. ही रेशमाची आहे.",
            category = "Handloom & Textiles",
            rawMaterialCost = 3000.0,
            laborHours = 30.0,
            targetLanguage = "Marathi"
        )
        val metadata = DemoAiDataset.getDemoMetadataResponse(request)
        assertNotNull(metadata)
        assertTrue(metadata.title.contains("Paithani") || metadata.title.contains("Silk") || metadata.title.contains("पैठणी"))
        assertEquals("mr", metadata.detectedLanguageCode)
    }
}
