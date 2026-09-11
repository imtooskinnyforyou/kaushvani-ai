package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.CropAspectRatio
import com.example.data.model.ImageEnhancementSettings
import com.example.ui.theme.*

/**
 * Custom Image Cropping Tool for Handcrafted Product Cataloging.
 *
 * Provides marketplace-standard aspect ratio presets:
 * - 1:1 Square (1080x1080) - Amazon / Flipkart / ONDC Standard
 * - 4:5 Portrait (1080x1350) - Instagram / Social Commerce Showcase
 * - 3:4 Catalog (1080x1440) - Handloom Textiles, Saree & Apparel
 * - 4:3 Wide (1440x1080) - Dhokra Art, Sculptures & Home Decor
 *
 * Features:
 * - Interactive Pan & Pinch-to-Zoom framing gestures
 * - Translucent darkened mask outside active crop box
 * - Rule-of-Thirds composition grid guidelines (3x3)
 * - Corner golden artisan alignment reticles
 * - 90° Clockwise Rotation & Horizontal Mirror Flip
 * - Auto-Center and Reset alignment
 * - Bilingual audio and visual guidance for artisans
 */
@Composable
fun CustomImageCroppingStageView(
    imageUri: String,
    category: String,
    currentSettings: ImageEnhancementSettings,
    onApplyCrop: (
        cropRatio: CropAspectRatio,
        rotationDegrees: Float,
        isFlipped: Boolean,
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ) -> Unit,
    onRetake: () -> Unit,
    onSpeakAdvice: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var selectedRatio by remember { mutableStateOf(currentSettings.cropRatio) }
    var rotationDegrees by remember { mutableFloatStateOf(currentSettings.rotationDegrees) }
    var isFlippedHorizontal by remember { mutableStateOf(currentSettings.isFlippedHorizontal) }
    var zoomScale by remember { mutableFloatStateOf(currentSettings.cropScale.coerceIn(0.8f, 3.5f)) }
    var panOffsetX by remember { mutableFloatStateOf(currentSettings.cropOffsetX) }
    var panOffsetY by remember { mutableFloatStateOf(currentSettings.cropOffsetY) }
    var showGridLines by remember { mutableStateOf(true) }

    val presetRatios = remember {
        listOf(
            CropAspectRatio.SQUARE_1_1,
            CropAspectRatio.PORTRAIT_4_5,
            CropAspectRatio.PORTRAIT_3_4,
            CropAspectRatio.CATALOG_4_3,
            CropAspectRatio.BANNER_16_9
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArtisanBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("custom_crop_stage_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner with Bilingual Advice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TerracottaContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Crop,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Crop & Frame Product",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "मार्केटप्लेस के अनुसार सही अनुपात व फ्रेम चुनें",
                            fontSize = 12.sp,
                            color = TerracottaPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val tip = when (selectedRatio) {
                            CropAspectRatio.SQUARE_1_1 -> "1:1 चौकोर अनुपात Amazon, Flipkart और ONDC के लिए मानक है। उत्पाद को बीच में रखें।"
                            CropAspectRatio.PORTRAIT_4_5 -> "4:5 पोर्ट्रेट अनुपात सोशल मीडिया व इंस्टाग्राम शॉप के लिए सर्वश्रेष्ठ है।"
                            CropAspectRatio.PORTRAIT_3_4 -> "3:4 अनुपात साड़ी, कुर्ती, शॉल व वस्त्रों के प्रदर्शन के लिए आदर्श है।"
                            CropAspectRatio.CATALOG_4_3 -> "4:3 लैंडस्केप अनुपात चौड़े हस्तशिल्प और मूर्तियों के लिए उपयुक्त है।"
                            CropAspectRatio.BANNER_16_9 -> "16:9 बैनर अनुपात दुकान के मुख्य कवर फोटो के लिए उपयोग करें।"
                        }
                        onSpeakAdvice(tip)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TerracottaContainer)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Audio Guide",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Interactive Cropping Viewport Canvas Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .testTag("crop_viewport_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141416)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2E2E34))
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(selectedRatio) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.75f, 4.0f)
                            panOffsetX += pan.x
                            panOffsetY += pan.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Interactive Craft Image with Transform Layers
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = if (isFlippedHorizontal) -zoomScale else zoomScale
                            scaleY = zoomScale
                            translationX = panOffsetX
                            translationY = panOffsetY
                            rotationZ = rotationDegrees
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri.isNotBlank() && (imageUri.startsWith("content://") || imageUri.startsWith("file://") || imageUri.startsWith("http"))) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Craft Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CraftCategoryVectorCanvas(
                            category = category,
                            modifier = Modifier.fillMaxSize(0.85f)
                        )
                    }
                }

                // Custom Cropping Overlay: Dark Mask, Border, Corner Reticles, Grid Lines
                CroppingOverlayCanvas(
                    aspectRatio = selectedRatio.ratio,
                    showGrid = showGridLines,
                    modifier = Modifier.fillMaxSize()
                )

                // Live Floating HUD Badges inside Viewport
                // Top-Left: Active Aspect Ratio & Resolution Tag
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MarigoldTertiary.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MarigoldTertiary)
                        )
                        Text(
                            text = "${selectedRatio.label} • ${selectedRatio.dimensionsLabel}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top-Right: Zoom & Framing Info
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Zoom ${(zoomScale * 100).toInt()}% • Rot ${rotationDegrees.toInt()}°",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                // Bottom Hint: Touch Gesture Prompt
                Surface(
                    color = Color.Black.copy(alpha = 0.60f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "👆 ड्रैग करके स्थिति बदलें • पिंच से ज़ूम करें (Pinch & Drag to adjust)",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Quick Transform Toolbar (Rotate, Flip, Reset, Grid Toggle)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rotate 90 deg button
                TransformActionButton(
                    icon = Icons.Default.RotateRight,
                    label = "Rotate 90°",
                    hindiLabel = "घुमाएं",
                    testTag = "crop_rotate_button",
                    onClick = {
                        rotationDegrees = (rotationDegrees + 90f) % 360f
                    }
                )

                // Flip horizontal button
                TransformActionButton(
                    icon = Icons.Default.Flip,
                    label = "Flip",
                    hindiLabel = "पलटें",
                    isActive = isFlippedHorizontal,
                    testTag = "crop_flip_button",
                    onClick = {
                        isFlippedHorizontal = !isFlippedHorizontal
                    }
                )

                // Toggle Grid lines
                TransformActionButton(
                    icon = Icons.Default.GridOn,
                    label = "Grid 3x3",
                    hindiLabel = "ग्रिड",
                    isActive = showGridLines,
                    testTag = "crop_grid_toggle_button",
                    onClick = {
                        showGridLines = !showGridLines
                    }
                )

                // Auto-center / Reset button
                TransformActionButton(
                    icon = Icons.Default.CenterFocusStrong,
                    label = "Center",
                    hindiLabel = "रीसेट",
                    testTag = "crop_reset_button",
                    onClick = {
                        zoomScale = 1.0f
                        panOffsetX = 0f
                        panOffsetY = 0f
                        rotationDegrees = 0f
                        isFlippedHorizontal = false
                    }
                )
            }
        }

        // Aspect Ratio Preset Selector Cards
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "मार्केटप्लेस आस्पेक्ट रेशियो (Aspect Ratio Presets)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )
                Text(
                    text = "HD 1080p+",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ForestSuccess
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presetRatios) { ratio ->
                    val isSelected = ratio == selectedRatio
                    AspectRatioPresetCard(
                        ratio = ratio,
                        isSelected = isSelected,
                        onClick = {
                            selectedRatio = ratio
                            // Auto adjust pan offset when switching ratios
                            panOffsetX = 0f
                            panOffsetY = 0f
                        }
                    )
                }
            }
        }

        // Active Ratio Marketplace Standards Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (selectedRatio) {
                    CropAspectRatio.SQUARE_1_1 -> Color(0xFFF0FDF4)
                    CropAspectRatio.PORTRAIT_4_5 -> Color(0xFFFAF5FF)
                    CropAspectRatio.PORTRAIT_3_4 -> Color(0xFFFFFBEB)
                    else -> ArtisanSurfaceVariant
                }
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    when (selectedRatio) {
                        CropAspectRatio.SQUARE_1_1 -> ForestSuccess.copy(alpha = 0.5f)
                        CropAspectRatio.PORTRAIT_4_5 -> Color(0xFF9333EA).copy(alpha = 0.5f)
                        CropAspectRatio.PORTRAIT_3_4 -> MarigoldTertiary.copy(alpha = 0.7f)
                        else -> ArtisanCardBorder
                    }
                )
            )
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = when (selectedRatio) {
                                CropAspectRatio.SQUARE_1_1 -> ForestSuccess
                                CropAspectRatio.PORTRAIT_4_5 -> Color(0xFF9333EA)
                                CropAspectRatio.PORTRAIT_3_4 -> Color(0xFFD97706)
                                else -> TerracottaPrimary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${selectedRatio.label} (${selectedRatio.hindiLabel})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ArtisanTextPrimary
                        )
                    }

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                    ) {
                        Text(
                            text = selectedRatio.dimensionsLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ArtisanTextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "🎯 उपयोग: ${selectedRatio.marketplaceTag}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ArtisanTextPrimary
                )
                Text(
                    text = "💡 अनुशंसित शिल्प: ${selectedRatio.recommendedFor}",
                    fontSize = 11.sp,
                    color = ArtisanTextSecondary
                )
            }
        }

        // Bottom CTA Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .weight(0.40f)
                    .height(52.dp)
                    .testTag("crop_retake_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ArtisanTextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("रीटेक", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    onApplyCrop(
                        selectedRatio,
                        rotationDegrees,
                        isFlippedHorizontal,
                        zoomScale,
                        panOffsetX,
                        panOffsetY
                    )
                },
                modifier = Modifier
                    .weight(0.60f)
                    .height(52.dp)
                    .testTag("crop_apply_continue_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerracottaPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("क्रॉप लागू करें और आगे बढ़ें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Apply Crop & AI Analyze", fontSize = 9.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}

/**
 * Aspect Ratio Preset Selection Card
 */
@Composable
private fun AspectRatioPresetCard(
    ratio: CropAspectRatio,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick)
            .testTag("preset_ratio_${ratio.name.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) TerracottaContainer else ArtisanSurface,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) TerracottaPrimary else ArtisanCardBorder
        ),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Miniature Aspect Ratio Box
            Box(
                modifier = Modifier
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                val boxWidth = when (ratio) {
                    CropAspectRatio.SQUARE_1_1 -> 28.dp
                    CropAspectRatio.PORTRAIT_4_5 -> 22.dp
                    CropAspectRatio.PORTRAIT_3_4 -> 20.dp
                    CropAspectRatio.CATALOG_4_3 -> 32.dp
                    CropAspectRatio.BANNER_16_9 -> 34.dp
                }
                val boxHeight = when (ratio) {
                    CropAspectRatio.SQUARE_1_1 -> 28.dp
                    CropAspectRatio.PORTRAIT_4_5 -> 28.dp
                    CropAspectRatio.PORTRAIT_3_4 -> 28.dp
                    CropAspectRatio.CATALOG_4_3 -> 24.dp
                    CropAspectRatio.BANNER_16_9 -> 20.dp
                }

                Box(
                    modifier = Modifier
                        .size(width = boxWidth, height = boxHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isSelected) TerracottaPrimary.copy(alpha = 0.2f) else ArtisanSurfaceVariant
                        )
                        .border(
                            1.5.dp,
                            if (isSelected) TerracottaPrimary else ArtisanTextMuted,
                            RoundedCornerShape(3.dp)
                        )
                )
            }

            Text(
                text = ratio.label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) TerracottaPrimary else ArtisanTextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = ratio.hindiLabel,
                fontSize = 10.sp,
                color = if (isSelected) TerracottaPrimary else ArtisanTextSecondary,
                textAlign = TextAlign.Center
            )

            Surface(
                color = if (isSelected) TerracottaPrimary.copy(alpha = 0.15f) else ArtisanSurfaceVariant,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = ratio.dimensionsLabel,
                    fontSize = 9.sp,
                    color = if (isSelected) TerracottaPrimary else ArtisanTextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Small Transform Action Button with Icon and Bilingual Label
 */
@Composable
private fun TransformActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    hindiLabel: String,
    isActive: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isActive) TerracottaContainer else ArtisanSurfaceVariant)
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) TerracottaPrimary else ArtisanTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = hindiLabel,
            fontSize = 10.sp,
            color = if (isActive) TerracottaPrimary else ArtisanTextSecondary,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Cropping Overlay Canvas:
 * - Calculates active crop rectangle centered inside available canvas size.
 * - Draws translucent dark mask outside the crop window.
 * - Draws golden borders and corner brackets.
 * - Draws 3x3 Rule-of-Thirds grid lines.
 */
@Composable
fun CroppingOverlayCanvas(
    aspectRatio: Float,
    showGrid: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (canvasWidth <= 0 || canvasHeight <= 0) return@Canvas

        val padding = 24.dp.toPx()
        val availableWidth = canvasWidth - (padding * 2)
        val availableHeight = canvasHeight - (padding * 2)

        // Determine crop rectangle dimensions based on aspect ratio (width / height)
        val targetRatio = if (aspectRatio > 0f) aspectRatio else 1.0f
        var cropWidth: Float
        var cropHeight: Float

        if (availableWidth / availableHeight > targetRatio) {
            // Height is constraint
            cropHeight = availableHeight
            cropWidth = cropHeight * targetRatio
        } else {
            // Width is constraint
            cropWidth = availableWidth
            cropHeight = cropWidth / targetRatio
        }

        val cropLeft = (canvasWidth - cropWidth) / 2f
        val cropTop = (canvasHeight - cropHeight) / 2f
        val cropRect = Rect(cropLeft, cropTop, cropLeft + cropWidth, cropTop + cropHeight)

        // 1. Draw Translucent Dark Mask Outside Crop Window
        val fullCanvasPath = Path().apply {
            addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
        }
        val cropWindowPath = Path().apply {
            addRect(cropRect)
        }

        val maskPath = Path().apply {
            op(fullCanvasPath, cropWindowPath, PathOperation.Difference)
        }

        drawPath(
            path = maskPath,
            color = Color.Black.copy(alpha = 0.65f)
        )

        // 2. Draw Crop Window Border (Golden Artisan Line)
        drawRect(
            color = Color(0xFFF59E0B).copy(alpha = 0.90f),
            topLeft = Offset(cropRect.left, cropRect.top),
            size = Size(cropRect.width, cropRect.height),
            style = Stroke(width = 2.dp.toPx())
        )

        // 3. Draw Rule-of-Thirds Grid Lines (3x3)
        if (showGrid) {
            val gridColor = Color.White.copy(alpha = 0.35f)
            val strokeWidth = 1.dp.toPx()

            // Vertical lines at 1/3 and 2/3
            val x1 = cropRect.left + (cropRect.width / 3f)
            val x2 = cropRect.left + (cropRect.width * 2f / 3f)
            drawLine(gridColor, Offset(x1, cropRect.top), Offset(x1, cropRect.bottom), strokeWidth)
            drawLine(gridColor, Offset(x2, cropRect.top), Offset(x2, cropRect.bottom), strokeWidth)

            // Horizontal lines at 1/3 and 2/3
            val y1 = cropRect.top + (cropRect.height / 3f)
            val y2 = cropRect.top + (cropRect.height * 2f / 3f)
            drawLine(gridColor, Offset(cropRect.left, y1), Offset(cropRect.right, y1), strokeWidth)
            drawLine(gridColor, Offset(cropRect.left, y2), Offset(cropRect.right, y2), strokeWidth)

            // Center subtle crosshair mark
            val centerX = cropRect.left + (cropRect.width / 2f)
            val centerY = cropRect.top + (cropRect.height / 2f)
            val crossSize = 8.dp.toPx()
            val centerCrossColor = Color(0xFFF59E0B).copy(alpha = 0.8f)
            drawLine(centerCrossColor, Offset(centerX - crossSize, centerY), Offset(centerX + crossSize, centerY), 1.5.dp.toPx())
            drawLine(centerCrossColor, Offset(centerX, centerY - crossSize), Offset(centerX, centerY + crossSize), 1.5.dp.toPx())
        }

        // 4. Draw Prominent Corner L-Brackets for Professional Framing
        val cornerLength = 20.dp.toPx()
        val cornerStroke = 3.5.dp.toPx()
        val cornerColor = Color(0xFFF59E0B) // Amber/Marigold

        // Top-Left Corner
        drawLine(cornerColor, Offset(cropRect.left - 1, cropRect.top), Offset(cropRect.left + cornerLength, cropRect.top), cornerStroke)
        drawLine(cornerColor, Offset(cropRect.left, cropRect.top - 1), Offset(cropRect.left, cropRect.top + cornerLength), cornerStroke)

        // Top-Right Corner
        drawLine(cornerColor, Offset(cropRect.right + 1, cropRect.top), Offset(cropRect.right - cornerLength, cropRect.top), cornerStroke)
        drawLine(cornerColor, Offset(cropRect.right, cropRect.top - 1), Offset(cropRect.right, cropRect.top + cornerLength), cornerStroke)

        // Bottom-Left Corner
        drawLine(cornerColor, Offset(cropRect.left - 1, cropRect.bottom), Offset(cropRect.left + cornerLength, cropRect.bottom), cornerStroke)
        drawLine(cornerColor, Offset(cropRect.left, cropRect.bottom + 1), Offset(cropRect.left, cropRect.bottom - cornerLength), cornerStroke)

        // Bottom-Right Corner
        drawLine(cornerColor, Offset(cropRect.right + 1, cropRect.bottom), Offset(cropRect.right - cornerLength, cropRect.bottom), cornerStroke)
        drawLine(cornerColor, Offset(cropRect.right, cropRect.bottom + 1), Offset(cropRect.right, cropRect.bottom - cornerLength), cornerStroke)
    }
}
