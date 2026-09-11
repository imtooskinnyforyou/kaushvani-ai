package com.example

import com.example.data.model.BuyerInquiryEntity
import com.example.data.model.ProductEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BuyerInquirySystemTest {

    @Test
    fun `test inquiry creation with all required fields`() {
        val sampleProduct = ProductEntity(
            id = 1,
            title = "Handcrafted Earthen Terracotta Decorative Vase",
            description = "Traditional terracotta vase hand sculpted on a potter wheel.",
            category = "Pottery",
            craftType = "Gorakhpur Terracotta",
            region = "Gorakhpur, UP",
            wholesalePrice = 850.0,
            retailPrice = 1499.0,
            minOrderQuantity = 15
        )

        // 1. Buyer submits REQUEST QUOTE form
        val inquiry = BuyerInquiryEntity(
            id = 101,
            productId = sampleProduct.id,
            productTitle = sampleProduct.title,
            buyerName = "Ananya Singhania",
            buyerCompany = "FabHeritage Lifestyle Boutiques",
            buyerType = "B2B_WHOLESALE",
            requestedQuantity = 50,
            offeredPricePerUnit = 850.0,
            message = "We need 50 pieces for our Diwali festive collection with custom gift boxes.",
            buyerPhone = "+91 98112 34567",
            buyerEmail = "sourcing@fabheritage.in",
            buyerCity = "New Delhi, Delhi",
            requiredDate = "15 Sep 2026",
            status = "NEW"
        )

        // Verify fields
        assertEquals(1L, inquiry.productId)
        assertEquals("Handcrafted Earthen Terracotta Decorative Vase", inquiry.productTitle)
        assertEquals("Ananya Singhania", inquiry.buyerName)
        assertEquals("FabHeritage Lifestyle Boutiques", inquiry.buyerCompany)
        assertEquals(50, inquiry.requestedQuantity)
        assertEquals("15 Sep 2026", inquiry.requiredDate)
        assertEquals("NEW", inquiry.status)
        assertEquals("+91 98112 34567", inquiry.buyerPhone)
        assertNotNull(inquiry.message)

        // 2. Artisan Action: [Respond]
        val respondedInquiry = inquiry.copy(
            status = "RESPONDED",
            artisanResponse = "नमस्ते अनन्य जी! हम 15 सितम्बर तक 50 पीस कस्टम गिफ्ट बॉक्स के साथ तैयार कर देंगे।"
        )
        assertEquals("RESPONDED", respondedInquiry.status)
        assertEquals("नमस्ते अनन्य जी! हम 15 सितम्बर तक 50 पीस कस्टम गिफ्ट बॉक्स के साथ तैयार कर देंगे।", respondedInquiry.artisanResponse)

        // 3. Artisan Action: [Accept]
        val acceptedInquiry = inquiry.copy(status = "ACCEPTED")
        assertEquals("ACCEPTED", acceptedInquiry.status)

        // 4. Artisan Action: [Reject]
        val rejectedInquiry = inquiry.copy(status = "REJECTED")
        assertEquals("REJECTED", rejectedInquiry.status)
    }
}
