package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CraftDisciplineItem
import com.example.data.model.CraftSubCategory
import com.example.data.model.IndustryStandardCraftCategory
import com.example.data.model.SelectedCategoryMapping
import com.example.data.repository.CraftTaxonomyRepository
import com.example.ui.theme.*

/**
 * Modal Bottom Sheet for Hierarchical Handicraft Category Selection.
 * Allows artisans to map their products to industry-standard taxonomy, HSN codes,
 * and ONDC e-commerce categories using interactive multi-level chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HierarchicalCategoryBottomSheet(
    initialMapping: SelectedCategoryMapping?,
    onDismiss: () -> Unit,
    onCategorySelected: (SelectedCategoryMapping) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ArtisanSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = ArtisanTextMuted.copy(alpha = 0.4f)
            )
        },
        modifier = modifier
            .fillMaxHeight(0.92f)
            .testTag("hierarchical_category_bottom_sheet")
    ) {
        HierarchicalCategoryPickerContent(
            initialMapping = initialMapping,
            onApply = onCategorySelected,
            onClose = onDismiss
        )
    }
}

/**
 * Reusable full content for hierarchical category selection with multi-level chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HierarchicalCategoryPickerContent(
    initialMapping: SelectedCategoryMapping?,
    onApply: (SelectedCategoryMapping) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember { CraftTaxonomyRepository.allTaxonomyCategories }
    var searchQuery by remember { mutableStateOf("") }
    var filterGiOnly by remember { mutableStateOf(false) }

    // Navigation and Drill-down Selection State
    var selectedCategory by remember {
        mutableStateOf(
            categories.find { it.id == initialMapping?.categoryId } ?: categories.first()
        )
    }

    var selectedSubCategory by remember {
        mutableStateOf(
            selectedCategory.subCategories.find { it.id == initialMapping?.subCategoryId }
                ?: selectedCategory.subCategories.first()
        )
    }

    var selectedDiscipline by remember {
        mutableStateOf(
            selectedSubCategory.disciplines.find { it.id == initialMapping?.disciplineId }
                ?: selectedSubCategory.disciplines.first()
        )
    }

    // Update subcategory and discipline when parent category changes
    LaunchedEffect(selectedCategory) {
        if (!selectedCategory.subCategories.contains(selectedSubCategory)) {
            val newSub = selectedCategory.subCategories.firstOrNull()
            if (newSub != null) {
                selectedSubCategory = newSub
                selectedDiscipline = newSub.disciplines.firstOrNull() ?: selectedDiscipline
            }
        }
    }

    // Search results when user enters text
    val searchResults = remember(searchQuery, filterGiOnly) {
        if (searchQuery.isNotBlank() || filterGiOnly) {
            val results = CraftTaxonomyRepository.searchTaxonomy(searchQuery)
            if (filterGiOnly) results.filter { it.isGiTagged } else results
        } else {
            emptyList()
        }
    }

    val currentMapping = remember(selectedCategory, selectedSubCategory, selectedDiscipline) {
        SelectedCategoryMapping(
            categoryId = selectedCategory.id,
            categoryName = selectedCategory.name,
            categoryHindiName = selectedCategory.hindiName,
            subCategoryId = selectedSubCategory.id,
            subCategoryName = selectedSubCategory.name,
            subCategoryHindiName = selectedSubCategory.hindiName,
            disciplineId = selectedDiscipline.id,
            disciplineName = selectedDiscipline.name,
            disciplineHindiName = selectedDiscipline.hindiName,
            region = "${selectedDiscipline.region}, ${selectedDiscipline.state}",
            isGiTagged = selectedDiscipline.isGiTagged,
            hsnCode = selectedDiscipline.hsnCode,
            ondcCategory = selectedDiscipline.ondcCategory,
            materials = selectedDiscipline.materials,
            technique = selectedDiscipline.technique,
            certifications = selectedDiscipline.standardCertifications
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArtisanBackground)
    ) {
        // 1. Top Header Bar
        Surface(
            color = ArtisanSurface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🏷️ मानक शिल्प वर्गीकरण",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Surface(
                                color = TerracottaContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "HSN & Catalog Code",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Industry-Standard Hierarchical Category Mapping",
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_close_category_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ArtisanTextPrimary
                        )
                    }
                }

                // Search Bar with clear button and GI Filter toggle
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "खोजें: पैठणी, Terracotta, HSN 5007, Channapatna...",
                            fontSize = 13.sp,
                            color = ArtisanTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Categories",
                            tint = TerracottaPrimary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = ArtisanTextMuted
                                    )
                                }
                            }
                            FilterChip(
                                selected = filterGiOnly,
                                onClick = { filterGiOnly = !filterGiOnly },
                                label = {
                                    Text(
                                        text = "⭐ GI Tag",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldAccent.copy(alpha = 0.25f),
                                    selectedLabelColor = Color(0xFF92400E)
                                ),
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .testTag("filter_gi_tag_toggle")
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = ArtisanDivider,
                        focusedContainerColor = ArtisanSurface,
                        unfocusedContainerColor = ArtisanSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_search_input"),
                    singleLine = true
                )
            }
        }

        // 2. Main Content Body (Search Results OR Hierarchical Multi-Level Chips)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (searchQuery.isNotBlank() || filterGiOnly) {
                // Search Results View
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔍", fontSize = 40.sp)
                            Text(
                                text = "कोई शिल्प श्रेणी नहीं मिली",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Text(
                                text = "कृपया अन्य कीवर्ड या श्रेणी नाम से खोजें",
                                fontSize = 13.sp,
                                color = ArtisanTextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("category_search_results_list"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "मिलती-जुलती श्रेणियां (${searchResults.size} परिणाम):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextSecondary
                            )
                        }

                        items(searchResults, key = { it.disciplineId }) { mapping ->
                            SearchResultMappingCard(
                                mapping = mapping,
                                isSelected = mapping.disciplineId == currentMapping.disciplineId,
                                onClick = {
                                    val parent = categories.find { it.id == mapping.categoryId }
                                    if (parent != null) {
                                        selectedCategory = parent
                                        val sub = parent.subCategories.find { it.id == mapping.subCategoryId }
                                        if (sub != null) {
                                            selectedSubCategory = sub
                                            val disc = sub.disciplines.find { it.id == mapping.disciplineId }
                                            if (disc != null) {
                                                selectedDiscipline = disc
                                            }
                                        }
                                    }
                                    searchQuery = ""
                                }
                            )
                        }
                    }
                }
            } else {
                // Interactive Hierarchical 3-Tier Selection
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("hierarchical_chips_container"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Pick Chips: Popular Crafts
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🔥 लोकप्रिय हस्तशिल्प (Quick Pick):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.testTag("popular_crafts_quick_pick_row")
                            ) {
                                items(CraftTaxonomyRepository.getDefaultPopularMappings()) { pop ->
                                    val isSelected = pop.disciplineId == currentMapping.disciplineId
                                    SuggestionChip(
                                        onClick = {
                                            val parent = categories.find { it.id == pop.categoryId }
                                            if (parent != null) {
                                                selectedCategory = parent
                                                val sub = parent.subCategories.find { it.id == pop.subCategoryId }
                                                if (sub != null) {
                                                    selectedSubCategory = sub
                                                    val disc = sub.disciplines.find { it.id == pop.disciplineId }
                                                    if (disc != null) {
                                                        selectedDiscipline = disc
                                                    }
                                                }
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = pop.disciplineHindiName,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        icon = {
                                            if (pop.isGiTagged) {
                                                Text("⭐", fontSize = 11.sp)
                                            }
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isSelected) TerracottaContainer else ArtisanSurface,
                                            labelColor = if (isSelected) TerracottaPrimary else ArtisanTextPrimary
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) TerracottaPrimary else ArtisanDivider
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // TIER 1: Main Craft Sectors (Parent Categories)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = IndigoSecondary,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("1", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "मुख्य शिल्प क्षेत्र (Main Craft Sector)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                }

                                Text(
                                    text = "${selectedCategory.hsnChapter}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TerracottaPrimary
                                )
                            }

                            // Horizontal Scrolling Chips with Emoji Icons
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.testTag("tier1_category_chips_row")
                            ) {
                                items(categories) { cat ->
                                    val isSelected = cat.id == selectedCategory.id
                                    val chipColor by animateColorAsState(
                                        targetValue = if (isSelected) IndigoSecondary else ArtisanSurface,
                                        label = "cat_color"
                                    )
                                    val textColor = if (isSelected) Color.White else ArtisanTextPrimary

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = chipColor,
                                        shadowElevation = if (isSelected) 3.dp else 1.dp,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = if (isSelected) 0.dp else 1.dp,
                                                color = ArtisanDivider,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .semantics {
                                                role = Role.Tab
                                                selected = isSelected
                                                contentDescription = "${cat.hindiName} - ${cat.name} श्रेणी"
                                                stateDescription = if (isSelected) "चयनित (Selected)" else "उपलब्ध (Available)"
                                            }
                                            .clickable {
                                                selectedCategory = cat
                                                selectedSubCategory = cat.subCategories.first()
                                                selectedDiscipline = cat.subCategories.first().disciplines.first()
                                            }
                                            .testTag("tier1_chip_${cat.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(text = cat.iconEmoji, fontSize = 16.sp)
                                            Column {
                                                Text(
                                                    text = cat.hindiName,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = textColor
                                                )
                                                Text(
                                                    text = cat.name,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else ArtisanTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TIER 2: Sub-Categories / Product Family
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = TerracottaPrimary,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("2", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "उप-श्रेणी एवं उत्पाद प्रकार (Sub-Category)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                            }

                            // Sub-category Filter Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedCategory.subCategories.forEach { subCat ->
                                    val isSelected = subCat.id == selectedSubCategory.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedSubCategory = subCat
                                            selectedDiscipline = subCat.disciplines.first()
                                        },
                                        label = {
                                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(
                                                    text = subCat.hindiName,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Text(
                                                    text = subCat.name,
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) TerracottaPrimary else ArtisanTextMuted
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .semantics {
                                                role = Role.Tab
                                                selected = isSelected
                                                contentDescription = "${subCat.hindiName} - ${subCat.name} उप-श्रेणी"
                                                stateDescription = if (isSelected) "चयनित (Selected)" else "उपलब्ध (Available)"
                                            }
                                            .testTag("tier2_chip_${subCat.id}"),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TerracottaContainer,
                                            selectedLabelColor = TerracottaPrimary,
                                            containerColor = ArtisanSurface
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = ArtisanDivider,
                                            selectedBorderColor = TerracottaPrimary
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // TIER 3: Specific Craft Specialization & GI Clusters
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = ForestSuccess,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("3", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "विशिष्ट शिल्प व जीआई क्लस्टर (GI Craft Discipline)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                }
                                Text(
                                    text = "${selectedSubCategory.disciplines.size} विकल्प",
                                    fontSize = 12.sp,
                                    color = ArtisanTextSecondary
                                )
                            }

                            // Cards for each discipline item
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedSubCategory.disciplines.forEach { disc ->
                                    DisciplineSelectionCard(
                                        discipline = disc,
                                        isSelected = disc.id == selectedDiscipline.id,
                                        onClick = { selectedDiscipline = disc }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Bottom Industry-Standard Summary Card & Action Bar
        Surface(
            color = ArtisanSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selected Mapping Path Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaContainer.copy(alpha = 0.45f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_category_summary_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "चयनित मानक वर्गीकरण (Selected Taxonomy)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                            if (currentMapping.isGiTagged) {
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GoldAccent)
                                ) {
                                    Text(
                                        text = "📍 GI Heritage Craft",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Breadcrumbs
                        Text(
                            text = "${currentMapping.categoryHindiName} > ${currentMapping.subCategoryHindiName} > ${currentMapping.disciplineHindiName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )

                        // HSN & ONDC Codes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ArtisanSurface,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = currentMapping.hsnCode,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ArtisanTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "📍 ${currentMapping.region}",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_cancel_category_picker")
                    ) {
                        Text("रद्द करें", fontSize = 14.sp, color = ArtisanTextPrimary)
                    }

                    Button(
                        onClick = { onApply(currentMapping) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerracottaPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .testTag("btn_apply_hierarchical_category")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "मानक श्रेणी लागू करें",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Discipline Card in Tier 3
 */
@Composable
private fun DisciplineSelectionCard(
    discipline: CraftDisciplineItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) ForestContainer.copy(alpha = 0.5f) else ArtisanSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ForestSuccess else ArtisanDivider
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "${discipline.hindiName} - ${discipline.name}${if (discipline.isGiTagged) ", GI Tag प्रमाणित" else ""}"
                stateDescription = if (isSelected) "चयनित (Selected)" else "उपलब्ध (Available)"
            }
            .clickable(onClick = onClick)
            .testTag("discipline_card_${discipline.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onClick,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ForestSuccess,
                            unselectedColor = ArtisanTextMuted
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = discipline.hindiName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = discipline.name,
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                if (discipline.isGiTagged) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "⭐ GI Tag",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = "📍 उत्पत्ति: ${discipline.region}, ${discipline.state} • ${discipline.hsnCode}",
                fontSize = 11.sp,
                color = ArtisanTextSecondary
            )

            Text(
                text = "कला तकनीक: ${discipline.technique}",
                fontSize = 11.sp,
                color = ArtisanTextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Materials Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                discipline.materials.take(3).forEach { mat ->
                    Surface(
                        color = ArtisanBackground,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = mat,
                            fontSize = 10.sp,
                            color = ArtisanTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Search Result Row Card
 */
@Composable
private fun SearchResultMappingCard(
    mapping: SelectedCategoryMapping,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) TerracottaContainer else ArtisanSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) TerracottaPrimary else ArtisanDivider
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("search_mapping_card_${mapping.disciplineId}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${mapping.categoryHindiName} > ${mapping.subCategoryHindiName}",
                    fontSize = 11.sp,
                    color = TerracottaPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (mapping.isGiTagged) {
                    Text(
                        text = "📍 GI Heritage Craft",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
            }

            Text(
                text = mapping.disciplineHindiName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ArtisanTextPrimary
            )

            Text(
                text = mapping.disciplineName,
                fontSize = 11.sp,
                color = ArtisanTextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mapping.hsnCode,
                    fontSize = 10.sp,
                    color = ArtisanTextMuted
                )
                Text(
                    text = "📍 ${mapping.region}",
                    fontSize = 11.sp,
                    color = ArtisanTextSecondary
                )
            }
        }
    }
}
