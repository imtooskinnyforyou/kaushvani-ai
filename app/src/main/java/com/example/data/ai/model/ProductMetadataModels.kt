package com.example.data.ai.model

import android.graphics.Bitmap

/**
 * Request payload containing captured image, transcribed voice description, and artisan craft context.
 */
data class ProductMetadataRequest(
    val imageBitmap: Bitmap? = null,
    val imageUri: String? = null,
    val voiceDescription: String = "",
    val category: String = "Pottery",
    val rawMaterialCost: Double = 0.0,
    val laborHours: Double = 0.0,
    val targetLanguage: String = "Hindi",
    val artisanName: String = "Ram Prasad Prajapati",
    val clusterLocation: String = "Bhiti Rawat, Gorakhpur",
    val state: String = "Uttar Pradesh"
)

/**
 * Structured tag category container for fine-grained e-commerce & export categorization.
 */
data class TagCategoryItem(
    val categoryName: String, // e.g. "Craft & Technique", "Materials", "Aesthetics & Style", "Occasion & Utility", "Heritage & GI"
    val tags: List<String>
)

/**
 * Extracted and Generated structured product metadata produced by the Multilingual Auto-Cataloger pipeline.
 *
 * PIPELINE:
 * Voice ➔ Speech-to-Text ➔ Language Detection ➔ Translation ➔ Product Information Extraction ➔ LLM Product Generation ➔ Human Review ➔ Catalog
 */
data class ProductStructuredMetadata(
    // Generated Core Fields
    val title: String,                               // 1. Product title
    val regionalTitle: String = "",
    val shortDescription: String = "",               // 2. Short description
    val description: String,                         // 3. Detailed description
    val regionalDescription: String = "",
    val seoKeywords: List<String> = emptyList(),      // 4. SEO keywords
    val suggestedTags: List<String> = emptyList(),    // 5. Product tags
    val craftStory: String = "",                     // 6. Craft story / Artisan story

    // Extracted Product Information
    val productName: String = "",                    // Extracted Product name
    val category: String,                            // Extracted Product category
    val specificCraftType: String,                   // Extracted Craft type
    val materialsUsed: String,                       // Extracted Material
    val color: String = "Natural / Traditional",     // Extracted Color
    val dimensionsEstimate: String = "",             // Extracted Size/dimensions
    val manufacturingTechnique: String = "",         // Extracted Manufacturing technique
    val regionOrigin: String = "",                   // Extracted Region / Origin
    val artisanStory: String = "",                   // Extracted Artisan story
    val keywords: List<String> = emptyList(),        // Extracted Keywords
    val allTags: List<String> = emptyList(),         // Extracted / Aggregated Tags

    // Multilingual & Language Detection
    val detectedLanguage: String = "Hindi",          // Detected language (e.g. Marathi, Hindi, English)
    val detectedLanguageCode: String = "hi",
    val detectedConfidence: Float = 0.96f,
    val translatedVoiceEnglish: String = "",         // Translated transcript to English

    // Fair-Wage Pricing & Logistics
    val tagCategories: List<TagCategoryItem> = emptyList(),
    val suggestedMinPrice: Double,
    val suggestedMaxPrice: Double,
    val wholesalePrice: Double,
    val retailPrice: Double,
    val hourlyWageRate: Double = 175.0,
    val packagingCost: Double = 50.0,
    val careInstructions: String = "",
    val giTagEligible: Boolean = true,
    val giTagReason: String = "",
    val culturalStory: String = "",
    val estimatedWeightKg: Double = 0.5,
    val isAiGenerated: Boolean = true,
    val confidenceScore: Float = 0.95f
)
