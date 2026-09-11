package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.pricing.PricingAdvisorComponent
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel
import com.example.ui.viewmodel.PricingAdvisorViewModel
import org.koin.androidx.compose.koinViewModel

private val AdvisorCraftCategories = listOf(
    Pair("Pottery", "टेराकोटा व मिट्टी"),
    Pair("Handloom", "हथकरघा व वस्त्र"),
    Pair("Metalcraft", "ढोकरा व धातु"),
    Pair("Woodcraft", "काष्ठ शिल्प"),
    Pair("Paintings", "मधुबनी व चित्रकला"),
    Pair("Jewelry", "पारंपरिक आभूषण"),
    Pair("Leather", "चर्म शिल्प")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingAdvisorScreen(
    hunarViewModel: HunarSetuViewModel,
    advisorViewModel: PricingAdvisorViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("Pottery") }

    // Synchronize initial data from HunarSetuViewModel if available
    LaunchedEffect(Unit) {
        val initialCat = hunarViewModel.pricingCategory.value.ifBlank { "Pottery" }
        val initialCraft = hunarViewModel.pricingSpecificCraftType.value.ifBlank { "Terracotta" }
        val initialMaterial = if (hunarViewModel.pricingMaterialCost.value > 0) hunarViewModel.pricingMaterialCost.value else 250.0
        val initialLabor = if (hunarViewModel.pricingLaborHours.value > 0) hunarViewModel.pricingLaborHours.value else 4.0
        val initialTitle = hunarViewModel.pricingSourceProductTitle.value ?: ""

        selectedCategory = initialCat
        advisorViewModel.setInitialData(
            title = initialTitle,
            category = initialCat,
            craftType = initialCraft,
            materialCost = initialMaterial,
            laborHours = initialLabor,
            hourlyWageRate = 180.0,
            packagingCost = 50.0,
            isGiTagged = false
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("pricing_advisor_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "शिल्प मूल्य सलाहकार",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "New Product Competitive Pricing Advisor",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { hunarViewModel.navigateTo(AppNavTab.ARTISAN_HOME) },
                        modifier = Modifier.testTag("btn_back_pricing_advisor")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Chips Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "शिल्प श्रेणी चुनें (Select Craft Category):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdvisorCraftCategories.forEach { (catKey, catHindi) ->
                            val isSelected = selectedCategory.equals(catKey, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategory = catKey
                                    advisorViewModel.updateCategory(catKey)
                                },
                                label = {
                                    Text(
                                        text = "$catHindi ($catKey)",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TerracottaPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = TerracottaPrimary
                                ),
                                modifier = Modifier.testTag("chip_cat_$catKey")
                            )
                        }
                    }
                }
            }

            // Core Pricing Advisor Component
            item {
                PricingAdvisorComponent(
                    viewModel = advisorViewModel,
                    onPriceApplied = { appliedPrice ->
                        hunarViewModel.reviewRetailPrice.value = appliedPrice
                        hunarViewModel.wizardRawCost.value = advisorViewModel.inputState.value.materialCost
                        hunarViewModel.wizardLaborHours.value = advisorViewModel.inputState.value.laborHours
                        Toast.makeText(
                            context,
                            "मूल्य ₹${appliedPrice.toInt()} चुना गया! नए उत्पाद में लागू किया जा रहा है...",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onSpeakText = { text, lang ->
                        hunarViewModel.speakText(text, lang)
                    },
                    showTitleHeader = true
                )
            }

            // How Pricing Advisor Works Explanation Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "मूल्य सलाहकार कैसे कार्य करता है?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "1. Room डेटाबेस मिलान: आपके कैटलॉग और पिछले बिक्री इतिहास से समान श्रेणी व शिल्प के उत्पादों को ढूँढता है।\n\n" +
                                    "2. आजीविका फ़्लोर गारंटी: कच्चा माल + श्रम (₹180/घंटा) + पैकेजिंग के आधार पर न्यूनतम ब्रेक-ईवन सीमा तय होती है, जिससे कारीगर को कभी नुकसान न हो।\n\n" +
                                    "3. बहु-स्तरीय मूल्य निर्धारण: मेला/हाट, कैटलॉग खुदरा, प्रीमियम बुटीक और थोक (B2B) चारों बाज़ारों के लिए अलग-अलग प्रतिस्पर्धी मूल्य सीमाएं प्रदान करता है।",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
