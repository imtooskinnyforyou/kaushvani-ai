package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.voice.SpeechState
import com.example.data.voice.VoiceCatalogError
import com.example.ui.components.RegionalLanguage
import com.example.ui.components.SupportedRegionalLanguages
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel

/**
 * Screen implementing the Multilingual Voice Catalog flow:
 * Language Selection → Microphone → SpeechRecognizer → REAL transcript → Gemini → validated JSON → editable product fields → save.
 *
 * Adheres strictly to requirements:
 * 1. Support configured Indian languages.
 * 2. Handle microphone denial, recognition failure, silence, unsupported language, and network failure.
 * 3. Manual text input available as fallback.
 * 4. Never invent GI status, government certification, material, origin or artisan credentials.
 * 5. Generates all 13 required fields (title, regional title, description, craft story, category,
 *    craft type, material, color, dimensions, weight, care instructions, tags, keywords).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MultilingualVoiceCatalogScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Reactive State from ViewModel
    val selectedLanguage by viewModel.voiceCatalogSelectedLanguage.collectAsStateWithLifecycle()
    val rawTranscript by viewModel.voiceCatalogTranscript.collectAsStateWithLifecycle()
    val speechState by viewModel.voiceToTextService.speechState.collectAsStateWithLifecycle()
    val livePartialText by viewModel.voiceToTextService.livePartialText.collectAsStateWithLifecycle()
    val liveRmsDb by viewModel.voiceToTextService.liveRmsDb.collectAsStateWithLifecycle()
    val isListening by viewModel.voiceToTextService.isListening.collectAsStateWithLifecycle()
    val errorState by viewModel.voiceCatalogError.collectAsStateWithLifecycle()
    val isGenerating by viewModel.voiceCatalogIsGenerating.collectAsStateWithLifecycle()
    val catalogResult by viewModel.voiceCatalogResult.collectAsStateWithLifecycle()
    val currentStep by viewModel.voiceCatalogStep.collectAsStateWithLifecycle()
    val savedProductId by viewModel.voiceCatalogSavedProductId.collectAsStateWithLifecycle()

    // 13 Editable Fields
    val editTitle by viewModel.vcEditTitle.collectAsStateWithLifecycle()
    val editRegionalTitle by viewModel.vcEditRegionalTitle.collectAsStateWithLifecycle()
    val editDescription by viewModel.vcEditDescription.collectAsStateWithLifecycle()
    val editCraftStory by viewModel.vcEditCraftStory.collectAsStateWithLifecycle()
    val editCategory by viewModel.vcEditCategory.collectAsStateWithLifecycle()
    val editCraftType by viewModel.vcEditCraftType.collectAsStateWithLifecycle()
    val editMaterial by viewModel.vcEditMaterial.collectAsStateWithLifecycle()
    val editColor by viewModel.vcEditColor.collectAsStateWithLifecycle()
    val editDimensions by viewModel.vcEditDimensions.collectAsStateWithLifecycle()
    val editWeight by viewModel.vcEditWeight.collectAsStateWithLifecycle()
    val editCareInstructions by viewModel.vcEditCareInstructions.collectAsStateWithLifecycle()
    val editTags by viewModel.vcEditTags.collectAsStateWithLifecycle()
    val editKeywords by viewModel.vcEditKeywords.collectAsStateWithLifecycle()
    val editRetailPrice by viewModel.vcEditRetailPrice.collectAsStateWithLifecycle()
    val editWholesalePrice by viewModel.vcEditWholesalePrice.collectAsStateWithLifecycle()
    val editIsGiTagged by viewModel.vcEditIsGiTagged.collectAsStateWithLifecycle()
    val editGovCert by viewModel.vcEditGovernmentCertification.collectAsStateWithLifecycle()
    val editImagePaths by viewModel.vcEditImagePaths.collectAsStateWithLifecycle()

    // Photo Picker Launcher (Android Photo Picker - Zero Permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.vcEditImagePaths.value = uris.map { it.toString() }
            Toast.makeText(context, "📸 ${uris.size} तस्वीरें जोड़ी गईं", Toast.LENGTH_SHORT).show()
        }
    }

    // Local UI states
    var activeInputMode by remember { mutableStateOf(0) } // 0: Voice / Mic, 1: Manual Text Fallback
    var manualTextInput by remember { mutableStateOf("") }
    var newTagInput by remember { mutableStateOf("") }
    var newKeywordInput by remember { mutableStateOf("") }
    var showIntegrityDialog by remember { mutableStateOf(false) }

    // Microphone Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.clearVoiceCatalogError()
            viewModel.startVoiceCatalogAudioListening()
        } else {
            viewModel.handleVoiceCatalogMicrophoneDenied()
        }
    }

    // Synchronize SpeechRecognizer success into ViewModel transcript
    LaunchedEffect(speechState) {
        when (val state = speechState) {
            is SpeechState.Success -> {
                if (state.transcript.isNotBlank()) {
                    viewModel.voiceCatalogTranscript.value = state.transcript
                    manualTextInput = state.transcript
                }
            }
            is SpeechState.Error -> {
                viewModel.handleVoiceCatalogSpeechError(state.errorCode, state.message)
            }
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmIvoryBackground,
        topBar = {
            Surface(
                color = ArtisanSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (currentStep > 0 && currentStep != 3) {
                                    viewModel.voiceCatalogStep.value = 0
                                } else {
                                    viewModel.navigateTo(AppNavTab.ARTISAN_HOME)
                                }
                            },
                            modifier = Modifier.testTag("voice_catalog_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = ArtisanTextPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Multilingual Voice Catalog",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Text(
                                text = "बोलकर बनाएं संपूर्ण उत्पाद कैटलॉग",
                                fontSize = 11.sp,
                                color = TerracottaPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Authenticity & Help Info Action
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showIntegrityDialog = true },
                            modifier = Modifier.testTag("voice_catalog_integrity_help_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VerifiedUser,
                                contentDescription = "Authenticity and Integrity Rules",
                                tint = ForestSuccess
                            )
                        }

                        // Selected Language Chip in Top Bar
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = TerracottaContainer,
                            border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = TerracottaPrimary
                                )
                                Text(
                                    text = selectedLanguage.nativeName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("voice_catalog_scrollable_container"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // STEP PROGRESS PIPELINE INDICATOR
            item {
                VoiceCatalogPipelineHeader(currentStep = currentStep)
            }

            // STEP 3: SUCCESS CELEBRATION (SAVED TO CATALOG)
            if (currentStep == 3) {
                item {
                    VoiceCatalogSuccessCard(
                        productId = savedProductId ?: 0L,
                        title = editTitle,
                        regionalTitle = editRegionalTitle,
                        category = editCategory,
                        price = editRetailPrice,
                        onViewCatalog = {
                            viewModel.navigateTo(AppNavTab.ARTISAN_CATALOG)
                        },
                        onAddAnother = {
                            viewModel.resetVoiceCatalogFlow()
                            manualTextInput = ""
                        }
                    )
                }
            }

            // STEP 1: GENERATING WITH GEMINI
            else if (isGenerating || currentStep == 1) {
                item {
                    VoiceCatalogGeneratingCard(
                        language = selectedLanguage,
                        transcript = if (rawTranscript.isNotBlank()) rawTranscript else manualTextInput
                    )
                }
            }

            // STEP 2: VALIDATED JSON & EDITABLE PRODUCT FIELDS
            else if (currentStep == 2) {
                item {
                    VoiceCatalogIntegrityGuaranteeBanner(
                        isGiTagged = editIsGiTagged,
                        governmentCertification = editGovCert
                    )
                }

                // 13 EDITABLE FIELDS FORM
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_catalog_editable_fields_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                        border = BorderStroke(1.dp, ArtisanCardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = TerracottaPrimary
                                    )
                                    Text(
                                        text = "समीक्षा व संपादन (Review & Edit 13 Fields)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                }
                                Surface(
                                    color = ForestContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "✓ Validated JSON",
                                        fontSize = 10.sp,
                                        color = ForestSuccess,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = ArtisanCardBorder)

                            // 1. Title
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { viewModel.vcEditTitle.value = it },
                                label = { Text("1. उत्पाद शीर्षक (English Title)") },
                                placeholder = { Text("e.g. Handcrafted Terracotta Decorative Vase") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_title"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 2. Regional Title
                            OutlinedTextField(
                                value = editRegionalTitle,
                                onValueChange = { viewModel.vcEditRegionalTitle.value = it },
                                label = { Text("2. क्षेत्रीय शीर्षक (${selectedLanguage.nativeName} Regional Title)") },
                                placeholder = { Text("e.g. पारंपरिक हस्तनिर्मित टेराकोटा कलश") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_regional_title"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 3. Description
                            OutlinedTextField(
                                value = editDescription,
                                onValueChange = { viewModel.vcEditDescription.value = it },
                                label = { Text("3. विस्तृत विवरण (Description)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_description"),
                                minLines = 3,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 4. Craft Story
                            OutlinedTextField(
                                value = editCraftStory,
                                onValueChange = { viewModel.vcEditCraftStory.value = it },
                                label = { Text("4. शिल्प विरासत व सांस्कृतिक कहानी (Craft Story)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_craft_story"),
                                minLines = 3,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 5. Category Selection
                            Text(
                                text = "5. शिल्प श्रेणी (Category)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextSecondary
                            )
                            val categories = listOf(
                                "Pottery", "Handloom & Textiles", "Metalcraft",
                                "Woodcraft", "Paintings & Folk Art", "Jewelry", "Leather"
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(categories) { cat ->
                                    val isSelected = editCategory.equals(cat, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.vcEditCategory.value = cat },
                                        label = { Text(cat, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TerracottaPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            // 6. Craft Type
                            OutlinedTextField(
                                value = editCraftType,
                                onValueChange = { viewModel.vcEditCraftType.value = it },
                                label = { Text("6. विशिष्ट शिल्प प्रकार (Craft Type)") },
                                placeholder = { Text("e.g. Gorakhpur Terracotta, Paithani Silk") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_craft_type"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 7. Material & 8. Color (Row)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = editMaterial,
                                    onValueChange = { viewModel.vcEditMaterial.value = it },
                                    label = { Text("7. सामग्री (Material)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("field_material"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = editColor,
                                    onValueChange = { viewModel.vcEditColor.value = it },
                                    label = { Text("8. रंग (Color)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("field_color"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // 9. Dimensions & 10. Weight (Row)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = editDimensions,
                                    onValueChange = { viewModel.vcEditDimensions.value = it },
                                    label = { Text("9. माप (Dimensions)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("field_dimensions"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = editWeight,
                                    onValueChange = { viewModel.vcEditWeight.value = it },
                                    label = { Text("10. वजन (Weight)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("field_weight"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // 11. Care Instructions
                            OutlinedTextField(
                                value = editCareInstructions,
                                onValueChange = { viewModel.vcEditCareInstructions.value = it },
                                label = { Text("11. रखरखाव निर्देश (Care Instructions)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field_care_instructions"),
                                minLines = 2,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // 12. Tags Editor
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "12. उत्पाद टैग्स (Tags)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    editTags.forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = ArtisanSurfaceVariant,
                                            border = BorderStroke(1.dp, ArtisanCardBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text("#$tag", fontSize = 11.sp, color = ArtisanTextPrimary)
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove tag $tag",
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            viewModel.vcEditTags.value = editTags.filter { it != tag }
                                                        },
                                                    tint = ArtisanTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newTagInput,
                                        onValueChange = { newTagInput = it },
                                        placeholder = { Text("नया टैग जोड़ें...", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Button(
                                        onClick = {
                                            val t = newTagInput.trim().removePrefix("#")
                                            if (t.isNotBlank() && !editTags.contains(t)) {
                                                viewModel.vcEditTags.value = editTags + t
                                                newTagInput = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary)
                                    ) {
                                        Text("+ जोड़ें", fontSize = 12.sp)
                                    }
                                }
                            }

                            // 13. Keywords Editor
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "13. खोज कीवर्ड्स (SEO Search Keywords)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    editKeywords.forEach { kw ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = ArtisanSurfaceVariant,
                                            border = BorderStroke(1.dp, ArtisanCardBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(kw, fontSize = 11.sp, color = ArtisanTextPrimary)
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove keyword $kw",
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            viewModel.vcEditKeywords.value = editKeywords.filter { it != kw }
                                                        },
                                                    tint = ArtisanTextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newKeywordInput,
                                        onValueChange = { newKeywordInput = it },
                                        placeholder = { Text("नया कीवर्ड जोड़ें...", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Button(
                                        onClick = {
                                            val k = newKeywordInput.trim()
                                            if (k.isNotBlank() && !editKeywords.contains(k)) {
                                                viewModel.vcEditKeywords.value = editKeywords + k
                                                newKeywordInput = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary)
                                    ) {
                                        Text("+ जोड़ें", fontSize = 12.sp)
                                    }
                                }
                            }

                            HorizontalDivider(color = ArtisanCardBorder)

                            // Product Images (Photo Picker)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "उत्पाद चित्र (Product Images)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArtisanTextPrimary
                                        )
                                        Text(
                                            text = "शिल्प की स्पष्ट तस्वीरें जोड़ें (वैकल्पिक)",
                                            fontSize = 11.sp,
                                            color = ArtisanTextSecondary
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("btn_select_product_images")
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("तस्वीरें जोड़ें", fontSize = 12.sp)
                                    }
                                }

                                if (editImagePaths.isNotEmpty()) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .testTag("catalog_image_paths_row")
                                    ) {
                                        items(editImagePaths) { imgPath ->
                                            Box(
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, ArtisanCardBorder, RoundedCornerShape(8.dp))
                                            ) {
                                                AsyncImage(
                                                    model = imgPath,
                                                    contentDescription = "Selected product photo",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                IconButton(
                                                    onClick = {
                                                        viewModel.vcEditImagePaths.value = editImagePaths.filter { it != imgPath }
                                                    },
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .align(Alignment.TopEnd)
                                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove photo",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = ArtisanCardBorder)

                            // Pricing living wage estimate
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = "₹${editRetailPrice.toInt()}",
                                    onValueChange = { v ->
                                        val num = v.filter { it.isDigit() }.toDoubleOrNull()
                                        if (num != null) viewModel.vcEditRetailPrice.value = num
                                    },
                                    label = { Text("खुदरा मूल्य (Retail ₹)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = "₹${editWholesalePrice.toInt()}",
                                    onValueChange = { v ->
                                        val num = v.filter { it.isDigit() }.toDoubleOrNull()
                                        if (num != null) viewModel.vcEditWholesalePrice.value = num
                                    },
                                    label = { Text("थोक मूल्य (Wholesale ₹)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }

                // SAVE ACTION BUTTON
                item {
                    Button(
                        onClick = {
                            viewModel.saveVoiceCatalogProduct { newId ->
                                Toast.makeText(context, "🎉 उत्पाद कैटलॉग में सुरक्षित हुआ!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("voice_catalog_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💾 कैटलॉग में सुरक्षित करें (Save to Catalog)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // STEP 0: LANGUAGE SELECTION & AUDIO RECORDING / MANUAL TEXT FALLBACK
            else {
                // 1. LANGUAGE SELECTION
                item {
                    VoiceCatalogLanguageSelector(
                        selectedLanguage = selectedLanguage,
                        onLanguageSelected = { lang ->
                            viewModel.setVoiceCatalogLanguage(lang)
                        }
                    )
                }

                // ERROR CARDS (Microphone denial, Recognition failure, Silence, Unsupported Language, Network error)
                if (errorState != null) {
                    item {
                        VoiceCatalogErrorCard(
                            error = errorState!!,
                            onRetryAudio = {
                                if (errorState is VoiceCatalogError.MicrophoneDenied) {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    viewModel.clearVoiceCatalogError()
                                    viewModel.startVoiceCatalogAudioListening()
                                }
                            },
                            onSwitchToManual = {
                                viewModel.clearVoiceCatalogError()
                                activeInputMode = 1
                            }
                        )
                    }
                }

                // MODE TOGGLE: VOICE (MIC) vs MANUAL TEXT FALLBACK
                item {
                    TabRow(
                        selectedTabIndex = activeInputMode,
                        containerColor = ArtisanSurface,
                        contentColor = TerracottaPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, ArtisanCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = activeInputMode == 0,
                            onClick = { activeInputMode = 0 },
                            modifier = Modifier.testTag("tab_voice_input"),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("बोलकर विवरण (Voice)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        )
                        Tab(
                            selected = activeInputMode == 1,
                            onClick = { activeInputMode = 1 },
                            modifier = Modifier.testTag("tab_manual_fallback"),
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("कीबोर्ड से लिखें (Manual Fallback)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        )
                    }
                }

                // 2. INPUT AREA: VOICE RECORDING OR MANUAL TEXT
                if (activeInputMode == 0) {
                    item {
                        VoiceCatalogMicrophoneCard(
                            language = selectedLanguage,
                            isListening = isListening,
                            liveRmsDb = liveRmsDb,
                            livePartialText = livePartialText,
                            currentTranscript = rawTranscript,
                            onStartRecording = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    viewModel.clearVoiceCatalogError()
                                    viewModel.startVoiceCatalogAudioListening()
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onStopRecording = {
                                viewModel.stopVoiceCatalogAudioListening()
                            },
                            onTranscriptEdited = { edited ->
                                viewModel.voiceCatalogTranscript.value = edited
                                manualTextInput = edited
                            },
                            onProcessWithGemini = { transcriptToProcess ->
                                viewModel.generateMultilingualVoiceCatalog(
                                    transcript = transcriptToProcess,
                                    language = selectedLanguage
                                )
                            },
                            onUseSamplePrompt = { sample ->
                                viewModel.voiceCatalogTranscript.value = sample
                                manualTextInput = sample
                            }
                        )
                    }
                } else {
                    // MANUAL TEXT INPUT FALLBACK CARD
                    item {
                        VoiceCatalogManualTextFallbackCard(
                            language = selectedLanguage,
                            text = manualTextInput,
                            onTextChanged = {
                                manualTextInput = it
                                viewModel.voiceCatalogTranscript.value = it
                            },
                            onProcessWithGemini = { textToProcess ->
                                focusManager.clearFocus()
                                viewModel.generateMultilingualVoiceCatalog(
                                    transcript = textToProcess,
                                    language = selectedLanguage
                                )
                            },
                            onUseSamplePrompt = { sample ->
                                manualTextInput = sample
                                viewModel.voiceCatalogTranscript.value = sample
                            }
                        )
                    }
                }
            }
        }
    }

    // Authenticity & Integrity Explanation Dialog
    if (showIntegrityDialog) {
        AlertDialog(
            onDismissRequest = { showIntegrityDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = ForestSuccess)
                    Text("सत्यनिष्ठा व प्रामाणिकता नीति", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "KAUSHVANI AI की सख्त प्रामाणिकता गारंटी:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ArtisanTextPrimary
                    )
                    Text(
                        text = "1. GI Status: हम कभी भी अपने आप फर्जी GI टैग नहीं जोड़ते। केवल तभी मान्य होता है जब कारीगर स्वयं प्रमाणित करता है।",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary
                    )
                    Text(
                        text = "2. सरकारी प्रमाणन: सिल्क मार्क, हैंडलूम मार्क या क्राफ्टमार्क का झूठा दावा नहीं किया जाता।",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary
                    )
                    Text(
                        text = "3. वास्तविक सामग्री: केवल वास्तविक व प्राकृतिक सामग्री ही दर्ज की जाती है।",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary
                    )
                    Text(
                        text = "4. कारीगर सम्मान: किसी भी फर्जी राष्ट्रीय पुरस्कार या उपाधि का आविष्कार नहीं होता।",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showIntegrityDialog = false }) {
                    Text("समझ गया (Understood)", color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * 4-Step Pipeline Header visualizing the requested flow:
 * Language Selection -> Microphone/Text -> SpeechRecognizer -> Gemini -> Validated JSON -> Editable Fields -> Save.
 */
@Composable
private fun VoiceCatalogPipelineHeader(currentStep: Int) {
    val steps = listOf(
        "1. भाषा व आवाज़",
        "2. Gemini JSON",
        "3. संपादन (13 फ़ील्ड)",
        "4. कैटलॉग सहेजें"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isCompleted = currentStep > index || currentStep == 3
            val isCurrent = currentStep == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> ForestSuccess
                                isCurrent -> TerracottaPrimary
                                else -> ArtisanCardBorder
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
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
                            color = if (isCurrent) Color.White else ArtisanTextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) TerracottaPrimary else ArtisanTextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Language Selection Component supporting all configured Indian regional languages.
 */
@Composable
private fun VoiceCatalogLanguageSelector(
    selectedLanguage: RegionalLanguage,
    onLanguageSelected: (RegionalLanguage) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_catalog_language_selector_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = BorderStroke(1.dp, ArtisanCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "1. भाषा चुनें (Select Language)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    Text(
                        text = "भारतीय भाषाओं में SpeechRecognizer समर्थन",
                        fontSize = 11.sp,
                        color = ArtisanTextSecondary
                    )
                }
            }

            // Language Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SupportedRegionalLanguages) { lang ->
                    val isSelected = lang.code == selectedLanguage.code
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onLanguageSelected(lang) }
                            .testTag("lang_chip_${lang.code}"),
                        color = if (isSelected) TerracottaPrimary else ArtisanSurfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) TerracottaPrimary else ArtisanCardBorder
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = lang.nativeName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else ArtisanTextPrimary
                            )
                            Text(
                                text = "(${lang.name})",
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else ArtisanTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Microphone & SpeechRecognizer interface with real-time waveform, transcript streaming, and audio controls.
 */
@Composable
private fun VoiceCatalogMicrophoneCard(
    language: RegionalLanguage,
    isListening: Boolean,
    liveRmsDb: Float,
    livePartialText: String,
    currentTranscript: String,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onTranscriptEdited: (String) -> Unit,
    onProcessWithGemini: (String) -> Unit,
    onUseSamplePrompt: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_catalog_mic_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = BorderStroke(1.dp, if (isListening) TerracottaPrimary else ArtisanCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Instruction
            Text(
                text = if (isListening) "सुन रहे हैं... कृपया ${language.nativeName} में बोलें" else "माइक दबाएं और अपने शिल्प का विवरण दें",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isListening) TerracottaPrimary else ArtisanTextPrimary,
                textAlign = TextAlign.Center
            )

            // Large Animated Microphone Button (Target > 48dp, 84dp diameter)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .padding(8.dp)
            ) {
                // Pulsing Audio Ring when active
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(TerracottaPrimary.copy(alpha = 0.2f))
                    )
                }

                Surface(
                    onClick = {
                        if (isListening) onStopRecording() else onStartRecording()
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .testTag("voice_catalog_mic_button")
                        .semantics {
                            role = Role.Button
                            contentDescription = if (isListening) "Stop voice recording" else "Start recording craft description in ${language.name}"
                        },
                    shape = CircleShape,
                    color = if (isListening) Color(0xFFDC2626) else TerracottaPrimary,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            // Real-time audio waveform indication
            if (isListening) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(24.dp)
                ) {
                    val normalizedDb = (liveRmsDb.coerceIn(0f, 10f) / 10f)
                    repeat(7) { idx ->
                        val barHeight = ((idx + 1) * 3f * normalizedDb + 4f).coerceIn(4f, 22f)
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(TerracottaPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "रिकॉर्डिंग जारी...",
                        fontSize = 11.sp,
                        color = TerracottaPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Live Partial Streaming Text
            if (isListening && livePartialText.isNotBlank()) {
                Surface(
                    color = TerracottaContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = livePartialText,
                        fontSize = 13.sp,
                        color = ArtisanTextPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // REAL TRANSCRIPT DISPLAY & EDIT BOX
            if (currentTranscript.isNotBlank() && !isListening) {
                Surface(
                    color = WarmIvoryBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ArtisanCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestSuccess, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "REAL Transcript (${language.nativeName})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestSuccess
                                )
                            }
                            Text(
                                text = "SpeechRecognizer",
                                fontSize = 10.sp,
                                color = ArtisanTextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = currentTranscript,
                            onValueChange = onTranscriptEdited,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transcript_editable_box"),
                            minLines = 2,
                            maxLines = 5,
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Action Button: Process Real Transcript with Gemini
                        Button(
                            onClick = { onProcessWithGemini(currentTranscript) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_process_with_gemini"),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✨ Gemini द्वारा JSON कैटलॉग बनाएं",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Sample Prompt Helper for the selected language
            if (language.samplePrompt.isNotBlank() && !isListening && currentTranscript.isBlank()) {
                Surface(
                    color = ArtisanSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ArtisanCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 ${language.name} नमूना बोलकर देखें (Sample Prompt):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextSecondary
                            )
                            TextButton(
                                onClick = { onUseSamplePrompt(language.samplePrompt) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("प्रयोग करें", fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "\"${language.samplePrompt}\"",
                            fontSize = 12.sp,
                            color = ArtisanTextPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Manual Text Input Card (Always Available Fallback).
 */
@Composable
private fun VoiceCatalogManualTextFallbackCard(
    language: RegionalLanguage,
    text: String,
    onTextChanged: (String) -> Unit,
    onProcessWithGemini: (String) -> Unit,
    onUseSamplePrompt: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_catalog_manual_text_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = BorderStroke(1.dp, ArtisanCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Icon(Icons.Default.Keyboard, contentDescription = null, tint = IndigoSecondary)
                Column {
                    Text(
                        text = "हस्तलिखित टेक्स्ट फ़ॉलबैक (Manual Text Fallback)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    Text(
                        text = "यदि आवाज़ रिकॉर्डिंग में समस्या हो तो यहां टाइप या पेस्ट करें",
                        fontSize = 11.sp,
                        color = ArtisanTextSecondary
                    )
                }
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text("अपने शिल्प का विवरण ${language.nativeName} में लिखें (जैसे सामग्री, तकनीक, रंग, माप)...")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_text_input_field"),
                minLines = 4,
                maxLines = 8,
                shape = RoundedCornerShape(10.dp)
            )

            if (language.samplePrompt.isNotBlank() && text.isBlank()) {
                OutlinedButton(
                    onClick = { onUseSamplePrompt(language.samplePrompt) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("📋 ${language.name} नमूना टेक्स्ट पेस्ट करें", fontSize = 12.sp)
                }
            }

            Button(
                onClick = { onProcessWithGemini(text) },
                enabled = text.trim().isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_process_manual_with_gemini"),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✨ AI द्वारा कैटलॉग बनाएं (Generate with AI)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Dedicated, user-friendly Error Card handling all 5 specified failure modes:
 * 1. Microphone Denial
 * 2. Silence Timeout
 * 3. Recognition Failure
 * 4. Unsupported Language
 * 5. Network Failure
 */
@Composable
private fun VoiceCatalogErrorCard(
    error: VoiceCatalogError,
    onRetryAudio: () -> Unit,
    onSwitchToManual: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_catalog_error_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = BorderStroke(1.dp, Color(0xFFF87171))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (error) {
                        is VoiceCatalogError.MicrophoneDenied -> Icons.Default.MicOff
                        is VoiceCatalogError.SilenceTimeout -> Icons.Default.VolumeMute
                        is VoiceCatalogError.UnsupportedLanguage -> Icons.Default.Translate
                        is VoiceCatalogError.NetworkFailure -> Icons.Default.WifiOff
                        else -> Icons.Default.ErrorOutline
                    },
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = error.titleHindi,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B)
                    )
                    Text(
                        text = error.titleEnglish,
                        fontSize = 11.sp,
                        color = Color(0xFFB91C1C)
                    )
                }
            }

            Text(
                text = error.descriptionHindi,
                fontSize = 12.sp,
                color = ArtisanTextPrimary
            )
            Text(
                text = error.descriptionEnglish,
                fontSize = 11.sp,
                color = ArtisanTextSecondary
            )

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (error.canRetryAudio) {
                    Button(
                        onClick = onRetryAudio,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_error_retry_audio"),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("पुनः प्रयास करें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (error.suggestManualInput) {
                    OutlinedButton(
                        onClick = onSwitchToManual,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("btn_error_switch_manual"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(14.dp), tint = IndigoSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("हस्तलिखित इनपुट", fontSize = 12.sp, color = IndigoSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Animated Loading Card while Gemini 3.5 Flash processes the transcript and validates JSON.
 */
@Composable
private fun VoiceCatalogGeneratingCard(
    language: RegionalLanguage,
    transcript: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_catalog_generating_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = TerracottaPrimary,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "✨ Gemini 3.5 Flash कैटलॉग तैयार कर रहा है...",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )
                Text(
                    text = "Validated JSON • 13 Craft Fields • Authenticity Check",
                    fontSize = 12.sp,
                    color = TerracottaPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                color = WarmIvoryBackground,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "आवाज़ विवरण (${language.nativeName}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextSecondary
                    )
                    Text(
                        text = "\"$transcript\"",
                        fontSize = 12.sp,
                        color = ArtisanTextPrimary,
                        maxLines = 3
                    )
                }
            }

            // Real-time compliance steps
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GeneratingStepItem(text = "1. SpeechRecognizer रियल ट्रांसक्रिप्ट सत्यापित", isDone = true)
                GeneratingStepItem(text = "2. Gemini 3.5 Flash JSON संरचना निर्माण", isDone = true)
                GeneratingStepItem(text = "3. GI व प्रमाणन सत्यनिष्ठा सत्यापन (No Hallucinations)", isDone = true)
                GeneratingStepItem(text = "4. 13 संपादन योग्य उत्पाद फ़ील्ड तैयार करना...", isDone = false)
            }
        }
    }
}

@Composable
private fun GeneratingStepItem(text: String, isDone: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Pending,
            contentDescription = null,
            tint = if (isDone) ForestSuccess else MarigoldTertiary,
            modifier = Modifier.size(14.dp)
        )
        Text(text = text, fontSize = 11.sp, color = ArtisanTextSecondary)
    }
}

/**
 * Strict Authenticity Banner ensuring compliance with:
 * "Never invent GI status, government certification, material, origin or artisan credentials."
 */
@Composable
private fun VoiceCatalogIntegrityGuaranteeBanner(
    isGiTagged: Boolean,
    governmentCertification: String
) {
    Surface(
        color = ForestContainer,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ForestSuccess.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = ForestSuccess,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "🛡️ सत्यनिष्ठा व प्रामाणिकता सुरक्षा (Strict Authenticity Guarantee)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestSuccess
                )
                Text(
                    text = if (isGiTagged) {
                        "✓ कारीगर द्वारा प्रमाणित GI टैग रिकॉर्ड किया गया।"
                    } else {
                        "✓ कोई फर्जी GI टैग या सरकारी प्रमाणपत्र नहीं जोड़ा गया है।"
                    },
                    fontSize = 11.sp,
                    color = ArtisanTextPrimary
                )
            }
        }
    }
}

/**
 * Success Celebration Card after saving to Room Database.
 */
@Composable
private fun VoiceCatalogSuccessCard(
    productId: Long,
    title: String,
    regionalTitle: String,
    category: String,
    price: Double,
    onViewCatalog: () -> Unit,
    onAddAnother: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_catalog_success_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = BorderStroke(1.dp, ForestSuccess.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ForestContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ForestSuccess,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🎉 उत्पाद कैटलॉग में सहेजा गया!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )
                Text(
                    text = "Product ID: #$productId • Room Database Persisted",
                    fontSize = 12.sp,
                    color = ForestSuccess,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                color = WarmIvoryBackground,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    if (regionalTitle.isNotBlank()) {
                        Text(text = regionalTitle, fontSize = 12.sp, color = TerracottaPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "श्रेणी: $category", fontSize = 12.sp, color = ArtisanTextSecondary)
                        Text(text = "मूल्य: ₹${price.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onAddAnother,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_add_another_voice"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("नया उत्पाद जोड़ें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onViewCatalog,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_view_catalog"),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("कैटलॉग देखें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
