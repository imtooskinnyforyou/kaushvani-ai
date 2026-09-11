package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.model.ArtisanSkillTier
import com.example.data.ai.model.CostSliceItem
import com.example.data.ai.model.DynamicPricingResponse
import com.example.data.ai.model.TagPriceImpact
import com.example.data.ai.model.TargetMarketChannel
import com.example.ui.components.ProductHistoryPricingView
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel
import com.example.ui.viewmodel.ProductHistoryPricingViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicPricingAssistantScreen(
    viewModel: HunarSetuViewModel,
    historyPricingViewModel: ProductHistoryPricingViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAssistantTab by remember { mutableIntStateOf(0) }
    val materialCost by viewModel.pricingMaterialCost.collectAsStateWithLifecycle()
    val laborHours by viewModel.pricingLaborHours.collectAsStateWithLifecycle()
    val packagingCost by viewModel.pricingPackagingCost.collectAsStateWithLifecycle()
    val category by viewModel.pricingCategory.collectAsStateWithLifecycle()
    val skillTier by viewModel.pricingSkillTier.collectAsStateWithLifecycle()
    val targetMarket by viewModel.pricingTargetMarket.collectAsStateWithLifecycle()
    val selectedTags by viewModel.pricingSelectedTags.collectAsStateWithLifecycle()
    val customTagInput by viewModel.pricingCustomTagInput.collectAsStateWithLifecycle()
    val isCalculating by viewModel.isPricingCalculating.collectAsStateWithLifecycle()
    val recommendation by viewModel.pricingRecommendation.collectAsStateWithLifecycle()
    val sourceTitle by viewModel.pricingSourceProductTitle.collectAsStateWithLifecycle()

    val totalLaborEarning = laborHours * skillTier.hourlyWage
    val totalProductionCost = materialCost + totalLaborEarning + packagingCost

    val suggestedTagsPool = listOf(
        "GI Tag Certified",
        "100% Eco-Friendly",
        "Natural Organic Dyes",
        "Master Handcrafted",
        "Wood-fired Kiln",
        "ODOP Certified",
        "Pure Handloom Silk",
        "Brass Relief Inlay",
        "Export Grade Packaging",
        "Ancestral Heritage"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dynamic Pricing Assistant",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "गतिशील मूल्य निर्धारण सहायक (Gemini AI)",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_HOME) },
                        modifier = Modifier.testTag("pricing_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ArtisanTextPrimary
                        )
                    }
                },
                actions = {
                    // Voice readout button
                    IconButton(
                        onClick = { viewModel.speakPricingRecommendation() },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("pricing_tts_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen Pricing",
                            tint = TerracottaPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArtisanSurface
                )
            )
        },
        bottomBar = {
            if (selectedAssistantTab == 1) {
                Surface(
                    color = ArtisanSurface,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share Quote Button
                        OutlinedButton(
                            onClick = {
                                val rec = recommendation
                                val shareText = if (rec != null) {
                                    """
                                        *कौशवाणी शिल्प मूल्य कोटेशन (KAUSHVANI Price Quote)*
                                        शिल्प: ${sourceTitle ?: category}
                                        • खुदरा मूल्य (Retail MRP): ₹${rec.optimalRetailPrice.toInt()}
                                        • थोक मूल्य (Wholesale B2B): ₹${rec.wholesalePrice.toInt()} (न्यूनतम ${rec.suggestedMinOrderQty} पीस)
                                        • सामग्री खर्च: ₹${materialCost.toInt()} | कारीगरी: ${laborHours} घंटे
                                        • विशेषताएं: ${selectedTags.joinToString(", ")}
                                        _KAUSHVANI निष्पक्ष मूल्य एवं कारीगर आजीविका मानक_
                                    """.trimIndent()
                                } else {
                                    "KAUSHVANI Artisan Craft Fair Price Quote"
                                }

                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "कोटेशन साझा करें (Share Quote)"))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pricing_share_quote_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "कोटेशन भेजें",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Apply to Product Wizard Button
                        Button(
                            onClick = { viewModel.applyPricingToProductWizard() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("pricing_apply_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "उत्पाद में लागू करें",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ArtisanBackground)
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedAssistantTab,
                containerColor = ArtisanSurface,
                contentColor = TerracottaPrimary,
                modifier = Modifier.fillMaxWidth().testTag("pricing_assistant_tab_row")
            ) {
                Tab(
                    selected = selectedAssistantTab == 0,
                    onClick = { selectedAssistantTab = 0 },
                    text = {
                        Text(
                            text = "ऐतिहासिक डेटा व AI मूल्य",
                            fontSize = 12.sp,
                            fontWeight = if (selectedAssistantTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_historical_dynamic_pricing")
                )
                Tab(
                    selected = selectedAssistantTab == 1,
                    onClick = { selectedAssistantTab = 1 },
                    text = {
                        Text(
                            text = "लागत कैलकुलेटर",
                            fontSize = 12.sp,
                            fontWeight = if (selectedAssistantTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_cost_calculator")
                )
            }

            if (selectedAssistantTab == 0) {
                ProductHistoryPricingView(
                    viewModel = historyPricingViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            // 1. Hero Context & Cluster Provenance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaContainer),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(TerracottaPrimary, MarigoldTertiary))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(TerracottaPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = sourceTitle ?: "$category Handcrafted Art",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                    Text(
                                        text = "📍 Gorakhpur Cluster • ODOP & GI Verified",
                                        fontSize = 11.sp,
                                        color = ArtisanTextSecondary
                                    )
                                }
                            }

                            Surface(
                                color = ForestContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Fair Living Wage AI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "यह सहायक आपकी सामग्री, कारीगरी के घंटे और टैग्स का विश्लेषण करके डिजिटल व राष्ट्रीय बाजार मानकों के अनुसार सबसे प्रतिस्पर्धी मूल्य सुझाता है।",
                            fontSize = 12.sp,
                            color = ArtisanTextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // 2. Target Market Channel Selection Tabs
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "लक्षित बाजार माध्यम (Target Market Channel):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TargetMarketChannel.values().forEach { channel ->
                            val isSelected = targetMarket == channel
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.pricingTargetMarket.value = channel
                                        viewModel.calculateDynamicPricing()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) IndigoSecondary else ArtisanSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) IndigoSecondary else ArtisanCardBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = when (channel) {
                                            TargetMarketChannel.RETAIL_D2C -> "🛍️ खुदरा"
                                            TargetMarketChannel.WHOLESALE_B2B -> "🏢 थोक B2B"
                                            TargetMarketChannel.EXPORT_PREMIUM -> "🌐 निर्यात"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else ArtisanTextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = channel.title.substringBefore("(").trim(),
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else ArtisanTextSecondary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Cost & Labor Inputs Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "1. लागत और मेहनत इनपुट (Cost & Labor Inputs)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                        }

                        // --- Raw Material Cost ---
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "कच्चा माल खर्च (Raw Materials):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ArtisanTextPrimary
                                    )
                                }

                                Text(
                                    text = "₹${materialCost.toInt()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TerracottaPrimary
                                )
                            }

                            // Steppers & Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        viewModel.pricingMaterialCost.value = (materialCost - 50.0).coerceAtLeast(20.0)
                                        viewModel.calculateDynamicPricing()
                                    },
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus 50")
                                }

                                Slider(
                                    value = materialCost.toFloat(),
                                    onValueChange = { viewModel.pricingMaterialCost.value = it.toDouble() },
                                    onValueChangeFinished = { viewModel.calculateDynamicPricing() },
                                    valueRange = 20f..5000f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = TerracottaPrimary,
                                        activeTrackColor = TerracottaPrimary
                                    )
                                )

                                FilledTonalIconButton(
                                    onClick = {
                                        viewModel.pricingMaterialCost.value = (materialCost + 50.0).coerceAtMost(5000.0)
                                        viewModel.calculateDynamicPricing()
                                    },
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Plus 50")
                                }
                            }

                            // Quick Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(100.0, 250.0, 500.0, 1000.0, 2000.0).forEach { preset ->
                                    val isCur = materialCost.toInt() == preset.toInt()
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.pricingMaterialCost.value = preset
                                                viewModel.calculateDynamicPricing()
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isCur) TerracottaPrimary else ArtisanSurface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isCur) TerracottaPrimary else ArtisanCardBorder
                                        )
                                    ) {
                                        Text(
                                            text = "₹${preset.toInt()}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isCur) Color.White else ArtisanTextPrimary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // --- Handcrafting Labor Time ---
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "कारीगरी का समय (Craft Labor Time):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ArtisanTextPrimary
                                    )
                                }

                                Text(
                                    text = "${String.format("%.1f", laborHours)} घंटे (hrs)",
                                    fontSize = 18.sp,
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
                                        viewModel.pricingLaborHours.value = (laborHours - 1.0).coerceAtLeast(0.5)
                                        viewModel.calculateDynamicPricing()
                                    },
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Minus 1h")
                                }

                                Slider(
                                    value = laborHours.toFloat(),
                                    onValueChange = { viewModel.pricingLaborHours.value = it.toDouble() },
                                    onValueChangeFinished = { viewModel.calculateDynamicPricing() },
                                    valueRange = 0.5f..50f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = IndigoSecondary,
                                        activeTrackColor = IndigoSecondary
                                    )
                                )

                                FilledTonalIconButton(
                                    onClick = {
                                        viewModel.pricingLaborHours.value = (laborHours + 1.0).coerceAtMost(50.0)
                                        viewModel.calculateDynamicPricing()
                                    },
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Plus 1h")
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
                                            .clickable {
                                                viewModel.pricingLaborHours.value = hrs
                                                viewModel.calculateDynamicPricing()
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isCur) IndigoSecondary else ArtisanSurface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isCur) IndigoSecondary else ArtisanCardBorder
                                        )
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isCur) Color.White else ArtisanTextPrimary,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // --- Artisan Skill Tier & Living Wage ---
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "कारीगर श्रेणी व प्रति घंटा मजदूरी दर (Skill & Wage Tier):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ArtisanTextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ArtisanSkillTier.values().forEach { tier ->
                                    val isSelected = skillTier == tier
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.pricingSkillTier.value = tier
                                                viewModel.calculateDynamicPricing()
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) ForestSuccess else ArtisanSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) ForestSuccess else ArtisanCardBorder
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = tier.hindiTitle,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else ArtisanTextPrimary,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "₹${tier.hourlyWage.toInt()}/घंटा",
                                                fontSize = 10.sp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.9f) else ForestSuccess,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Product Tags & Price Multiplier Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = MarigoldTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "2. उत्पाद टैग्स एवं मूल्य प्रभाव (Tags Multiplier)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                            }
                        }

                        Text(
                            text = "जेमिनी AI टैग्स के आधार पर उत्पाद का मूल्य प्रीमियम (+15% से +40%) तय करता है। टैग्स जोड़ें या हटाएं:",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )

                        // Active Selected Tags Flow / Chips
                        if (selectedTags.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "सक्रिय टैग्स (Active Tags):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TerracottaPrimary
                                )

                                FlowRowTagList(
                                    tags = selectedTags,
                                    onRemove = {
                                        viewModel.removePricingTag(it)
                                        viewModel.calculateDynamicPricing()
                                    }
                                )
                            }
                        }

                        // Quick Add Tag Suggestions
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "सुझाए गए मूल्य-वर्धक टैग्स (+ Add Value Tags):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ArtisanTextSecondary
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(suggestedTagsPool.filterNot { selectedTags.contains(it) }) { tag ->
                                    Surface(
                                        modifier = Modifier.clickable {
                                            viewModel.addCustomPricingTag(tag)
                                            viewModel.calculateDynamicPricing()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = ArtisanSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = IndigoSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = tag,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = ArtisanTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Custom Tag Input Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customTagInput,
                                onValueChange = { viewModel.pricingCustomTagInput.value = it },
                                placeholder = { Text("कस्टम टैग लिखें (e.g. Pure Clay)", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            FilledTonalButton(
                                onClick = {
                                    if (customTagInput.isNotBlank()) {
                                        viewModel.addCustomPricingTag(customTagInput)
                                        viewModel.calculateDynamicPricing()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+ जोड़ें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5. Recalculate CTA Button
            item {
                Button(
                    onClick = { viewModel.calculateDynamicPricing() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pricing_recalculate_button")
                ) {
                    if (isCalculating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "जेमिनी AI गणना कर रहा है...", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "जेमिनी AI से मूल्य विश्लेषण करें (Calculate with Gemini AI)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // 6. Gemini Dynamic Price Recommendation Display
            val rec = recommendation
            if (rec != null) {
                // Price Range Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, TerracottaPrimary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "सुझाया गया प्रतिस्पर्धी मूल्य (Recommended Range)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                }

                                Surface(
                                    color = TerracottaPrimary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Gemini 3.5 Flash",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Big Price Highlight
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(TerracottaContainer)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "₹${rec.optimalRetailPrice.toInt()}",
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TerracottaPrimary
                                    )
                                    Text(
                                        text = "उचित खुदरा मूल्य • Fair Consumer Retail Price",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TerracottaDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "प्रतिस्पर्धी दायरा: ₹${rec.minFairPrice.toInt()} से ₹${rec.maxFairPrice.toInt()}",
                                        fontSize = 11.sp,
                                        color = ArtisanTextSecondary
                                    )
                                }
                            }

                            // 3-Channel Comparison Row (Retail vs Wholesale vs Export)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ChannelPriceMiniCard(
                                    title = "खुदरा (D2C)",
                                    price = "₹${rec.optimalRetailPrice.toInt()}",
                                    unitText = "प्रति पीस",
                                    tagText = "सीधे ग्राहक",
                                    containerColor = TerracottaContainer,
                                    textColor = TerracottaPrimary,
                                    modifier = Modifier.weight(1f)
                                )

                                ChannelPriceMiniCard(
                                    title = "थोक (Wholesale)",
                                    price = "₹${rec.wholesalePrice.toInt()}",
                                    unitText = "न्यूनतम ${rec.suggestedMinOrderQty} पीस",
                                    tagText = "B2B / GeM",
                                    containerColor = IndigoSecondaryContainer.copy(alpha = 0.5f),
                                    textColor = IndigoSecondary,
                                    modifier = Modifier.weight(1f)
                                )

                                ChannelPriceMiniCard(
                                    title = "निर्यात (Export)",
                                    price = "₹${rec.exportPrice.toInt()}",
                                    unitText = "बुटीक / गिफ्टिंग",
                                    tagText = "ग्लोबल खरीदार",
                                    containerColor = ForestContainer,
                                    textColor = ForestSuccess,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Tag Price Multiplier Analysis Cards
                if (rec.tagImpacts.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "टैग्स से प्राप्त मूल्य वृद्धि (Tag Valuation Multipliers)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                }

                                rec.tagImpacts.forEach { impact ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ArtisanSurfaceVariant)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "#${impact.tag}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TerracottaPrimary
                                            )
                                            Text(
                                                text = impact.justification,
                                                fontSize = 10.sp,
                                                color = ArtisanTextSecondary,
                                                lineHeight = 14.sp
                                            )
                                        }

                                        Surface(
                                            color = ForestContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "+${impact.percentageBoost}%",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = ForestSuccess,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Cost Breakdown Bar & Percentage Slices
                if (rec.costBreakdown.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "लागत संरचना एवं लाभ (Cost Composition & Fair Margin)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )

                                // Multi-segment Progress Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                ) {
                                    rec.costBreakdown.forEach { slice ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(slice.percentage.coerceAtLeast(1f))
                                                .background(Color(slice.colorHex))
                                        )
                                    }
                                }

                                // Legend Grid
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rec.costBreakdown.forEach { slice ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(slice.colorHex))
                                                )
                                                Text(
                                                    text = "${slice.hindiLabel} (${slice.label})",
                                                    fontSize = 11.sp,
                                                    color = ArtisanTextPrimary
                                                )
                                            }

                                            Text(
                                                text = "₹${slice.amount.toInt()} (${slice.percentage.toInt()}%)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ArtisanTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Market Benchmark & Negotiation Defense Tip
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF8F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MarigoldTertiary.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MarigoldTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "खरीदार से बातचीत सलाह (Negotiation Defense Tip)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                            }

                            Text(
                                text = rec.regionalNegotiationTip.ifBlank { rec.negotiationTip },
                                fontSize = 12.sp,
                                color = ArtisanTextPrimary,
                                lineHeight = 17.sp
                            )

                            if (rec.benchmarkComparison.isNotBlank()) {
                                Surface(
                                    color = ArtisanSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                                ) {
                                    Text(
                                        text = "📊 ${rec.benchmarkComparison}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = IndigoSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
}
}

@Composable
private fun FlowRowTagList(
    tags: List<String>,
    onRemove: (String) -> Unit
) {
    // Render in rows
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val chunked = tags.chunked(3)
        chunked.forEach { rowTags ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowTags.forEach { tag ->
                    Surface(
                        color = TerracottaPrimary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TerracottaPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = TerracottaPrimary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onRemove(tag) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelPriceMiniCard(
    title: String,
    price: String,
    unitText: String,
    tagText: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = price,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Text(
                text = unitText,
                fontSize = 9.sp,
                color = textColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = tagText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}
