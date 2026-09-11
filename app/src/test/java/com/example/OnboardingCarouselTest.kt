package com.example

import com.example.ui.components.OnboardingCarouselSlides
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingCarouselTest {

    @Test
    fun `test onboarding carousel slides content and integrity`() {
        assertEquals(4, OnboardingCarouselSlides.size)

        // Slide 1: Photography
        val slide1 = OnboardingCarouselSlides[0]
        assertEquals(1, slide1.stepNumber)
        assertEquals("PHOTO", slide1.stepVisualType)
        assertTrue(slide1.titleHindi.contains("फोटोग्राफी"))
        assertTrue(slide1.explanationHindi.isNotEmpty())
        assertTrue(slide1.practicalTips.isNotEmpty())
        assertNotNull(slide1.voiceNarrationText)

        // Slide 2: Regional Voice Recording
        val slide2 = OnboardingCarouselSlides[1]
        assertEquals(2, slide2.stepNumber)
        assertEquals("VOICE", slide2.stepVisualType)
        assertTrue(slide2.titleHindi.contains("आवाज़"))
        assertTrue(slide2.explanationEnglish.contains("typing"))
        assertTrue(slide2.practicalTips.size >= 3)

        // Slide 3: Fair Living Wage & Pricing
        val slide3 = OnboardingCarouselSlides[2]
        assertEquals(3, slide3.stepNumber)
        assertEquals("PRICING", slide3.stepVisualType)
        assertTrue(slide3.explanationHindi.contains("175"))
        assertTrue(slide3.explanationEnglish.contains("175"))

        // Slide 4: AI Smart Catalog & Export
        val slide4 = OnboardingCarouselSlides[3]
        assertEquals(4, slide4.stepNumber)
        assertEquals("EXPORT", slide4.stepVisualType)
        assertTrue(slide4.explanationHindi.contains("ONDC"))
    }
}
