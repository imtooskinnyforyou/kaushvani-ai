package com.example.data.model

/**
 * Industry Standard Craft Classification Models.
 * Mapped to Ministry of Textiles, EPCH (Export Promotion Council for Handicrafts),
 * ONDC Handicrafts Taxonomy, and Harmonized System (HSN) Codes.
 */

data class IndustryStandardCraftCategory(
    val id: String,
    val name: String,
    val hindiName: String,
    val iconEmoji: String,
    val description: String,
    val hindiDescription: String,
    val hsnChapter: String,
    val subCategories: List<CraftSubCategory>
)

data class CraftSubCategory(
    val id: String,
    val parentCategoryId: String,
    val name: String,
    val hindiName: String,
    val description: String,
    val hsnPrefix: String,
    val ondcTaxonomy: String,
    val disciplines: List<CraftDisciplineItem>
)

data class CraftDisciplineItem(
    val id: String,
    val parentCategoryId: String,
    val subCategoryId: String,
    val name: String,
    val hindiName: String,
    val region: String,
    val state: String,
    val isGiTagged: Boolean,
    val hsnCode: String,
    val ondcCategory: String,
    val epchCode: String,
    val materials: List<String>,
    val technique: String,
    val standardCertifications: List<String>,
    val suggestedRetailBenchmark: String = "₹1,200 - ₹8,500"
)

data class SelectedCategoryMapping(
    val categoryId: String,
    val categoryName: String,
    val categoryHindiName: String,
    val subCategoryId: String,
    val subCategoryName: String,
    val subCategoryHindiName: String,
    val disciplineId: String,
    val disciplineName: String,
    val disciplineHindiName: String,
    val region: String,
    val isGiTagged: Boolean,
    val hsnCode: String,
    val ondcCategory: String,
    val materials: List<String>,
    val technique: String,
    val certifications: List<String>
) {
    fun getBreadcrumb(isHindi: Boolean = false): String {
        return if (isHindi) {
            "$categoryHindiName > $subCategoryHindiName > $disciplineHindiName"
        } else {
            "$categoryName > $subCategoryName > $disciplineName"
        }
    }
}
