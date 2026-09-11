package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.*

@Composable
fun CraftArtworkDisplay(
    imageUri: String,
    category: String,
    styleFilter: String = "Artisan Natural",
    isGiTagged: Boolean = false,
    modifier: Modifier = Modifier,
    showEnhancementBadge: Boolean = true,
    brightnessOverride: Float? = null,
    contrastOverride: Float? = null,
    saturationOverride: Float? = null
) {
    val context = LocalContext.current
    val filter = remember(styleFilter) { CraftImageFilter.fromIdOrDefault(styleFilter) }

    val effectiveBrightness = brightnessOverride ?: filter.autoBrightness
    val effectiveContrast = contrastOverride ?: filter.autoContrast
    val effectiveSaturation = saturationOverride ?: filter.autoSaturation

    val colorMatrix = remember(effectiveBrightness, effectiveContrast, effectiveSaturation) {
        calculateCraftColorMatrix(
            brightness = effectiveBrightness,
            contrast = effectiveContrast,
            saturation = effectiveSaturation
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        filter.bgColors.firstOrNull() ?: ArtisanSurfaceVariant,
                        filter.bgColors.getOrNull(1) ?: Color(0xFFEDE4D7),
                        filter.bgColors.lastOrNull() ?: Color(0xFFDCCFBE)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Subtle dynamic scale/elevation for enhanced textures
                    alpha = 1.0f
                }
        ) {
            if (imageUri.isNotBlank() && (imageUri.startsWith("content://") || imageUri.startsWith("file://") || imageUri.startsWith("http"))) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Craft Photo",
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(colorMatrix),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Render rich high-fidelity craft vector visual tailored to craft category
                CraftCategoryVectorCanvas(category = category, modifier = Modifier.fillMaxSize())
            }

            // AI Studio enhancement overlay tint and Canvas filter effects
            val overlayColor = filter.overlayTint
            if (overlayColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(overlayColor)
                )
            }

            // Dedicated Canvas Filter enhancements based on the selected AI filter
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                when (filter) {
                    CraftImageFilter.HANDLOOM_VIVID, CraftImageFilter.VIBRANT_LOOM -> {
                        // Handloom Vivid: Golden Zari warp-weft sheen and micro-highlights across canvas
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0x22FFD54F),
                                    Color(0x00FFD54F),
                                    Color(0x18FF8F00),
                                    Color(0x00FFD54F)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(w, h)
                            )
                        )
                        // Diagonal warp-weft light reflection lines
                        for (i in 0..4) {
                            val startX = w * (0.15f + i * 0.20f)
                            drawLine(
                                color = Color(0x15FFF8E1),
                                start = Offset(startX, 0f),
                                end = Offset(startX + w * 0.25f, h),
                                strokeWidth = 2.5f
                            )
                        }
                    }
                    CraftImageFilter.NATURAL_STUDIO, CraftImageFilter.ARTISAN_NATURAL -> {
                        // Natural Studio: Ambient organic drop-shadow pedestal & soft clay daylight radial
                        drawOval(
                            color = Color(0x245C2415),
                            topLeft = Offset(w * 0.15f, h * 0.78f),
                            size = Size(w * 0.70f, h * 0.12f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x00FFFFFF), Color(0x158D4E2D)),
                                center = Offset(w * 0.5f, h * 0.5f),
                                radius = w * 0.65f
                            ),
                            radius = w * 0.60f,
                            center = Offset(w * 0.5f, h * 0.5f)
                        )
                    }
                    CraftImageFilter.MINIMALIST_NEUTRAL, CraftImageFilter.CLASSIC_CATALOG -> {
                        // Minimalist Neutral: Crisp clean edge isolation & neutral studio pedestal
                        drawOval(
                            color = Color(0x18000000),
                            topLeft = Offset(w * 0.20f, h * 0.82f),
                            size = Size(w * 0.60f, h * 0.08f)
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x08FFFFFF), Color(0x00000000), Color(0x12000000))
                            )
                        )
                    }
                    else -> {}
                }
            }
        }

        // Top Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isGiTagged) {
                Surface(
                    color = ForestSuccess,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "GI Tag",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "GI TAG CERTIFIED",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            if (showEnhancementBadge) {
                Surface(
                    color = TerracottaPrimary.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Enhanced",
                            tint = MarigoldTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = filter.title,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CraftCategoryVectorCanvas(category: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when {
            category.contains("Pottery", ignoreCase = true) || category.contains("Terracotta", ignoreCase = true) -> {
                // Draw warm clay studio backdrop with geometric tribal pot
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFAF0E6), Color(0xFFEAD5C3), Color(0xFFD4BBA5))
                    )
                )
                // Studio shadow
                drawOval(
                    color = Color(0x335C2415),
                    topLeft = Offset(w * 0.2f, h * 0.72f),
                    size = Size(w * 0.6f, h * 0.12f)
                )
                // Terracotta vase silhouette
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.22f)
                    cubicTo(w * 0.38f, h * 0.22f, w * 0.32f, h * 0.28f, w * 0.30f, h * 0.36f)
                    cubicTo(w * 0.24f, h * 0.44f, w * 0.20f, h * 0.54f, w * 0.22f, h * 0.64f)
                    cubicTo(w * 0.24f, h * 0.72f, w * 0.38f, h * 0.76f, w * 0.5f, h * 0.76f)
                    cubicTo(w * 0.62f, h * 0.76f, w * 0.76f, h * 0.72f, w * 0.78f, h * 0.64f)
                    cubicTo(w * 0.80f, h * 0.54f, w * 0.76f, h * 0.44f, w * 0.70f, h * 0.36f)
                    cubicTo(w * 0.68f, h * 0.28f, w * 0.62f, h * 0.22f, w * 0.5f, h * 0.22f)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFB33918), Color(0xFFC84B26), Color(0xFFE06D44), Color(0xFF87230F))
                    )
                )
                // Rim
                drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(w * 0.38f, h * 0.20f),
                    size = Size(w * 0.24f, h * 0.04f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Tribal etched decorative belt
                drawLine(
                    color = Color(0xFFFFF7ED),
                    start = Offset(w * 0.24f, h * 0.52f),
                    end = Offset(w * 0.76f, h * 0.52f),
                    strokeWidth = 5f
                )
                drawLine(
                    color = Color(0xFFFFF7ED),
                    start = Offset(w * 0.25f, h * 0.56f),
                    end = Offset(w * 0.75f, h * 0.56f),
                    strokeWidth = 3f
                )
            }
            category.contains("Handloom", ignoreCase = true) || category.contains("Textile", ignoreCase = true) || category.contains("Silk", ignoreCase = true) -> {
                // Royal Banarasi textile drape with golden zari brocade motifs
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF831843), Color(0xFF9D174D), Color(0xFF4C0519)),
                        start = Offset.Zero,
                        end = Offset(w, h)
                    )
                )
                // Golden Zari weaves
                for (i in 0..12) {
                    val y = h * (0.15f + i * 0.06f)
                    drawLine(
                        color = Color(0x66F59E0B),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 2f
                    )
                }
                for (i in 0..8) {
                    val x = w * (0.1f + i * 0.1f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD54F), Color(0xFFD97706), Color.Transparent),
                            center = Offset(x, h * 0.45f),
                            radius = 24f
                        ),
                        radius = 24f,
                        center = Offset(x, h * 0.45f)
                    )
                }
                // Grand border
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFF59E0B), Color(0xFFFFE082), Color(0xFFD97706))
                    ),
                    topLeft = Offset(0f, h * 0.78f),
                    size = Size(w, h * 0.18f)
                )
            }
            category.contains("Metal", ignoreCase = true) || category.contains("Dhokra", ignoreCase = true) || category.contains("Brass", ignoreCase = true) -> {
                // Rustic oxidized dark bronze with golden antique highlights
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF292524), Color(0xFF1C1917), Color(0xFF0C0A09))
                    )
                )
                // Lost-wax wire mesh texture
                drawCircle(
                    color = Color(0x33D97706),
                    radius = w * 0.35f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                // Tribal horse outline
                val horsePath = Path().apply {
                    moveTo(w * 0.35f, h * 0.70f)
                    lineTo(w * 0.35f, h * 0.50f)
                    lineTo(w * 0.25f, h * 0.35f)
                    lineTo(w * 0.38f, h * 0.28f)
                    lineTo(w * 0.55f, h * 0.42f)
                    lineTo(w * 0.72f, h * 0.48f)
                    lineTo(w * 0.72f, h * 0.70f)
                    lineTo(w * 0.65f, h * 0.70f)
                    lineTo(w * 0.65f, h * 0.56f)
                    lineTo(w * 0.42f, h * 0.56f)
                    lineTo(w * 0.42f, h * 0.70f)
                    close()
                }
                drawPath(
                    path = horsePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFD54F), Color(0xFFD97706), Color(0xFF78350F))
                    )
                )
            }
            category.contains("Wood", ignoreCase = true) -> {
                // Rich seasoned teak/sheesham timber with golden jaali carvings
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFF92400E))
                    )
                )
                // Carved box silhouette
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFB45309), Color(0xFFD97706), Color(0xFF78350F))
                    ),
                    topLeft = Offset(w * 0.2f, h * 0.35f),
                    size = Size(w * 0.6f, h * 0.4f),
                    cornerRadius = CornerRadius(16f, 16f)
                )
                // Brass inlay geometric lines
                drawRoundRect(
                    color = Color(0xFFFDE68A),
                    topLeft = Offset(w * 0.25f, h * 0.40f),
                    size = Size(w * 0.5f, h * 0.3f),
                    style = Stroke(width = 3f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
            category.contains("Jewel", ignoreCase = true) || category.contains("Filigree", ignoreCase = true) -> {
                // Royal velvet backdrop with glittering silver filigree
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF020617))
                    )
                )
                // Silver peacock filigree circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1), Color(0xFF94A3B8)),
                        center = Offset(w * 0.5f, h * 0.48f),
                        radius = w * 0.25f
                    ),
                    radius = w * 0.22f,
                    center = Offset(w * 0.5f, h * 0.48f),
                    style = Stroke(width = 4f)
                )
                drawCircle(
                    color = Color(0xFF38BDF8),
                    radius = w * 0.06f,
                    center = Offset(w * 0.5f, h * 0.48f)
                )
            }
            category.contains("Decor", ignoreCase = true) || category.contains("Blue Pottery", ignoreCase = true) -> {
                // Persian turquoise and cobalt blue pottery canvas
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF0FDF4), Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF075985)),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.32f
                    ),
                    radius = w * 0.30f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                drawCircle(
                    color = Color(0xFFF8FAFC),
                    radius = w * 0.12f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }
            else -> {
                // Madhubani / Folk Art Folk Sun Canvas
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color(0xFFFDE68A))
                    )
                )
                // Sacred geometric border
                drawRoundRect(
                    color = Color(0xFF87230F),
                    topLeft = Offset(w * 0.08f, h * 0.08f),
                    size = Size(w * 0.84f, h * 0.84f),
                    style = Stroke(width = 6f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Folk Sun motif
                drawCircle(
                    color = Color(0xFFB33918),
                    radius = w * 0.22f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
                drawCircle(
                    color = Color(0xFFF59E0B),
                    radius = w * 0.16f,
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }
        }
    }
}

@Composable
fun BeforeAfterCraftComparisonSlider(
    imageUri: String,
    category: String,
    backdropType: com.example.data.model.BackdropType,
    brightness: Float,
    contrast: Float,
    sharpness: Float,
    isCentered: Boolean,
    sliderPosition: Float,
    onSliderPositionChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    originalImageUri: String = imageUri,
    enhancedImageUri: String = imageUri
) {
    var componentWidth by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(18.dp))
            .onGloballyPositioned { coordinates ->
                componentWidth = coordinates.size.width.toFloat()
            }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    if (componentWidth > 0f) {
                        val newPos = (sliderPosition + (delta / componentWidth)).coerceIn(0.05f, 0.95f)
                        onSliderPositionChange(newPos)
                    }
                }
            )
    ) {
        // 1. Enhanced Studio Layer (Base Layer)
        StudioEnhancedLayer(
            imageUri = if (enhancedImageUri.isNotBlank()) enhancedImageUri else imageUri,
            category = category,
            backdropType = backdropType,
            brightness = brightness,
            contrast = contrast,
            sharpness = sharpness,
            isCentered = isCentered,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Original Raw Workshop Layer (Clipped to sliderPosition from Left)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = sliderPosition)
                .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
        ) {
            val originalWidthDp = with(density) {
                if (componentWidth > 0f) componentWidth.toDp() else 800.dp
            }
            OriginalRawLayer(
                imageUri = if (originalImageUri.isNotBlank()) originalImageUri else imageUri,
                category = category,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(originalWidthDp)
            )
        }

        // 3. Draggable Divider Line & Handle
        val handleOffsetDp = with(density) {
            val handleWidthPx = 18.dp.toPx()
            ((componentWidth * sliderPosition) - handleWidthPx).coerceAtLeast(0f).toDp()
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .offset(x = handleOffsetDp)
                .width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Vertical Line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(Color.White)
            )

            // Center Floating Circular Handle
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = TerracottaPrimary,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = "Slide to compare Original and Enhanced",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Top Badges: ORIGINAL vs AI ENHANCED
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "ORIGINAL (मूल फोटो)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                color = ForestSuccess.copy(alpha = 0.90f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "AI ENHANCED (स्टूडियो)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Bottom Non-Destructive Craft Integrity Tag
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "✨ Authentic handmade texture preserved • Presentation enhanced only",
                fontSize = 9.sp,
                color = Color(0xFFFFE082),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun OriginalRawLayer(
    imageUri: String,
    category: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3E2723), Color(0xFF2E1C14), Color(0xFF1B110D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri.isNotBlank() && (imageUri.startsWith("content://") || imageUri.startsWith("file://") || imageUri.startsWith("http"))) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Raw Original Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Vector representation with simulated workshop background
            CraftCategoryVectorCanvas(category = category, modifier = Modifier.fillMaxSize())
        }

        // Raw workshop underexposure & shadow simulation overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
        )
    }
}

@Composable
fun StudioEnhancedLayer(
    imageUri: String,
    category: String,
    backdropType: com.example.data.model.BackdropType,
    brightness: Float,
    contrast: Float,
    sharpness: Float,
    isCentered: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val backdropBrush = when (backdropType) {
        com.example.data.model.BackdropType.WHITE_STUDIO -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFF8F9FA), Color(0xFFEDF2F7))
        )
        com.example.data.model.BackdropType.WARM_TERRACOTTA -> Brush.radialGradient(
            colors = listOf(Color(0xFFFFF7ED), Color(0xFFFED7AA), Color(0xFFFDBA74))
        )
        com.example.data.model.BackdropType.RAW_LINEN -> Brush.linearGradient(
            colors = listOf(Color(0xFFF5EBE0), Color(0xFFE3D5CA), Color(0xFFD5BDAF))
        )
        com.example.data.model.BackdropType.DARK_TEAKWOOD -> Brush.radialGradient(
            colors = listOf(Color(0xFF3F2E23), Color(0xFF2B1F17), Color(0xFF17100B))
        )
        com.example.data.model.BackdropType.PEDESTAL_GRADIENT -> Brush.verticalGradient(
            colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFCBD5E1))
        )
    }

    Box(
        modifier = modifier.background(backdropBrush),
        contentAlignment = if (isCentered) Alignment.Center else Alignment.CenterStart
    ) {
        // Pedestal Drop Shadow for clean studio depth
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawOval(
                color = Color(0x33000000),
                topLeft = Offset(w * 0.20f, h * 0.74f),
                size = Size(w * 0.60f, h * 0.14f)
            )
        }

        if (imageUri.isNotBlank() && (imageUri.startsWith("content://") || imageUri.startsWith("file://") || imageUri.startsWith("http"))) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "AI Enhanced Craft Photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize(if (isCentered) 0.88f else 0.82f)
                    .padding(8.dp)
            )
        } else {
            CraftCategoryVectorCanvas(
                category = category,
                modifier = Modifier.fillMaxSize(if (isCentered) 0.90f else 0.85f)
            )
        }

        // Lighting balance & brightness correction tint
        val lightingTint = if (brightness > 0f) {
            Color.White.copy(alpha = (brightness * 0.25f).coerceIn(0f, 0.35f))
        } else {
            Color.Black.copy(alpha = (-brightness * 0.25f).coerceIn(0f, 0.35f))
        }
        if (lightingTint != Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(lightingTint)
            )
        }
    }
}

/**
 * AI Canvas Tone & RGB Color Response Curve Visualizer
 *
 * Dynamically plots tone response curves on Compose Canvas based on active
 * brightness, contrast, and saturation multipliers.
 */
@Composable
fun CanvasToneCurveVisualizer(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    filterName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181512)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E342B))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AI Canvas Tone Curve ($filterName)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFF7ED)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Channel badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFEF4444), CircleShape))
                        Text("R", fontSize = 9.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF22C55E), CircleShape))
                        Text("G", fontSize = 9.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF3B82F6), CircleShape))
                        Text("B", fontSize = 9.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFFFD54F), CircleShape))
                        Text("Luma", fontSize = 9.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real-time Canvas Graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F0D0B))
            ) {
                val w = size.width
                val h = size.height

                // Draw Gridlines (Shadows, Midtones, Highlights)
                for (i in 1..3) {
                    val x = w * (i * 0.25f)
                    val y = h * (i * 0.25f)
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // Baseline Linear 45-degree diagonal
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(0f, h),
                    end = Offset(w, 0f),
                    strokeWidth = 1.5f
                )

                // Luminance Curve Path calculation
                fun calculateCurveY(normX: Float, b: Float, c: Float): Float {
                    // S-curve centered at 0.5 with contrast 'c' and brightness offset 'b'
                    val centered = normX - 0.5f
                    val sCurved = 0.5f + (centered * c) + (b - 1.0f) * 0.5f
                    val clamped = sCurved.coerceIn(0f, 1f)
                    return h * (1f - clamped)
                }

                // Draw Channel Curves
                val lumaPath = Path()
                val redPath = Path()
                val greenPath = Path()
                val bluePath = Path()

                val steps = 30
                for (step in 0..steps) {
                    val normX = step.toFloat() / steps
                    val px = w * normX

                    // Luminance
                    val pyLuma = calculateCurveY(normX, brightness, contrast)
                    if (step == 0) lumaPath.moveTo(px, pyLuma) else lumaPath.lineTo(px, pyLuma)

                    // Red Channel (boosted if warm/vivid)
                    val pyRed = calculateCurveY(normX, brightness * (1f + (saturation - 1f) * 0.15f), contrast)
                    if (step == 0) redPath.moveTo(px, pyRed) else redPath.lineTo(px, pyRed)

                    // Green Channel
                    val pyGreen = calculateCurveY(normX, brightness, contrast)
                    if (step == 0) greenPath.moveTo(px, pyGreen) else greenPath.lineTo(px, pyGreen)

                    // Blue Channel
                    val pyBlue = calculateCurveY(normX, brightness * (1f - (saturation - 1f) * 0.10f), contrast)
                    if (step == 0) bluePath.moveTo(px, pyBlue) else bluePath.lineTo(px, pyBlue)
                }

                // Draw Red, Green, Blue subtle curves
                drawPath(path = redPath, color = Color(0x66EF4444), style = Stroke(width = 2f))
                drawPath(path = greenPath, color = Color(0x6622C55E), style = Stroke(width = 2f))
                drawPath(path = bluePath, color = Color(0x663B82F6), style = Stroke(width = 2f))

                // Draw Main Luminance Curve with Glow
                drawPath(
                    path = lumaPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFF59E0B), Color(0xFFFFD54F), Color(0xFFFDE68A))
                    ),
                    style = Stroke(width = 3.5f)
                )

                // Key Nodes: Shadows (25%), Midtones (50%), Highlights (75%)
                val shadowY = calculateCurveY(0.25f, brightness, contrast)
                val midtoneY = calculateCurveY(0.50f, brightness, contrast)
                val highlightY = calculateCurveY(0.75f, brightness, contrast)

                drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(w * 0.25f, shadowY))
                drawCircle(color = Color(0xFFFFD54F), radius = 5f, center = Offset(w * 0.50f, midtoneY))
                drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(w * 0.75f, highlightY))
            }

            // Bottom Metric readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🌑 Shadows: ${((brightness * 0.85f) * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = Color(0xFFD6D3D1)
                )
                Text(
                    text = "🌓 Midtones: ${((contrast) * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = Color(0xFFD6D3D1)
                )
                Text(
                    text = "🌕 Highlights: ${((brightness * 1.15f) * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = Color(0xFFD6D3D1)
                )
            }
        }
    }
}


