package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class LoadingStageInfo(
    val stageNumber: Int,
    val englishText: String,
    val hindiText: String,
    val icon: ImageVector
)

data class GlobalLoadingState(
    val isVisible: Boolean = false,
    val primaryMessage: String = "AI Business Assistant Working...",
    val hindiMessage: String = "AI आपकी सहायता कर रहा है...",
    val currentStageIndex: Int = 0,
    val stages: List<LoadingStageInfo> = listOf(
        LoadingStageInfo(1, "Enhancing photo...", "फोटो की गुणवत्ता व बैकग्राउंड सुधार...", Icons.Default.AutoFixHigh),
        LoadingStageInfo(2, "Translating description...", "मातृभाषा विवरण का अनुवाद...", Icons.Default.Translate),
        LoadingStageInfo(3, "Calculating fair price...", "उचित कारीगरी मजदूरी गणना...", Icons.Default.Calculate),
        LoadingStageInfo(4, "Generating SEO tags...", "B2B व निर्यात टैग्स निर्माण...", Icons.Default.Tag)
    ),
    val progress: Float = 0.25f,
    val tip: String = "धीमी इंटरनेट गति पर भी यह सुरक्षित रूप से कार्य करता है।"
)

@Composable
fun GlobalLoadingOverlay(
    state: GlobalLoadingState,
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(250)),
        modifier = modifier
    ) {
        // Scrim background with subtle dark blur & warm terracotta undertone
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* Prevent touches to background */ },
            contentAlignment = Alignment.Center
        ) {
            // Ambient glowing rings
            val infiniteTransition = rememberInfiniteTransition(label = "loadingGlow")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
                    .testTag("global_loading_overlay_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        colors = listOf(TerracottaPrimary, MarigoldTertiary, IndigoSecondary)
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Top Visual Spinner with Heritage Motif Glow
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer rotating dotted gradient ring
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(rotation)
                                .border(
                                    3.dp,
                                    Brush.sweepGradient(
                                        colors = listOf(
                                            TerracottaPrimary,
                                            MarigoldTertiary,
                                            IndigoSecondary,
                                            TerracottaPrimary
                                        )
                                    ),
                                    CircleShape
                                )
                        )

                        // Pulsating central hub
                        Box(
                            modifier = Modifier
                                .size((64 * pulseScale).dp)
                                .clip(CircleShape)
                                .background(TerracottaContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val activeIcon = state.stages.getOrNull(state.currentStageIndex)?.icon
                                ?: Icons.Default.AutoAwesome
                            Icon(
                                imageVector = activeIcon,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Status Messages (English + Hindi)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AnimatedContent(
                            targetState = state.primaryMessage,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "primaryStatusAnim"
                        ) { msg ->
                            Text(
                                text = msg,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }

                        AnimatedContent(
                            targetState = state.hindiMessage,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "hindiStatusAnim"
                        ) { hindiMsg ->
                            Text(
                                text = hindiMsg,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TerracottaPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Progress Bar with Percentage
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "प्रगति (Progress):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = ArtisanTextSecondary
                            )
                            val percent = (state.progress.coerceIn(0f, 1f) * 100).toInt()
                            Text(
                                text = "$percent%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        }

                        val animatedProgress by animateFloatAsState(
                            targetValue = state.progress,
                            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
                            label = "animatedProgress"
                        )

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TerracottaPrimary,
                            trackColor = TerracottaContainer
                        )
                    }

                    // Step Tracker (Checkmarks for completed stages)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = ArtisanSurfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.stages.forEachIndexed { index, stage ->
                                val isDone = index < state.currentStageIndex
                                val isCurrent = index == state.currentStageIndex

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Status icon badge
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isDone -> ForestSuccess
                                                    isCurrent -> TerracottaPrimary
                                                    else -> ArtisanCardBorder
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Completed",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else if (isCurrent) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${stage.stageNumber}",
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Stage titles
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stage.englishText,
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) ArtisanTextPrimary else if (isDone) ForestSuccess else ArtisanTextMuted
                                        )
                                        Text(
                                            text = stage.hindiText,
                                            fontSize = 10.sp,
                                            color = if (isCurrent) TerracottaPrimary else ArtisanTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Artisan Tip Box for low-bandwidth expectations
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MarigoldTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = state.tip.ifBlank { "धीमी इंटरनेट गति पर भी यह सुरक्षित रूप से कार्य करता है।" },
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Cancel / Run in Background Button
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArtisanTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "रद्द करें या बाद में देखें (Dismiss)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
