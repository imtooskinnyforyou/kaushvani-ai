package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.utils.FocusStatus
import com.example.ui.utils.LightingStatus
import com.example.ui.utils.RealtimeFrameQuality
import com.example.ui.utils.StabilityStatus

/**
 * Modern real-time CameraX viewfinder overlay displaying live lighting and focus evaluation HUD,
 * interactive touch-to-focus animation reticles, framing guide lines, and visual feedback indicators.
 */
@Composable
fun CameraQualityOverlay(
    quality: RealtimeFrameQuality,
    isTorchEnabled: Boolean,
    onToggleTorch: () -> Unit,
    onTapToFocus: (Offset) -> Unit,
    onSpeakAdvice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var focusTapPosition by remember { mutableStateOf<Offset?>(null) }
    var focusTapTimestamp by remember { mutableLongStateOf(0L) }

    // Pulsing animation for ready state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ready")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Fade out tap-to-focus indicator after 2 seconds
    LaunchedEffect(focusTapTimestamp) {
        if (focusTapTimestamp > 0) {
            kotlinx.coroutines.delay(2200)
            focusTapPosition = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    focusTapPosition = offset
                    focusTapTimestamp = System.currentTimeMillis()
                    onTapToFocus(offset)
                }
            }
            .testTag("camera_realtime_quality_overlay")
    ) {
        // 1. Center Craft Framing Reticle with Dynamic Quality Color Feedback
        DynamicFramingReticle(
            quality = quality,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Interactive Tap-to-Focus Reticle at Touch Point
        focusTapPosition?.let { pos ->
            TapToFocusReticle(
                position = pos,
                isFocused = quality.focusStatus == FocusStatus.SHARP
            )
        }

        // 3. Top Real-Time Analysis HUD Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Status Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Lighting Pill
                LightingFeedbackPill(
                    lightingStatus = quality.lightingStatus,
                    score = quality.lightingScore,
                    isTorchEnabled = isTorchEnabled,
                    onToggleTorch = onToggleTorch
                )

                // Live Focus Pill
                FocusFeedbackPill(
                    focusStatus = quality.focusStatus,
                    score = quality.focusScore
                )

                // Readiness Circular Score Badge
                CaptureReadinessBadge(
                    score = quality.overallScore,
                    isReady = quality.isReadyToCapture,
                    pulseScale = if (quality.isReadyToCapture) pulseScale else 1f
                )
            }
        }

        // 4. Floating Real-Time Actionable Guidance Banner (Above Shutter Controls)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 118.dp)
                .testTag("camera_live_guidance_banner"),
            shape = RoundedCornerShape(14.dp),
            color = if (quality.isReadyToCapture) {
                Color(0xCC064E3B) // Dark Emerald
            } else {
                Color(0xD91F2937) // Dark Slate
            },
            border = androidx.compose.foundation.BorderStroke(
                1.2.dp,
                if (quality.isReadyToCapture) ForestSuccess else Color(0xFFF59E0B)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when {
                            quality.isReadyToCapture -> Icons.Default.CheckCircle
                            quality.lightingStatus == LightingStatus.TOO_DARK -> Icons.Default.FlashOn
                            quality.lightingStatus == LightingStatus.TOO_BRIGHT -> Icons.Default.WbSunny
                            quality.focusStatus == FocusStatus.BLURRY -> Icons.Default.CenterFocusWeak
                            quality.stabilityStatus == StabilityStatus.SHAKING -> Icons.Default.ScreenRotation
                            else -> Icons.Default.Lightbulb
                        },
                        contentDescription = null,
                        tint = if (quality.isReadyToCapture) ForestSuccess else Color(0xFFFFD54F),
                        modifier = Modifier.size(20.dp)
                    )

                    Column {
                        Text(
                            text = quality.primaryGuidanceTextHindi,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = quality.primaryGuidanceTextEnglish,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Voice Prompt Button
                IconButton(
                    onClick = { onSpeakAdvice(quality.primaryGuidanceTextHindi) },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .semantics { contentDescription = "सुझाव बोलकर सुनें" }
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Center Framing Reticle with color feedback reflecting real-time lighting & focus readiness.
 */
@Composable
private fun DynamicFramingReticle(
    quality: RealtimeFrameQuality,
    modifier: Modifier = Modifier
) {
    val reticleColor = when {
        quality.isReadyToCapture -> ForestSuccess
        quality.overallScore >= 60 -> Color(0xFFFFD54F) // Marigold Amber
        else -> TerracottaPrimary
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val frameSize = width * 0.76f
        val left = (width - frameSize) / 2f
        val top = (height - frameSize) / 2.3f

        val cornerLength = 34.dp.toPx()
        val strokeWidth = if (quality.isReadyToCapture) 4.5.dp.toPx() else 3.5.dp.toPx()

        // Corner Brackets
        // Top-Left
        drawLine(reticleColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth, StrokeCap.Round)
        drawLine(reticleColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth, StrokeCap.Round)

        // Top-Right
        drawLine(reticleColor, Offset(left + frameSize, top), Offset(left + frameSize - cornerLength, top), strokeWidth, StrokeCap.Round)
        drawLine(reticleColor, Offset(left + frameSize, top), Offset(left + frameSize, top + cornerLength), strokeWidth, StrokeCap.Round)

        // Bottom-Left
        drawLine(reticleColor, Offset(left, top + frameSize), Offset(left + cornerLength, top + frameSize), strokeWidth, StrokeCap.Round)
        drawLine(reticleColor, Offset(left, top + frameSize), Offset(left, top + frameSize - cornerLength), strokeWidth, StrokeCap.Round)

        // Bottom-Right
        drawLine(reticleColor, Offset(left + frameSize, top + frameSize), Offset(left + frameSize - cornerLength, top + frameSize), strokeWidth, StrokeCap.Round)
        drawLine(reticleColor, Offset(left + frameSize, top + frameSize), Offset(left + frameSize, top + frameSize - cornerLength), strokeWidth, StrokeCap.Round)

        // Center Crosshair Target Ticks
        val centerX = left + (frameSize / 2f)
        val centerY = top + (frameSize / 2f)
        val tickLength = 10.dp.toPx()
        val tickColor = reticleColor.copy(alpha = 0.55f)
        val tickStroke = 1.5.dp.toPx()

        drawLine(tickColor, Offset(centerX - tickLength, centerY), Offset(centerX + tickLength, centerY), tickStroke)
        drawLine(tickColor, Offset(centerX, centerY - tickLength), Offset(centerX, centerY + tickLength), tickStroke)

        // Rule of thirds subtle grid lines inside box
        val oneThirdW = frameSize / 3f
        val oneThirdH = frameSize / 3f
        val gridColor = Color.White.copy(alpha = 0.20f)
        val gridStroke = 1.dp.toPx()

        drawLine(gridColor, Offset(left + oneThirdW, top), Offset(left + oneThirdW, top + frameSize), gridStroke)
        drawLine(gridColor, Offset(left + 2 * oneThirdW, top), Offset(left + 2 * oneThirdW, top + frameSize), gridStroke)
        drawLine(gridColor, Offset(left, top + oneThirdH), Offset(left + frameSize, top + oneThirdH), gridStroke)
        drawLine(gridColor, Offset(left, top + 2 * oneThirdH), Offset(left + frameSize, top + 2 * oneThirdH), gridStroke)
    }
}

/**
 * Animated Tap-to-Focus Reticle showing immediate target focus ring at touch location.
 */
@Composable
private fun TapToFocusReticle(
    position: Offset,
    isFocused: Boolean
) {
    val transition = rememberInfiniteTransition(label = "focus_pulse")
    val ringAlpha by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Box(
        modifier = Modifier
            .offset(
                x = (position.x / androidx.compose.ui.platform.LocalDensity.current.density).dp - 32.dp,
                y = (position.y / androidx.compose.ui.platform.LocalDensity.current.density).dp - 32.dp
            )
            .size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val color = if (isFocused) ForestSuccess else Color(0xFFFFD54F)
            val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)

            // Outer Reticle Ring
            drawCircle(
                color = color.copy(alpha = ringAlpha),
                radius = size.width / 2.2f,
                style = stroke
            )

            // Center Pin Point
            drawCircle(
                color = color,
                radius = 3.dp.toPx()
            )

            // 4 Corner brackets on tap circle
            val r = size.width / 2.2f
            val c = size.width / 2f
            val len = 7.dp.toPx()

            // Top tick
            drawLine(color, Offset(c, c - r - len), Offset(c, c - r), 2.dp.toPx())
            // Bottom tick
            drawLine(color, Offset(c, c + r), Offset(c, c + r + len), 2.dp.toPx())
            // Left tick
            drawLine(color, Offset(c - r - len, c), Offset(c - r, c), 2.dp.toPx())
            // Right tick
            drawLine(color, Offset(c + r, c), Offset(c + r + len, c), 2.dp.toPx())
        }
    }
}

/**
 * Lighting Feedback Pill with quick torch toggle action.
 */
@Composable
private fun LightingFeedbackPill(
    lightingStatus: LightingStatus,
    score: Int,
    isTorchEnabled: Boolean,
    onToggleTorch: () -> Unit
) {
    val (bgColor, iconColor, icon, textHindi) = when (lightingStatus) {
        LightingStatus.OPTIMAL -> Quad(
            Color(0xCC064E3B),
            ForestSuccess,
            Icons.Default.WbSunny,
            "रोशनी उत्तम ($score%)"
        )
        LightingStatus.DIM -> Quad(
            Color(0xCC78350F),
            Color(0xFFF59E0B),
            Icons.Default.NightsStay,
            "कम रोशनी ($score%)"
        )
        LightingStatus.TOO_DARK -> Quad(
            Color(0xCC7F1D1D),
            Color(0xFFEF4444),
            Icons.Default.FlashOn,
            "अंधेरा ($score%)"
        )
        LightingStatus.TOO_BRIGHT -> Quad(
            Color(0xCC78350F),
            Color(0xFFF59E0B),
            Icons.Default.WbTwilight,
            "अधिक चमक ($score%)"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.7f)),
        modifier = Modifier.clickable(enabled = lightingStatus == LightingStatus.TOO_DARK || lightingStatus == LightingStatus.DIM) {
            onToggleTorch()
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = textHindi,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if ((lightingStatus == LightingStatus.TOO_DARK || lightingStatus == LightingStatus.DIM) && !isTorchEnabled) {
                Text(
                    text = "टॉर्च?",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
            }
        }
    }
}

/**
 * Focus Feedback Pill showing sharpness / blur state.
 */
@Composable
private fun FocusFeedbackPill(
    focusStatus: FocusStatus,
    score: Int
) {
    val (bgColor, iconColor, icon, textHindi) = when (focusStatus) {
        FocusStatus.SHARP -> Quad(
            Color(0xCC064E3B),
            ForestSuccess,
            Icons.Default.CenterFocusStrong,
            "स्पष्ट फोकस ($score%)"
        )
        FocusStatus.FAIR -> Quad(
            Color(0xCC78350F),
            Color(0xFFF59E0B),
            Icons.Default.CenterFocusWeak,
            "स्थिर रखें ($score%)"
        )
        FocusStatus.BLURRY -> Quad(
            Color(0xCC7F1D1D),
            Color(0xFFEF4444),
            Icons.Default.FilterCenterFocus,
            "धुंधला ($score%)"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = textHindi,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Capture Readiness Circular Gauge Badge.
 */
@Composable
private fun CaptureReadinessBadge(
    score: Int,
    isReady: Boolean,
    pulseScale: Float
) {
    Surface(
        color = if (isReady) Color(0xCC064E3B) else Color(0xCC1F2937),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isReady) ForestSuccess else Color.White.copy(alpha = 0.3f)
        ),
        modifier = Modifier.scale(pulseScale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Mini circular progress
            Box(
                modifier = Modifier.size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Sweep
                    drawArc(
                        color = if (isReady) ForestSuccess else Color(0xFFFFD54F),
                        startAngle = -90f,
                        sweepAngle = (score / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Text(
                text = if (isReady) "तैयार ✓" else "$score%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isReady) ForestSuccess else Color.White
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
