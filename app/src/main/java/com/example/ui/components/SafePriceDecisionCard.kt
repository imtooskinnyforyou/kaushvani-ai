package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.pricing.CostComponentBreakdown
import com.example.data.pricing.PriceCalculationResult
import com.example.data.pricing.PricingConfidence
import com.example.ui.theme.*

/**
 * Transparent, Safety-First Price Decision Calculator UI.
 *
 * Displays:
 * - Base Cost: ₹X (Material + Labour + Packaging)
 * - Fair Price Range: ₹X–₹Y
 * - Suggested Price: ₹Z (fully editable by artisan)
 * - Confidence: Low / Medium / High
 * - "Why this price?" itemized breakdown
 * - Safety alert if abnormal inputs detected (asks artisan to confirm)
 * - Clear disclaimer when market comparison is unavailable
 */
@Composable
fun SafePriceDecisionCard(
    calculationResult: PriceCalculationResult,
    artisanSellingPrice: Double,
    onArtisanPriceChange: (Double) -> Unit,
    onConfirmSuspiciousInputs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showBreakdownDetails by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("safe_price_decision_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, if (calculationResult.safetyCheck.isSuspicious) Color(0xFFE11D48) else TerracottaPrimary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Confidence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = TerracottaContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "पारदर्शी मूल्य निर्धारण (Fair Price Calculator)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "नियम-आधारित सुरक्षित गणना • लागत से कम कभी नहीं",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                // Confidence badge
                val (confBg, confFg, confText) = when (calculationResult.confidence) {
                    PricingConfidence.HIGH -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "उच्च (High)")
                    PricingConfidence.MEDIUM -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "मध्यम (Medium)")
                    PricingConfidence.LOW -> Triple(Color(0xFFFFE4E6), Color(0xFFE11D48), "कम (Low)")
                }

                Surface(
                    color = confBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "विश्वास: $confText",
                        color = confFg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // SAFETY WARNING (If abnormal input detected)
            if (calculationResult.safetyCheck.isSuspicious) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pricing_safety_warning_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFF43F5E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Safety Alert",
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "⚠️ असामान्य इनपुट का पता चला (Unusual Input Detected)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9F1239)
                            )
                        }

                        Text(
                            text = calculationResult.safetyCheck.hindiWarningMessage
                                ?: calculationResult.safetyCheck.warningMessage.orEmpty(),
                            fontSize = 11.sp,
                            color = Color(0xFF881337)
                        )

                        Text(
                            text = calculationResult.safetyCheck.warningMessage.orEmpty(),
                            fontSize = 10.sp,
                            color = Color(0xFF9F1239)
                        )

                        OutlinedButton(
                            onClick = onConfirmSuspiciousInputs,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE11D48)),
                            border = BorderStroke(1.dp, Color(0xFFE11D48)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("हाँ, यह इनपुट सही है (Confirm)", fontSize = 11.sp)
                        }
                    }
                }
            }

            // 3-BOX SUMMARY: Base Cost, Fair Range, Suggested Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // BOX 1: Base Cost Floor
                Surface(
                    modifier = Modifier.weight(1f),
                    color = ArtisanSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ArtisanCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "कुल मूल लागत",
                            fontSize = 10.sp,
                            color = ArtisanTextSecondary
                        )
                        Text(
                            text = "Base Cost",
                            fontSize = 9.sp,
                            color = ArtisanTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${calculationResult.baseCost.toInt()}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "लागत तल (Floor)",
                            fontSize = 9.sp,
                            color = ForestSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // BOX 2: Fair Price Range
                Surface(
                    modifier = Modifier.weight(1.2f),
                    color = Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ForestSuccess.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "उचित मूल्य सीमा",
                            fontSize = 10.sp,
                            color = ForestSuccess
                        )
                        Text(
                            text = "Fair Price Range",
                            fontSize = 9.sp,
                            color = ForestSuccess
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${calculationResult.minFairPrice.toInt()} – ₹${calculationResult.maxFairPrice.toInt()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestSuccess
                        )
                        Text(
                            text = "15% - 45% मार्जिन",
                            fontSize = 9.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                // BOX 3: Suggested Price
                Surface(
                    modifier = Modifier.weight(1f),
                    color = TerracottaContainer,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "सुझाया गया मूल्य",
                            fontSize = 10.sp,
                            color = TerracottaPrimary
                        )
                        Text(
                            text = "Suggested",
                            fontSize = 9.sp,
                            color = TerracottaPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${calculationResult.suggestedPrice.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                        Text(
                            text = "+${calculationResult.configurableMarginPercent.toInt()}% संतुलित",
                            fontSize = 9.sp,
                            color = TerracottaPrimary
                        )
                    }
                }
            }

            // ARTISAN MANUAL SELLING PRICE ADJUSTMENT
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "आपका अंतिम बिक्री मूल्य (Your Final Selling Price):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    Text(
                        text = "मनचाहा दाम बदलें",
                        fontSize = 10.sp,
                        color = TerracottaPrimary
                    )
                }

                OutlinedTextField(
                    value = if (artisanSellingPrice > 0) artisanSellingPrice.toInt().toString() else "",
                    onValueChange = { str ->
                        val parsed = str.toDoubleOrNull() ?: 0.0
                        onArtisanPriceChange(parsed)
                    },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepNavy) },
                    suffix = {
                        if (artisanSellingPrice < calculationResult.baseCost && artisanSellingPrice > 0) {
                            Text(
                                text = "⚠️ लागत से कम!",
                                color = Color(0xFFE11D48),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (artisanSellingPrice >= calculationResult.baseCost) {
                            Text(
                                text = "✓ सुरक्षित लाभ",
                                color = ForestSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("artisan_final_selling_price_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (artisanSellingPrice < calculationResult.baseCost && artisanSellingPrice > 0) Color(0xFFE11D48) else ForestSuccess,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )

                if (artisanSellingPrice < calculationResult.baseCost && artisanSellingPrice > 0) {
                    Text(
                        text = "सावधानी: यह दाम आपकी कुल मूल लागत (₹${calculationResult.baseCost.toInt()}) से कम है। इससे आपको नुकसान होगा।",
                        fontSize = 10.sp,
                        color = Color(0xFFE11D48),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // MARKET STATUS DISCLOSURE
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = calculationResult.hindiMarketDataStatus + "\n" + calculationResult.marketDataStatus,
                        fontSize = 10.sp,
                        color = Color(0xFF475569),
                        lineHeight = 14.sp
                    )
                }
            }

            // "WHY THIS PRICE?" BREAKDOWN (Expandable)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBreakdownDetails = !showBreakdownDetails }
                    .testTag("why_this_price_toggle"),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = DeepNavy,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "यह दाम क्यों? विस्तृत विवरण देखें (Why this price?)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                    }
                    Icon(
                        imageVector = if (showBreakdownDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = DeepNavy,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showBreakdownDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    calculationResult.explanationBreakdown.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.hindiTitle} (${item.title})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = item.hindiExplanation,
                                    fontSize = 9.sp,
                                    color = ArtisanTextSecondary
                                )
                            }
                            Text(
                                text = "₹${item.amount.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)
                    }
                }
            }
        }
    }
}
