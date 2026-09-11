package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.CraftArtworkDisplay
import com.example.ui.theme.*
import com.example.ui.viewmodel.HunarSetuViewModel

enum class AdminTab {
    OVERVIEW,
    PRODUCTS_MODERATION,
    ARTISANS_DIRECTORY,
    INQUIRIES_MONITOR,
    AI_TELEMETRY,
    CATEGORIES,
    FLAGGED_CONTENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    var selectedAdminTab by remember { mutableStateOf(AdminTab.OVERVIEW) }

    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val inquiries by viewModel.allInquiries.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val moderationFlags by viewModel.moderationFlags.collectAsStateWithLifecycle()
    val artisanRegistry by viewModel.artisanRegistry.collectAsStateWithLifecycle()
    val aiEvents by viewModel.aiEvents.collectAsStateWithLifecycle()

    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ArtisanSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = TerracottaPrimary)
                            Text(
                                text = "कौशवाणी प्रशासन कंसोल (KAUSHVANI Admin Console)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                        }
                        Text(
                            text = "Platform Governance, Moderation & AI Telemetry",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }

                    Surface(
                        color = TerracottaContainer,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "🛡️ SUPER ADMIN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Sub Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedAdminTab.ordinal,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    AdminTab.values().forEach { tab ->
                        val isSelected = selectedAdminTab == tab
                        val title = when (tab) {
                            AdminTab.OVERVIEW -> "📊 अवलोकन (Overview)"
                            AdminTab.PRODUCTS_MODERATION -> "📦 उत्पाद समीक्षा (${products.size})"
                            AdminTab.ARTISANS_DIRECTORY -> "👨‍🎨 कारीगर (${artisanRegistry.size})"
                            AdminTab.INQUIRIES_MONITOR -> "💼 व्यापार ऑर्डर्स (${inquiries.size})"
                            AdminTab.AI_TELEMETRY -> "🤖 AI टेलीमेट्री"
                            AdminTab.CATEGORIES -> "🏷️ श्रेणियां"
                            AdminTab.FLAGGED_CONTENT -> "🚨 फ्लैग रिपोर्ट (${moderationFlags.count { it.status == "PENDING_REVIEW" }})"
                        }
                        Tab(
                            selected = isSelected,
                            onClick = { selectedAdminTab = tab },
                            modifier = Modifier.semantics {
                                role = androidx.compose.ui.semantics.Role.Tab
                                this.selected = isSelected
                                contentDescription = "$title टैब"
                                stateDescription = if (isSelected) "चयनित (Selected)" else "उपलब्ध (Available)"
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TerracottaPrimary else ArtisanTextSecondary
                                )
                            }
                        )
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
            when (selectedAdminTab) {
                AdminTab.OVERVIEW -> AdminOverviewSection(
                    products = products,
                    inquiries = inquiries,
                    artisanRegistry = artisanRegistry,
                    aiEvents = aiEvents,
                    onNavigateToTab = { selectedAdminTab = it }
                )
                AdminTab.PRODUCTS_MODERATION -> AdminProductsModerationSection(
                    products = products,
                    onModerate = { id, action -> viewModel.moderateProduct(id, action) },
                    onDelete = { id -> viewModel.deleteProduct(id) }
                )
                AdminTab.ARTISANS_DIRECTORY -> AdminArtisansDirectorySection(
                    artisans = artisanRegistry,
                    onToggleVerification = { viewModel.toggleArtisanVerification(it) }
                )
                AdminTab.INQUIRIES_MONITOR -> AdminInquiriesMonitorSection(
                    inquiries = inquiries,
                    onUpdateStatus = { id, status -> viewModel.updateInquiryStatus(id, status) }
                )
                AdminTab.AI_TELEMETRY -> AdminAiTelemetrySection(
                    aiEvents = aiEvents
                )
                AdminTab.CATEGORIES -> AdminCategoriesSection(
                    categories = categories,
                    onToggleActive = { viewModel.toggleCategoryActive(it) },
                    onAddNew = { showAddCategoryDialog = true }
                )
                AdminTab.FLAGGED_CONTENT -> AdminFlaggedContentSection(
                    flags = moderationFlags,
                    onModerate = { prodId, action -> viewModel.moderateProduct(prodId, action) }
                )
            }
        }
    }

    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var catHindiName by remember { mutableStateOf("") }
        var catDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("नई शिल्प श्रेणी जोड़ें (Add Craft Category)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = catHindiName,
                        onValueChange = { catHindiName = it },
                        label = { Text("Category Name (Hindi/Regional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = catDesc,
                        onValueChange = { catDesc = it },
                        label = { Text("Description & Materials") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.addCategory(catName, catHindiName, catDesc)
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("जोड़ें (Add Category)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

@Composable
private fun AdminOverviewSection(
    products: List<ProductEntity>,
    inquiries: List<BuyerInquiryEntity>,
    artisanRegistry: List<ArtisanRegistryItem>,
    aiEvents: List<AiProcessingEvent>,
    onNavigateToTab: (AdminTab) -> Unit
) {
    val totalRevenue = inquiries.sumOf { it.requestedQuantity * it.offeredPricePerUnit }
    val activeListings = products.count { it.status == "PUBLISHED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Platform KPI Cards Grid
            Text("📊 प्लेटफ़ॉर्म मुख्य सांख्यिकी (Key Metrics)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    title = "सत्यापित कारीगर",
                    subtitle = "Registered Artisans",
                    value = "${artisanRegistry.size}",
                    color = TerracottaPrimary,
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(AdminTab.ARTISANS_DIRECTORY) }
                )
                AdminStatCard(
                    title = "कुल शिल्प उत्पाद",
                    subtitle = "Active Listings",
                    value = "$activeListings",
                    color = IndigoSecondary,
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(AdminTab.PRODUCTS_MODERATION) }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(
                    title = "व्यापार पूछताछ GMV",
                    subtitle = "Inquiry Pipeline",
                    value = "₹${totalRevenue.toInt()}",
                    color = ForestSuccess,
                    icon = Icons.Default.CurrencyRupee,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(AdminTab.INQUIRIES_MONITOR) }
                )
                AdminStatCard(
                    title = "AI प्रोसेसिंग कार्य",
                    subtitle = "Jobs Completed",
                    value = "${aiEvents.size}",
                    color = MarigoldTertiary,
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(AdminTab.AI_TELEMETRY) }
                )
            }
        }

        // Quick Actions & Moderation Alerts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(TerracottaPrimary, IndigoSecondary)))
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
                        Text(
                            text = "🛡️ ONDC & GeM क्लस्टर एकीकरण स्थिति",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Surface(
                            color = ForestSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "● LIVE CONNECTED",
                                color = ForestSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "सभी कारीगरों की सूची ONDC नेटवर्क, GeM हस्तशिल्प पोर्टल और राष्ट्रीय क्लस्टर डेटाबेस से स्वचालित रूप से समन्वयित है।",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToTab(AdminTab.PRODUCTS_MODERATION) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("उत्पाद समीक्षा", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onNavigateToTab(AdminTab.AI_TELEMETRY) },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("AI मॉनिटर", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Recent Inquiries Summary
        item {
            Text("💼 हालिया B2B थोक मांग व पूछताछ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
        }

        items(inquiries.take(3)) { inq ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = inq.buyerCompany.ifBlank { inq.buyerName }, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "आइटम: ${inq.productTitle} (${inq.requestedQuantity} pcs)", fontSize = 11.sp, color = ArtisanTextSecondary)
                    }
                    Surface(
                        color = if (inq.status == "NEW") TerracottaContainer else ArtisanSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = inq.status,
                            color = if (inq.status == "NEW") TerracottaPrimary else ArtisanTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    subtitle: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .semantics {
                role = androidx.compose.ui.semantics.Role.Button
                contentDescription = "$title: $value ($subtitle). विवरण देखने के लिए क्लिक करें"
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                }
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            }
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
            Text(text = subtitle, fontSize = 10.sp, color = ArtisanTextMuted)
        }
    }
}

@Composable
private fun AdminProductsModerationSection(
    products: List<ProductEntity>,
    onModerate: (Long, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("📦 उत्पाद समीक्षा व मॉडरेटर सूची", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    Text("Total Products: ${products.size}", fontSize = 11.sp, color = ArtisanTextSecondary)
                }
            }
        }

        items(products, key = { it.id }) { prod ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (prod.status == "FLAGGED") TerracottaPrimary else ArtisanCardBorder
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CraftArtworkDisplay(
                            imageUri = prod.imageUri,
                            category = prod.category,
                            styleFilter = prod.imageStyleFilter,
                            isGiTagged = prod.isGiTagged,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )

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
                                    text = prod.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = when (prod.status) {
                                        "PUBLISHED" -> ForestSuccess.copy(alpha = 0.15f)
                                        "FLAGGED" -> TerracottaContainer
                                        else -> ArtisanSurfaceVariant
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = prod.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (prod.status) {
                                            "PUBLISHED" -> ForestSuccess
                                            "FLAGGED" -> TerracottaPrimary
                                            else -> ArtisanTextSecondary
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "कारीगर: ${prod.artisanName} • ${prod.region}",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "खुदरा: ₹${prod.retailPrice.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestSuccess
                                )
                                Text(
                                    text = "थोक: ₹${prod.wholesalePrice.toInt()} (MOQ ${prod.minOrderQuantity})",
                                    fontSize = 11.sp,
                                    color = IndigoSecondary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = ArtisanCardBorder)

                    // Moderation Actions Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (prod.status != "PUBLISHED") {
                            Button(
                                onClick = { onModerate(prod.id, "APPROVE") },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("स्वीकारें (Approve)", fontSize = 11.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onModerate(prod.id, "REJECT_TAKEDOWN") },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("अप्रकाशित करें", fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = { onModerate(prod.id, "FEATURE") },
                            colors = ButtonDefaults.buttonColors(containerColor = MarigoldTertiary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("फ़ीचर्ड", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { onDelete(prod.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminArtisansDirectorySection(
    artisans: List<ArtisanRegistryItem>,
    onToggleVerification: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("👨‍🎨 कारीगर डायरेक्टरी व प्रमाणीकरण (Artisan Directory)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
            Text("Master registry of verified Indian craftspeople and self-help clusters", fontSize = 11.sp, color = ArtisanTextSecondary)
        }

        items(artisans, key = { it.id }) { artisan ->
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = TerracottaContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = artisan.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = artisan.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    if (artisan.isVerified) {
                                        Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified", tint = ForestSuccess, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(text = "${artisan.craftSpecialty} • ${artisan.villageOrCluster}, ${artisan.state}", fontSize = 11.sp, color = ArtisanTextSecondary)
                            }
                        }

                        Switch(
                            checked = artisan.isVerified,
                            onCheckedChange = { onToggleVerification(artisan.id) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ForestSuccess,
                                checkedTrackColor = ForestSuccess.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Surface(
                        color = ArtisanSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "PM Vishwakarma ID:", fontSize = 10.sp, color = ArtisanTextMuted)
                                Text(text = artisan.vishwakarmaId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "फ़ोन / संपर्क:", fontSize = 10.sp, color = ArtisanTextMuted)
                                Text(text = artisan.phone, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminInquiriesMonitorSection(
    inquiries: List<BuyerInquiryEntity>,
    onUpdateStatus: (Long, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("💼 B2B थोक व्यापार व कोटेशन मॉनिटर", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
            Text("All corporate and bulk wholesale inquiries across the marketplace", fontSize = 11.sp, color = ArtisanTextSecondary)
        }

        if (inquiries.isEmpty()) {
            item {
                Text("कोई सक्रिय पूछताछ नहीं है।", fontSize = 13.sp, color = ArtisanTextMuted)
            }
        }

        items(inquiries, key = { it.id }) { inq ->
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
                        Column {
                            Text(text = inq.buyerCompany.ifBlank { inq.buyerName }, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "उत्पाद: ${inq.productTitle}", fontSize = 12.sp, color = IndigoSecondary)
                        }
                        Surface(
                            color = when (inq.status) {
                                "NEW" -> TerracottaContainer
                                "ACCEPTED" -> ForestSuccess.copy(alpha = 0.15f)
                                else -> ArtisanSurfaceVariant
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = inq.status,
                                color = when (inq.status) {
                                    "NEW" -> TerracottaPrimary
                                    "ACCEPTED" -> ForestSuccess
                                    else -> ArtisanTextPrimary
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "मांग: ${inq.requestedQuantity} pcs", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(text = "प्रस्तावित मूल्य: ₹${inq.offeredPricePerUnit.toInt()} / pc", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                        Text(text = "कुल: ₹${(inq.requestedQuantity * inq.offeredPricePerUnit).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                    }

                    Text(text = "संदेश: \"${inq.message}\"", fontSize = 11.sp, color = ArtisanTextSecondary)

                    // Status Dropdown / Action row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("NEW", "QUOTE_SENT", "ACCEPTED", "SHIPPED", "COMPLETED").forEach { st ->
                            val isCurrent = inq.status == st
                            Surface(
                                modifier = Modifier.clickable { onUpdateStatus(inq.id, st) },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrent) IndigoSecondary else ArtisanSurfaceVariant
                            ) {
                                Text(
                                    text = st,
                                    fontSize = 9.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color.White else ArtisanTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminAiTelemetrySection(
    aiEvents: List<AiProcessingEvent>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("🤖 AI इंजन मॉनिटर व टेलीमेट्री (AI Telemetry)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
            Text("Live status of Gemini Vision Studio, Multilingual Voice & Fair Living Wage Engine", fontSize = 11.sp, color = ArtisanTextSecondary)
        }

        // Live Health Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = ForestSuccess.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Engine Health", fontSize = 10.sp, color = ArtisanTextMuted)
                        Text(text = "99.8% Online", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                        Text(text = "Gemini 2.5 Pro & Flash", fontSize = 10.sp, color = ArtisanTextSecondary)
                    }
                }
                Surface(
                    color = IndigoSecondary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Avg AI Latency", fontSize = 10.sp, color = ArtisanTextMuted)
                        Text(text = "820 ms", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                        Text(text = "Edge Cache Active", fontSize = 10.sp, color = ArtisanTextSecondary)
                    }
                }
            }
        }

        item {
            Text("📋 हालिया AI प्रोसेसिंग इवेंट लॉग्स (Live Event Stream)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
        }

        items(aiEvents, key = { it.id }) { event ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = ForestSuccess.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = ForestSuccess, modifier = Modifier.size(16.dp))
                            }
                        }

                        Column {
                            Text(text = event.taskType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                            Text(text = event.details.ifBlank { event.languageOrFilter }, fontSize = 11.sp, color = ArtisanTextSecondary)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "${event.durationMs} ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                        Text(text = event.status, fontSize = 10.sp, color = ForestSuccess, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCategoriesSection(
    categories: List<CraftCategory>,
    onToggleActive: (String) -> Unit,
    onAddNew: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🏷️ शिल्प श्रेणियां प्रबंधन (Category Manager)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    Text("Manage marketplace craft taxonomies", fontSize = 11.sp, color = ArtisanTextSecondary)
                }

                Button(
                    onClick = onAddNew,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("नई श्रेणी जोड़ें", fontSize = 11.sp)
                }
            }
        }

        items(categories, key = { it.id }) { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "${cat.name} (${cat.hindiName})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                        Text(text = cat.description, fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(text = "उत्पाद संख्या: ${cat.itemCount} items", fontSize = 10.sp, color = TerracottaPrimary, fontWeight = FontWeight.Medium)
                    }

                    Switch(
                        checked = cat.isActive,
                        onCheckedChange = { onToggleActive(cat.id) },
                        colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminFlaggedContentSection(
    flags: List<ModerationFlag>,
    onModerate: (Long, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("🚨 फ्लैग की गई सामग्री व शिकायतें (Flagged Content)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
            Text("Community reports & Automated GI Tag / Quality audits", fontSize = 11.sp, color = ArtisanTextSecondary)
        }

        if (flags.isEmpty()) {
            item {
                Text("कोई फ्लैग की गई सामग्री लंबित नहीं है।", fontSize = 13.sp, color = ForestSuccess)
            }
        }

        items(flags, key = { it.id }) { flag ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (flag.status == "PENDING_REVIEW") TerracottaPrimary else ForestSuccess
                    )
                )
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
                        Text(text = flag.productTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                        Surface(
                            color = if (flag.status == "PENDING_REVIEW") TerracottaContainer else ForestSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = flag.status,
                                color = if (flag.status == "PENDING_REVIEW") TerracottaPrimary else ForestSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(text = "कारीगर: ${flag.artisanName}", fontSize = 12.sp, color = ArtisanTextSecondary)
                    Text(text = "फ्लैग का कारण: ${flag.reason}", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Medium)
                    Text(text = "फ्लैग स्रोत: ${flag.flaggedBy}", fontSize = 10.sp, color = ArtisanTextMuted)

                    if (flag.status == "PENDING_REVIEW") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onModerate(flag.productId, "APPROVE") },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("मान्य करें (Approve)", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onModerate(flag.productId, "REJECT_TAKEDOWN") },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("हटाएं (Take Down)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
