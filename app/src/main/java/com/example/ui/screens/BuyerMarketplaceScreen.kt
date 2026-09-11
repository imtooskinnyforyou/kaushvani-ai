package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.SemanticSearchEngine
import com.example.data.ai.SemanticSearchResult
import com.example.data.model.BulkRequirement
import com.example.data.model.ProductEntity
import com.example.ui.components.CraftArtworkDisplay
import com.example.ui.theme.*
import com.example.ui.viewmodel.HunarSetuViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BuyerSubTab {
    MARKETPLACE,
    SAVED_FAVORITES,
    BULK_REQUIREMENTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerMarketplaceScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedSubTab by remember { mutableStateOf(BuyerSubTab.MARKETPLACE) }

    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteProductIds.collectAsStateWithLifecycle()
    val bulkReqs by viewModel.bulkRequirements.collectAsStateWithLifecycle()

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSemanticSearching by remember { mutableStateOf(false) }

    // Categories as specified: Textiles, Handloom, Pottery, Woodcraft, Metalcraft, Jewellery, Home Decor, Other
    val categories = remember {
        listOf(
            "All",
            "Textiles",
            "Handloom",
            "Pottery",
            "Woodcraft",
            "Metalcraft",
            "Jewellery",
            "Home Decor",
            "Other"
        )
    }
    var selectedCategory by remember { mutableStateOf("All") }

    // Filter states: Category, Price, Material, Location, Craft type
    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var selectedPriceRange by remember { mutableStateOf("All") } // "All", "Under ₹1,000", "₹1,000 - ₹3,000", "₹3,000 - ₹8,000", "Above ₹8,000"
    var selectedMaterial by remember { mutableStateOf("All") }
    var selectedLocation by remember { mutableStateOf("All") }
    var selectedCraftType by remember { mutableStateOf("All") }
    var onlyGiTagged by remember { mutableStateOf(false) }

    val priceOptions = listOf("All", "Under ₹1,000", "₹1,000 - ₹3,000", "₹3,000 - ₹8,000", "Above ₹8,000")
    val materialOptions = listOf("All", "Silk", "Clay", "Brass", "Sheesham Wood", "Silver", "Cotton Khadi", "Quartz")
    val locationOptions = listOf("All", "Uttar Pradesh", "Rajasthan", "Chhattisgarh", "Bihar", "Odisha")
    val craftTypeOptions = listOf("All", "Banarasi Weaving", "Gorakhpur Terracotta", "Bastar Dhokra", "Mithila Painting", "Saharanpur Woodcraft", "Silver Filigree", "Jaipur Blue Pottery", "Bagru Block Print")

    // Active filters count
    val activeFilterCount = remember(selectedCategory, selectedPriceRange, selectedMaterial, selectedLocation, selectedCraftType, onlyGiTagged) {
        var count = 0
        if (selectedCategory != "All") count++
        if (selectedPriceRange != "All") count++
        if (selectedMaterial != "All") count++
        if (selectedLocation != "All") count++
        if (selectedCraftType != "All") count++
        if (onlyGiTagged) count++
        count
    }

    // Semantic search results
    var searchResults by remember { mutableStateOf<List<SemanticSearchResult>>(emptyList()) }

    // Calculate Price bounds
    val (minPrice, maxPrice) = remember(selectedPriceRange) {
        when (selectedPriceRange) {
            "Under ₹1,000" -> Pair(0.0, 1000.0)
            "₹1,000 - ₹3,000" -> Pair(1000.0, 3000.0)
            "₹3,000 - ₹8,000" -> Pair(3000.0, 8000.0)
            "Above ₹8,000" -> Pair(8000.0, Double.MAX_VALUE)
            else -> Pair(0.0, Double.MAX_VALUE)
        }
    }

    // Trigger Semantic Search with Debounce
    LaunchedEffect(
        searchQuery,
        selectedCategory,
        selectedPriceRange,
        selectedMaterial,
        selectedLocation,
        selectedCraftType,
        onlyGiTagged,
        products
    ) {
        isSemanticSearching = true
        if (searchQuery.isNotBlank()) {
            delay(250) // Debounce typing
        }
        val res = SemanticSearchEngine.search(
            query = searchQuery,
            products = products,
            selectedCategory = selectedCategory,
            minPrice = minPrice,
            maxPrice = maxPrice,
            selectedMaterial = selectedMaterial,
            selectedLocation = selectedLocation,
            selectedCraftType = selectedCraftType
        ).filter {
            !onlyGiTagged || it.product.isGiTagged
        }
        searchResults = res
        isSemanticSearching = false
    }

    var selectedProductForDetail by remember { mutableStateOf<ProductEntity?>(null) }
    var isRfqDialogOpen by remember { mutableStateOf(false) }
    var isPostBulkDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ArtisanSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🛍️ B2B Buyer Marketplace",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "Direct Ethical Sourcing from Authentic Artisan Clusters",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }

                    if (selectedSubTab == BuyerSubTab.BULK_REQUIREMENTS) {
                        Button(
                            onClick = { isPostBulkDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post RFQ", fontSize = 11.sp)
                        }
                    }
                }

                // Sub Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedSubTab.ordinal,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedSubTab == BuyerSubTab.MARKETPLACE,
                        onClick = { selectedSubTab = BuyerSubTab.MARKETPLACE },
                        modifier = Modifier.semantics {
                            role = androidx.compose.ui.semantics.Role.Tab
                            this.selected = selectedSubTab == BuyerSubTab.MARKETPLACE
                            contentDescription = "मार्केटप्लेस टैब, ${products.size} उपलब्ध उत्पाद"
                            stateDescription = if (selectedSubTab == BuyerSubTab.MARKETPLACE) "चयनित (Selected)" else "उपलब्ध (Available)"
                        },
                        text = { Text("Marketplace (${products.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSubTab == BuyerSubTab.SAVED_FAVORITES,
                        onClick = { selectedSubTab = BuyerSubTab.SAVED_FAVORITES },
                        modifier = Modifier.semantics {
                            role = androidx.compose.ui.semantics.Role.Tab
                            this.selected = selectedSubTab == BuyerSubTab.SAVED_FAVORITES
                            contentDescription = "पसंदीदा उत्पाद टैब, ${favorites.size} सहेजे गए उत्पाद"
                            stateDescription = if (selectedSubTab == BuyerSubTab.SAVED_FAVORITES) "चयनित (Selected)" else "उपलब्ध (Available)"
                        },
                        text = { Text("❤️ Saved (${favorites.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedSubTab == BuyerSubTab.BULK_REQUIREMENTS,
                        onClick = { selectedSubTab = BuyerSubTab.BULK_REQUIREMENTS },
                        modifier = Modifier.semantics {
                            role = androidx.compose.ui.semantics.Role.Tab
                            this.selected = selectedSubTab == BuyerSubTab.BULK_REQUIREMENTS
                            contentDescription = "थोक मांग बोर्ड टैब, ${bulkReqs.size} आवश्यकताएं"
                            stateDescription = if (selectedSubTab == BuyerSubTab.BULK_REQUIREMENTS) "चयनित (Selected)" else "उपलब्ध (Available)"
                        },
                        text = { Text("📢 Bulk RFQ Board (${bulkReqs.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                if (selectedSubTab != BuyerSubTab.BULK_REQUIREMENTS) {
                    // Search Bar as requested: "Search handcrafted products..."
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search handcrafted products...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = if (searchQuery.isNotBlank()) TerracottaPrimary else ArtisanTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.semantics {
                                            role = androidx.compose.ui.semantics.Role.Button
                                            contentDescription = "खोज क्वेरी साफ करें (Clear search query)"
                                        }
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = ArtisanTextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "हस्तशिल्प उत्पाद खोजें (Search handcrafted products)"
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = ArtisanSurfaceVariant,
                                focusedBorderColor = TerracottaPrimary,
                                unfocusedBorderColor = ArtisanCardBorder
                            ),
                            singleLine = true
                        )

                        // Filters button with Badge
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(containerColor = TerracottaPrimary) {
                                        Text("$activeFilterCount")
                                    }
                                }
                            }
                        ) {
                            FilledTonalIconButton(
                                onClick = { isFilterSheetOpen = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (activeFilterCount > 0) TerracottaContainer else ArtisanSurfaceVariant
                                ),
                                modifier = Modifier.semantics {
                                    role = androidx.compose.ui.semantics.Role.Button
                                    contentDescription = "फ़िल्टर मेनू खोलें. $activeFilterCount फ़िल्टर सक्रिय हैं"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = if (activeFilterCount > 0) TerracottaPrimary else ArtisanTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Categories List
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            Surface(
                                modifier = Modifier
                                    .semantics {
                                        role = androidx.compose.ui.semantics.Role.Tab
                                        this.selected = isSelected
                                        contentDescription = "$cat श्रेणी फ़िल्टर"
                                        stateDescription = if (isSelected) "चयनित (Selected)" else "उपलब्ध (Available)"
                                    }
                                    .clickable { selectedCategory = cat },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) IndigoSecondary else ArtisanSurfaceVariant,
                                border = if (!isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder)) else null
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color.White else ArtisanTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Semantic Search indicator & suggestions when searching or empty
                    if (searchQuery.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TerracottaContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isSemanticSearching) "✨ Semantic vector matching in progress..." else "✨ Semantic AI Search active • ${searchResults.size} results",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TerracottaPrimary
                                    )
                                }
                                Text(
                                    text = "Clear",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary,
                                    modifier = Modifier.clickable { searchQuery = "" }
                                )
                            }
                        }
                    } else if (activeFilterCount == 0) {
                        // Helpful semantic search query chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Try:", fontSize = 10.sp, color = ArtisanTextMuted, fontWeight = FontWeight.Bold)
                            listOf(
                                "traditional red handwoven saree",
                                "eco-friendly terracotta clay",
                                "antique tribal brass figurine",
                                "sheesham wood jewelry box"
                            ).forEach { prompt ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ArtisanSurfaceVariant,
                                    modifier = Modifier.clickable { searchQuery = prompt }
                                ) {
                                    Text(
                                        text = "🔍 $prompt",
                                        fontSize = 10.sp,
                                        color = ArtisanTextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ArtisanBackground)
                .padding(innerPadding)
        ) {
            when (selectedSubTab) {
                BuyerSubTab.MARKETPLACE, BuyerSubTab.SAVED_FAVORITES -> {
                    val displayedResults = remember(searchResults, selectedSubTab, favorites) {
                        if (selectedSubTab == BuyerSubTab.SAVED_FAVORITES) {
                            searchResults.filter { favorites.contains(it.product.id) }
                        } else {
                            searchResults
                        }
                    }

                    if (displayedResults.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedSubTab == BuyerSubTab.SAVED_FAVORITES) Icons.Outlined.FavoriteBorder else Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = ArtisanTextMuted,
                                    modifier = Modifier.size(54.dp)
                                )
                                Text(
                                    text = if (selectedSubTab == BuyerSubTab.SAVED_FAVORITES) {
                                        "You haven't saved any handcrafted products yet."
                                    } else {
                                        "No products match your search or filters."
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "Try searching for techniques (e.g. 'handwoven'), materials (e.g. 'pure silk'), or clear your filters.",
                                    fontSize = 12.sp,
                                    color = ArtisanTextSecondary
                                )
                                if (activeFilterCount > 0 || searchQuery.isNotBlank()) {
                                    Button(
                                        onClick = {
                                            searchQuery = ""
                                            selectedCategory = "All"
                                            selectedPriceRange = "All"
                                            selectedMaterial = "All"
                                            selectedLocation = "All"
                                            selectedCraftType = "All"
                                            onlyGiTagged = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Reset All Filters")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(displayedResults, key = { it.product.id }) { result ->
                                val product = result.product
                                val isFav = favorites.contains(product.id)
                                BuyerProductCardEnhanced(
                                    product = product,
                                    semanticResult = result,
                                    isFavorite = isFav,
                                    onToggleFavorite = { viewModel.toggleFavorite(product.id) },
                                    onViewDetails = { viewModel.openProductDetail(product) },
                                    onSendInquiry = {
                                        selectedProductForDetail = product
                                        isRfqDialogOpen = true
                                    }
                                )
                            }
                        }
                    }
                }

                BuyerSubTab.BULK_REQUIREMENTS -> {
                    BulkRequirementsList(
                        requirements = bulkReqs,
                        onPostNew = { isPostBulkDialogOpen = true }
                    )
                }
            }
        }
    }

    // Filter Bottom Sheet (Category, Price, Material, Location, Craft Type)
    if (isFilterSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFilterSheetOpen = false },
            containerColor = ArtisanSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛠️ Filter Products",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    TextButton(
                        onClick = {
                            selectedCategory = "All"
                            selectedPriceRange = "All"
                            selectedMaterial = "All"
                            selectedLocation = "All"
                            selectedCraftType = "All"
                            onlyGiTagged = false
                        }
                    ) {
                        Text("Reset All", color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                // 1. Category Filter
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 2. Price Filter
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Price Range (₹)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(priceOptions) { price ->
                            FilterChip(
                                selected = selectedPriceRange == price,
                                onClick = { selectedPriceRange = price },
                                label = { Text(price, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 3. Material Filter
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Material", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(materialOptions) { mat ->
                            FilterChip(
                                selected = selectedMaterial == mat,
                                onClick = { selectedMaterial = mat },
                                label = { Text(mat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 4. Location Filter
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Artisan Location / Cluster", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(locationOptions) { loc ->
                            FilterChip(
                                selected = selectedLocation == loc,
                                onClick = { selectedLocation = loc },
                                label = { Text(loc, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 5. Craft Type Filter
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Craft Type", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(craftTypeOptions) { craft ->
                            FilterChip(
                                selected = selectedCraftType == craft,
                                onClick = { selectedCraftType = craft },
                                label = { Text(craft, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // 6. GI Tagged Certification Check
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArtisanSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = ForestSuccess, modifier = Modifier.size(20.dp))
                        Column {
                            Text("GI Tag Certified Only", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ArtisanTextPrimary)
                            Text("Official Geographical Indication crafts", fontSize = 10.sp, color = ArtisanTextSecondary)
                        }
                    }
                    Switch(
                        checked = onlyGiTagged,
                        onCheckedChange = { onlyGiTagged = it }
                    )
                }

                // Apply Filters Button
                Button(
                    onClick = { isFilterSheetOpen = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filters (${searchResults.size} Products Found)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Detail & Artisan Story Dialog
    if (selectedProductForDetail != null && !isRfqDialogOpen) {
        val prod = selectedProductForDetail!!
        val isFav = favorites.contains(prod.id)

        AlertDialog(
            onDismissRequest = { selectedProductForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = prod.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.toggleFavorite(prod.id) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) TerracottaPrimary else ArtisanTextMuted
                        )
                    }
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        CraftArtworkDisplay(
                            imageUri = prod.imageUri,
                            category = prod.category,
                            styleFilter = prod.imageStyleFilter,
                            isGiTagged = prod.isGiTagged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    item {
                        Text(text = prod.description, fontSize = 13.sp, color = ArtisanTextSecondary, lineHeight = 18.sp)
                    }

                    // Artisan & Cluster Info Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ArtisanSurfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "👨‍🎨 Master Artisan:", fontSize = 10.sp, color = ArtisanTextMuted)
                                        Text(text = prod.artisanName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                                        Text(text = "📍 ${prod.region}", fontSize = 11.sp, color = ArtisanTextSecondary)
                                    }
                                    Surface(color = MarigoldTertiary.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                        Text("PM Vishwakarma", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary, modifier = Modifier.padding(4.dp))
                                    }
                                }

                                // Contact Artisan Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${prod.artisanPhone}"))
                                            context.startActivity(dialIntent)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91${prod.artisanPhone.replace("+91", "").trim()}?text=Namaste! I am interested in ordering '${prod.title}' on KAUSHVANI."))
                                            context.startActivity(waIntent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("WhatsApp", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Transparent Labor & Materials Details
                    item {
                        Surface(
                            color = ArtisanSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("🌿 Materials: ${prod.materialsUsed}", fontSize = 11.sp, color = ArtisanTextPrimary)
                                Text("⏱️ Handcraft Labor: ${prod.laborHours} hours", fontSize = 11.sp, color = ArtisanTextSecondary)
                                Text("⚖️ Dimensions & Weight: ${prod.dimensions} (${prod.weightKg} kg)", fontSize = 11.sp, color = ArtisanTextSecondary)
                                Text("🛡️ Care Instructions: ${prod.careInstructions}", fontSize = 11.sp, color = ArtisanTextMuted)
                            }
                        }
                    }

                    // Price & MOQ Details
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "B2B Wholesale Price:", fontSize = 11.sp, color = ArtisanTextMuted)
                                Text(text = "₹${prod.wholesalePrice.toInt()} / pc", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                                Text(text = "MOQ: ${prod.minOrderQuantity} pcs", fontSize = 11.sp, color = ArtisanTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Retail MRP:", fontSize = 11.sp, color = ArtisanTextMuted)
                                Text(text = "₹${prod.retailPrice.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                                Text(text = "Stock: ${prod.stockAvailable} available", fontSize = 11.sp, color = ForestSuccess)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { isRfqDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send RFQ Quotation")
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { com.example.ui.utils.ShareUtils.shareProductToWhatsApp(context, prod) }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                    TextButton(onClick = { selectedProductForDetail = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // RFQ Quotation Modal
    if (isRfqDialogOpen && selectedProductForDetail != null) {
        val prod = selectedProductForDetail!!
        var buyerName by remember { mutableStateOf("") }
        var buyerCompany by remember { mutableStateOf("") }
        var buyerType by remember { mutableStateOf("B2B Wholesale / Boutique") }
        var requestedQty by remember { mutableStateOf(prod.minOrderQuantity.toString()) }
        var offeredPrice by remember { mutableStateOf(prod.wholesalePrice.toInt().toString()) }
        var buyerPhone by remember { mutableStateOf("") }
        var buyerEmail by remember { mutableStateOf("") }
        var buyerCity by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("Namaste! We would like to place a bulk order for our store/corporate gifting. Please confirm availability and quotation.") }

        AlertDialog(
            onDismissRequest = { isRfqDialogOpen = false },
            title = {
                Text(
                    text = "💼 Request B2B Quotation (RFQ)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text(text = "Product: ${prod.title}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                    }
                    item {
                        OutlinedTextField(
                            value = buyerName,
                            onValueChange = { buyerName = it },
                            label = { Text("Your Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = buyerCompany,
                            onValueChange = { buyerCompany = it },
                            label = { Text("Company / Store Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = requestedQty,
                                onValueChange = { requestedQty = it },
                                label = { Text("Quantity (pcs)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = offeredPrice,
                                onValueChange = { offeredPrice = it },
                                label = { Text("Offer Rate (₹/pc)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = buyerPhone,
                                onValueChange = { buyerPhone = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = buyerCity,
                                onValueChange = { buyerCity = it },
                                label = { Text("City") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Customization & Delivery Details") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = requestedQty.toIntOrNull() ?: prod.minOrderQuantity
                        val price = offeredPrice.toDoubleOrNull() ?: prod.wholesalePrice
                        viewModel.submitBuyerInquiry(
                            product = prod,
                            buyerName = buyerName.ifBlank { "B2B Buyer" },
                            buyerCompany = buyerCompany.ifBlank { "Handicraft Emporium" },
                            buyerType = buyerType,
                            qty = qty,
                            offeredPrice = price,
                            message = message,
                            phone = buyerPhone.ifBlank { "+91 98765 00000" },
                            email = buyerEmail.ifBlank { "buyer@craftmarket.in" },
                            city = buyerCity.ifBlank { "Delhi / Mumbai" }
                        )
                        isRfqDialogOpen = false
                        selectedProductForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Submit RFQ")
                }
            },
            dismissButton = {
                TextButton(onClick = { isRfqDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Post Bulk Requirement Modal
    if (isPostBulkDialogOpen) {
        var bulkTitle by remember { mutableStateOf("") }
        var bulkCategory by remember { mutableStateOf("Pottery") }
        var bulkQty by remember { mutableStateOf("200") }
        var bulkBudget by remember { mutableStateOf("350") }
        var bulkBuyerName by remember { mutableStateOf("") }
        var bulkCompany by remember { mutableStateOf("") }
        var bulkCity by remember { mutableStateOf("") }
        var bulkPhone by remember { mutableStateOf("") }
        var bulkDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isPostBulkDialogOpen = false },
            title = {
                Text("📢 Post Bulk Sourcing Requirement", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    item {
                        OutlinedTextField(
                            value = bulkTitle,
                            onValueChange = { bulkTitle = it },
                            label = { Text("Title (e.g. 500 Festive Terracotta Diya Sets)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bulkQty,
                                onValueChange = { bulkQty = it },
                                label = { Text("Required Qty (pcs)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = bulkBudget,
                                onValueChange = { bulkBudget = it },
                                label = { Text("Budget (₹/pc)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bulkBuyerName,
                                onValueChange = { bulkBuyerName = it },
                                label = { Text("Buyer Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = bulkCompany,
                                onValueChange = { bulkCompany = it },
                                label = { Text("Company / Org") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bulkCity,
                                onValueChange = { bulkCity = it },
                                label = { Text("Delivery City") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = bulkPhone,
                                onValueChange = { bulkPhone = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = bulkDesc,
                            onValueChange = { bulkDesc = it },
                            label = { Text("Specific Craft Requirements & Custom Packaging") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bulkTitle.isNotBlank()) {
                            viewModel.submitBulkRequirement(
                                title = bulkTitle,
                                category = bulkCategory,
                                quantityRequired = bulkQty.toIntOrNull() ?: 100,
                                targetBudgetPerUnit = bulkBudget.toDoubleOrNull() ?: 300.0,
                                buyerName = bulkBuyerName.ifBlank { "Corporate Sourcing" },
                                buyerCompany = bulkCompany.ifBlank { "B2B Gifting Partner" },
                                buyerCity = bulkCity.ifBlank { "Mumbai" },
                                buyerPhone = bulkPhone.ifBlank { "+91 98000 00000" },
                                buyerEmail = "sourcing@partner.in",
                                description = bulkDesc.ifBlank { "Handcrafted authentic craft batch requirement" }
                            )
                            isPostBulkDialogOpen = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary)
                ) {
                    Text("Post Requirement")
                }
            },
            dismissButton = {
                TextButton(onClick = { isPostBulkDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BuyerProductCardEnhanced(
    product: ProductEntity,
    semanticResult: SemanticSearchResult,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onViewDetails: () -> Unit,
    onSendInquiry: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box {
                CraftArtworkDisplay(
                    imageUri = product.imageUri,
                    category = product.category,
                    styleFilter = product.imageStyleFilter,
                    isGiTagged = product.isGiTagged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                )

                // Favorite Heart Button overlay
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .clickable { onToggleFavorite() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) TerracottaPrimary else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Semantic Match Badge if applicable
                if (semanticResult.isSemanticMatch && semanticResult.matchExplanation.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MarigoldTertiary.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MarigoldTertiary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "✨ ${(semanticResult.score * 100).toInt()}% Match: ${semanticResult.matchExplanation}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary,
                            maxLines = 2
                        )
                        Text(
                            text = "👨‍🎨 ${product.artisanName} • 📍 ${product.region}",
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary
                        )
                        Text(
                            text = "🏷️ ${product.category} • ${product.craftType}",
                            fontSize = 11.sp,
                            color = IndigoSecondary
                        )
                    }
                }

                // Price and MOQ Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArtisanSurfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "B2B Wholesale Rate (MOQ ${product.minOrderQuantity})", fontSize = 10.sp, color = ArtisanTextMuted)
                        Text(text = "₹${product.wholesalePrice.toInt()} / pc", fontSize = 15.sp, fontWeight = FontWeight.Black, color = IndigoSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Retail MRP", fontSize = 10.sp, color = ArtisanTextMuted)
                        Text(text = "₹${product.retailPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TerracottaPrimary)
                    }
                }

                // Action Row: WhatsApp Share, Details, and RFQ Inquiry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // WhatsApp Share Icon Button
                    OutlinedIconButton(
                        onClick = { com.example.ui.utils.ShareUtils.shareProductToWhatsApp(context, product) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = ForestSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onViewDetails,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Details", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onSendInquiry,
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send RFQ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkRequirementsList(
    requirements: List<BulkRequirement>,
    onPostNew: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoSecondary)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📢 B2B Bulk Sourcing Board",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Open RFQ bulk requirements posted by corporate gifting partners, retail emporiums, and export buyers for direct fulfillment by artisan clusters.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                    Button(
                        onClick = onPostNew,
                        colors = ButtonDefaults.buttonColors(containerColor = MarigoldTertiary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ Post Sourcing Requirement", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        items(requirements, key = { it.id }) { req ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = TerracottaContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = req.category,
                                color = TerracottaPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = ForestSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⏱️ ${req.deadlineDate}",
                                color = ForestSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(text = req.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    Text(text = req.description, fontSize = 12.sp, color = ArtisanTextSecondary, lineHeight = 16.sp)

                    Surface(
                        color = ArtisanSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Required Volume:", fontSize = 10.sp, color = ArtisanTextMuted)
                                Text(text = "${req.quantityRequired} pcs", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Budget Target:", fontSize = 10.sp, color = ArtisanTextMuted)
                                Text(text = "₹${req.targetBudgetPerUnit.toInt()} / pc", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Total Order Value:", fontSize = 10.sp, color = ArtisanTextMuted)
                                Text(text = "₹${(req.quantityRequired * req.targetBudgetPerUnit).toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Buyer: ${req.buyerCompany} (${req.buyerName})", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(text = "📍 ${req.buyerCity} • ${req.buyerPhone}", fontSize = 10.sp, color = ArtisanTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
