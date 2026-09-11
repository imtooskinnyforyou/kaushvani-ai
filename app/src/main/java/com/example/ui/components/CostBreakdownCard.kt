package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CostBreakdownCard(
    rawMaterialCost: Double,
    onRawMaterialCostChange: (Double) -> Unit,
    laborHours: Double,
    onLaborHoursChange: (Double) -> Unit,
    hourlyWage: Double = 175.0,
    packagingCost: Double = 60.0,
    suggestedMinPrice: Double,
    suggestedMaxPrice: Double,
    wholesalePrice: Double,
    retailPrice: Double,
    modifier: Modifier = Modifier,
    onOpenDynamicPricingAssistant: (() -> Unit)? = null,
    onOpenPricingAdvisor: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(true) }

    val totalLaborEarning = laborHours * hourlyWage
    val totalProductionCost = rawMaterialCost + totalLaborEarning + packagingCost

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with AI Price Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MarigoldContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Cost Calculator",
                            tint = MarigoldTertiary,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "लागत और सही मूल्य (Cost & Fair Pricing)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "AI Living Wage & Market Benchmark",
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle"
                    )
                }
            }

            // Interactive Input Sliders/Fields
            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Raw Material Cost Row with Large Steppers & Quick Chips
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ArtisanSurfaceVariant)
                            .padding(12.dp)
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
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "1. कच्चा माल खर्च (Raw Material):",
                                    fontSize = 13.sp,
                                    color = ArtisanTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "₹${rawMaterialCost.toInt()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TerracottaPrimary
                            )
                        }

                        // Stepper buttons for zero-typing convenience
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    onRawMaterialCostChange((rawMaterialCost - 50.0).coerceAtLeast(20.0))
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "कच्चा माल खर्च ₹50 घटाएं (Decrease raw material cost by 50 rupees)"
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                            }

                            Slider(
                                value = rawMaterialCost.toFloat(),
                                onValueChange = { onRawMaterialCostChange(it.toDouble()) },
                                valueRange = 20f..5000f,
                                steps = 99,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = "कच्चा माल लागत स्लाइडर (Raw material cost slider)"
                                        stateDescription = "₹${rawMaterialCost.toInt()}"
                                    },
                                colors = SliderDefaults.colors(
                                    thumbColor = TerracottaPrimary,
                                    activeTrackColor = TerracottaPrimary
                                )
                            )

                            FilledTonalIconButton(
                                onClick = {
                                    onRawMaterialCostChange((rawMaterialCost + 50.0).coerceAtMost(5000.0))
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "कच्चा माल खर्च ₹50 बढ़ाएं (Increase raw material cost by 50 rupees)"
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            }
                        }

                        // Quick Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(100.0, 250.0, 500.0, 1000.0).forEach { preset ->
                                val isCur = rawMaterialCost.toInt() == preset.toInt()
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics {
                                            role = Role.Button
                                            selected = isCur
                                            contentDescription = "प्रीसेट कच्चा माल ₹${preset.toInt()} (Preset ₹${preset.toInt()})"
                                            stateDescription = if (isCur) "चयनित (Selected)" else "उपलब्ध (Available)"
                                        }
                                        .clickable { onRawMaterialCostChange(preset) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCur) TerracottaPrimary else ArtisanSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isCur) TerracottaPrimary else ArtisanCardBorder)
                                ) {
                                    Text(
                                        text = "₹${preset.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCur) Color.White else ArtisanTextPrimary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Labor Time in Hours with Steppers & Presets
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ArtisanSurfaceVariant)
                            .padding(12.dp)
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
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = IndigoSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "2. कारीगरी का समय (Craft Labor Time):",
                                    fontSize = 13.sp,
                                    color = ArtisanTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${String.format("%.1f", laborHours)} घंटे (hrs)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = IndigoSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    onLaborHoursChange((laborHours - 1.0).coerceAtLeast(0.5))
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "कारीगरी का समय 1 घंटा घटाएं (Decrease labor time by 1 hour)"
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                            }

                            Slider(
                                value = laborHours.toFloat(),
                                onValueChange = { onLaborHoursChange(it.toDouble()) },
                                valueRange = 0.5f..50f,
                                steps = 98,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = "कारीगरी समय स्लाइडर (Labor time slider)"
                                        stateDescription = "${String.format("%.1f", laborHours)} घंटे"
                                    },
                                colors = SliderDefaults.colors(
                                    thumbColor = IndigoSecondary,
                                    activeTrackColor = IndigoSecondary
                                )
                            )

                            FilledTonalIconButton(
                                onClick = {
                                    onLaborHoursChange((laborHours + 1.0).coerceAtMost(50.0))
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "कारीगरी का समय 1 घंटा बढ़ाएं (Increase labor time by 1 hour)"
                                    },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            }
                        }

                        // Quick Hours Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(2.0 to "2 घंटे", 4.0 to "4 घंटे", 8.0 to "1 दिन (8h)", 16.0 to "2 दिन (16h)").forEach { (hrs, label) ->
                                val isCur = laborHours.toInt() == hrs.toInt()
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics {
                                            role = Role.Button
                                            selected = isCur
                                            contentDescription = "समय प्रीसेट: $label (Labor preset: $label)"
                                            stateDescription = if (isCur) "चयनित (Selected)" else "उपलब्ध (Available)"
                                        }
                                        .clickable { onLaborHoursChange(hrs) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCur) IndigoSecondary else ArtisanSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isCur) IndigoSecondary else ArtisanCardBorder)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isCur) Color.White else ArtisanTextPrimary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Cost Breakup Summary Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = ForestContainer
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "शिल्पकार मेहनताना", fontSize = 10.sp, color = ForestSuccess, fontWeight = FontWeight.SemiBold)
                                Text(text = "₹${totalLaborEarning.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                                Text(text = "(@₹${hourlyWage.toInt()}/hr)", fontSize = 9.sp, color = ForestSuccess)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = ArtisanSurfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "कुल मूल लागत", fontSize = 10.sp, color = ArtisanTextSecondary, fontWeight = FontWeight.SemiBold)
                                Text(text = "₹${totalProductionCost.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                                Text(text = "(सामान + मजदूरी)", fontSize = 9.sp, color = ArtisanTextMuted)
                            }
                        }
                    }

                    HorizontalDivider(color = ArtisanCardBorder)

                    // Recommended Price Highlights (B2B vs B2C)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Wholesale Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(IndigoSecondaryContainer.copy(alpha = 0.5f))
                                .border(1.dp, IndigoSecondary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = "B2B",
                                        tint = IndigoSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "B2B थोक मूल्य",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${wholesalePrice.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = IndigoSecondary
                                )
                                Text(
                                    text = "न्यूनतम 10 पीस पर",
                                    fontSize = 10.sp,
                                    color = IndigoSecondary.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Retail Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(TerracottaContainer)
                                .border(1.dp, TerracottaPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = "Retail",
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "खुदरा मूल्य (Retail)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${retailPrice.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TerracottaPrimary
                                )
                                Text(
                                    text = "सीमा: ₹${suggestedMinPrice.toInt()} - ₹${suggestedMaxPrice.toInt()}",
                                    fontSize = 10.sp,
                                    color = TerracottaDark
                                )
                            }
                        }
                    }

                    // Transparent Pricing Assumptions Explainer Card (Never hide assumptions)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MarigoldTertiary.copy(alpha = 0.5f))
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
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = MarigoldTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "मूल्य पारदर्शिता (Fair Pricing Formula):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                            }

                            Text(
                                text = "• न्यूनतम कारीगर दर: ₹$hourlyWage/घंटा (Fair Living Wage मानक)\n• कुल आधार लागत = कच्चा माल (₹${rawMaterialCost.toInt()}) + मेहनत (₹${totalLaborEarning.toInt()}) + पैकेजिंग (₹${packagingCost.toInt()}) = ₹${totalProductionCost.toInt()}\n• थोक मूल्य (+20% मार्जिन) = ₹${wholesalePrice.toInt()} (बल्क खरीदार के लिए)\n• खुदरा मूल्य (+40% मार्जिन) = ₹${retailPrice.toInt()} (सीधे ग्राहक के लिए)",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary,
                                lineHeight = 16.sp
                            )

                            Text(
                                text = "💡 यह गणना सुनिश्चित करती है कि किसी भी बिचौलिए द्वारा कारीगर के पारिश्रमिक का शोषण न हो।",
                                fontSize = 10.sp,
                                color = ForestSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Open Room Database Pricing Advisor Button
                    if (onOpenPricingAdvisor != null) {
                        OutlinedButton(
                            onClick = onOpenPricingAdvisor,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, TerracottaPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriceCheck,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "समान शिल्पों से मूल्य सलाह (Pricing Advisor)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        }
                    }

                    // Open Full Dynamic Pricing Assistant Button
                    if (onOpenDynamicPricingAssistant != null) {
                        Button(
                            onClick = onOpenDynamicPricingAssistant,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "गहन मूल्य सहायक खोलें (Dynamic Pricing Assistant)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
