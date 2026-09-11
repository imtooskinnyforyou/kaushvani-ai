package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RegionalLanguage
import com.example.ui.components.SupportedRegionalLanguages
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel
import kotlinx.coroutines.launch

/**
 * KAARIGAR — Language Preferences & SpeechRecognizer Locale Configuration Screen
 *
 * Persists the chosen regional language to:
 * 1. User document in Cloud Firestore (`users/{uid}`)
 * 2. Jetpack DataStore local user preferences
 * 3. SpeechRecognizer locale configuration for the "Add Product" (HunarSetu) workflow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePreferencesScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = { viewModel.navigateTo(AppNavTab.ARTISAN_HOME) }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentWizardLanguage by viewModel.wizardLanguage.collectAsStateWithLifecycle()
    val preferredLanguageFromPref by viewModel.preferredLanguage.collectAsStateWithLifecycle()
    val artisanProfile by viewModel.artisanProfile.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLang by remember(preferredLanguageFromPref, currentWizardLanguage) {
        mutableStateOf(currentWizardLanguage)
    }
    var isSaving by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var testAudioPlayingCode by remember { mutableStateOf<String?>(null) }

    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            SupportedRegionalLanguages
        } else {
            val q = searchQuery.trim().lowercase()
            SupportedRegionalLanguages.filter {
                it.name.lowercase().contains(q) ||
                it.nativeName.lowercase().contains(q) ||
                it.code.lowercase().contains(q) ||
                it.localeTag.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("language_preferences_screen"),
        topBar = {
            Surface(
                color = ArtisanSurface,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("language_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "पीछे जाएं (Go Back)",
                            tint = TerracottaPrimary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "भाषा प्राथमिकताएं (Language Settings)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "SpeechRecognizer और AI कैटलॉग भाषा चुनें",
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TerracottaContainer
                    ) {
                        Text(
                            text = selectedLang.code.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Live Status Banner: SpeechRecognizer & Firestore sync
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speech_recognizer_status_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.3f))
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(TerracottaPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "सक्रिय स्पीच पहचान (Speech Recognizer)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                    Text(
                                        text = "Add Product विजार्ड व AI ट्रांसक्रिप्शन कॉन्फ़िगरेशन",
                                        fontSize = 11.sp,
                                        color = ArtisanTextSecondary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TerracottaPrimary
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = TerracottaPrimary.copy(alpha = 0.2f))

                        // Configuration Details
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ConfigStatusRow(
                                label = "चयनित भाषा (Language):",
                                value = "${selectedLang.nativeName} (${selectedLang.name})"
                            )
                            ConfigStatusRow(
                                label = "SpeechRecognizer Locale:",
                                value = selectedLang.localeTag
                            )
                            ConfigStatusRow(
                                label = "Cloud Firestore Sync:",
                                value = "users/{uid} document auto-sync"
                            )
                            ConfigStatusRow(
                                label = "Add Product Workflow:",
                                value = "STT extraLanguage = '${selectedLang.localeTag}'"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    testAudioPlayingCode = selectedLang.code
                                    val sampleText = when (selectedLang.code) {
                                        "hi" -> "नमस्ते! आपकी आवाज़ पहचानी जाएगी।"
                                        "mr" -> "नमस्कार! तुमचा आवाज ओळखला जाईल."
                                        "gu" -> "નમસ્તે! તમારો અવાજ ઓળખાશે."
                                        "bn" -> "নমস্কার! আপনার ভয়েস রেকর্ড হবে।"
                                        "ta" -> "வணக்கம்! உங்கள் குரல் பதிவு செய்யப்படும்."
                                        "te" -> "నమస్కారం! మీ వాయిస్ గుర్తించబడుతుంది."
                                        "kn" -> "ನಮಸ್ಕಾರ! ನಿಮ್ಮ ಧ್ವನಿ ಗುರುತಿಸಲಾಗುವುದು."
                                        "or" -> "ନମସ୍କାର! ଆପଣଙ୍କ ସ୍ୱର ଚିହ୍ନଟ ହେବ।"
                                        "pa" -> "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ! ਤੁਹਾਡੀ ਆਵਾਜ਼ ਪਛਾਣੀ ਜਾਵੇਗੀ।"
                                        else -> "Hello! Your speech recognizer locale is configured."
                                    }
                                    viewModel.speakText(sampleText, selectedLang.code)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_voice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "उच्चारण सुनें (Test TTS)",
                                    fontSize = 11.sp,
                                    color = TerracottaPrimary
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.wizardLanguage.value = selectedLang
                                    viewModel.wizardStep.value = 1
                                    viewModel.navigateTo(AppNavTab.SMART_CATALOG_WIZARD)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("try_in_add_product_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add Product में आज़माएं",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Success feedback notification
            if (saveSuccessMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        border = BorderStroke(1.dp, Color(0xFF16A34A))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = saveSuccessMessage ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Search and Language Selection Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "उपलब्ध भारतीय क्षेत्रीय भाषाएं (${SupportedRegionalLanguages.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    Text(
                        text = "अपनी क्षेत्रीय भाषा का चयन करें। यह भाषा स्पीच-टू-टेक्स्ट और कैटलॉग निर्माण दोनों के लिए लागू होगी:",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("language_search_input"),
                        placeholder = { Text("भाषा खोजें (Search Hindi, Marathi, Tamil...)", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = ArtisanTextSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = ArtisanTextSecondary
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = ArtisanCardBorder
                        )
                    )
                }
            }

            // List of Languages
            items(filteredLanguages) { lang ->
                val isSelected = selectedLang.code == lang.code
                LanguageCard(
                    language = lang,
                    isSelected = isSelected,
                    onSelect = {
                        selectedLang = lang
                        isSaving = true
                        saveSuccessMessage = null
                        viewModel.updateUserLanguagePreference(lang) { success ->
                            isSaving = false
                            if (success) {
                                saveSuccessMessage = "✅ भाषा '${lang.nativeName}' Firestore और SpeechRecognizer में सफलतापूर्वक अपडेट हो गई!"
                            }
                        }
                    },
                    onPreviewAudio = {
                        val previewText = when (lang.code) {
                            "hi" -> "नमस्ते! कौशवाणी (KAUSHVANI) में आपका स्वागत है।"
                            "mr" -> "नमस्कार! कौशवाणी (KAUSHVANI) मध्ये आपले स्वागत आहे."
                            "gu" -> "નમસ્તે! કૌશવાણી (KAUSHVANI) માં આપનું સ્વાગત છે."
                            "bn" -> "নমস্কার! কৌশবাণী (KAUSHVANI) তে আপনাকে স্বাগতম।"
                            "ta" -> "வணக்கம்! கௌஷவாணி (KAUSHVANI) உங்களை வரவேற்கிறது."
                            "te" -> "నమస్కారం! కౌశవాణి (KAUSHVANI) కి స్వాగతం."
                            "kn" -> "ನಮಸ್ಕಾರ! ಕೌಶವಾಣಿ (KAUSHVANI) ಗೆ ಸ್ವಾಗತ."
                            "or" -> "ନମସ୍କାର! କୌଶବାଣୀ (KAUSHVANI) କୁ ସ୍ୱାଗତ।"
                            "pa" -> "ਸਤਿ ਸ੍ਰੀ ਅਕਾਲ! ਕੌਸ਼ਵਾਣੀ (KAUSHVANI) ਵਿੱਚ ਤੁਹਾਡਾ ਸੁਆਗਤ ਹੈ।"
                            else -> "Hello! Welcome to KAUSHVANI."
                        }
                        viewModel.speakText(previewText, lang.code)
                    }
                )
            }

            // Save Confirmation Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isSaving = true
                        saveSuccessMessage = null
                        viewModel.updateUserLanguagePreference(selectedLang) { success ->
                            isSaving = false
                            if (success) {
                                saveSuccessMessage = "✅ भाषा '${selectedLang.nativeName} (${selectedLang.localeTag})' Firestore और SpeechRecognizer में सुरक्षित!"
                                Toast.makeText(context, "✅ भाषा प्राथमिकता अपडेट हो गई!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_language_preference_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Firestore में अपडेट हो रहा है...", fontSize = 14.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "प्राथमिकता सुरक्षित करें (Save & Apply)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigStatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = ArtisanTextSecondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = ArtisanTextPrimary
        )
    }
}

@Composable
private fun LanguageCard(
    language: RegionalLanguage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreviewAudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = "${language.name} - ${language.nativeName} (Locale: ${language.localeTag})"
            }
            .clickable { onSelect() }
            .testTag("language_card_${language.code}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TerracottaContainer.copy(alpha = 0.4f) else ArtisanSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) TerracottaPrimary else ArtisanCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Circular Indicator
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) TerracottaPrimary else ArtisanSurfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = language.code.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = language.nativeName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) TerracottaPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = language.localeTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) TerracottaPrimary else ArtisanTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${language.name} • SpeechRecognizer STT Supported",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary
                    )
                }
            }

            // Audio Sample Preview Button
            IconButton(
                onClick = onPreviewAudio,
                modifier = Modifier.testTag("preview_audio_${language.code}")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "उच्चारण सुनें (${language.nativeName})",
                    tint = if (isSelected) TerracottaPrimary else ArtisanTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
