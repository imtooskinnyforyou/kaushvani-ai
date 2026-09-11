package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProductEntity
import com.example.ui.components.BulkDraftQueueSection
import com.example.ui.components.BulkPhotoCaptureModal
import com.example.ui.components.CraftArtworkDisplay
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanCatalogScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val profile by viewModel.artisanProfile.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showQrDialog by remember { mutableStateOf(false) }
    var showBulkCaptureModal by remember { mutableStateOf(false) }

    val draftProducts = remember(products) {
        products.filter { it.status != "PUBLISHED" }
    }

    val filteredProducts = remember(products, selectedFilter, searchQuery) {
        products.filter { product ->
            val matchesFilter = when (selectedFilter) {
                "PUBLISHED" -> product.status == "PUBLISHED"
                "DRAFT" -> product.status != "PUBLISHED"
                "GI_TAG" -> product.isGiTagged
                else -> true
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                product.title.contains(searchQuery, ignoreCase = true) ||
                product.regionalTitle.contains(searchQuery, ignoreCase = true) ||
                product.category.contains(searchQuery, ignoreCase = true) ||
                product.tags.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }
    }

    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Text(
                    text = "डिजिटल कैटलॉग QR (Digital Showroom)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, ArtisanCardBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Showroom QR",
                            modifier = Modifier.size(130.dp),
                            tint = IndigoSecondary
                        )
                    }
                    Text(
                        text = "${profile.name} का प्रमाणित शिल्प शोरूम",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    Text(
                        text = "इस QR कोड को स्कैन करके देश-विदेश के खरीदार आपके सभी ${products.size} शिल्प सीधे देख व थोक आर्डर कर सकते हैं।",
                        fontSize = 11.sp,
                        color = ArtisanTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("शेयर करें (Share Showroom)", fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showQrDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("बंद करें (Close)", fontSize = 12.sp)
                }
            }
        )
    }

    if (showBulkCaptureModal) {
        BulkPhotoCaptureModal(
            initialCategory = "Pottery",
            onDismiss = { showBulkCaptureModal = false },
            onSaveBulkDrafts = { photoUris, category, craftType ->
                viewModel.saveBulkDraftPhotos(photoUris, category, craftType)
                showBulkCaptureModal = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startNewProductWizard() },
                containerColor = TerracottaPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .semantics {
                        role = Role.Button
                        contentDescription = "नया शिल्प जोड़ें (Add New Craft Listing)"
                    }
                    .testTag("catalog_fab_add_product")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "शिल्प जोड़ें (+ Add Craft)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(ArtisanBackground)
                .testTag("artisan_catalog_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header summary bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "मेरा शिल्प कैटलॉग (My Catalog)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Text(
                                text = "${products.size} शिल्प दर्ज • ${products.count { it.status == "PUBLISHED" }} बाज़ार में लाइव",
                                fontSize = 12.sp,
                                color = ArtisanTextSecondary
                            )
                        }

                        FilledTonalButton(
                            onClick = { showQrDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.semantics {
                                role = Role.Button
                                contentDescription = "डिजिटल कैटलॉग QR कोड और शोरूम खोलें (Open Digital Showroom QR)"
                            }
                        ) {
                            Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QR शोरूम", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Bulk Draft Queue Section
            item {
                BulkDraftQueueSection(
                    drafts = draftProducts,
                    onSnapBulkPhotosClick = { showBulkCaptureModal = true },
                    onProcessDraftWithAi = { draft ->
                        viewModel.loadDraftIntoWizard(draft)
                    },
                    onDeleteDraft = { draftId ->
                        viewModel.deleteProduct(draftId)
                    },
                    onBatchProcessAll = {
                        viewModel.batchProcessAllDraftsWithAi()
                    }
                )
            }

            // Search and Filter Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "कैटलॉग में शिल्प, श्रेणी या टैग खोजें"
                        }
                        .testTag("catalog_search_bar"),
                    placeholder = { Text("शिल्प का नाम, श्रेणी या टैग खोजें...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ArtisanTextMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.semantics {
                                    role = Role.Button
                                    contentDescription = "खोज क्वेरी साफ़ करें (Clear Search Query)"
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = ArtisanTextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ArtisanSurface,
                        unfocusedContainerColor = ArtisanSurface
                    )
                )
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("सभी (${products.size})", fontSize = 12.sp) },
                            modifier = Modifier.semantics {
                                role = Role.Tab
                                selected = selectedFilter == "ALL"
                                contentDescription = "सभी उत्पाद फ़िल्टर, कुल ${products.size} उत्पाद"
                                stateDescription = if (selectedFilter == "ALL") "चयनित (Selected)" else "उपलब्ध (Available)"
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaContainer,
                                selectedLabelColor = TerracottaPrimary
                            )
                        )
                    }
                    item {
                        val pubCount = products.count { it.status == "PUBLISHED" }
                        FilterChip(
                            selected = selectedFilter == "PUBLISHED",
                            onClick = { selectedFilter = "PUBLISHED" },
                            label = { Text("लाइव ($pubCount)", fontSize = 12.sp) },
                            modifier = Modifier.semantics {
                                role = Role.Tab
                                selected = selectedFilter == "PUBLISHED"
                                contentDescription = "लाइव उत्पाद फ़िल्टर, कुल $pubCount उत्पाद"
                                stateDescription = if (selectedFilter == "PUBLISHED") "चयनित (Selected)" else "उपलब्ध (Available)"
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestContainer,
                                selectedLabelColor = ForestSuccess
                            )
                        )
                    }
                    item {
                        val draftCount = products.count { it.status != "PUBLISHED" }
                        FilterChip(
                            selected = selectedFilter == "DRAFT",
                            onClick = { selectedFilter = "DRAFT" },
                            label = { Text("ड्राफ्ट ($draftCount)", fontSize = 12.sp) },
                            modifier = Modifier.semantics {
                                role = Role.Tab
                                selected = selectedFilter == "DRAFT"
                                contentDescription = "ड्राफ्ट उत्पाद फ़िल्टर, कुल $draftCount उत्पाद"
                                stateDescription = if (selectedFilter == "DRAFT") "चयनित (Selected)" else "उपलब्ध (Available)"
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoSecondaryContainer,
                                selectedLabelColor = IndigoSecondary
                            )
                        )
                    }
                    item {
                        val giCount = products.count { it.isGiTagged }
                        FilterChip(
                            selected = selectedFilter == "GI_TAG",
                            onClick = { selectedFilter = "GI_TAG" },
                            label = { Text("GI Tag प्रमाणित", fontSize = 12.sp) },
                            modifier = Modifier.semantics {
                                role = Role.Tab
                                selected = selectedFilter == "GI_TAG"
                                contentDescription = "GI Tag प्रमाणित उत्पाद फ़िल्टर, कुल $giCount उत्पाद"
                                stateDescription = if (selectedFilter == "GI_TAG") "चयनित (Selected)" else "उपलब्ध (Available)"
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MarigoldContainer,
                                selectedLabelColor = Color(0xFFD97706)
                            )
                        )
                    }
                }
            }

            // Product Cards List
            if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = ArtisanTextMuted
                            )
                            Text(
                                text = "कोई शिल्प नहीं मिला",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextSecondary
                            )
                            Text(
                                text = "नया उत्पाद जोड़ने के लिए नीचे दिए बटन पर क्लिक करें।",
                                fontSize = 12.sp,
                                color = ArtisanTextMuted
                            )
                            Button(
                                onClick = { viewModel.startNewProductWizard() },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("नया शिल्प जोड़ें (+ Add Craft)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ArtisanProductCatalogItemCard(
                        product = product,
                        onCardClick = { viewModel.openProductDetail(product) },
                        onTogglePublish = { viewModel.toggleProductPublishStatus(product.id) },
                        onPlayAudio = {
                            val text = product.regionalDescription.ifBlank { product.description }
                            viewModel.speakText(text, "hi")
                        },
                        onDelete = {
                            viewModel.deleteProduct(product.id)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun ArtisanProductCatalogItemCard(
    product: ProductEntity,
    onCardClick: () -> Unit = {},
    onTogglePublish: () -> Unit,
    onPlayAudio: () -> Unit,
    onDelete: () -> Unit
) {
    val sharedTransitionScope = com.example.ui.utils.LocalSharedTransitionScope.current
    val animatedVisibilityScope = com.example.ui.utils.LocalAnimatedVisibilityScope.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = "${product.title}, श्रेणी ${product.category}, खुदरा मूल्य ₹${product.retailPrice.toInt()}, B2B थोक मूल्य ₹${product.wholesalePrice.toInt()}, स्थिति ${product.status}${if (product.isGiTagged) ", GI Tag प्रमाणित" else ""}. विवरण देखने के लिए टैप करें"
            }
            .clickable(onClick = onCardClick)
            .testTag("catalog_item_${product.id}")
            .let {
                if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        it.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "card_${product.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else it
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .let {
                            if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                with(sharedTransitionScope) {
                                    it.sharedElement(
                                        state = rememberSharedContentState(key = "image_${product.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    )
                                }
                            } else it
                        }
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    CraftArtworkDisplay(
                        imageUri = product.imageUri,
                        category = product.category,
                        styleFilter = product.imageStyleFilter,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (product.isGiTagged) {
                        Surface(
                            color = MarigoldTertiary,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "GI Tag",
                                color = ArtisanTextPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Details Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .let {
                                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                        with(sharedTransitionScope) {
                                            it.sharedBounds(
                                                sharedContentState = rememberSharedContentState(key = "title_${product.id}"),
                                                animatedVisibilityScope = animatedVisibilityScope
                                            )
                                        }
                                    } else it
                                }
                        )

                        // Status Badge
                        val (statusColor, statusText, statusBg) = when (product.status.uppercase()) {
                            "PUBLISHED" -> Triple(ForestSuccess, "प्रकाशित (Live)", ForestContainer)
                            "UNDER REVIEW" -> Triple(Color(0xFFD97706), "समीक्षाधीन (Review)", MarigoldContainer)
                            "SOLD OUT" -> Triple(Color(0xFFDC2626), "बिक चुका (Sold Out)", Color(0xFFFEE2E2))
                            else -> Triple(Color(0xFF4B5563), "ड्राफ्ट (Draft)", Color(0xFFE5E7EB))
                        }
                        
                        Surface(
                            color = statusBg,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = statusText,
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (product.regionalTitle.isNotBlank()) {
                        Text(
                            text = product.regionalTitle,
                            fontSize = 12.sp,
                            color = IndigoSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "${product.category} • views: ${product.viewCount} • inquiries: ${product.inquiryCount}",
                        fontSize = 11.sp,
                        color = ArtisanTextSecondary
                    )

                    // Price Line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "₹${product.retailPrice.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                        if (product.wholesalePrice > 0) {
                            Text(
                                text = "B2B: ₹${product.wholesalePrice.toInt()}",
                                fontSize = 11.sp,
                                color = ForestSuccess,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = ArtisanCardBorder.copy(alpha = 0.5f))

            // Action Row
            val context = androidx.compose.ui.platform.LocalContext.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share Action
                TextButton(
                    onClick = { com.example.ui.utils.ShareUtils.shareProductToWhatsApp(context, product) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "WhatsApp पर साझा करें: ${product.title} (Share ${product.title} on WhatsApp)"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ForestSuccess
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Share",
                        fontSize = 11.sp,
                        color = ForestSuccess
                    )
                }

                // Edit Action
                TextButton(
                    onClick = { /* Handle edit */ },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "संपादित करें: ${product.title} (Edit ${product.title})"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = IndigoSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit",
                        fontSize = 11.sp,
                        color = IndigoSecondary
                    )
                }
                
                // Duplicate Action
                TextButton(
                    onClick = { /* Handle duplicate */ },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "प्रतिलिपि बनाएं: ${product.title} (Duplicate ${product.title})"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ArtisanTextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Duplicate",
                        fontSize = 11.sp,
                        color = ArtisanTextSecondary
                    )
                }

                // Unpublish/Publish Action
                TextButton(
                    onClick = onTogglePublish,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = if (product.status == "PUBLISHED") 
                            "अप्रकाशित करें: ${product.title} (Unpublish ${product.title})" 
                            else "प्रकाशित करें: ${product.title} (Publish ${product.title})"
                    }
                ) {
                    Icon(
                        imageVector = if (product.status == "PUBLISHED") Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (product.status == "PUBLISHED") Color(0xFFDC2626) else ForestSuccess
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (product.status == "PUBLISHED") "Unpublish" else "Publish",
                        fontSize = 11.sp,
                        color = if (product.status == "PUBLISHED") Color(0xFFDC2626) else ForestSuccess
                    )
                }

                // Delete Action
                TextButton(
                    onClick = onDelete,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "हटाएं: ${product.title} (Delete ${product.title})"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Delete",
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}
