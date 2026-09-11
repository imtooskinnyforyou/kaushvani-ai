package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.DemoAiDataset
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel

val CraftCategories = listOf(
    "Pottery",
    "Handloom & Textiles",
    "Metalcraft",
    "Paintings & Folk Art",
    "Woodcraft",
    "Jewelry",
    "Leather"
)

@Composable
fun SmartCatalogWizardScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.wizardStep.collectAsStateWithLifecycle()
    val category by viewModel.wizardCategory.collectAsStateWithLifecycle()
    val hierarchicalMapping by viewModel.hierarchicalCategoryMapping.collectAsStateWithLifecycle()
    val imageUri by viewModel.wizardImageUri.collectAsStateWithLifecycle()
    val filter by viewModel.wizardFilter.collectAsStateWithLifecycle()
    val voiceTranscript by viewModel.wizardVoiceTranscript.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.wizardLanguage.collectAsStateWithLifecycle()
    val rawCost by viewModel.wizardRawCost.collectAsStateWithLifecycle()
    val laborHours by viewModel.wizardLaborHours.collectAsStateWithLifecycle()
    val isGiTagged by viewModel.wizardIsGiTagged.collectAsStateWithLifecycle()
    val isGenerating by viewModel.wizardIsGenerating.collectAsStateWithLifecycle()
    val generatedCatalog by viewModel.wizardGeneratedCatalog.collectAsStateWithLifecycle()
    val parsedVoiceCatalogFields by viewModel.parsedVoiceCatalogFields.collectAsStateWithLifecycle()
    val isVoiceParsing by viewModel.isVoiceParsing.collectAsStateWithLifecycle()

    // Review & Editable Fields
    val reviewTitle by viewModel.reviewTitle.collectAsStateWithLifecycle()
    val reviewRegionalTitle by viewModel.reviewRegionalTitle.collectAsStateWithLifecycle()
    val reviewShortDescription by viewModel.reviewShortDescription.collectAsStateWithLifecycle()
    val reviewDescription by viewModel.reviewDescription.collectAsStateWithLifecycle()
    val reviewRegionalDescription by viewModel.reviewRegionalDescription.collectAsStateWithLifecycle()
    val reviewCraftStory by viewModel.reviewCraftStory.collectAsStateWithLifecycle()
    val reviewProductName by viewModel.reviewProductName.collectAsStateWithLifecycle()
    val reviewColor by viewModel.reviewColor.collectAsStateWithLifecycle()
    val reviewManufacturingTechnique by viewModel.reviewManufacturingTechnique.collectAsStateWithLifecycle()
    val reviewRegionOrigin by viewModel.reviewRegionOrigin.collectAsStateWithLifecycle()
    val reviewSeoKeywords by viewModel.reviewSeoKeywords.collectAsStateWithLifecycle()
    val reviewSuggestedTags by viewModel.reviewSuggestedTags.collectAsStateWithLifecycle()
    val reviewDetectedLanguage by viewModel.reviewDetectedLanguage.collectAsStateWithLifecycle()
    val reviewTranslatedVoiceEnglish by viewModel.reviewTranslatedVoiceEnglish.collectAsStateWithLifecycle()
    val isReviewEditModeActive by viewModel.isReviewEditModeActive.collectAsStateWithLifecycle()
    val reviewRetailPrice by viewModel.reviewRetailPrice.collectAsStateWithLifecycle()
    val reviewWholesalePrice by viewModel.reviewWholesalePrice.collectAsStateWithLifecycle()
    val reviewMinOrderQty by viewModel.reviewMinOrderQty.collectAsStateWithLifecycle()
    val reviewStock by viewModel.reviewStock.collectAsStateWithLifecycle()
    val reviewTags by viewModel.reviewTags.collectAsStateWithLifecycle()
    val reviewMaterials by viewModel.reviewMaterials.collectAsStateWithLifecycle()
    val reviewTagCategories by viewModel.reviewTagCategories.collectAsStateWithLifecycle()
    val reviewSpecificCraftType by viewModel.reviewSpecificCraftType.collectAsStateWithLifecycle()
    val reviewCareInstructions by viewModel.reviewCareInstructions.collectAsStateWithLifecycle()
    val reviewGiTagReason by viewModel.reviewGiTagReason.collectAsStateWithLifecycle()
    val reviewDimensions by viewModel.reviewDimensions.collectAsStateWithLifecycle()
    val reviewGiTagEligible by viewModel.reviewGiTagEligible.collectAsStateWithLifecycle()
    val structuredMetadata by viewModel.wizardStructuredMetadata.collectAsStateWithLifecycle()
    val isTourVisible by viewModel.isAddProductTourVisible.collectAsStateWithLifecycle()
    val tourStep by viewModel.addProductTourStep.collectAsStateWithLifecycle()
    val isOnboardingCarouselVisible by viewModel.isOnboardingCarouselVisible.collectAsStateWithLifecycle()

    val simpleQuestions by viewModel.wizardSimpleQuestions.collectAsStateWithLifecycle()
    val priceCalculationResult by viewModel.priceCalculationResult.collectAsStateWithLifecycle()
    val artisanSellingPrice by viewModel.artisanSellingPrice.collectAsStateWithLifecycle()
    val isConfirmedSuspiciousPricing by viewModel.isConfirmedSuspiciousPricing.collectAsStateWithLifecycle()
    val photoQualityEvaluation by viewModel.photoQualityEvaluation.collectAsStateWithLifecycle()
    val productIntegrityReport by viewModel.productIntegrityReport.collectAsStateWithLifecycle()

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSuccessCelebration by remember { mutableStateOf(false) }
    var photoConfirmed by remember { mutableStateOf(true) }
    var wageConfirmed by remember { mutableStateOf(true) }
    var storyConfirmed by remember { mutableStateOf(true) }

    // Progressive loading tip index
    val loadingTips = listOf(
        "✨ शिल्प की सांस्कृतिक कहानी तैयार हो रही है...",
        "🏛️ GI Tag और हस्तशिल्प मानकों का सत्यापन...",
        "💰 ₹175/घंटे की दर से उचित आजीविका मूल्य की गणना...",
        "🌐 अंग्रेजी और मातृभाषा में SEO टैग्स तैयार हो रहे हैं..."
    )
    var currentTipIndex by remember { mutableStateOf(0) }
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            while (true) {
                kotlinx.coroutines.delay(1800)
                currentTipIndex = (currentTipIndex + 1) % loadingTips.size
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = ArtisanSurface,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_HOME) }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back to Home",
                                    tint = ArtisanTextPrimary
                                )
                            }
                            Column {
                                Text(
                                    text = "नया उत्पाद जोड़ें (Add Product)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "बहुभाषी AI कैटलॉग विजार्ड • चरण ${step + 1}/4",
                                    fontSize = 11.sp,
                                    color = ArtisanTextSecondary
                                )
                            }
                        }

                        // Help / Tour Trigger
                        OutlinedButton(
                            onClick = { viewModel.startAddProductTour() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = TerracottaPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("सहायता", fontSize = 11.sp, color = TerracottaPrimary)
                        }
                    }

                    // Step Indicator Breadcrumbs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val steps = listOf(
                            "1. फ़ोटो व स्टूडियो",
                            "2. आवाज़ विवरण",
                            "3. पारदर्शी लागत",
                            "4. समीक्षा व संपादन"
                        )
                        steps.forEachIndexed { index, label ->
                            val isCompleted = index < step
                            val isCurrent = index == step
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        role = Role.Tab
                                        selected = isCurrent
                                        contentDescription = "चरण ${index + 1}: $label"
                                        stateDescription = when {
                                            isCurrent -> "वर्तमान चरण (Current Step)"
                                            isCompleted -> "पूर्ण (Completed)"
                                            else -> "आगामी चरण (Upcoming Step)"
                                        }
                                    }
                                    .clickable {
                                        if (index < step || (index == 3 && generatedCatalog != null)) {
                                            viewModel.wizardStep.value = index
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index <= step) TerracottaPrimary else ArtisanCardBorder
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (index < step) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index <= step) Color.White else ArtisanTextSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = if (index == step) FontWeight.Bold else FontWeight.Normal,
                                    color = if (index == step) TerracottaPrimary else ArtisanTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (step + 1) / 4.0f },
                        modifier = Modifier.fillMaxWidth(),
                        color = TerracottaPrimary,
                        trackColor = ArtisanCardBorder
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = ArtisanSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 0) {
                        OutlinedButton(
                            onClick = { viewModel.wizardStep.value = step - 1 },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "पिछला (Back)", fontSize = 13.sp)
                        }
                    }

                    if (step == 3) {
                        // In Step 3, show [Save Draft] and [Publish Product]
                        OutlinedButton(
                            onClick = { 
                                viewModel.saveProductAsDraft()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("review_save_draft_button")
                        ) {
                            Text(
                                text = "Save Draft",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                        }

                        Button(
                            onClick = { 
                                showConfirmationDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("review_publish_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Publish Product",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                when (step) {
                                    0 -> viewModel.wizardStep.value = 1
                                    1 -> viewModel.wizardStep.value = 2
                                    2 -> viewModel.triggerAiCatalogGeneration()
                                }
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(52.dp)
                                .testTag("wizard_next_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (step == 2) ForestSuccess else TerracottaPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "AI गणना कर रहा है...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else {
                                val buttonText = when (step) {
                                    0 -> "आगे बढ़ें (Next: Voice) →"
                                    1 -> "आगे बढ़ें (Next: Pricing) →"
                                    2 -> "✨ AI कैटलॉग बनाएँ (Generate)"
                                    else -> "Next"
                                }
                                Text(
                                    text = buttonText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(ArtisanBackground)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP 0: PHOTO & AI STUDIO LIGHTING
            if (step == 0) {
                item {
                    // Guided Tour Coach Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.startAddProductTour() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = TerracottaContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = TerracottaPrimary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "नए कारीगर हैं? मार्गदर्शिका देखें",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                    Text(
                                        text = "फ़ोटो, आवाज़ और समीक्षा के 4 आसान चरण (1-Min Tour)",
                                        fontSize = 11.sp,
                                        color = ArtisanTextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.startAddProductTour() },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("टूर शुरू करें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    // DEMO AI MODE: Predefined Multilingual Showcase Dataset for Hackathon
                    val isDemoActive by viewModel.isDemoAiMode.collectAsState()
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("demo_ai_samples_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDemoActive) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDemoActive) Color(0xFFD97706) else ArtisanCardBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Demo Samples",
                                        tint = if (isDemoActive) Color(0xFFD97706) else IndigoSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "हैकथॉन डेमो प्रीसेट (Multilingual Demo Samples)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDemoActive) Color(0xFF92400E) else ArtisanTextPrimary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDemoActive) Color(0xFFD97706) else ArtisanSurfaceVariant
                                ) {
                                    Text(
                                        text = if (isDemoActive) "DEMO MODE ON" else "1-CLICK PRESET",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDemoActive) Color.White else ArtisanTextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "इंटरनेट या AI रुकावट से सुरक्षित: किसी भी भाषा के नमूने पर क्लिक करके तुरंत ऑटो-फिल करें:",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(viewModel.demoMultilingualSamples) { sample ->
                                    Surface(
                                        modifier = Modifier.clickable {
                                            viewModel.loadPredefinedMultilingualSample(sample)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = sample.languageName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TerracottaPrimary
                                            )
                                            Text(
                                                text = "• ${sample.craftType.take(16)}",
                                                fontSize = 10.sp,
                                                color = ArtisanTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    // Category Selection Chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "शिल्प श्रेणी चुनें (Select Craft Category):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CraftCategories) { cat ->
                                val isSelected = cat == category
                                Surface(
                                    modifier = Modifier
                                        .semantics {
                                            role = Role.Tab
                                            selected = isSelected
                                            contentDescription = "श्रेणी: $cat (Category: $cat)"
                                            stateDescription = if (isSelected) "चयनित (Selected)" else "उपलब्ध (Available)"
                                        }
                                        .clickable {
                                            viewModel.wizardCategory.value = cat
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) TerracottaPrimary else ArtisanSurface,
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) Color.White else ArtisanTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    PhotoStudioSelector(
                        selectedImageUri = imageUri,
                        onImageSelected = { viewModel.wizardImageUri.value = it },
                        currentFilter = filter,
                        onFilterSelected = { viewModel.wizardFilter.value = it },
                        category = category,
                        isGiTagged = isGiTagged,
                        onGiTagToggled = { viewModel.wizardIsGiTagged.value = it },
                        onLaunchCamera = { viewModel.startImageStudioFlow(imageUri, category) },
                        onOpenStudioEnhancer = { viewModel.startImageStudioFlow(imageUri, category) }
                    )
                }

                item {
                    ProductIntegrityAndQualityCard(
                        qualityEvaluation = photoQualityEvaluation,
                        integrityReport = productIntegrityReport,
                        onImprovePhotoClick = { viewModel.startImageStudioFlow(imageUri, category) },
                        onRetakePhotoClick = { viewModel.startImageStudioFlow(null, category) },
                        modifier = Modifier.fillMaxWidth().testTag("wizard_product_integrity_card")
                    )
                }
            }

            // STEP 1: 5 SIMPLE PRODUCT QUESTIONS & OPTIONAL VOICE INPUT
            if (step == 1) {
                item {
                    SimpleProductQuestionsCard(
                        data = simpleQuestions,
                        onDataChange = { viewModel.updateWizardSimpleQuestions(it) },
                        onStartVoiceForField = { qNum: Int -> viewModel.startVoiceForSimpleQuestion(qNum) },
                        modifier = Modifier.fillMaxWidth().testTag("wizard_simple_questions_card")
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ArtisanSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🎙️ वैकल्पिक: अपनी आवाज़ में शिल्प की पूरी कहानी बताएं (Optional Story)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Text(
                                text = "यदि चाहें, तो विस्तृत इतिहास या कारीगरी का ब्यौरा रिकॉर्ड करें। AI इसका उपयोग विवरण बनाने में करेगा।",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary
                            )
                        }
                    }
                }

                item {
                    VoiceRecordingComponent(
                        currentTranscript = voiceTranscript,
                        onTranscriptChanged = { viewModel.wizardVoiceTranscript.value = it },
                        onTranscriptionComplete = { text, lang ->
                            viewModel.wizardVoiceTranscript.value = text
                            viewModel.wizardLanguage.value = lang
                            viewModel.parseAndPopulateCatalogFromVoice(text, lang)
                        },
                        initialLanguage = selectedLanguage,
                        onLanguageChanged = { lang ->
                            viewModel.setArtisanPreferredLanguage(lang.name, lang.localeTag)
                        },
                        onTriggerAiProcess = { transcript ->
                            viewModel.wizardVoiceTranscript.value = transcript
                            viewModel.wizardStep.value = 2
                            viewModel.triggerAiCatalogGeneration()
                        },
                        onSpeakText = { text, lang ->
                            viewModel.speakText(text, lang)
                        },
                        voiceToTextService = viewModel.voiceToTextService,
                        parsedCatalogFields = parsedVoiceCatalogFields,
                        isVoiceParsing = isVoiceParsing,
                        onApplyParsedFields = { parsed ->
                            viewModel.applyParsedVoiceFields(parsed, advanceToNext = true)
                        },
                        onParseVoiceTranscript = { text, lang ->
                            viewModel.parseAndPopulateCatalogFromVoice(text, lang)
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(AppNavTab.VOICE_CATALOG) }
                            .testTag("wizard_open_dedicated_voice_catalog"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = TerracottaContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "🎙️ संपूर्ण 13-फ़ील्ड वॉइस कैटलॉग इंजन",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary
                                    )
                                    Text(
                                        text = "Language Selection → SpeechRecognizer → Gemini → 13 Editable Fields",
                                        fontSize = 11.sp,
                                        color = ArtisanTextSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                item {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(AppNavTab.LANGUAGE_PREFERENCES) }
                            .testTag("wizard_open_language_preferences_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = ArtisanSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "भाषा प्राथमिकताएं और स्पीच सेटिंग्स (Language Preferences)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                    Text(
                                        text = "सक्रिय SpeechRecognizer: ${selectedLanguage.nativeName} (${selectedLanguage.localeTag})",
                                        fontSize = 10.sp,
                                        color = ArtisanTextSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configure",
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // STEP 2: COSTING & FAIR LIVING WAGE
            if (step == 2) {
                if (isGenerating) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = TerracottaContainer)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = TerracottaPrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Multimodal Gemini AI विश्लेषण जारी है...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                                Text(
                                    text = loadingTips[currentTipIndex],
                                    fontSize = 13.sp,
                                    color = ArtisanTextPrimary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                item {
                    SafePriceDecisionCard(
                        calculationResult = priceCalculationResult,
                        artisanSellingPrice = artisanSellingPrice,
                        onArtisanPriceChange = { viewModel.updateArtisanSellingPrice(it) },
                        onConfirmSuspiciousInputs = { viewModel.confirmSuspiciousPricingInputs() },
                        modifier = Modifier.fillMaxWidth().testTag("wizard_safe_price_decision_card")
                    )
                }
            }

            // STEP 3: HUMAN REVIEW & 1-CLICK SAVE / PUBLISH (Multilingual Auto-Cataloger Review)
            if (step == 3) {
                item {
                    // REQUIREMENT 7: FINAL VALIDATION CHECKLIST
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wizard_final_validation_summary_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestSuccess.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FactCheck,
                                    contentDescription = null,
                                    tint = ForestSuccess,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "कृपया प्रकाशित करने से पहले समीक्षा करें (Please Review Before Publishing)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                            }
                            Text(
                                text = "कारीगर द्वारा अंतिम स्वीकृति आवश्यक है। उत्पाद प्रकाशित होने से पहले सभी विवरण सत्यापित करें:",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary
                            )
                            HorizontalDivider(color = Color(0xFFBBF7D0))

                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(
                                    text = "✓ Product Photo: ${if (imageUri.isNotBlank()) "Verified Craft Image Attached" else "Standard Craft Image"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "✓ Product Details: $reviewTitle",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "✓ Material: ${reviewMaterials.ifBlank { simpleQuestions.material }}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "✓ Labour Time: ${simpleQuestions.laborHours} Hours",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "✓ Cost: Material ₹${simpleQuestions.rawMaterialCost.toInt()} | Base Cost ₹${priceCalculationResult.baseCost.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "✓ Suggested Price: ₹${reviewRetailPrice.toInt()} (Fair Range: ₹${priceCalculationResult.minFairPrice.toInt()}–₹${priceCalculationResult.maxFairPrice.toInt()})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                                Text(
                                    text = "✓ AI-Generated Description: Factual, Grounded, & Editable",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }
                }

                item {
                    // Prominent "AI Generated — Please Review" Status Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, IndigoSecondary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = IndigoSecondary,
                                        shape = CircleShape,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.padding(8.dp).size(20.dp)
                                        )
                                    }
                                    Column {
                                        val isDemoActive by viewModel.isDemoAiMode.collectAsState()
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (isDemoActive) "DEMO AI Generated — Please Review" else "AI Generated — Please Review",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E3A8A)
                                            )
                                            if (isDemoActive) {
                                                Surface(
                                                    color = Color(0xFFD97706),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "DEMO DATA",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "AI द्वारा निर्मित विवरण — सभी फ़ील्ड संपादन योग्य हैं (All fields editable)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF3B82F6)
                                        )
                                    }
                                }

                                // Quick Audio Readout Button
                                FilledTonalButton(
                                    onClick = {
                                        val text = "$reviewRegionalTitle. $reviewShortDescription. $reviewRegionalDescription. खुदरा मूल्य $reviewRetailPrice रुपये, थोक मूल्य $reviewWholesalePrice रुपये।"
                                        viewModel.speakText(text, selectedLanguage.code)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Listen",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "🔊 सुनें (Listen)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 3 Explicit Quick Action Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { viewModel.toggleReviewEditMode() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("[Edit]", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { viewModel.regenerateAiCatalog() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("[Regenerate]", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showConfirmationDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("[Save]", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Section 1: Generated E-Commerce & Export Content (Editable)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
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
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "1. AI जनरेटेड विवरण (Generated Outputs):",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                            }

                            CraftArtworkDisplay(
                                imageUri = imageUri,
                                category = category,
                                styleFilter = filter,
                                isGiTagged = isGiTagged,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )

                            // 1. Product Title (English)
                            OutlinedTextField(
                                value = reviewTitle,
                                onValueChange = { viewModel.reviewTitle.value = it },
                                label = { Text("1. Product Title (English E-Commerce)") },
                                modifier = Modifier.fillMaxWidth().testTag("review_title_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Regional Title
                            OutlinedTextField(
                                value = reviewRegionalTitle,
                                onValueChange = { viewModel.reviewRegionalTitle.value = it },
                                label = { Text("Regional Title in Native Script (${selectedLanguage.nativeName})") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 2. Short Description
                            OutlinedTextField(
                                value = reviewShortDescription,
                                onValueChange = { viewModel.reviewShortDescription.value = it },
                                label = { Text("2. Short Description (Concise Summary for Cards)") },
                                modifier = Modifier.fillMaxWidth().testTag("review_short_desc_input"),
                                minLines = 2,
                                maxLines = 3,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 3. Detailed Description
                            OutlinedTextField(
                                value = reviewDescription,
                                onValueChange = { viewModel.reviewDescription.value = it },
                                label = { Text("3. Detailed Description (Storytelling, Authenticity, Texture)") },
                                modifier = Modifier.fillMaxWidth().testTag("review_detailed_desc_input"),
                                minLines = 3,
                                maxLines = 6,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 6. Craft Story / Artisan Story
                            OutlinedTextField(
                                value = reviewCraftStory,
                                onValueChange = { viewModel.reviewCraftStory.value = it },
                                label = { Text("6. Craft Story (Cultural Heritage & Tradition)") },
                                modifier = Modifier.fillMaxWidth().testTag("review_craft_story_input"),
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 4. SEO Keywords
                            OutlinedTextField(
                                value = reviewSeoKeywords,
                                onValueChange = { viewModel.reviewSeoKeywords.value = it },
                                label = { Text("4. SEO Keywords (Search Ranking Terms)") },
                                modifier = Modifier.fillMaxWidth().testTag("review_seo_keywords_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 5. Product Tags
                            OutlinedTextField(
                                value = reviewTags,
                                onValueChange = { viewModel.reviewTags.value = it },
                                label = { Text("5. Product Tags (Marketplace & Export Tags)") },
                                modifier = Modifier.fillMaxWidth().testTag("review_tags_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // Section 2: Extracted Product Information (Editable)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
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
                                Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = IndigoSecondary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "2. निकाली गई उत्पाद जानकारी (Extracted Information):",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                            }

                            // Product Name & Category
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = reviewProductName,
                                    onValueChange = { viewModel.reviewProductName.value = it },
                                    label = { Text("Product Name") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = { viewModel.wizardCategory.value = it },
                                    label = { Text("Category") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Craft Type & Material
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = reviewSpecificCraftType,
                                    onValueChange = { viewModel.reviewSpecificCraftType.value = it },
                                    label = { Text("Craft Type") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = reviewMaterials,
                                    onValueChange = { viewModel.reviewMaterials.value = it },
                                    label = { Text("Material") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Color & Dimensions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = reviewColor,
                                    onValueChange = { viewModel.reviewColor.value = it },
                                    label = { Text("Color") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = reviewDimensions,
                                    onValueChange = { viewModel.reviewDimensions.value = it },
                                    label = { Text("Size / Dimensions") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Manufacturing Technique & Region
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = reviewManufacturingTechnique,
                                    onValueChange = { viewModel.reviewManufacturingTechnique.value = it },
                                    label = { Text("Manufacturing Technique") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = reviewRegionOrigin,
                                    onValueChange = { viewModel.reviewRegionOrigin.value = it },
                                    label = { Text("Region / Origin") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                // Section 3: Pricing & Fair Wages (Editable)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = null, tint = ForestSuccess, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "3. उचित आजीविका मूल्य निर्धारण (Fair Wage & Pricing):",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = reviewRetailPrice.toInt().toString(),
                                    onValueChange = {
                                        viewModel.reviewRetailPrice.value = it.toDoubleOrNull() ?: 0.0
                                    },
                                    label = { Text("खुदरा मूल्य ₹ (Retail)") },
                                    modifier = Modifier.weight(1f).testTag("review_retail_price_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = reviewWholesalePrice.toInt().toString(),
                                    onValueChange = {
                                        viewModel.reviewWholesalePrice.value = it.toDoubleOrNull() ?: 0.0
                                    },
                                    label = { Text("थोक B2B मूल्य ₹ (Wholesale)") },
                                    modifier = Modifier.weight(1f).testTag("review_wholesale_price_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Care Instructions
                            OutlinedTextField(
                                value = reviewCareInstructions,
                                onValueChange = { viewModel.reviewCareInstructions.value = it },
                                label = { Text("देखभाल निर्देश (Care Instructions)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Save Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ForestSuccess)
                    Text("उत्पाद सहेजें व प्रकाशित करें (Save Catalog)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("क्या आप इस AI-कैटलॉग को सहेजना और अपने ऑनलाइन शोरूम में प्रकाशित करना चाहते हैं?", fontSize = 13.sp)
                    Surface(
                        color = ForestContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📦 शीर्षक: $reviewTitle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                            Text("💰 खुदरा दर: ₹$reviewRetailPrice | थोक: ₹$reviewWholesalePrice", fontSize = 12.sp, color = ForestSuccess)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        viewModel.publishProductToCatalog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess)
                ) {
                    Text("हाँ, सहेजें (Save Now)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmationDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    // Guided Interactive Tour Overlay & Onboarding Carousel for Artisans
    AddProductTourOverlay(
        isVisible = isTourVisible,
        currentStepIndex = tourStep,
        onStepChange = { viewModel.setAddProductTourStep(it) },
        onDismiss = { viewModel.dismissAddProductTour() },
        onSpeakText = { text, lang -> viewModel.speakText(text, lang) }
    )

    AddProductOnboardingCarouselDialog(
        isOpen = isOnboardingCarouselVisible,
        onDismiss = { viewModel.dismissOnboardingCarousel() },
        onStartAddProduct = { viewModel.dismissOnboardingCarousel() },
        onSpeakText = { text, lang -> viewModel.speakText(text, lang) }
    )
}
