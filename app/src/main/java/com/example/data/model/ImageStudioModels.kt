package com.example.data.model

enum class ImageStudioStage {
    CAPTURE_UPLOAD,
    CROP_ALIGN,
    AI_ANALYSIS,
    ENHANCE_STUDIO,
    QUALITY_REVIEW,
    SAVED_SUCCESS
}

enum class QualityRating {
    GOOD,
    NEEDS_IMPROVEMENT
}

enum class CheckStatus {
    PASSED,
    WARNING,
    FAILED
}

data class QualityCheckItem(
    val id: String,
    val name: String,
    val hindiName: String,
    val status: CheckStatus,
    val description: String,
    val hindiDescription: String
)

data class ImageQualityReport(
    val overallRating: QualityRating,
    val scorePercent: Int,
    val checks: List<QualityCheckItem>,
    val actionableSuggestions: List<String>,
    val hindiSuggestions: List<String>,
    val dimensions: String = "1080 x 1080 px",
    val aspectRatio: String = "1:1 Square (E-Commerce Standard)"
)

enum class BackdropType {
    WHITE_STUDIO,
    WARM_TERRACOTTA,
    RAW_LINEN,
    DARK_TEAKWOOD,
    PEDESTAL_GRADIENT
}

data class StudioBackdrop(
    val id: String,
    val name: String,
    val hindiName: String,
    val description: String,
    val type: BackdropType,
    val badge: String
)

enum class CropAspectRatio(
    val ratio: Float,
    val label: String,
    val hindiLabel: String,
    val marketplaceTag: String,
    val dimensionsLabel: String,
    val recommendedFor: String
) {
    SQUARE_1_1(
        ratio = 1.0f,
        label = "1:1 Square",
        hindiLabel = "1:1 वर्गाकार",
        marketplaceTag = "Amazon • Flipkart • ONDC",
        dimensionsLabel = "1080 × 1080 px",
        recommendedFor = "Pottery, Jewelry, Metalcraft, General Catalog"
    ),
    PORTRAIT_4_5(
        ratio = 0.8f,
        label = "4:5 Portrait",
        hindiLabel = "4:5 पोर्ट्रेट",
        marketplaceTag = "Instagram • Social Marketplace",
        dimensionsLabel = "1080 × 1350 px",
        recommendedFor = "Social Commerce, Tall Crafts, Showcase"
    ),
    PORTRAIT_3_4(
        ratio = 0.75f,
        label = "3:4 Catalog",
        hindiLabel = "3:4 कैटलॉग",
        marketplaceTag = "Apparel • Saree & Handloom",
        dimensionsLabel = "1080 × 1440 px",
        recommendedFor = "Textiles, Kurtas, Shawls, Wall Hangings"
    ),
    CATALOG_4_3(
        ratio = 1.333f,
        label = "4:3 Wide",
        hindiLabel = "4:3 लैंडस्केप",
        marketplaceTag = "Home Decor & Sculptures",
        dimensionsLabel = "1440 × 1080 px",
        recommendedFor = "Dhokra art, Rugs, Wide Crafts"
    ),
    BANNER_16_9(
        ratio = 1.777f,
        label = "16:9 Banner",
        hindiLabel = "16:9 बैनर",
        marketplaceTag = "Storefront Header Banner",
        dimensionsLabel = "1920 × 1080 px",
        recommendedFor = "Artisan Store Header & Cover Photos"
    )
}

data class ImageEnhancementSettings(
    val brightness: Float = 0.15f,     // -1.0 to 1.0
    val contrast: Float = 0.20f,       // -1.0 to 1.0
    val sharpness: Float = 0.65f,      // 0.0 to 1.0
    val selectedBackdrop: StudioBackdrop = StudioBackdropPresets.first(),
    val cropRatio: CropAspectRatio = CropAspectRatio.SQUARE_1_1,
    val rotationDegrees: Float = 0f,
    val isFlippedHorizontal: Boolean = false,
    val cropScale: Float = 1.0f,
    val cropOffsetX: Float = 0f,
    val cropOffsetY: Float = 0f,
    val isCentered: Boolean = true,
    val isBackgroundRemoved: Boolean = true,
    val sliderPosition: Float = 0.50f, // 0.0 (full original) to 1.0 (full enhanced)
    val autoImproved: Boolean = false
)

object StudioBackdropPresets {
    val presets = listOf(
        StudioBackdrop(
            id = "studio_white",
            name = "Clean Studio White",
            hindiName = "क्लीन स्टूडियो व्हाइट",
            description = "E-commerce digital catalog standard clean neutral backdrop",
            type = BackdropType.WHITE_STUDIO,
            badge = "E-Commerce"
        ),
        StudioBackdrop(
            id = "warm_terracotta",
            name = "Terracotta & Cream",
            hindiName = "मिट्टी और क्रीम",
            description = "Warm Indian heritage earth tone accentuating handcrafted authenticity",
            type = BackdropType.WARM_TERRACOTTA,
            badge = "Heritage"
        ),
        StudioBackdrop(
            id = "raw_linen",
            name = "Raw Linen & Jute",
            hindiName = "खादी व जूट टेक्सचर",
            description = "Organic textured background ideal for handloom textiles and pottery",
            type = BackdropType.RAW_LINEN,
            badge = "Organic"
        ),
        StudioBackdrop(
            id = "dark_teakwood",
            name = "Dark Teakwood Showcase",
            hindiName = "शीशम व सागौन डिस्प्ले",
            description = "Rich dark wooden pedestal elevating brass, jewelry, and metalcraft",
            type = BackdropType.DARK_TEAKWOOD,
            badge = "Luxury"
        ),
        StudioBackdrop(
            id = "pedestal_gradient",
            name = "Soft Shadow Gradient",
            hindiName = "सॉफ्ट शैडो ग्रेडिएंट",
            description = "Floating museum pedestal with soft diffused lighting shadow",
            type = BackdropType.PEDESTAL_GRADIENT,
            badge = "Gallery"
        )
    )

    fun first(): StudioBackdrop = presets.first()
}
