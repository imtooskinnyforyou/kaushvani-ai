package com.example

import com.example.ui.components.CraftImageFilter
import com.example.ui.components.calculateCraftColorMatrix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CraftImageFiltersTest {

    @Test
    fun `test craft image filter presets exist with accurate auto tuning`() {
        val filters = CraftImageFilter.values()
        assertTrue(filters.size >= 6)

        // 1. Artisan Natural
        val natural = CraftImageFilter.ARTISAN_NATURAL
        assertEquals("Artisan Natural", natural.title)
        assertEquals("प्राकृतिक कारीगरी", natural.hindiTitle)
        assertTrue(natural.autoBrightness in 1.0f..1.15f)
        assertTrue(natural.autoContrast in 1.05f..1.20f)
        assertTrue(natural.craftFocus.contains("Terracotta"))

        // 2. Vibrant Loom
        val loom = CraftImageFilter.VIBRANT_LOOM
        assertEquals("Vibrant Loom", loom.title)
        assertEquals("सजीव हथकरघा", loom.hindiTitle)
        assertTrue(loom.autoBrightness >= 1.10f)
        assertTrue(loom.autoContrast >= 1.20f)
        assertTrue(loom.autoSaturation >= 1.20f)
        assertTrue(loom.craftFocus.contains("Handloom") || loom.craftFocus.contains("Silk"))

        // 3. Classic Catalog
        val catalog = CraftImageFilter.CLASSIC_CATALOG
        assertEquals("Classic Catalog", catalog.title)
        assertEquals("क्लासिक कैटलॉग", catalog.hindiTitle)
        assertTrue(catalog.autoBrightness >= 1.05f)
        assertTrue(catalog.autoContrast >= 1.05f)
        assertTrue(catalog.craftFocus.contains("ONDC") || catalog.craftFocus.contains("E-Commerce"))

        // 4. Heritage Warmth
        val heritage = CraftImageFilter.HERITAGE_WARMTH
        assertEquals("Heritage Warmth", heritage.title)
        assertEquals("विरासत आभा", heritage.hindiTitle)

        // 5. Indigo & Block Print
        val indigo = CraftImageFilter.INDIGO_BLOCK_PRINT
        assertEquals("Indigo & Block Print", indigo.title)
        assertTrue(indigo.craftFocus.contains("Ajrakh") || indigo.craftFocus.contains("Block"))

        // 6. Muted Linen
        val linen = CraftImageFilter.MUTED_LINEN
        assertEquals("Muted Linen & Raw Fiber", linen.title)
        assertTrue(linen.craftFocus.contains("Khadi") || linen.craftFocus.contains("Jute"))
    }

    @Test
    fun `test category matching auto recommends appropriate filter`() {
        assertEquals(CraftImageFilter.VIBRANT_LOOM, CraftImageFilter.findMatchingForCategory("Banarasi Silk Saree"))
        assertEquals(CraftImageFilter.VIBRANT_LOOM, CraftImageFilter.findMatchingForCategory("Handloom Brocade"))
        assertEquals(CraftImageFilter.INDIGO_BLOCK_PRINT, CraftImageFilter.findMatchingForCategory("Ajrakh Block Print"))
        assertEquals(CraftImageFilter.ARTISAN_NATURAL, CraftImageFilter.findMatchingForCategory("Terracotta Clay Pot"))
        assertEquals(CraftImageFilter.HERITAGE_WARMTH, CraftImageFilter.findMatchingForCategory("Dhokra Bell Metal"))
        assertEquals(CraftImageFilter.MUTED_LINEN, CraftImageFilter.findMatchingForCategory("Jute & Cane Basket"))
    }

    @Test
    fun `test color matrix calculation generates valid 4x5 matrix`() {
        val matrix = calculateCraftColorMatrix(
            brightness = 1.14f,
            contrast = 1.22f,
            saturation = 1.28f
        )
        assertNotNull(matrix)
        val values = matrix.values
        assertEquals(20, values.size)

        // Alpha channel diagonal should be 1.0f
        assertEquals(1.0f, values[18], 0.001f)
        // Offset for brightness/contrast should be non-zero
        assertTrue(values[4] != 0f)
    }

    @Test
    fun `test fromIdOrDefault fallback handles various inputs`() {
        assertEquals(CraftImageFilter.ARTISAN_NATURAL, CraftImageFilter.fromIdOrDefault(null))
        assertEquals(CraftImageFilter.VIBRANT_LOOM, CraftImageFilter.fromIdOrDefault("VIBRANT_LOOM"))
        assertEquals(CraftImageFilter.VIBRANT_LOOM, CraftImageFilter.fromIdOrDefault("Vibrant Loom"))
        assertEquals(CraftImageFilter.CLASSIC_CATALOG, CraftImageFilter.fromIdOrDefault("Classic Catalog"))
        assertEquals(CraftImageFilter.ARTISAN_NATURAL, CraftImageFilter.fromIdOrDefault("Unknown Filter XYZ"))
    }
}
