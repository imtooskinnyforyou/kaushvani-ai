package com.example.ui.components

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
import com.example.ui.viewmodel.ProductHistoryPricingViewModel
import kotlin.math.roundToInt

@Composable
fun ProductHistoryPricingView(
    viewModel: ProductHistoryPricingViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val selectedProductId by viewModel.selectedProductId.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val historyList by viewModel.productHistoryList.collectAsStateWithLifecycle()
    val metrics by viewModel.historicalMetrics.collectAsStateWithLifecycle()
    val currentMaterialCost by viewModel.currentMaterialCostInput.collectAsStateWithLifecycle()
    val currentLaborHours by viewModel.currentLaborHoursInput.collectAsStateWithLifecycle()
    val selectedSeason by viewModel.selectedSeason.collectAsStateWithLifecycle()
    val selectedChannel by viewModel.selectedChannel.collectAsStateWithLifecycle()
    val isAiAnalyzing by viewModel.isAiAnalyzing.collectAsStateWithLifecycle()
    val recommendation by viewModel.pricingRecommendation.collectAsStateWithLifecycle()
    val notification by viewModel.actionNotification.collectAsStateWithLifecycle()

    var showAddSaleDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("product_history_pricing_container"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Banner
        item {
            if (!notification.isNullOrBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TerracottaContainer),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TerracottaPrimary)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pricing_notification_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notification ?: "",
                            fontSize = 13.sp,
                            color = ArtisanTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.dismissNotification() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ArtisanTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 1. Product Selector Carousel
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "उत्पाद चुनें (Select Craft Product)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    Text(
                        text = "${products.size} उत्पाद उपलब्ध",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("product_selector_row")
                ) {
                    items(products, key = { it.id }) { product ->
                        val isSelected = product.id == selectedProductId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) TerracottaPrimary else ArtisanSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) TerracottaPrimary else ArtisanCardBorder
                            ),
                            modifier = Modifier
                                .clickable { viewModel.selectProduct(product.id) }
                                .testTag("product_chip_${product.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = product.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else ArtisanTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 160.dp)
                                )
                                Text(
                                    text = "₹${product.retailPrice.toInt()} • ${product.category}",
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else ArtisanTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Historical Sales & Material Cost Overview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder),
                modifier = Modifier.fillMaxWidth().testTag("historical_metrics_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ऐतिहासिक रुझान (Historical Sales & Cost)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                        }

                        Button(
                            onClick = { showAddSaleDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_sale_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "बिक्री जोड़ें", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Grid (2x2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricSmallBox(
                            title = "कुल बिक्री मात्रा",
                            subtitle = "Total Volume Sold",
                            value = "${metrics.totalUnitsSold} पीस",
                            modifier = Modifier.weight(1f)
                        )
                        MetricSmallBox(
                            title = "औसत बिक्री मूल्य",
                            subtitle = "Avg Realized Price",
                            value = "₹${metrics.averageSellingPrice.roundToInt()}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val inflation = metrics.materialCostInflationPercent
                        val inflationColor = if (inflation > 10.0) TerracottaPrimary else ForestSuccess
                        val inflationSign = if (inflation > 0) "+${inflation.roundToInt()}%" else "${inflation.roundToInt()}%"

                        MetricSmallBox(
                            title = "कच्चा माल लागत प्रभाव",
                            subtitle = "Material Inflation",
                            value = inflationSign,
                            valueColor = inflationColor,
                            modifier = Modifier.weight(1f)
                        )
                        MetricSmallBox(
                            title = "शीर्ष मांग मौसम",
                            subtitle = "Peak Demand Phase",
                            value = metrics.highestDemandSeason.take(16),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (metrics.elasticitySummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = TerracottaContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = metrics.elasticitySummary,
                                    fontSize = 12.sp,
                                    color = ArtisanTextPrimary
                                )
                            }
                        }
                    }

                    // Historical Sales Records List
                    if (historyList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "विगत बिक्री अभिलेख (Recent Sales History):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        historyList.take(4).forEach { item ->
                            HistoryRowItem(item = item)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // 3. Dynamic Pricing Market Controls (Material Cost, Season, Channel)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder),
                modifier = Modifier.fillMaxWidth().testTag("pricing_controls_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "वर्तमान लागत एवं बाजार कारक (Market & Cost Inputs)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Material Cost Slider/Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "कच्चा माल लागत (Raw Material Cost)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "धागा, धातु, मिट्टी, रंग आदि", fontSize = 11.sp, color = ArtisanTextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.updateMaterialCost(currentMaterialCost - 50.0) },
                                modifier = Modifier.size(32.dp).testTag("decrease_material_cost_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TerracottaPrimary)
                            }
                            Text(
                                text = "₹${currentMaterialCost.roundToInt()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { viewModel.updateMaterialCost(currentMaterialCost + 50.0) },
                                modifier = Modifier.size(32.dp).testTag("increase_material_cost_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = TerracottaPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ArtisanCardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Labor Hours Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "कारीगरी श्रम (Labor Hours)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "न्यूनतम ₹180/घंटा आजीविका सुरक्षा", fontSize = 11.sp, color = ArtisanTextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.updateLaborHours(currentLaborHours - 1.0) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TerracottaPrimary)
                            }
                            Text(
                                text = "${currentLaborHours}h",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { viewModel.updateLaborHours(currentLaborHours + 1.0) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = TerracottaPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Season Selector
                    Text(
                        text = "मौसमी मांग कारक (Seasonal Demand):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ArtisanTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PricingSeason.values().forEach { season ->
                            val isSelected = season == selectedSeason
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateSeason(season) },
                                label = {
                                    Text(
                                        text = season.hindiName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f).testTag("season_chip_${season.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Channel Selector
                    Text(
                        text = "बिक्री माध्यम (Sales Channel):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ArtisanTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChannelType.values().forEach { channel ->
                            val isSelected = channel == selectedChannel
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateChannel(channel) },
                                label = {
                                    Text(
                                        text = channel.hindiName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MarigoldTertiary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f).testTag("channel_chip_${channel.name}")
                            )
                        }
                    }
                }
            }
        }

        // 4. AI Dynamic Pricing Recommendation Hero Card
        item {
            if (isAiAnalyzing) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = TerracottaPrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "AI ऐतिहासिक डेटा एवं लागत विश्लेषण कर रहा है...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ArtisanTextPrimary
                        )
                    }
                }
            } else if (recommendation != null) {
                val rec = recommendation!!
                Card(
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TerracottaPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("ai_dynamic_pricing_hero_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TerracottaContainer
                            ) {
                                Text(
                                    text = rec.pricingStrategyTag,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MarigoldTertiary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Gemini AI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MarigoldTertiary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Price Hero
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "अनुशंसित खुदरा मूल्य (Retail MRP)",
                                    fontSize = 12.sp,
                                    color = ArtisanTextSecondary
                                )
                                Text(
                                    text = "₹${rec.recommendedRetailPrice.toInt()}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TerracottaPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "थोक दर (B2B Wholesale)",
                                    fontSize = 12.sp,
                                    color = ArtisanTextSecondary
                                )
                                Text(
                                    text = "₹${rec.recommendedWholesalePrice.toInt()}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ArtisanCardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Living Wage Floor Price Badge (Security Floor)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F8E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5E1A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = ForestSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "न्यूनतम आजीविका सुरक्षा सीमा (Fair Wage Floor): ₹${rec.fairLivingWageFloorPrice.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestSuccess
                                    )
                                    Text(
                                        text = "इस दर से नीचे कभी न बेचें ताकि ₹180/घंटे का पारिश्रमिक सुरक्षित रहे।",
                                        fontSize = 11.sp,
                                        color = ArtisanTextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Reasoning Box
                        Text(
                            text = "AI तर्क एवं मार्गदर्शन (Economic Reasoning):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rec.justificationHindi,
                            fontSize = 13.sp,
                            color = ArtisanTextPrimary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rec.justificationEnglish,
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action: Apply Price to Product
                        Button(
                            onClick = { viewModel.applyDynamicPricingToProduct() },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("apply_dynamic_price_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "उत्पाद पर यह मूल्य लागू करें (Apply to Catalog)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog to Record a Past or Offline Sale into Room DB
    if (showAddSaleDialog) {
        AddSaleRecordDialog(
            productTitle = selectedProduct?.title ?: "",
            currentMaterialCost = currentMaterialCost,
            onDismiss = { showAddSaleDialog = false },
            onSave = { units, price, channel, notes ->
                viewModel.recordNewSale(units, price, channel, notes)
                showAddSaleDialog = false
            }
        )
    }
}

@Composable
private fun MetricSmallBox(
    title: String,
    subtitle: String,
    value: String,
    valueColor: Color = ArtisanTextPrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ArtisanBackground,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = ArtisanTextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(text = subtitle, fontSize = 10.sp, color = ArtisanTextSecondary, maxLines = 1)
        }
    }
}

@Composable
private fun HistoryRowItem(item: ProductHistoryEntity) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = ArtisanBackground,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ArtisanCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.salePeriod,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArtisanTextPrimary
                )
                Text(
                    text = "${item.salesChannel} • लागत: ₹${item.materialCost.toInt()}",
                    fontSize = 11.sp,
                    color = ArtisanTextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${item.sellingPrice.toInt()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaPrimary
                )
                Text(
                    text = "${item.unitsSold} पीस",
                    fontSize = 11.sp,
                    color = ForestSuccess,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AddSaleRecordDialog(
    productTitle: String,
    currentMaterialCost: Double,
    onDismiss: () -> Unit,
    onSave: (units: Int, price: Double, channel: String, notes: String) -> Unit
) {
    var unitsText by remember { mutableStateOf("10") }
    var priceText by remember { mutableStateOf("1500") }
    var channelText by remember { mutableStateOf("Direct Craft Fair") }
    var notesText by remember { mutableStateOf("Craft haat exhibition customer sales") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "नई बिक्री दर्ज करें (Record Sale)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = productTitle, fontSize = 12.sp, color = ArtisanTextSecondary)

                OutlinedTextField(
                    value = unitsText,
                    onValueChange = { unitsText = it },
                    label = { Text("बेची गई इकाइयाँ (Units Sold)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_sale_units")
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("बिक्री मूल्य प्रति इकाई (Selling Price ₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_sale_price")
                )

                OutlinedTextField(
                    value = channelText,
                    onValueChange = { channelText = it },
                    label = { Text("बिक्री माध्यम (Channel e.g. Haat, B2B)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("टिप्पणी (Notes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val units = unitsText.toIntOrNull() ?: 1
                    val price = priceText.toDoubleOrNull() ?: 1000.0
                    onSave(units, price, channelText, notesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                modifier = Modifier.testTag("confirm_save_sale_button")
            ) {
                Text("सुरक्षित करें (Save)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}
