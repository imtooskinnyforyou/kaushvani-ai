package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.d3.D3AnalyticsWebView
import com.example.ui.theme.*
import com.example.ui.viewmodel.AnalyticsTimeFilter
import com.example.ui.viewmodel.ArtisanSalesAnalyticsViewModel
import com.example.ui.viewmodel.HunarSetuViewModel
import com.example.ui.viewmodel.AppNavTab
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanSalesAnalyticsDashboardScreen(
    viewModel: HunarSetuViewModel,
    analyticsViewModel: ArtisanSalesAnalyticsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = { viewModel.navigateTo(AppNavTab.ARTISAN_HOME) }
) {
    val context = LocalContext.current
    val uiState by analyticsViewModel.uiState.collectAsStateWithLifecycle()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("artisan_sales_analytics_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sales & Pricing Analytics",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "बिक्री, श्रेणी वितरण व मूल्य रुझान (D3)",
                            fontSize = 11.sp,
                            color = TerracottaPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("analytics_back_button")
                            .semantics { contentDescription = "पीछे जाएं (Go back to dashboard)" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ArtisanTextPrimary
                        )
                    }
                },
                actions = {
                    // Audio Narration Button
                    IconButton(
                        onClick = {
                            viewModel.speakText(
                                text = uiState.aiNarrativeSummary,
                                languageCode = "hi"
                            )
                        },
                        modifier = Modifier
                            .testTag("analytics_audio_button")
                            .semantics { contentDescription = "बिक्री विश्लेषण व रुझान सारांश सुनें (Listen to Analytics Audio Summary)" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen",
                            tint = TerracottaPrimary
                        )
                    }

                    // Share Report Button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "हुनरसेतु शिल्पकार बिक्री रिपोर्ट")
                                putExtra(Intent.EXTRA_TEXT, analyticsViewModel.getExportableSummaryText())
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "बिक्री रिपोर्ट साझा करें"))
                        },
                        modifier = Modifier
                            .testTag("analytics_share_button")
                            .semantics { contentDescription = "बिक्री रिपोर्ट शेयर करें (Share Analytics Report)" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = IndigoSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArtisanSurface
                )
            )
        },
        containerColor = ArtisanBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Time Filter Selector Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "अवधि:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextSecondary
                    )
                    AnalyticsTimeFilter.values().forEach { filter ->
                        val isSelected = uiState.selectedTimeFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { analyticsViewModel.setTimeFilter(filter) },
                            label = {
                                Text(
                                    text = "${filter.hindiLabel} (${filter.label})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = ArtisanSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) TerracottaPrimary else ArtisanCardBorder
                            ),
                            modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                        )
                    }
                }
            }

            // 2. High-Impact KPI Metrics Row
            item {
                val summary = uiState.data.summaryMetrics
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiSummaryCard(
                            title = "Total Sales Revenue",
                            hindiTitle = "कुल संचित राजस्व",
                            value = currencyFormatter.format(summary.totalGrossRevenue),
                            subText = "+${summary.monthlyRevenueGrowthPercent}% मासिक वृद्धि",
                            icon = Icons.Default.AccountBalanceWallet,
                            accentColor = ForestSuccess,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_total_revenue"
                        )

                        KpiSummaryCard(
                            title = "Units Sold",
                            hindiTitle = "कुल बिकी इकाइयाँ",
                            value = "${summary.totalUnitsSold} पीस",
                            subText = "औसत ₹${summary.averageOrderValue.toInt()}/पीस",
                            icon = Icons.Default.Inventory2,
                            accentColor = TerracottaPrimary,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_units_sold"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiSummaryCard(
                            title = "Fair Wage Protection",
                            hindiTitle = "उचित मजदूरी अनुपालन",
                            value = "${summary.fairLivingWageComplianceRate}%",
                            subText = "जीवन-यापन न्यूनतम मजदूरी सुरक्षित",
                            icon = Icons.Default.Verified,
                            accentColor = ForestSuccess,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_fair_wage_protection"
                        )

                        KpiSummaryCard(
                            title = "Average Profit Margin",
                            hindiTitle = "औसत लाभ मार्जिन",
                            value = "${summary.averageProfitMarginPercent}%",
                            subText = "कच्चा माल व श्रम काटकर",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            accentColor = IndigoSecondary,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_profit_margin"
                        )
                    }
                }
            }

            // 3. AI Insights Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_insights_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaContainer.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TerracottaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI शिल्पकार अंतर्दृष्टि (Living Wage & Market Insight)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.aiNarrativeSummary,
                                fontSize = 12.sp,
                                color = ArtisanTextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 4. Interactive D3 Visualizer WebView (Host 3 D3 Charts)
            item {
                Text(
                    text = "D3 इंटरएक्टिव चार्ट विज़ुअलाइज़ेशन (D3.js Charts)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary,
                    modifier = Modifier.padding(top = 6.dp)
                )

                D3AnalyticsWebView(
                    data = uiState.data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    onChartItemClicked = { type, id, details ->
                        analyticsViewModel.handleChartItemClicked(type, id, details)
                    }
                )
            }

            // 5. Bottom Action: Jump to AI Dynamic Pricing Assistant
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            role = androidx.compose.ui.semantics.Role.Button
                            contentDescription = "उचित मूल्य कैलकुलेटर खोलें (Open AI Dynamic Pricing Assistant)"
                        }
                        .clickable { viewModel.openDynamicPricingAssistant() }
                        .testTag("goto_pricing_assistant_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestSuccess.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                    .background(ForestContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PriceCheck,
                                    contentDescription = null,
                                    tint = ForestSuccess,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "AI Dynamic Fair Pricing Assistant",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "लागत, समय और कच्चा माल मुद्रास्फीति अनुसार नया मूल्य तय करें",
                                    fontSize = 11.sp,
                                    color = ArtisanTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = ForestSuccess
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiSummaryCard(
    title: String,
    hindiTitle: String,
    value: String,
    subText: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = hindiTitle,
                    fontSize = 10.sp,
                    color = ArtisanTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ArtisanTextPrimary
            )

            Text(
                text = subText,
                fontSize = 10.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
