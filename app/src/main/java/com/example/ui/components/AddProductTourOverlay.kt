package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Tour Step Data Model for 'Add Product' Artisan Onboarding
 */
data class AddProductTourStepItem(
    val stepIndex: Int,
    val titleHindi: String,
    val titleEnglish: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconContainerColor: Color,
    val descriptionHindi: String,
    val descriptionEnglish: String,
    val keyTips: List<Pair<String, String>>, // (Hindi, English)
    val audioExplanationHindi: String,
    val actionSuggestion: String
)

val AddProductTourSteps = listOf(
    AddProductTourStepItem(
        stepIndex = 0,
        titleHindi = "१. शिल्प फोटोग्राफी एवं AI स्टूडियो",
        titleEnglish = "1. Craft Photography & AI Studio",
        icon = Icons.Default.CameraAlt,
        iconColor = TerracottaPrimary,
        iconContainerColor = TerracottaContainer,
        descriptionHindi = "अपने हस्तशिल्प की साफ़ तस्वीर लें। AI स्टूडियो वर्कशॉप का बिखरा सामान हटाकर प्रामाणिक भारतीय बैकड्रॉप्स (टेराकोटा, लिनेन, सागौन की लकड़ी) जोड़ता है।",
        descriptionEnglish = "Capture a clear photo of your craft. The AI Studio automatically removes workshop clutter and adds neutral authentic backdrops without altering genuine handmade details.",
        keyTips = listOf(
            "📐 1:1 स्क्वायर या 4:3 कैटलॉग साइज़ चुनें" to "Choose 1:1 Square or 4:3 Catalog aspect ratio",
            "✨ 'स्वचालित सुधार' से रोशनी और तीखापन बढ़ाएँ" to "Tap 'Auto Enhance' to optimize lighting & sharpness",
            "🛡️ हस्तशिल्प की वास्तविकता सुरक्षित रहती है" to "Handmade texture fidelity is 100% preserved"
        ),
        audioExplanationHindi = "पहला कदम है फोटोग्राफी। अपने उत्पाद की तस्वीर खींचें या गैलरी से चुनें। एआई स्टूडियो आपके उत्पाद की रोशनी और पृष्ठभूमि को ई-कॉमर्स के लिए तैयार करेगा।",
        actionSuggestion = "कैमरा खोलें या सैंपल तस्वीर चुनें"
    ),
    AddProductTourStepItem(
        stepIndex = 1,
        titleHindi = "२. मातृभाषा में आवाज़ रिकॉर्डिंग",
        titleEnglish = "2. Regional Voice Description",
        icon = Icons.Default.Mic,
        iconColor = Color(0xFFDC2626),
        iconContainerColor = Color(0xFFFEE2E2),
        descriptionHindi = "टाइपिंग की कोई ज़रूरत नहीं! माइक दबाएं और हिंदी, मराठी, तमिल, गुजराती या 12+ क्षेत्रीय भाषाओं में अपने शिल्प की खासियत बोलें।",
        descriptionEnglish = "No typing required! Tap the microphone and speak naturally in Hindi, Marathi, Tamil, Gujarati, or 12+ regional languages describing your craft materials and heritage.",
        keyTips = listOf(
            "🎙️ इस्तेमाल की गई प्राकृतिक सामग्री बताएं" to "Mention natural materials used (e.g. clay, silk, brass)",
            "⏳ बनाने में लगे घंटे व दिन का ज़िक्र करें" to "Mention artisan crafting hours and days taken",
            "🏛️ GI Tag व पारंपरिक तकनीक का विवरण दें" to "Mention GI heritage or heirloom weaving style"
        ),
        audioExplanationHindi = "दूसरा कदम है आवाज़ से विवरण देना। अपनी क्षेत्रीय भाषा चुनकर माइक दबाएं और शिल्प के बारे में बोलें। एआई खुद ब खुद इसका स्मार्ट कैटलॉग बनाएगा।",
        actionSuggestion = "माइक दबाकर 15-20 सेकंड बोलें"
    ),
    AddProductTourStepItem(
        stepIndex = 2,
        titleHindi = "३. उचित पारिश्रमिक एवं लागत गणना",
        titleEnglish = "3. Fair Living Wage & Costing",
        icon = Icons.Default.Balance,
        iconColor = ForestSuccess,
        iconContainerColor = ForestContainer,
        descriptionHindi = "कच्चे माल की लागत और कारीगरी के घंटे दर्ज करें। कौशवाणी (KAUSHVANI) न्यूनतम ₹175/घंटे की दर से उचित पारिश्रमिक और खुदरा/थोक मूल्य की निष्पक्ष गणना करता है।",
        descriptionEnglish = "Enter your raw material costs and hours spent. KAUSHVANI enforces a fair ₹175/hr artisan living wage and transparently calculates B2C Retail and B2B Wholesale pricing.",
        keyTips = listOf(
            "💰 ₹175/घंटे न्यूनतम आजीविका पारिश्रमिक सुरक्षित" to "Guaranteed ₹175/hr minimum artisan living wage",
            "📦 खुदरा (Retail) और थोक (B2B Bulk) दोनों मूल्य" to "Calculates both retail and bulk wholesale prices",
            "📊 पारदर्शी लागत ब्रेकडाउन (सामग्री + श्रम + पैकेजिंग)" to "Transparent cost breakdown for honest buyers"
        ),
        audioExplanationHindi = "तीसरा कदम है उचित मूल्य तय करना। कच्चे माल की लागत और अपनी मेहनत के घंटे बताएं। हम आपकी कमाई को सुरक्षित रखते हुए सही दाम निकालते हैं।",
        actionSuggestion = "घंटे और सामग्री लागत दर्ज करें"
    ),
    AddProductTourStepItem(
        stepIndex = 3,
        titleHindi = "४. AI समीक्षा, अनुवाद एवं डिजिटल निर्यात",
        titleEnglish = "4. Smart Review & Multi-Platform Export",
        icon = Icons.Default.AutoAwesome,
        iconColor = IndigoSecondary,
        iconContainerColor = Color(0xFFEEF2FF),
        descriptionHindi = "AI द्वारा तैयार की गई सांस्कृतिक कहानी और अंग्रेजी अनुवाद की समीक्षा करें। एक क्लिक में अपने डिजिटल कैटलॉग और WhatsApp पर साझा करें।",
        descriptionEnglish = "Review the AI-generated cultural story, SEO tags, and English translation. Publish with 1-click to your Digital Showcase and share on WhatsApp.",
        keyTips = listOf(
            "🌐 द्विभाषी कैटलॉग (मातृभाषा + अंतरराष्ट्रीय अंग्रेजी)" to "Bilingual catalog in native tongue and English",
            "🏷️ ई-कॉमर्स व डिजिटल सर्च के लिए स्वचालित टैग्स" to "Auto-generated tags for digital storefronts and online discovery",
            "📜 शिल्प का डिजिटल पासपोर्ट व QR कोड प्राप्त करें" to "Generate Digital Craft Passport & traceability QR"
        ),
        audioExplanationHindi = "अंतिम कदम है समीक्षा और प्रकाशन। अपने उत्पाद का विवरण देखें और एक क्लिक में इसे अपने डिजिटल कैटलॉग में जोड़ें।",
        actionSuggestion = "समीक्षा करें और 'कैटलॉग प्रकाशित करें' दबाएं"
    )
)

/**
 * Interactive Coach Mark / Guided Tour Overlay for the 'Add Product' flow.
 * Designed with accessible typography, high contrast, warm artisan aesthetics,
 * and regional audio explanations.
 */
@Composable
fun AddProductTourOverlay(
    isVisible: Boolean,
    currentStepIndex: Int,
    onStepChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSpeakText: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val currentStep = AddProductTourSteps.getOrElse(currentStepIndex) { AddProductTourSteps.first() }
    val totalSteps = AddProductTourSteps.size

    // Infinite breathing pulse for coach mark indicator
    val infiniteTransition = rememberInfiniteTransition(label = "tourPulse")
    val pulseBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Semi-transparent backdrop with click-to-prevent background tap
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* keep tour active or tap next */ }
            )
            .padding(16.dp)
            .testTag("add_product_tour_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .border(
                    width = 2.dp,
                    color = currentStep.iconColor.copy(alpha = pulseBorderAlpha),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Tour Badge & Close / Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = TerracottaContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "कारीगर मार्गदर्शिका (Artisan Guide)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Step Counter
                        Text(
                            text = "चरण ${currentStepIndex + 1} / $totalSteps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextSecondary
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp).testTag("tour_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Tour",
                                tint = ArtisanTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Step Dots Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(totalSteps) { idx ->
                        val isCurrent = idx == currentStepIndex
                        val isPassed = idx < currentStepIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(if (isCurrent) 24.dp else 10.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> TerracottaPrimary
                                        isPassed -> ForestSuccess
                                        else -> ArtisanCardBorder
                                    }
                                )
                                .clickable { onStepChange(idx) }
                        )
                    }
                }

                // Step Hero Icon & Main Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(currentStep.iconContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentStep.icon,
                            contentDescription = null,
                            tint = currentStep.iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentStep.titleHindi,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = currentStep.titleEnglish,
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary
                        )
                    }

                    // Voice Narration Button for Non-Tech/Illiterate Artisans
                    if (onSpeakText != null) {
                        Surface(
                            color = TerracottaContainer,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    onSpeakText(currentStep.audioExplanationHindi, "hi")
                                }
                                .testTag("tour_speak_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen Explanation",
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Description Box
                Surface(
                    color = ArtisanSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currentStep.descriptionHindi,
                            fontSize = 13.sp,
                            color = ArtisanTextPrimary,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = currentStep.descriptionEnglish,
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Practical Key Tips Checklist
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 मुख्य सुझाव (Important Tips):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerracottaPrimary
                    )

                    currentStep.keyTips.forEach { (hindiTip, engTip) ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ForestSuccess,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(14.dp)
                            )
                            Column {
                                Text(
                                    text = hindiTip,
                                    fontSize = 12.sp,
                                    color = ArtisanTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = engTip,
                                    fontSize = 10.sp,
                                    color = ArtisanTextMuted
                                )
                            }
                        }
                    }
                }

                // Action Footnote
                Surface(
                    color = ForestContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = ForestSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "सुझाव: ${currentStep.actionSuggestion}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous or Skip
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { onStepChange(currentStepIndex - 1) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("पिछला (Prev)", fontSize = 12.sp)
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("tour_skip_button")
                        ) {
                            Text("टूर छोड़ें (Skip)", fontSize = 12.sp, color = ArtisanTextMuted)
                        }
                    }

                    // Next or Finish
                    Button(
                        onClick = {
                            if (currentStepIndex < totalSteps - 1) {
                                onStepChange(currentStepIndex + 1)
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("tour_next_button")
                    ) {
                        Text(
                            text = if (currentStepIndex < totalSteps - 1) "आगे बढ़ें (Next)" else "समझ आ गया (Got it! ✨)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (currentStepIndex < totalSteps - 1) Icons.Default.ArrowForward else Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
