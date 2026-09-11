package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import java.util.Locale

data class RegionalLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val localeTag: String,
    val samplePrompt: String = "",
    val isPrimaryTier: Boolean = false
)

val SupportedRegionalLanguages = listOf(
    RegionalLanguage(
        code = "hi",
        name = "Hindi",
        nativeName = "हिन्दी",
        localeTag = "hi-IN",
        samplePrompt = "मिट्टी का बड़ा कलश है, 2 दिन चाक पर गढ़ा गया है, प्राकृतिक टेराकोटा रंगों से पकाया है।",
        isPrimaryTier = true
    ),
    RegionalLanguage(
        code = "en",
        name = "English",
        nativeName = "English",
        localeTag = "en-IN",
        samplePrompt = "Traditional Paithani silk saree handwoven with pure mulberry silk and gold zari border.",
        isPrimaryTier = true
    ),
    RegionalLanguage(
        code = "mr",
        name = "Marathi",
        nativeName = "मराठी",
        localeTag = "mr-IN",
        samplePrompt = "हा पैठणी साडी आहे. ही रेशमाची आहे. पारंपरिक मोराची नक्षी हातमागावर विणली आहे.",
        isPrimaryTier = true
    ),
    RegionalLanguage(
        code = "gu",
        name = "Gujarati",
        nativeName = "ગુજરાતી",
        localeTag = "gu-IN",
        samplePrompt = "હાથેથી બનાવેલ બાંધણી દુપટ્ટો, કુદરતી રંગો અને આભલા ભરતકામ સાથે."
    ),
    RegionalLanguage(
        code = "bn",
        name = "Bengali",
        nativeName = "বাংলা",
        localeTag = "bn-IN",
        samplePrompt = "বাঁকুড়ার টেরাকোটা ঘোড়া, নিখুঁত হাতে গড়া ও লাল মাটির পোড়ামাটি শিল্প।"
    ),
    RegionalLanguage(
        code = "ta",
        name = "Tamil",
        nativeName = "தமிழ்",
        localeTag = "ta-IN",
        samplePrompt = "தஞ்சாவூர் வெண்கல நடராஜர் சிலை, பாரம்பரிய மெழுகு வார்ப்பு முறையில் செய்யப்பட்டது."
    ),
    RegionalLanguage(
        code = "te",
        name = "Telugu",
        nativeName = "తెలుగు",
        localeTag = "te-IN",
        samplePrompt = "ధర్మవరం పట్టు చీర, చేనేత మగ్గంపై 8 రోజుల్లో నేసిన బంగారు జరీ అంచు."
    ),
    RegionalLanguage(
        code = "kn",
        name = "Kannada",
        nativeName = "ಕನ್ನಡ",
        localeTag = "kn-IN",
        samplePrompt = "ಚನ್ನಪಟ್ಟಣ ಮರದ ಆಟಿಕೆಗಳು, ನೈಸರ್ಗಿಕ ಬಣ್ಣಗಳಿಂದ ಮೆರುಗುಗೊಳಿಸಿದ ಸುರಕ್ಷಿತ ಕರಕುಶಲ ವಸ್ತು."
    ),
    RegionalLanguage(
        code = "or",
        name = "Odia",
        nativeName = "ଓଡ଼ିଆ",
        localeTag = "or-IN",
        samplePrompt = "ପିପିଲି ଚାନ୍ଦୁଆ ହାତକାମ, ପାରମ୍ପରିକ ରଙ୍ଗବେରଙ୍ଗ କପଡ଼ା ଆପ୍ଲିକ କାମ।"
    ),
    RegionalLanguage(
        code = "pa",
        name = "Punjabi",
        nativeName = "ਪੰਜਾਬੀ",
        localeTag = "pa-IN",
        samplePrompt = "ਹੱਥ ਨਾਲ ਕੱਢੀ ਫੁਲਕਾਰੀ ਦੁਪੱਟਾ, ਰੇਸ਼ਮੀ ਧਾਗਿਆਂ ਨਾਲ ਕੀਤੀ ਬਰੀਕ ਕਸੀਦਾਕਾਰੀ।"
    )
)

@Composable
fun VoiceInputBar(
    currentTranscript: String,
    onTranscriptChanged: (String) -> Unit,
    selectedLanguage: RegionalLanguage,
    onLanguageSelected: (RegionalLanguage) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "अपनी भाषा में बोलें (Tap mic to speak)..."
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    // Speech Recognizer Launcher for native Google Voice typing
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val updated = if (currentTranscript.isBlank()) spokenText else "$currentTranscript $spokenText"
                onTranscriptChanged(updated)
            }
        }
    }

    // Animation for active microphone pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val sampleVoicePrompts = listOf(
        "मिट्टी का बड़ा कलश है, 2 दिन चाक पर गढ़ा गया है, प्राकृतिक रंगों से पकाया है।",
        "शुद्ध कतान रेशम का दुपट्टा है, असली सुनहरी ज़री का काम, 12 दिन में बुना है।",
        "बस्तर का ढोकरा पीतल का घोड़ा है, प्राचीन मोम विधि से ढलाई की है।",
        "Mithila Madhubani handmade painting with natural vegetable dyes on silk canvas.",
        "शीशम की लकड़ी पर हाथ की बारीक नक्काशीदार जाली का काम किया गया है।"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TerracottaContainer.copy(alpha = 0.65f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TerracottaPrimary.copy(alpha = 0.35f)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Language selector row
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
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Language",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "बोलने की भाषा (Voice Language):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ArtisanTextPrimary
                    )
                }

                // Current Language Badge
                Surface(
                    color = TerracottaPrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${selectedLanguage.nativeName} (${selectedLanguage.code.uppercase()})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Horizontal Language Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SupportedRegionalLanguages) { lang ->
                    val isSelected = lang.code == selectedLanguage.code
                    Surface(
                        modifier = Modifier
                            .semantics {
                                role = Role.Tab
                                selected = isSelected
                                contentDescription = "${lang.nativeName} (${lang.name})"
                                stateDescription = if (isSelected) "चयनित भाषा (Selected Language)" else "उपलब्ध भाषा (Available Language)"
                            }
                            .clickable { onLanguageSelected(lang) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TerracottaPrimary else ArtisanSurface,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder),
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Text(
                            text = "${lang.nativeName}",
                            color = if (isSelected) Color.White else ArtisanTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Live Voice Input Box with animated Mic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ArtisanSurface)
                    .border(1.dp, if (isListening) TerracottaPrimary else ArtisanCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Big Voice Record Button
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFFDC2626) else TerracottaPrimary)
                            .semantics {
                                role = Role.Button
                                contentDescription = if (isListening) "रिकॉर्डिंग जारी है, रोकने के लिए टैप करें (Recording in progress, tap to stop)" else "आवाज़ से विवरण बोलने के लिए टैप करें (Tap to speak craft details in ${selectedLanguage.nativeName})"
                                stateDescription = if (isListening) "Listening" else "Idle"
                            }
                            .clickable {
                                try {
                                    isListening = true
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                        )
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.localeTag)
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "KAUSHVANI: अपने शिल्प का विवरण बोलें...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    isListening = false
                                    Toast.makeText(context, "Voice input starting...", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Transcript Text or Placeholder
                    Column(modifier = Modifier.weight(1f)) {
                        if (currentTranscript.isNotBlank()) {
                            Text(
                                text = currentTranscript,
                                fontSize = 14.sp,
                                color = ArtisanTextPrimary,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = if (isListening) "सुन रहे हैं... (Listening...)" else placeholderText,
                                fontSize = 13.sp,
                                color = if (isListening) TerracottaPrimary else ArtisanTextMuted,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        if (isListening) {
                            Text(
                                text = "● Recording audio in ${selectedLanguage.name}...",
                                fontSize = 11.sp,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    if (currentTranscript.isNotBlank()) {
                        IconButton(
                            onClick = { onTranscriptChanged("") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = ArtisanTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Quick Example Voice Prompts (Minimal typing for artisan convenience)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "त्वरित उदाहरण (Tap sample voice note):",
                    fontSize = 11.sp,
                    color = ArtisanTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sampleVoicePrompts) { prompt ->
                        Surface(
                            modifier = Modifier.clickable {
                                onTranscriptChanged(prompt)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = ArtisanSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = prompt.take(35) + "...",
                                    fontSize = 11.sp,
                                    color = ArtisanTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
