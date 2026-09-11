package com.example.ui.components.pricing

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PricingAdvisorUiState
import com.example.ui.viewmodel.PricingAdvisorViewModel
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

private val WarmTerracotta = Color(0xFFC85A32)
private val ForestGreen = Color(0xFF2E7D32)
private val ForestGreenContainer = Color(0xFFE8F5E9)
private val DeepIndigo = Color(0xFF283593)
private val IndigoContainer = Color(0xFFE8EAF6)
private val AmberGold = Color(0xFFD97706)
private val AmberContainer = Color(0xFFFEF3C7)

/**
 * Reusable 'Pricing Advisor' component that leverages Room database history
 * and material costs to suggest competitive price ranges for new products
 * based on similar items in the catalog.
 */
@Composable
fun PricingAdvisorComponent(
    viewModel: PricingAdvisorViewModel,
    modifier: Modifier = Modifier,
    onPriceApplied: ((Double) -> Unit)? = null,
    onSpeakText: ((String, String) -> Unit)? = null,
    showTitleHeader: Boolean = true
) {
    val input by viewModel.inputState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTierType by viewModel.selectedTierType.collectAsStateWithLifecycle()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN")) }

    var showPeersExpanded by remember { mutableStateOf(false) }
    var showCostBreakdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pricing_advisor_component"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            if (showTitleHeader) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = WarmTerracotta.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PriceCheck,
                                    contentDescription = "Pricing Advisor",
                                    tint = WarmTerracotta,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "मूल्य सलाहकार (Pricing Advisor)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = IndigoContainer
                                ) {
                                    Text(
                                        text = "Room DB",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "कैटलॉग समकक्ष शिल्पों व लागत पर आधारित प्रतिस्पर्धी मूल्य सीमा",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (onSpeakText != null && uiState is PricingAdvisorUiState.Success) {
                        val result = (uiState as PricingAdvisorUiState.Success).result
                        IconButton(
                            onClick = {
                                onSpeakText(result.marketInsightsHindi, "hi")
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_listen_pricing_advice")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen to Advice",
                                tint = WarmTerracotta
                            )
                        }
                    }
                }
            }

            // Input Parameters Card (Material Cost, Labor Hours, Category)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. नए उत्पाद के लागत घटक (Cost Factors)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Material Cost Input + Quick Adjust Steppers
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "कच्चा माल लागत (Raw Material Cost):",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${input.materialCost.toInt()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WarmTerracotta
                            )
                        }

                        // Quick buttons to adjust material cost
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(50.0, 100.0, 250.0, 500.0).forEach { amt ->
                                OutlinedButton(
                                    onClick = { viewModel.updateMaterialCost(amt) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = if (input.materialCost == amt) {
                                        ButtonDefaults.outlinedButtonColors(containerColor = WarmTerracotta.copy(alpha = 0.15f))
                                    } else {
                                        ButtonDefaults.outlinedButtonColors()
                                    }
                                ) {
                                    Text("₹${amt.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Labor Hours Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "श्रम के घंटे (Crafting Labor):",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${input.laborHours} घंटे (₹${(input.laborHours * input.hourlyWageRate).toInt()} @ ₹${input.hourlyWageRate.toInt()}/घं.)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }

                        Slider(
                            value = input.laborHours.toFloat(),
                            onValueChange = { viewModel.updateLaborHours((it * 2).roundToInt() / 2.0) },
                            valueRange = 0.5f..24.0f,
                            steps = 47,
                            modifier = Modifier.testTag("slider_labor_hours"),
                            colors = SliderDefaults.colors(
                                thumbColor = WarmTerracotta,
                                activeTrackColor = WarmTerracotta
                            )
                        )
                    }

                    // Category / Craft Type and GI Tag selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp), tint = WarmTerracotta)
                                Text(
                                    text = "${input.category} • ${input.craftType.ifBlank { "सामान्य" }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleGiTag(!input.isGiTagged) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = input.isGiTagged,
                                onCheckedChange = { viewModel.toggleGiTag(it) },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GI Tag (+18% प्रीमियम)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (input.isGiTagged) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Results Section
            when (val state = uiState) {
                is PricingAdvisorUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = WarmTerracotta, modifier = Modifier.size(36.dp))
                            Text(
                                text = "Room डेटाबेस से समान शिल्पों का विश्लेषण हो रहा है...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is PricingAdvisorUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(text = state.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                is PricingAdvisorUiState.Success -> {
                    val result = state.result

                    // 2. Room Database Peers Evidence Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IndigoContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPeersExpanded = !showPeersExpanded }
                            .testTag("pricing_peer_evidence_card")
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Storage, contentDescription = null, tint = DeepIndigo, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Room डेटाबेस समकक्ष शिल्प (${result.peerCount} मिले)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepIndigo
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (result.peerAverageRetailPrice > 0) {
                                        Text(
                                            text = "औसत: ₹${result.peerAverageRetailPrice.toInt()}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepIndigo
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showPeersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand Peers",
                                        tint = DeepIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (!showPeersExpanded && result.similarProducts.isNotEmpty()) {
                                Text(
                                    text = result.similarProducts.take(2).joinToString(" • ") { "${it.title} (₹${it.retailPrice.toInt()})" },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            AnimatedVisibility(visible = showPeersExpanded) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (result.similarProducts.isEmpty()) {
                                        Text(
                                            text = "कैटलॉग में इस श्रेणी के उत्पाद अभी कम हैं, मानक शिल्प मॉडलों के अनुसार गणना की गई।",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        result.similarProducts.forEach { peer ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                border = CardDefaults.outlinedCardBorder(),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = peer.title,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = peer.similarityReason,
                                                            fontSize = 11.sp,
                                                            color = ForestGreen
                                                        )
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = "₹${peer.retailPrice.toInt()}",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = WarmTerracotta
                                                        )
                                                        if (peer.unitsSold > 0) {
                                                            Text(
                                                                text = "${peer.unitsSold} बिके",
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Four Competitive Price Tiers Cards
                    Text(
                        text = "2. प्रतिस्पर्धी मूल्य श्रेणियां (Select Competitive Price):",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        result.tiers.forEach { tier ->
                            val isSelected = tier.tierType == selectedTierType
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.selectTier(tier.tierType) }
                                    .testTag("pricing_tier_${tier.tierType.name.lowercase()}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        WarmTerracotta.copy(alpha = 0.08f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSelected) WarmTerracotta else ArtisanCardBorder
                                    )
                                )
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { viewModel.selectTier(tier.tierType) },
                                                colors = RadioButtonDefaults.colors(selectedColor = WarmTerracotta)
                                            )
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = tier.hindiTitle,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (tier.isRecommended) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = ForestGreenContainer
                                                        ) {
                                                            Text(
                                                                text = "सुझावित (Best)",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = ForestGreen,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = tier.title,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "₹${tier.recommendedPrice.toInt()}",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSelected) WarmTerracotta else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "सीमा: ₹${tier.minPrice.toInt()} - ₹${tier.maxPrice.toInt()}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Metrics strip: Margin % and Artisan Net Profit
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) WarmTerracotta.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "लाभ: +${tier.profitMarginPercent.toInt()}% (₹${tier.netArtisanEarnings.toInt()} कुल कमाई)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ForestGreen
                                        )
                                        Text(
                                            text = "₹${tier.hourlyReturn.toInt()}/घंटा कारीगर दर",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = tier.hindiDescription,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 4. Living Wage Guarantee & Cost Breakdown Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ForestGreenContainer.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCostBreakdownExpanded = !showCostBreakdownExpanded }
                            .testTag("pricing_living_wage_banner")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "कारीगर न्यूनतम आजीविका सुरक्षा (100% Guaranteed)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                }
                                Icon(
                                    imageVector = if (showCostBreakdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand Breakdown",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = "लागत फ़्लोर: ₹${result.totalCostFloor.toInt()} • कोई भी मूल्य इस सीमा से नीचे नहीं गिरेगा",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            AnimatedVisibility(visible = showCostBreakdownExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "• कच्चा माल: ₹${result.materialCost.toInt()} (${result.materialCostPercentOfRetail.toInt()}% of Retail)",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "• कारीगर श्रम: ₹${result.directLaborCost.toInt()} (${result.laborHours} घंटे @ ₹${result.hourlyWageRate.toInt()}/घंटा)",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "• पैकेजिंग व सुरक्षा: ₹${result.packagingCost.toInt()}",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "• कुल आधारभूत लागत: ₹${(result.materialCost + result.directLaborCost + result.packagingCost).toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                }
                            }
                        }
                    }

                    // 5. Narrative Explanation & Spoken Advice Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "सलाहकार विश्लेषण (Advisor Insight):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Text(
                                text = result.marketInsightsHindi,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 6. Action: Apply Price Button
                    val chosenTier = result.tiers.find { it.tierType == selectedTierType } ?: result.recommendedTier
                    Button(
                        onClick = {
                            val price = viewModel.applySelectedTierPrice()
                            if (price != null && onPriceApplied != null) {
                                onPriceApplied(price)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmTerracotta),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_apply_pricing_advisor_price")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "यह मूल्य चुनें (Apply ₹${chosenTier.recommendedPrice.toInt()})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom Sheet modal wrapper for the Pricing Advisor.
 * Allows artisans to open pricing advice from any product form, wizard, or detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingAdvisorBottomSheet(
    viewModel: PricingAdvisorViewModel,
    onDismissRequest: () -> Unit,
    onPriceApplied: (Double) -> Unit,
    onSpeakText: ((String, String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxHeight(0.88f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "शिल्प मूल्य सलाहकार",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    PricingAdvisorComponent(
                        viewModel = viewModel,
                        onPriceApplied = { price ->
                            onPriceApplied(price)
                            onDismissRequest()
                        },
                        onSpeakText = onSpeakText,
                        showTitleHeader = false
                    )
                }
            }
        }
    }
}
