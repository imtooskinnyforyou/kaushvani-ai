package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.image.ImageQualityEvaluation
import com.example.data.image.ProductIntegrityReport
import com.example.ui.theme.*

/**
 * Image Quality Evaluation and Product Integrity Verification Card.
 *
 * Mandates:
 * - Checks Blur, Lighting, Visibility, Background Clutter.
 * - If photo is already good: "Your photo is already suitable for cataloging."
 * - If photo is poor: "Your photo needs better lighting" with [Improve Photo] action button.
 * - If photo is blurry: "Please retake photo rather than AI guessing details." with [Retake Photo] button.
 * - Shows "Product Integrity Check" badge proving craft details were never altered.
 */
@Composable
fun ProductIntegrityAndQualityCard(
    qualityEvaluation: ImageQualityEvaluation?,
    integrityReport: ProductIntegrityReport?,
    onImprovePhotoClick: () -> Unit,
    onRetakePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (qualityEvaluation == null && integrityReport == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_integrity_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section 1: Pre-enhancement Image Quality Feedback
            qualityEvaluation?.let { eval ->
                if (eval.needsRetake) {
                    // Blurry / Unclear Photo Warning
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFF87171)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = eval.primaryMessageHindi,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Text(
                                text = eval.primaryMessageEnglish,
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C)
                            )

                            Button(
                                onClick = onRetakePhotoClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("retake_photo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("दोबारा साफ़ फ़ोटो लें (Retake Photo)", fontSize = 11.sp)
                            }
                        }
                    }
                } else if (eval.isSuitableForCatalog) {
                    // Photo is already good!
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = eval.primaryMessageHindi,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = eval.primaryMessageEnglish,
                                    fontSize = 11.sp,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                } else if (eval.needsImprovement) {
                    // Needs Lighting or Background Cleanup
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = eval.primaryMessageHindi,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = eval.primaryMessageEnglish,
                                        fontSize = 10.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }

                            Button(
                                onClick = onImprovePhotoClick,
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("improve_photo_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("फ़ोटो सुधारें", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Section 2: Product Integrity Report (Authenticity guarantee)
            integrityReport?.let { report ->
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (report.isCraftPreserved) Color(0xFF16A34A) else Color(0xFFE11D48),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = report.hindiStatusBadge,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (report.isCraftPreserved) Color(0xFF166534) else Color(0xFF9F1239)
                            )
                        }

                        Text(
                            text = report.explanationHindi,
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )

                        Text(
                            text = "शिल्प प्रामाणिकता नीति: रंग, पैटर्न या सजावट में कोई कृत्रिम बदलाव नहीं किया गया है।",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
