package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.components.CraftArtworkDisplay
import com.example.ui.components.CraftImageFilter
import com.example.ui.components.CanvasToneCurveVisualizer
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanStudioScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedProduct by remember(products) { mutableStateOf(products.firstOrNull()) }
    var selectedFilter by remember { mutableStateOf(CraftImageFilter.ARTISAN_NATURAL) }

    // Auto-adjusted values dynamically tuned per filter, with artisan fine-tuning capability
    var brightnessAdjustment by remember { mutableFloatStateOf(CraftImageFilter.ARTISAN_NATURAL.autoBrightness) }
    var contrastAdjustment by remember { mutableFloatStateOf(CraftImageFilter.ARTISAN_NATURAL.autoContrast) }
    var saturationAdjustment by remember { mutableFloatStateOf(CraftImageFilter.ARTISAN_NATURAL.autoSaturation) }

    var showBeforeAfterSplit by remember { mutableStateOf(false) }
    var isMacroTextureZoomActive by remember { mutableStateOf(false) }
    var includeGiWatermark by remember { mutableStateOf(true) }
    var detailEnhanceActive by remember { mutableStateOf(true) }
    var isProcessedSaved by remember { mutableStateOf(false) }

    // Sync filter when product is changed
    LaunchedEffect(selectedProduct) {
        selectedProduct?.let { prod ->
            val initialFilter = if (prod.imageStyleFilter.isNotBlank()) {
                CraftImageFilter.fromIdOrDefault(prod.imageStyleFilter)
            } else {
                CraftImageFilter.findMatchingForCategory(prod.category)
            }
            selectedFilter = initialFilter
            brightnessAdjustment = initialFilter.autoBrightness
            contrastAdjustment = initialFilter.autoContrast
            saturationAdjustment = initialFilter.autoSaturation
            isProcessedSaved = false
        }
    }

    // Function to apply 1-tap filter with automatic brightness/contrast tuning
    fun applyFilterWithAutoAdjust(filter: CraftImageFilter) {
        selectedFilter = filter
        brightnessAdjustment = filter.autoBrightness
        contrastAdjustment = filter.autoContrast
        saturationAdjustment = filter.autoSaturation
        isProcessedSaved = false
        viewModel.speakText(filter.narrationText, "hi")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "AI Image Studio",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Surface(
                                color = TerracottaPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "1-टैप फ़िल्टर",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "हथकरघा व शिल्प टेक्सचर के लिए ब्राइटनेस-कंट्रास्ट ट्यून्ड",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_HOME) },
                        modifier = Modifier.testTag("studio_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = TerracottaPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.speakText(
                                "AI इमेज स्टूडियो में आपके हथकरघा और हस्तशिल्प के लिए 6 विशेष फ़िल्टर हैं। यह फ़िल्टर धागे की चमक, मिट्टी की बनावट और जरी के रंगों को खुद ब खुद साफ़ करते हैं।",
                                "hi"
                            )
                        },
                        modifier = Modifier.testTag("studio_voice_help_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Audio Guidance",
                            tint = TerracottaPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArtisanSurface,
                    titleContentColor = ArtisanTextPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(ArtisanBackground)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. Camera & Imagen Studio Feature Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerracottaContainer),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppNavTab.PRODUCT_CAPTURE) }
                        .testTag("banner_camera_imagen_studio")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(TerracottaPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Camera & Imagen 3 Studio",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                Surface(
                                    color = MarigoldContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "NEW AI",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MarigoldTertiary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "शिल्प की लाइव फ़ोटो खींचें और AI से मिनिमलिस्ट स्टूडियो बैकग्राउंड तैयार करें",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open Studio",
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 1. Select Craft Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "शिल्प चुनें (Select Craft to Enhance):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        TextButton(
                            onClick = { viewModel.startNewProductWizard() },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.semantics {
                                role = Role.Button
                                contentDescription = "नई फ़ोटो लें और नया उत्पाद जोड़ें (Take new photo and add product)"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TerracottaPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ नई फोटो लें",
                                fontSize = 12.sp,
                                color = TerracottaPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(products) { prod ->
                            val isSelected = prod.id == selectedProduct?.id
                            Card(
                                modifier = Modifier
                                    .width(115.dp)
                                    .semantics {
                                        role = Role.Tab
                                        selected = isSelected
                                        contentDescription = "${prod.title}, कीमत ₹${prod.retailPrice.toInt()}"
                                        stateDescription = if (isSelected) "चयनित शिल्प (Selected Craft)" else "उपलब्ध शिल्प (Available Craft)"
                                    }
                                    .clickable {
                                        selectedProduct = prod
                                        isProcessedSaved = false
                                    }
                                    .testTag("studio_product_select_${prod.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) TerracottaContainer else ArtisanSurface
                                ),
                                border = if (isSelected) {
                                    androidx.compose.foundation.BorderStroke(2.dp, TerracottaPrimary)
                                } else {
                                    androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        CraftArtworkDisplay(
                                            imageUri = prod.imageUri,
                                            category = prod.category,
                                            styleFilter = selectedFilter.title,
                                            isGiTagged = prod.isGiTagged,
                                            modifier = Modifier.fillMaxSize(),
                                            showEnhancementBadge = false,
                                            brightnessOverride = if (isSelected) brightnessAdjustment else null,
                                            contrastOverride = if (isSelected) contrastAdjustment else null,
                                            saturationOverride = if (isSelected) saturationAdjustment else null
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = prod.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = ArtisanTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "₹${prod.retailPrice.toInt()}",
                                        fontSize = 10.sp,
                                        color = TerracottaPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Interactive Studio Stage Canvas
            selectedProduct?.let { product ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("studio_viewport_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Viewport Header Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = TerracottaPrimary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${selectedFilter.hindiTitle} (${selectedFilter.title})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaPrimary
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // 2x Texture Zoom Toggle
                                    FilledTonalButton(
                                        onClick = { isMacroTextureZoomActive = !isMacroTextureZoomActive },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .semantics {
                                                role = Role.Button
                                                contentDescription = "2x सूक्ष्म बुनाई व टेक्सचर ज़ूम टॉगल"
                                                stateDescription = if (isMacroTextureZoomActive) "2x ज़ूम चालू (Zoom Active)" else "सामान्य दृश्य (Normal View)"
                                            }
                                            .testTag("studio_toggle_macro_zoom")
                                    ) {
                                        Icon(
                                            imageVector = if (isMacroTextureZoomActive) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isMacroTextureZoomActive) "सामान्य" else "2x टेक्सचर",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Before / After Toggle Button
                                    FilledTonalButton(
                                        onClick = { showBeforeAfterSplit = !showBeforeAfterSplit },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .semantics {
                                                role = Role.Button
                                                contentDescription = "मूल फोटो और AI फ़िल्टर तुलना टॉगल"
                                                stateDescription = if (showBeforeAfterSplit) "तुलना दृश्य (Comparison View)" else "एन्हांस्ड दृश्य (Enhanced View)"
                                            }
                                            .testTag("studio_toggle_compare")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Compare,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (showBeforeAfterSplit) "Enhanced" else "तुलना करें",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Studio Viewport Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isMacroTextureZoomActive) 280.dp else 240.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = selectedFilter.bgColors
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (showBeforeAfterSplit) {
                                    // Side-by-side Before/After
                                    Row(modifier = Modifier.fillMaxSize()) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(Color(0xFFE5E7EB)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CraftArtworkDisplay(
                                                imageUri = product.imageUri,
                                                category = product.category,
                                                styleFilter = "Artisan Natural",
                                                isGiTagged = false,
                                                modifier = Modifier.fillMaxSize(),
                                                showEnhancementBadge = false,
                                                brightnessOverride = 1.0f,
                                                contrastOverride = 1.0f,
                                                saturationOverride = 1.0f
                                            )
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(8.dp),
                                                color = Color.Black.copy(alpha = 0.65f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "मूल फ़ोटो (Raw)",
                                                    fontSize = 9.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .fillMaxHeight()
                                                .background(TerracottaPrimary)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(
                                                    brush = Brush.radialGradient(
                                                        colors = selectedFilter.bgColors
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CraftArtworkDisplay(
                                                imageUri = product.imageUri,
                                                category = product.category,
                                                styleFilter = selectedFilter.title,
                                                isGiTagged = product.isGiTagged,
                                                modifier = Modifier.fillMaxSize(),
                                                showEnhancementBadge = false,
                                                brightnessOverride = brightnessAdjustment,
                                                contrastOverride = contrastAdjustment,
                                                saturationOverride = saturationAdjustment
                                            )
                                            Surface(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp),
                                                color = ForestSuccess,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "✨ AI Filtered",
                                                    fontSize = 9.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Full Enhanced View with Lighting Glow
                                    CraftArtworkDisplay(
                                        imageUri = product.imageUri,
                                        category = product.category,
                                        styleFilter = selectedFilter.title,
                                        isGiTagged = product.isGiTagged,
                                        modifier = Modifier.fillMaxSize(),
                                        showEnhancementBadge = true,
                                        brightnessOverride = brightnessAdjustment,
                                        contrastOverride = contrastAdjustment,
                                        saturationOverride = saturationAdjustment
                                    )
                                }

                                // 2x Texture Inspection Zoom Watermark Tag
                                if (isMacroTextureZoomActive) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(10.dp),
                                        color = IndigoSecondary.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ZoomIn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "2X WEAVE & GRAIN DETAIL",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                // GI Authentic Watermark Overlay
                                if (includeGiWatermark) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp),
                                        color = Color.Black.copy(alpha = 0.75f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VerifiedUser,
                                                contentDescription = null,
                                                tint = MarigoldTertiary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "KAUSHVANI VERIFIED AUTHENTIC",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Active Texture Tuning Intelligence Info Banner
                            Surface(
                                color = TerracottaContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Texture,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedFilter.textureDetailHindi,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArtisanTextPrimary
                                        )
                                        Text(
                                            text = "${selectedFilter.textureDetailTag} • ${selectedFilter.craftFocus}",
                                            fontSize = 10.sp,
                                            color = ArtisanTextSecondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.speakText(selectedFilter.narrationText, "hi") },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Speak",
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Watermark & Sharpness Toggles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Checkbox(
                                        checked = includeGiWatermark,
                                        onCheckedChange = { includeGiWatermark = it },
                                        colors = CheckboxDefaults.colors(checkedColor = TerracottaPrimary)
                                    )
                                    Text(
                                        text = "GI व प्रामाणिकता वॉटरमार्क",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ArtisanTextPrimary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Checkbox(
                                        checked = detailEnhanceActive,
                                        onCheckedChange = { detailEnhanceActive = it },
                                        colors = CheckboxDefaults.colors(checkedColor = ForestSuccess)
                                    )
                                    Text(
                                        text = "सूक्ष्म नक्काशी/बुनाई शार्प करें",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ArtisanTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real-time Canvas Tone Curve Visualizer
            item {
                CanvasToneCurveVisualizer(
                    brightness = brightnessAdjustment,
                    contrast = contrastAdjustment,
                    saturation = saturationAdjustment,
                    filterName = selectedFilter.title
                )
            }

            // 3. AI One-Tap Image Filter Selection Grid & Carousel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🎨 AI इमेज पोस्ट-प्रोसेसिंग फ़िल्टर:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Text(
                                text = "Canvas आधारित 3 मुख्य AI फ़िल्टर: Handloom Vivid, Natural Studio, Minimalist Neutral",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary
                            )
                        }
                    }

                    // Quick Selection Chips for the 3 Primary Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CraftImageFilter.PRIMARY_THREE_FILTERS.forEach { filter ->
                            val isSelected = filter == selectedFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = { applyFilterWithAutoAdjust(filter) },
                                label = {
                                    Text(
                                        text = "${filter.emoji} ${filter.title}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaContainer,
                                    selectedLabelColor = TerracottaPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CraftImageFilter.values().forEach { filter ->
                            val isSelected = filter == selectedFilter
                            val isRecommended = selectedProduct?.let {
                                CraftImageFilter.findMatchingForCategory(it.category) == filter
                            } ?: false

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        role = Role.RadioButton
                                        selected = isSelected
                                        contentDescription = "${filter.hindiTitle} (${filter.title}) AI फ़िल्टर${if (isRecommended) ", AI द्वारा सुझाया गया" else ""}"
                                        stateDescription = if (isSelected) "लागू फ़िल्टर (Applied)" else "उपलब्ध (Available)"
                                    }
                                    .clickable { applyFilterWithAutoAdjust(filter) }
                                    .testTag("studio_filter_${filter.id.lowercase()}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) TerracottaContainer else ArtisanSurface
                                ),
                                border = if (isSelected) {
                                    androidx.compose.foundation.BorderStroke(2.dp, TerracottaPrimary)
                                } else if (isRecommended) {
                                    androidx.compose.foundation.BorderStroke(1.5.dp, ForestSuccess.copy(alpha = 0.6f))
                                } else {
                                    androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        brush = Brush.radialGradient(filter.bgColors)
                                                    )
                                                    .border(1.dp, ArtisanCardBorder, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = TerracottaPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = filter.hindiTitle,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = ArtisanTextPrimary
                                                    )
                                                    Text(
                                                        text = "(${filter.title})",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = ArtisanTextSecondary
                                                    )
                                                    if (isRecommended) {
                                                        Surface(
                                                            color = ForestSuccess.copy(alpha = 0.15f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "✨ AI सुझाव",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = ForestSuccess,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = filter.tagline,
                                                    fontSize = 11.sp,
                                                    color = ArtisanTextSecondary
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = TerracottaPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    // Filter Texture & Auto-Tuning Badges
                                    val bPct = ((filter.autoBrightness - 1.0f) * 100).toInt()
                                    val cPct = ((filter.autoContrast - 1.0f) * 100).toInt()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = ArtisanSurfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "☀️ ब्राइटनेस: " + (if (bPct >= 0) "+$bPct%" else "$bPct%"),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ArtisanTextPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }

                                        Surface(
                                            color = ArtisanSurfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "🌓 कंट्रास्ट: " + (if (cPct >= 0) "+$cPct%" else "$cPct%"),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ArtisanTextPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }

                                        Surface(
                                            color = ArtisanSurfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = filter.hindiCraftFocus,
                                                fontSize = 10.sp,
                                                color = ArtisanTextSecondary,
                                                maxLines = 1,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Fine-Tuning Sliders (Brightness & Contrast) with AI Optimal Reset
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
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
                            Column {
                                Text(
                                    text = "ब्राइटनेस व कंट्रास्ट फाइन-ट्यूनिंग:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "AI द्वारा ऑटो-ट्यून्ड, आवश्यकतानुसार बदलें",
                                    fontSize = 11.sp,
                                    color = ArtisanTextSecondary
                                )
                            }

                            TextButton(
                                onClick = {
                                    brightnessAdjustment = selectedFilter.autoBrightness
                                    contrastAdjustment = selectedFilter.autoContrast
                                    saturationAdjustment = selectedFilter.autoSaturation
                                    isProcessedSaved = false
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.semantics {
                                    role = Role.Button
                                    contentDescription = "AI द्वारा सुझाई गई इष्टतम ब्राइटनेस और कंट्रास्ट रीसेट करें"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI रीसेट",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            }
                        }

                        // Brightness Slider
                        val currentBPct = ((brightnessAdjustment - 1.0f) * 100).toInt()
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "☀️ ब्राइटनेस (प्रकाश तीव्रता):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = if (currentBPct >= 0) "+$currentBPct%" else "$currentBPct%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            }

                            Slider(
                                value = brightnessAdjustment,
                                onValueChange = {
                                    brightnessAdjustment = it
                                    isProcessedSaved = false
                                },
                                valueRange = 0.8f..1.4f,
                                colors = SliderDefaults.colors(
                                    thumbColor = TerracottaPrimary,
                                    activeTrackColor = TerracottaPrimary
                                ),
                                modifier = Modifier
                                    .semantics {
                                        contentDescription = "ब्राइटनेस प्रकाश तीव्रता स्लाइडर"
                                        stateDescription = if (currentBPct >= 0) "+$currentBPct प्रतिशत" else "$currentBPct प्रतिशत"
                                    }
                                    .testTag("studio_brightness_slider")
                            )
                        }

                        // Contrast Slider
                        val currentCPct = ((contrastAdjustment - 1.0f) * 100).toInt()
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🌓 कंट्रास्ट (धागा व नक्काशी उभार):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = if (currentCPct >= 0) "+$currentCPct%" else "$currentCPct%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            }

                            Slider(
                                value = contrastAdjustment,
                                onValueChange = {
                                    contrastAdjustment = it
                                    isProcessedSaved = false
                                },
                                valueRange = 0.8f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = TerracottaPrimary,
                                    activeTrackColor = TerracottaPrimary
                                ),
                                modifier = Modifier
                                    .semantics {
                                        contentDescription = "कंट्रास्ट धागा व नक्काशी उभार स्लाइडर"
                                        stateDescription = if (currentCPct >= 0) "+$currentCPct प्रतिशत" else "$currentCPct प्रतिशत"
                                    }
                                    .testTag("studio_contrast_slider")
                            )
                        }
                    }
                }
            }

            // 5. Actions: Apply to Catalog & Share
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AnimatedVisibility(visible = isProcessedSaved) {
                        Surface(
                            color = ForestSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ForestSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "✨ कैटलॉग में HD स्टूडियो फोटो ('${selectedFilter.title}') सफलतापूर्वक अपडेट हो गई!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestSuccess
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedProduct?.let { prod ->
                                    viewModel.updateProductImageFilter(prod.id, selectedFilter.title)
                                    isProcessedSaved = true
                                    viewModel.speakText("स्टूडियो फोटो कैटलॉग में सुरक्षित कर ली गई है।", "hi")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "कैटलॉग में स्टूडियो फ़ोटो सहेजें (Save Studio Photo to Catalog)"
                                }
                                .testTag("studio_apply_catalog_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "कैटलॉग में सहेजें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedProduct?.let { prod ->
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Namaste! Check out this studio-quality handcrafted piece on KAUSHVANI: ${prod.title} (₹${prod.retailPrice.toInt()}). Enhanced with ${selectedFilter.title} AI Filter for true handmade texture."
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Studio HD Photo"))
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "WhatsApp पर HD स्टूडियो फ़ोटो साझा करें (Share on WhatsApp)"
                                }
                                .testTag("studio_share_whatsapp_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestSuccess)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = ForestSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "शेयर करें (WhatsApp)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestSuccess
                            )
                        }
                    }
                }
            }
        }
    }
}
