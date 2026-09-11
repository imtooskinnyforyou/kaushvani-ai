package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * AI-powered one-tap image filters specifically tuned for textile weaves,
 * natural clay pottery, metallic patinas, block prints, and handicraft textures.
 */
enum class CraftImageFilter(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val craftFocus: String,
    val hindiCraftFocus: String,
    val tagline: String,
    val description: String,
    val autoBrightness: Float,   // Multiplier where 1.0f is default (e.g. 1.14f = +14% brightness)
    val autoContrast: Float,     // Multiplier where 1.0f is default (e.g. 1.25f = +25% contrast)
    val autoSaturation: Float,   // Multiplier where 1.0f is default (e.g. 1.32f = +32% vibrance)
    val bgColors: List<Color>,
    val overlayTint: Color,
    val textureDetailTag: String,
    val textureDetailHindi: String,
    val narrationText: String
) {
    HANDLOOM_VIVID(
        id = "HANDLOOM_VIVID",
        title = "Handloom Vivid",
        hindiTitle = "हैंडलूम विविड",
        craftFocus = "Silk Sarees, Zari Weaves & Brocades",
        hindiCraftFocus = "सिल्क साड़ियां, जरी बुनाई व कशीदाकारी",
        tagline = "रेशम, जरी व हथकरघा बुनाई की जीवंत चमक",
        description = "साड़ियों, शॉल और जरीदार वस्त्रों के ताना-बाना (Warp & Weft) धागों की चमक और समृद्ध रंगों को उभारने के लिए विशेष रूप से ट्यून किया गया AI Canvas फ़िल्टर।",
        autoBrightness = 1.14f,
        autoContrast = 1.25f,
        autoSaturation = 1.32f,
        bgColors = listOf(Color(0xFFFFFBF5), Color(0xFFFDE8CE), Color(0xFFF9D5A7)),
        overlayTint = Color(0x1AFFD54F),
        textureDetailTag = "🧵 Warp & Weft Zari Thread Sheen: Enhanced",
        textureDetailHindi = "ताना-बाना धागे और जरी की धात्विक चमक बढ़ाई गई",
        narrationText = "हैंडलूम विविड फ़िल्टर हथकरघा रेशम, जरी और कशीदाकारी के लिए तैयार किया गया है। यह बुनाई के हर धागे और रंग की चमक को स्पष्ट करता है।"
    ),

    NATURAL_STUDIO(
        id = "NATURAL_STUDIO",
        title = "Natural Studio",
        hindiTitle = "नेचुरल स्टूडियो",
        craftFocus = "Terracotta, Clay Pottery & Stone Crafts",
        hindiCraftFocus = "टेराकोटा, मिट्टी के बर्तन व कच्चा शिल्प",
        tagline = "प्रामाणिक प्राकृतिक मिट्टी व शिल्प की सौम्य बनावट",
        description = "टेराकोटा, मिट्टी और प्राकृतिक हस्तशिल्प के लिए संतुलित प्राकृतिक प्रकाश। असली मिट्टी के कणों और धागों की बनावट को 100% सुरक्षित रखता है।",
        autoBrightness = 1.06f,
        autoContrast = 1.12f,
        autoSaturation = 1.05f,
        bgColors = listOf(Color(0xFFFDF8F3), Color(0xFFF4E5D4), Color(0xFFEADBCA)),
        overlayTint = Color(0x18B33918),
        textureDetailTag = "🏺 Natural Clay & Fiber Grain: Preserved",
        textureDetailHindi = "मिट्टी व प्राकृतिक रेशे के सूक्ष्म कण सुरक्षित",
        narrationText = "नेचुरल स्टूडियो फ़िल्टर आपके मिट्टी के बर्तनों और प्राकृतिक शिल्प के लिए एकदम सही है। यह असली रंग और बनावट को निखारता है।"
    ),

    MINIMALIST_NEUTRAL(
        id = "MINIMALIST_NEUTRAL",
        title = "Minimalist Neutral",
        hindiTitle = "मिनिमलिस्ट न्यूट्रल",
        craftFocus = "Modern Crafts, Khadi & E-Commerce Export",
        hindiCraftFocus = "आधुनिक शिल्प, खादी व स्वच्छ स्टूडियो एक्सपोर्ट",
        tagline = "ई-कॉमर्स व कैटलॉग के लिए क्रिस्प स्वच्छ बैकड्रॉप",
        description = "राष्ट्रीय व अंतरराष्ट्रीय खरीदारों के लिए शुद्ध स्टूडियो न्यूट्रल बैकग्राउंड। उत्पाद के किनारों और सटीक रंगों को स्पष्ट दिखाता है।",
        autoBrightness = 1.10f,
        autoContrast = 1.15f,
        autoSaturation = 1.00f,
        bgColors = listOf(Color(0xFFFFFFFF), Color(0xFFF8F9FA), Color(0xFFF1F3F5)),
        overlayTint = Color(0x0C000000),
        textureDetailTag = "📦 Crisp Edge Isolation & True Color Balance",
        textureDetailHindi = "सटीक प्राकृतिक रंग और क्रिस्प उत्पाद किनारे",
        narrationText = "मिनिमलिस्ट न्यूट्रल फ़िल्टर ऑनलाइन खरीदारों और डिजिटल कैटलॉग के लिए एकदम साफ़ बैकग्राउंड और शुद्ध रंग संतुलन प्रदान करता है।"
    ),

    // Backward-compatibility aliases
    ARTISAN_NATURAL(
        id = "ARTISAN_NATURAL",
        title = "Artisan Natural",
        hindiTitle = "प्राकृतिक कारीगरी",
        craftFocus = "Terracotta, Pottery & Raw Cotton",
        hindiCraftFocus = "टेराकोटा, मिट्टी के बर्तन व कच्चा सूत",
        tagline = "प्रामाणिक प्राकृतिक मिट्टी व रेशे की बनावट",
        description = "टेराकोटा, मिट्टी और प्राकृतिक सूती वस्त्रों के लिए संतुलित प्राकृतिक प्रकाश। असली मिट्टी के कणों और धागों की बनावट को 100% सुरक्षित रखता है।",
        autoBrightness = 1.06f,
        autoContrast = 1.12f,
        autoSaturation = 1.05f,
        bgColors = listOf(Color(0xFFFDF8F3), Color(0xFFF4E5D4), Color(0xFFEADBCA)),
        overlayTint = Color(0x18B33918),
        textureDetailTag = "🏺 Natural Clay & Fiber Grain: Preserved",
        textureDetailHindi = "मिट्टी व प्राकृतिक रेशे के सूक्ष्म कण सुरक्षित",
        narrationText = "नेचुरल स्टूडियो फ़िल्टर आपके मिट्टी के बर्तनों और प्राकृतिक सूती शिल्प के लिए एकदम सही है।"
    ),

    VIBRANT_LOOM(
        id = "VIBRANT_LOOM",
        title = "Vibrant Loom",
        hindiTitle = "सजीव हथकरघा",
        craftFocus = "Handloom Silk, Zari & Embroidery",
        hindiCraftFocus = "बनारसी सिल्क, जरी, बांधनी व कशीदाकारी",
        tagline = "रेशम, जरी व हथकरघा बुनाई की जीवंत चमक",
        description = "साड़ियों, शॉल और जरीदार वस्त्रों के ताना-बाना (Warp & Weft) धागों की चमक और समृद्ध रंगों को उभारने के लिए विशेष रूप से ट्यून किया गया।",
        autoBrightness = 1.14f,
        autoContrast = 1.25f,
        autoSaturation = 1.32f,
        bgColors = listOf(Color(0xFFFFFBF5), Color(0xFFFDE8CE), Color(0xFFF9D5A7)),
        overlayTint = Color(0x1AFFD54F),
        textureDetailTag = "🧵 Warp & Weft Zari Thread Sheen: Enhanced",
        textureDetailHindi = "ताना-बाना धागे और जरी की धात्विक चमक बढ़ाई गई",
        narrationText = "हैंडलूम विविड फ़िल्टर हथकरघा रेशम, जरी और कशीदाकारी के लिए तैयार किया गया है।"
    ),

    CLASSIC_CATALOG(
        id = "CLASSIC_CATALOG",
        title = "Classic Catalog",
        hindiTitle = "क्लासिक कैटलॉग",
        craftFocus = "E-Commerce & Digital Catalog Export",
        hindiCraftFocus = "डिजिटल कैटलॉग व ऑनलाइन स्टोर",
        tagline = "ई-कॉमर्स व कैटलॉग के लिए क्रिस्प व्हाइट बैकड्रॉप",
        description = "राष्ट्रीय व अंतरराष्ट्रीय खरीदारों के लिए शुद्ध स्टूडियो व्हाइट बैकग्राउंड।",
        autoBrightness = 1.10f,
        autoContrast = 1.15f,
        autoSaturation = 1.00f,
        bgColors = listOf(Color(0xFFFFFFFF), Color(0xFFF8F9FA), Color(0xFFF1F3F5)),
        overlayTint = Color(0x10FFFFFF),
        textureDetailTag = "📦 Crisp Edge Isolation & True Color Balance",
        textureDetailHindi = "सटीक प्राकृतिक रंग और क्रिस्प उत्पाद किनारे",
        narrationText = "मिनिमलिस्ट न्यूट्रल फ़िल्टर ऑनलाइन खरीदारों और डिजिटल कैटलॉग के लिए एकदम साफ़ बैकग्राउंड प्रदान करता है।"
    ),

    HERITAGE_WARMTH(
        id = "HERITAGE_WARMTH",
        title = "Heritage Warmth",
        hindiTitle = "विरासत आभा",
        craftFocus = "Dhokra Bell Metal, Brass & Woodcarving",
        hindiCraftFocus = "ढोकरा पीतल, कांस्य व शीशम नक्काशी",
        tagline = "सुनहरी प्राचीन चमक व नक्काशी गहराई",
        description = "पीतल, कांस्य, धातु की ढलाई और नक्काशीदार लकड़ी के शिल्प में सुनहरी चमक और प्राचीन पेटिना (Patina) गहराई को निखारता है।",
        autoBrightness = 1.08f,
        autoContrast = 1.18f,
        autoSaturation = 1.14f,
        bgColors = listOf(Color(0xFF2C241D), Color(0xFF1E1712), Color(0xFF140F0B)),
        overlayTint = Color(0x22D97706),
        textureDetailTag = "✨ Metal Patina & Woodgrain Depth: Boosted",
        textureDetailHindi = "धातु का सुनहरा प्रतिबिंब व लकड़ी के रेशों की गहराई",
        narrationText = "हेरिटेज वॉर्मथ फ़िल्टर ढोकरा पीतल, कांस्य और नक्काशीदार लकड़ी के लिए सुनहरा प्राचीन लुक देता है।"
    ),

    INDIGO_BLOCK_PRINT(
        id = "INDIGO_BLOCK_PRINT",
        title = "Indigo & Block Print",
        hindiTitle = "इंडीगो व ब्लॉक प्रिंट",
        craftFocus = "Ajrakh, Dabu, Bagru & Kalamkari",
        hindiCraftFocus = "अजरक, दाबू, बागरू व कलमकारी प्रिंट",
        tagline = "लकड़ी के ठप्पों और प्राकृतिक रंगों का सूक्ष्म विवरण",
        description = "हाथ से बने लकड़ी के ब्लॉक ठप्पों की ज्यामितीय नक्काशी और प्राकृतिक नील (Indigo) रंगों के किनारों को क्रिस्प और शार्प करता है।",
        autoBrightness = 1.05f,
        autoContrast = 1.25f,
        autoSaturation = 1.20f,
        bgColors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
        overlayTint = Color(0x181E40AF),
        textureDetailTag = "🪡 Block Stamp Edges & Natural Dye Clarity: High",
        textureDetailHindi = "ब्लॉक प्रिंट के ठप्पों के बारीक किनारे व नील की गहराई",
        narrationText = "इंडीगो व ब्लॉक प्रिंट फ़िल्टर अजरक और कलमकारी प्रिंट के ठप्पों के बारीक किनारों और गहरे प्राकृतिक रंगों को उभारता है।"
    ),

    MUTED_LINEN(
        id = "MUTED_LINEN",
        title = "Muted Linen & Raw Fiber",
        hindiTitle = "कच्चा रेशा व लिनेन",
        craftFocus = "Khadi Cotton, Jute & Bamboo Cane",
        hindiCraftFocus = "खादी, जूट, बांस व प्राकृतिक रेशा शिल्प",
        tagline = "प्राकृतिक रेशा बनावट व सौम्य मैट फिनिश",
        description = "खादी, जूट और बांस की टोकरियों के लिए सौम्य मैट फिनिश। बिना किसी कृत्रिम चमक के प्राकृतिक रेशों की बनावट को दर्शाता है।",
        autoBrightness = 1.02f,
        autoContrast = 1.06f,
        autoSaturation = 0.94f,
        bgColors = listOf(Color(0xFFFAF7F2), Color(0xFFF2ECE1), Color(0xFFE5DCCF)),
        overlayTint = Color(0x1278716C),
        textureDetailTag = "🌾 Raw Strand Weave & Matte Highlights",
        textureDetailHindi = "कच्चे रेशों की बुनावट और सौम्य मैट शेडिंग",
        narrationText = "म्यूटेड लिनेन फ़िल्टर खादी, जूट और बांस शिल्प के लिए सौम्य और प्राकृतिक मैट लुक देता है।"
    );

    val emoji: String
        get() = when (this) {
            HANDLOOM_VIVID, VIBRANT_LOOM -> "🧵"
            NATURAL_STUDIO, ARTISAN_NATURAL -> "🏺"
            MINIMALIST_NEUTRAL, CLASSIC_CATALOG -> "✨"
            HERITAGE_WARMTH -> "🪵"
            INDIGO_BLOCK_PRINT -> "🪡"
            MUTED_LINEN -> "🌾"
            else -> "🎨"
        }

    companion object {
        val PRIMARY_THREE_FILTERS: List<CraftImageFilter> = listOf(
            HANDLOOM_VIVID,
            NATURAL_STUDIO,
            MINIMALIST_NEUTRAL
        )

        fun fromIdOrDefault(id: String?): CraftImageFilter {
            if (id == null) return ARTISAN_NATURAL
            values().firstOrNull {
                it.id.equals(id, ignoreCase = true) ||
                it.name.equals(id, ignoreCase = true) ||
                it.title.equals(id, ignoreCase = true) ||
                it.hindiTitle.equals(id, ignoreCase = true)
            }?.let { return it }

            val normalized = id.trim().lowercase().replace("_", " ").replace("-", " ")
            return when {
                normalized.contains("vibrant loom") || normalized.contains("vibrant") || normalized.contains("loom") || normalized.contains("handloom") || normalized.contains("vivid") -> VIBRANT_LOOM
                normalized.contains("artisan natural") || normalized.contains("natural studio") || normalized.contains("natural") -> ARTISAN_NATURAL
                normalized.contains("classic catalog") || normalized.contains("catalog") || normalized.contains("minimalist") || normalized.contains("neutral") -> CLASSIC_CATALOG
                normalized.contains("indigo") || normalized.contains("ajrakh") || normalized.contains("block") -> INDIGO_BLOCK_PRINT
                normalized.contains("heritage") || normalized.contains("warmth") || normalized.contains("brass") || normalized.contains("metal") -> HERITAGE_WARMTH
                normalized.contains("linen") || normalized.contains("fiber") || normalized.contains("jute") -> MUTED_LINEN
                else -> ARTISAN_NATURAL
            }
        }

        fun findMatchingForCategory(category: String): CraftImageFilter {
            val cat = category.lowercase()
            return when {
                cat.contains("indigo") || cat.contains("ajrakh") || cat.contains("block") -> INDIGO_BLOCK_PRINT
                cat.contains("metal") || cat.contains("dhokra") || cat.contains("brass") || cat.contains("bronze") || cat.contains("bell") -> HERITAGE_WARMTH
                cat.contains("linen") || cat.contains("jute") || cat.contains("cane") || cat.contains("bamboo") || cat.contains("basket") || cat.contains("fiber") -> MUTED_LINEN
                cat.contains("silk") || cat.contains("saree") || cat.contains("embroid") || cat.contains("zari") || cat.contains("textile") || cat.contains("handloom") || cat.contains("loom") || cat.contains("brocade") || cat.contains("shawl") -> VIBRANT_LOOM
                cat.contains("terracotta") || cat.contains("pottery") || cat.contains("clay") || cat.contains("ceramic") || cat.contains("stone") || cat.contains("mud") -> ARTISAN_NATURAL
                else -> CLASSIC_CATALOG
            }
        }
    }
}

/**
 * Calculates a precise 4x5 ColorMatrix combining Saturation, Contrast, and Brightness
 * tuned for textile weaves, clay grain, and metallic reflections.
 */
fun calculateCraftColorMatrix(
    brightness: Float,  // 1.0f = normal, >1.0f = brighter
    contrast: Float,    // 1.0f = normal, >1.0f = higher contrast
    saturation: Float   // 1.0f = normal, >1.0f = more saturated
): ColorMatrix {
    // Luminance coefficients for sRGB (Rec. 709)
    val lr = 0.2126f * (1f - saturation)
    val lg = 0.7152f * (1f - saturation)
    val lb = 0.0722f * (1f - saturation)

    val rR = (lr + saturation) * contrast
    val rG = lg * contrast
    val rB = lb * contrast

    val gR = lr * contrast
    val gG = (lg + saturation) * contrast
    val gB = lb * contrast

    val bR = lr * contrast
    val bG = lg * contrast
    val bB = (lb + saturation) * contrast

    // Center contrast around middle gray (0.5), scale by 255 for color matrix offset
    val offset = ((0.5f * (1f - contrast)) + (brightness - 1.0f)) * 255f

    val matrixArray = floatArrayOf(
        rR, rG, rB, 0f, offset,
        gR, gG, gB, 0f, offset,
        bR, bG, bB, 0f, offset,
        0f, 0f, 0f, 1f, 0f
    )

    return ColorMatrix(matrixArray)
}
