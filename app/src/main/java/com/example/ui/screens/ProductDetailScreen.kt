package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProductEntity
import com.example.ui.components.CraftArtworkDisplay
import com.example.ui.theme.*
import com.example.ui.utils.ShareUtils
import com.example.ui.viewmodel.HunarSetuViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun ProductDetailScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val sharedTransitionScope = com.example.ui.utils.LocalSharedTransitionScope.current
    val animatedVisibilityScope = com.example.ui.utils.LocalAnimatedVisibilityScope.current

    val context = LocalContext.current
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val selectedProductState by viewModel.selectedProductForDetail.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteProductIds.collectAsStateWithLifecycle()

    // Fallback to first product if none selected
    val product: ProductEntity = selectedProductState ?: allProducts.firstOrNull() ?: return

    val isFavorite = favorites.contains(product.id)

    // Dialog / Modal states for the 4 action buttons
    var isRfqDialogOpen by remember { mutableStateOf(false) }
    var isInquiryDialogOpen by remember { mutableStateOf(false) }
    var isContactArtisanDialogOpen by remember { mutableStateOf(false) }
    var isImageZoomOpen by remember { mutableStateOf(false) }
    var isStoryPlaying by remember { mutableStateOf(false) }

    // Derive Craft Story narrative if not present
    val craftStoryText = remember(product) {
        generateCraftStory(product)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = product.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "${product.craftType} • ${product.region}",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.closeProductDetail() },
                        modifier = Modifier.testTag("product_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TerracottaPrimary
                        )
                    }
                },
                actions = {
                    // Save / Favorite Action in TopBar
                    IconButton(
                        onClick = { viewModel.toggleFavorite(product.id) },
                        modifier = Modifier.testTag("product_detail_save_top_btn")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Saved" else "Save Product",
                            tint = if (isFavorite) TerracottaPrimary else ArtisanTextMuted
                        )
                    }

                    // WhatsApp Share
                    IconButton(
                        onClick = { ShareUtils.shareProductToWhatsApp(context, product) },
                        modifier = Modifier.testTag("product_detail_share_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = ForestSuccess
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArtisanSurface,
                    titleContentColor = ArtisanTextPrimary
                )
            )
        },
        bottomBar = {
            // Prominent Bottom Action Bar containing the 4 primary buttons
            Surface(
                color = ArtisanSurface,
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // [Save] Button
                        OutlinedButton(
                            onClick = { viewModel.toggleFavorite(product.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_save_product"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isFavorite) TerracottaPrimary else ArtisanCardBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isFavorite) TerracottaContainer else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavorite) TerracottaPrimary else ArtisanTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFavorite) "Saved" else "Save",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFavorite) TerracottaPrimary else ArtisanTextPrimary
                            )
                        }

                        // [Contact Artisan] Button
                        Button(
                            onClick = { isContactArtisanDialogOpen = true },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("btn_contact_artisan"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Contact Artisan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // [Send Inquiry] Button
                        OutlinedButton(
                            onClick = { isInquiryDialogOpen = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_send_inquiry"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, IndigoSecondary),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = IndigoSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Send Inquiry",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoSecondary
                            )
                        }

                        // [Request Quote] Button (Primary Hero Action)
                        Button(
                            onClick = { isRfqDialogOpen = true },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("btn_request_quote"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Request Quote",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(ArtisanBackground)
                .padding(innerPadding)
                .testTag("product_detail_scroll_container"),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LARGE PRODUCT IMAGE WITH STUDIO HERO PRESENTATION
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2C241E),
                                    Color(0xFF1E1813),
                                    ArtisanBackground
                                )
                            )
                        )
                ) {
                    // Hero image container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
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
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { isImageZoomOpen = true }
                            .testTag("large_product_image_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        CraftArtworkDisplay(
                            imageUri = product.imageUri,
                            category = product.category,
                            styleFilter = product.imageStyleFilter,
                            isGiTagged = product.isGiTagged,
                            modifier = Modifier.fillMaxSize(),
                            showEnhancementBadge = true
                        )

                        // GI Authenticity Tag Ribbon Overlay
                        if (product.isGiTagged) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp),
                                color = MarigoldTertiary,
                                shape = RoundedCornerShape(6.dp),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = ArtisanTextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "GI AUTHENTIC CERTIFIED",
                                        color = ArtisanTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        // Image Zoom Tap Hint
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = CircleShape
                        ) {
                            IconButton(
                                onClick = { isImageZoomOpen = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Zoom Image",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Studio Style Badge
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MarigoldTertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${product.imageStyleFilter} HD",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 2. PRODUCT NAME & PRIMARY BADGES
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Craft type category pill & origin chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = TerracottaContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = product.category,
                                color = TerracottaPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = IndigoSecondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = product.craftType,
                                color = IndigoSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Stock Availability Pill
                        val inStock = product.stockAvailable > 0
                        Surface(
                            color = if (inStock) ForestContainer else Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (inStock) "🟢 ${product.stockAvailable} in Stock" else "🔴 Sold Out",
                                color = if (inStock) ForestSuccess else Color(0xFFDC2626),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Product Name / Title
                    Text(
                        text = product.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary,
                        lineHeight = 28.sp,
                        modifier = Modifier
                            .testTag("product_detail_title")
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

                    // Regional Hindi Title
                    if (product.regionalTitle.isNotBlank()) {
                        Text(
                            text = product.regionalTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoSecondary
                        )
                    }

                    // Origin & Geographical Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Origin",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Origin: ${product.region} (Cluster Heritage)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ArtisanTextSecondary
                        )
                    }
                }
            }

            // 3. PRICING & FAIR WAGE TRANSPARENCY CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("product_detail_pricing_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Retail Price
                            Column {
                                Text(
                                    text = "खुदरा मूल्य (Retail MRP)",
                                    fontSize = 11.sp,
                                    color = ArtisanTextMuted
                                )
                                Text(
                                    text = "₹${product.retailPrice.toInt()}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TerracottaPrimary
                                )
                                Text(
                                    text = "Inclusive of all craft taxes",
                                    fontSize = 10.sp,
                                    color = ArtisanTextMuted
                                )
                            }

                            // B2B Wholesale Bulk Price
                            if (product.wholesalePrice > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        color = ForestContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "B2B Wholesale",
                                            color = ForestSuccess,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${product.wholesalePrice.toInt()} / pc",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestSuccess
                                    )
                                    Text(
                                        text = "न्यूनतम MOQ: ${product.minOrderQuantity} पीस",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ArtisanTextSecondary
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = ArtisanCardBorder.copy(alpha = 0.6f))

                        // Fair Wage Artisan Standard Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ArtisanBackground, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Balance,
                                    contentDescription = null,
                                    tint = ForestSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "KAUSHVANI Fair Living Wage",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                    Text(
                                        text = "₹${product.hourlyWageRate.toInt()}/घंटे पारिश्रमिक • ${product.laborHours} घंटे हस्तकला श्रम",
                                        fontSize = 10.sp,
                                        color = ArtisanTextSecondary
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    viewModel.openDynamicPricingAssistant(
                                        initialMaterial = product.rawMaterialCost,
                                        initialLabor = product.laborHours,
                                        category = product.category,
                                        specificCraft = product.craftType,
                                        sourceTitle = product.title
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text(
                                    text = "लागत विवरण ›",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 4. MATERIAL, CRAFT TYPE & SPECIFICATIONS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("product_detail_specs_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "शिल्प विनिर्देश व सामग्री (Specifications & Material)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )

                        // Specification Rows
                        SpecificationGridItem(
                            icon = Icons.Default.Eco,
                            label = "सामग्री (Material)",
                            value = product.materialsUsed.ifBlank { "प्राकृतिक शुद्ध सामग्री (Natural Earth Silt & Eco Dyes)" }
                        )

                        SpecificationGridItem(
                            icon = Icons.Default.Category,
                            label = "शिल्प प्रकार (Craft Type)",
                            value = "${product.craftType} (${product.category})"
                        )

                        SpecificationGridItem(
                            icon = Icons.Default.Public,
                            label = "उत्पत्ति क्षेत्र (Origin)",
                            value = "${product.region} (Registered Craft Heritage)"
                        )

                        SpecificationGridItem(
                            icon = Icons.Default.Straighten,
                            label = "आयाम व वजन (Dimensions & Weight)",
                            value = "${product.dimensions.ifBlank { "Standard Artisan Dimensions" }} • ${product.weightKg} kg"
                        )

                        SpecificationGridItem(
                            icon = Icons.Default.Inventory,
                            label = "उपलब्धता (Availability)",
                            value = "${product.stockAvailable} पीस उपलब्ध • थोक उत्पादन: 7-10 दिन"
                        )

                        if (product.careInstructions.isNotBlank()) {
                            SpecificationGridItem(
                                icon = Icons.Default.CleanHands,
                                label = "देखभाल (Care Instructions)",
                                value = product.careInstructions
                            )
                        }
                    }
                }
            }

            // 5. DESCRIPTION
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("product_detail_description_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "शिल्प विवरण (Description)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )

                            IconButton(
                                onClick = {
                                    val textToRead = product.regionalDescription.ifBlank { product.description }
                                    viewModel.speakText(textToRead, "hi")
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Read Aloud",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            color = ArtisanTextSecondary,
                            lineHeight = 20.sp
                        )

                        if (product.regionalDescription.isNotBlank() && product.regionalDescription != product.description) {
                            Surface(
                                color = ArtisanSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "क्षेत्रीय विवरण (Hindi/Regional):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = product.regionalDescription,
                                        fontSize = 13.sp,
                                        color = ArtisanTextPrimary,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. CRAFT STORY (HERITAGE & GENERATIONAL NARRATIVE)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("product_detail_craft_story_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaContainer.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TerracottaPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "शिल्प कथा व धरोहर (Craft Story)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            }

                            // Listen to Craft Story Button (TTS)
                            FilledTonalButton(
                                onClick = {
                                    isStoryPlaying = true
                                    viewModel.speakText(craftStoryText, "hi")
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = TerracottaPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.testTag("btn_listen_craft_story")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hearing,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "कथा सुनें (Audio)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = craftStoryText,
                            fontSize = 13.sp,
                            color = ArtisanTextPrimary,
                            lineHeight = 20.sp
                        )

                        Surface(
                            color = ArtisanSurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HistoryEdu,
                                    contentDescription = null,
                                    tint = MarigoldTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "पीढ़ियों से सुरक्षित परम्परा • 100% प्रामाणिक हस्तनिर्मित",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ArtisanTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 7. ARTISAN INFORMATION & PROFILE CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("product_detail_artisan_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "शिल्पकार परिचय (Artisan Information)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Artisan Avatar
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaContainer)
                                    .border(2.dp, TerracottaPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Artisan Avatar",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = product.artisanName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Artisan",
                                        tint = ForestSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "📍 ${product.artisanLocation}",
                                    fontSize = 12.sp,
                                    color = ArtisanTextSecondary
                                )

                                Surface(
                                    color = MarigoldContainer,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "PM Vishwakarma Certified Master Craftsman",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Quick Direct Contact Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${product.artisanPhone}"))
                                    context.startActivity(dialIntent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = TerracottaPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("कॉल करें (Call)", fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val waIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://wa.me/91${product.artisanPhone.replace("+91", "").trim()}?text=Namaste ${product.artisanName}! I am interested in ordering '${product.title}' (Price: ₹${product.retailPrice.toInt()}) on KAUSHVANI.")
                                    )
                                    context.startActivity(waIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG 1: REQUEST QUOTE (B2B RFQ MODAL)
    // ==========================================
    if (isRfqDialogOpen) {
        var rfqQuantity by remember { mutableStateOf(product.minOrderQuantity.toString()) }
        var rfqRequiredDate by remember { mutableStateOf("15 Sep 2026") }
        var rfqMessage by remember { mutableStateOf("") }
        var rfqBuyerName by remember { mutableStateOf("") }
        var rfqBuyerOrganization by remember { mutableStateOf("") }
        var rfqBuyerContact by remember { mutableStateOf("+91 98000 12345") }
        var rfqBuyerCity by remember { mutableStateOf("New Delhi") }

        val quickDates = listOf("Within 1 week", "Within 2-3 weeks", "Festive Delivery (Diwali)", "15 Sep 2026")

        Dialog(onDismissRequest = { isRfqDialogOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .testTag("dialog_request_quote"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "REQUEST QUOTE (कोटेशन अनुरोध)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "B2B Bulk Inquiry & Direct Artisan Sourcing",
                                    fontSize = 11.sp,
                                    color = ArtisanTextSecondary
                                )
                            }
                            IconButton(onClick = { isRfqDialogOpen = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ArtisanTextMuted)
                            }
                        }
                    }

                    // Field 1: Product
                    item {
                        Surface(
                            color = ArtisanSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = TerracottaContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Product (उत्पाद)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextMuted
                                    )
                                    Text(
                                        text = product.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Wholesale: ₹${product.wholesalePrice.toInt()}/pc • MOQ: ${product.minOrderQuantity} pcs",
                                        fontSize = 10.sp,
                                        color = ForestSuccess,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Field 2: Quantity
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = rfqQuantity,
                                onValueChange = { rfqQuantity = it },
                                label = { Text("Quantity (मात्रा - पीस)", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("rfq_quantity_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                trailingIcon = {
                                    Text("Pcs", fontSize = 11.sp, color = ArtisanTextSecondary, modifier = Modifier.padding(end = 12.dp))
                                }
                            )

                            // Quick Quantity Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(product.minOrderQuantity, 25, 50, 100).forEach { qtyChoice ->
                                    Surface(
                                        modifier = Modifier.clickable { rfqQuantity = qtyChoice.toString() },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (rfqQuantity == qtyChoice.toString()) TerracottaContainer else ArtisanSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                                    ) {
                                        Text(
                                            text = "$qtyChoice pcs",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (rfqQuantity == qtyChoice.toString()) TerracottaPrimary else ArtisanTextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Field 3: Required Date
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = rfqRequiredDate,
                                onValueChange = { rfqRequiredDate = it },
                                label = { Text("Required Date (आवश्यक डिलीवरी तिथि)", fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("rfq_required_date_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = ArtisanTextMuted)
                                }
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(quickDates) { dateOption ->
                                    Surface(
                                        modifier = Modifier.clickable { rfqRequiredDate = dateOption },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (rfqRequiredDate == dateOption) IndigoSecondaryContainer else ArtisanSurfaceVariant
                                    ) {
                                        Text(
                                            text = dateOption,
                                            fontSize = 10.sp,
                                            color = if (rfqRequiredDate == dateOption) IndigoSecondary else ArtisanTextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Field 4: Message
                    item {
                        OutlinedTextField(
                            value = rfqMessage,
                            onValueChange = { rfqMessage = it },
                            label = { Text("Message (सन्देश व विशेष आवश्यकताएं)", fontSize = 11.sp) },
                            placeholder = { Text("e.g. Need festive gift box packaging and export-grade bubble wrapping", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rfq_message_input"),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 2,
                            maxLines = 3
                        )
                    }

                    // Field 5: Buyer Name
                    item {
                        OutlinedTextField(
                            value = rfqBuyerName,
                            onValueChange = { rfqBuyerName = it },
                            label = { Text("Buyer Name (खरीदार का नाम)", fontSize = 11.sp) },
                            placeholder = { Text("e.g. Ananya Singhania", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rfq_buyer_name_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    // Field 6: Organization
                    item {
                        OutlinedTextField(
                            value = rfqBuyerOrganization,
                            onValueChange = { rfqBuyerOrganization = it },
                            label = { Text("Organization (संस्था / कंपनी का नाम)", fontSize = 11.sp) },
                            placeholder = { Text("e.g. FabHeritage Boutiques Ltd.", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rfq_organization_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    // Field 7: Contact
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = rfqBuyerContact,
                                onValueChange = { rfqBuyerContact = it },
                                label = { Text("Contact (फ़ोन / ईमेल)", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("rfq_contact_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = rfqBuyerCity,
                                onValueChange = { rfqBuyerCity = it },
                                label = { Text("City (शहर)", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .testTag("rfq_city_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                    }

                    // Field 8: Submit Button
                    item {
                        Button(
                            onClick = {
                                val qty = rfqQuantity.toIntOrNull() ?: product.minOrderQuantity
                                val name = rfqBuyerName.ifBlank { "Verified B2B Buyer" }
                                val company = rfqBuyerOrganization.ifBlank { "Heritage Retail Group" }
                                val contact = rfqBuyerContact.ifBlank { "+91 98000 12345" }
                                val city = rfqBuyerCity.ifBlank { "New Delhi" }
                                val msg = rfqMessage.ifBlank { "Requesting wholesale quote for $qty pieces with delivery by $rfqRequiredDate." }

                                viewModel.submitBuyerInquiry(
                                    product = product,
                                    buyerName = name,
                                    buyerCompany = company,
                                    buyerType = "B2B_WHOLESALE",
                                    qty = qty,
                                    offeredPrice = product.wholesalePrice,
                                    message = msg,
                                    phone = contact,
                                    email = "buyer@craftmarket.in",
                                    city = city,
                                    requiredDate = rfqRequiredDate
                                )
                                isRfqDialogOpen = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("dialog_rfq_submit_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Submit (कोटेशन अनुरोध सबमिट करें)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG 2: SEND INQUIRY (QUICK QUESTION MODAL)
    // ==========================================
    if (isInquiryDialogOpen) {
        var inquiryMessage by remember { mutableStateOf("") }
        var buyerContactName by remember { mutableStateOf("") }
        var buyerContactPhone by remember { mutableStateOf("") }

        val presetQuestions = listOf(
            "क्या आप इस शिल्प में कस्टम रंग/साइज बना सकते हैं?",
            "1 पीस सैंपल ऑर्डर की सुविधा है क्या?",
            "50 पीस का आर्डर तैयार करने में कितने दिन लगेंगे?",
            "क्या GI Tag का प्रामाणिकता प्रमाणपत्र साथ मिलेगा?"
        )

        Dialog(onDismissRequest = { isInquiryDialogOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .testTag("dialog_send_inquiry"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "शिल्पकार से पूछताछ (Send Inquiry)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Text(text = "सीधे कारीगर ${product.artisanName} को संदेश भेजें", fontSize = 11.sp, color = ArtisanTextSecondary)
                        }
                        IconButton(onClick = { isInquiryDialogOpen = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ArtisanTextMuted)
                        }
                    }

                    Text(text = "त्वरित प्रश्न चुनें (Quick Suggestions):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArtisanTextSecondary)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetQuestions) { q ->
                            SuggestionChip(
                                onClick = { inquiryMessage = q },
                                label = { Text(q, fontSize = 10.sp, maxLines = 1) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inquiryMessage,
                        onValueChange = { inquiryMessage = it },
                        label = { Text("आपकी पूछताछ / संदेश (Message)", fontSize = 11.sp) },
                        placeholder = { Text("शिल्प के बारे में अपना सवाल लिखें...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = buyerContactName,
                            onValueChange = { buyerContactName = it },
                            label = { Text("नाम", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = buyerContactPhone,
                            onValueChange = { buyerContactPhone = it },
                            label = { Text("फ़ोन", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.submitBuyerInquiry(
                                product = product,
                                buyerName = buyerContactName.ifBlank { "Direct Craft Lover" },
                                buyerCompany = "Individual Buyer",
                                buyerType = "RETAIL_INQUIRY",
                                qty = 1,
                                offeredPrice = product.retailPrice,
                                message = inquiryMessage.ifBlank { "I have an inquiry regarding craft specifications and delivery for ${product.title}." },
                                phone = buyerContactPhone.ifBlank { "+91 99888 77665" },
                                email = "inquiry@artisan.in",
                                city = "India"
                            )
                            isInquiryDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dialog_inquiry_submit_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("पूछताछ भेजें (Submit Inquiry)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG 3: CONTACT ARTISAN DIRECT SHEET
    // ==========================================
    if (isContactArtisanDialogOpen) {
        Dialog(onDismissRequest = { isContactArtisanDialogOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .testTag("dialog_contact_artisan"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "शिल्पकार से सीधा संपर्क (Contact Artisan)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        IconButton(onClick = { isContactArtisanDialogOpen = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ArtisanTextMuted)
                        }
                    }

                    // Artisan summary box
                    Surface(
                        color = ArtisanBackground,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TerracottaPrimary)
                            }
                            Column {
                                Text(text = product.artisanName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                                Text(text = "📍 ${product.artisanLocation}", fontSize = 11.sp, color = ArtisanTextSecondary)
                                Text(text = "📞 ${product.artisanPhone}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = IndigoSecondary)
                            }
                        }
                    }

                    // Contact Methods
                    Button(
                        onClick = {
                            val waIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/91${product.artisanPhone.replace("+91", "").trim()}?text=Namaste ${product.artisanName}! I am interested in '${product.title}' (Price: ₹${product.retailPrice.toInt()}) on KAUSHVANI.")
                            )
                            context.startActivity(waIntent)
                            isContactArtisanDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "WhatsApp पर बात करें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${product.artisanPhone}"))
                            context.startActivity(dialIntent)
                            isContactArtisanDialogOpen = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, TerracottaPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "फ़ोन कॉल करें (${product.artisanPhone})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    }
                }
            }
        }
    }

    // ==========================================
    // FULLSCREEN IMAGE ZOOM MODAL
    // ==========================================
    if (isImageZoomOpen) {
        Dialog(onDismissRequest = { isImageZoomOpen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CraftArtworkDisplay(
                    imageUri = product.imageUri,
                    category = product.category,
                    styleFilter = product.imageStyleFilter,
                    isGiTagged = product.isGiTagged,
                    modifier = Modifier.fillMaxSize(),
                    showEnhancementBadge = true
                )

                IconButton(
                    onClick = { isImageZoomOpen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Zoom", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SpecificationGridItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(TerracottaContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TerracottaPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ArtisanTextMuted
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ArtisanTextPrimary
            )
        }
    }
}

/**
 * Generates an authentic, engaging craft heritage backstory for the product
 */
private fun generateCraftStory(product: ProductEntity): String {
    return when (product.category.lowercase()) {
        "pottery" -> """
            यह शिल्प गोरखपुर और पूर्वांचल की प्राचीन टेराकोटा मिट्टी कला की सदियों पुरानी विरासत है। स्थानीय राप्ती नदी की शुद्ध गाद मिट्टी से कुशल कुम्हार इसे चाक पर ढालते हैं। इसे पारंपरिक खुले भट्ठों (आवां) में सूखे पत्तों और गोबर के कंडों की धीमी आंच पर पकाया जाता है, जिससे इसका विशिष्ट प्राकृतिक लाल रंग और प्राकृतिक मजबूती उभरती है। यह सिर्फ एक वस्तु नहीं, बल्कि सदियों की लोक परम्परा और शिल्पकार के समर्पण का जीवंत प्रतीक है।
        """.trimIndent()
        "handloom", "textiles" -> """
            वाराणसी के प्राचीन हथकरघा बुनकरों द्वारा तैयार यह वस्त्र पीढ़ियों से चली आ रही कतान रेशम और शुद्ध जरी बुनाई की उत्कृष्ट मिसाल है। एक-एक ताना और बाना हाथ से गूंथा जाता है, जिसमें प्रत्येक साड़ी या शॉल को तैयार करने में कई हफ़्तों का धैर्यपूर्ण परिश्रम लगता है। यह भारतीय राजसी परिधान और सांस्कृतिक गरिमा का गौरवशाली प्रतिनिधित्व करता है।
        """.trimIndent()
        "metalcraft", "dhokra" -> """
            बस्तर और मध्य भारत की 4000 वर्ष प्राचीन खोई मोम ढलाई (Lost-Wax Casting / Dhokra) तकनीक से निर्मित यह धातु शिल्प हड़प्पा काल से जुड़ा हुआ है। प्रत्येक कृति पूरी तरह से अद्वितीय होती है क्योंकि सांचे को तोड़कर ही अंतिम पीतल की मूर्ति निकाली जाती है। यह जनजातीय प्रकृति पूजा, लोक संगीत और आध्यात्मिक आस्था की अमूल्य धरोहर है।
        """.trimIndent()
        "woodcraft" -> """
            कर्नाटक के चन्नपटना अथवा सहारनपुर की काष्ठकला की यह सुंदर रचना प्राकृतिक शीशम व सागौन की लकड़ी से तराशी गई है। इस पर वनस्पति आधारित प्राकृतिक लाख के रंगों की पॉलिश की जाती है जो बच्चों और पर्यावरण के लिए 100% सुरक्षित है। यह खिलौना और सजावटी शिल्प भारतीय बाल साहित्य और लोक कला का प्रतीक है।
        """.trimIndent()
        "paintings" -> """
            मिथिला (मधुबनी) की पारम्परिक लोक चित्रकला शैली में रचित यह कलाकृति प्राकृतिक रंगों (नील, हल्दी, महुआ, काजल) और बांस की पतली तीलियों से चित्रित की गई है। इसमें प्रकृति, जीवन, सूर्य-चंद्र और मांगलिक अनुष्ठानों का मनोरम अंकन है, जो किसी भी घर में सकारात्मक ऊर्जा और सौभाग्य का संचार करता है।
        """.trimIndent()
        else -> """
            यह हस्तशिल्प भारत के सुदूर ग्रामीण अंचल के मास्टर शिल्पकार द्वारा विशुद्ध हस्तनिर्मित तकनीकों और प्राकृतिक कच्चे माल से तैयार किया गया है। हर एक रचना में स्थानीय लोक संस्कृति, पीढ़ियों की साधना और निष्पक्ष आजीविका का सच्चा संगम है।
        """.trimIndent()
    }
}
