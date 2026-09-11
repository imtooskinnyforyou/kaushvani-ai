package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Onboarding Carousel Slide Model for 'Add Product with AI'
 */
data class OnboardingCarouselSlide(
    val stepNumber: Int,
    val titleHindi: String,
    val titleEnglish: String,
    val subtitleHindi: String,
    val subtitleEnglish: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val containerColor: Color,
    val explanationHindi: String,
    val explanationEnglish: String,
    val practicalTips: List<Pair<String, String>>, // (Hindi Tip, English Tip)
    val voiceNarrationText: String,
    val stepVisualType: String // "PHOTO", "VOICE", "PRICING", "EXPORT"
)

val OnboardingCarouselSlides = listOf(
    OnboardingCarouselSlide(
        stepNumber = 1,
        titleHindi = "१. शिल्प फोटोग्राफी एवं AI स्टूडियो",
        titleEnglish = "Step 1: Craft Photography & AI Studio",
        subtitleHindi = "साफ़ तस्वीर लें • AI बैकग्राउंड सुधार",
        subtitleEnglish = "Take clear photo • AI studio cleanup",
        icon = Icons.Default.CameraAlt,
        primaryColor = TerracottaPrimary,
        containerColor = TerracottaContainer,
        explanationHindi = "अपने हस्तशिल्प को दिन की प्राकृतिक रोशनी में रखें और 1:1 स्क्वायर फ्रेम में तस्वीर लें। हमारा AI स्टूडियो वर्कशॉप का बिखराव हटाकर प्रामाणिक लिनेन, टेराकोटा या लकड़ी का बैकड्रॉप जोड़ता है, जिससे उत्पाद ई-कॉमर्स और ONDC पर आकर्षक दिखे।",
        explanationEnglish = "Place your craft in natural daylight and take a centered 1:1 photo. The AI Studio automatically cleans workshop clutter and adds authentic neutral backdrops without altering genuine handmade details.",
        practicalTips = listOf(
            "📐 1:1 स्क्वायर या 4:3 कैटलॉग अनुपात चुनें" to "Choose 1:1 Square framing for high visibility",
            "☀️ प्राकृतिक दिन की रोशनी में तस्वीर खींचें" to "Use daylight to showcase authentic craft colors",
            "🛡️ हस्तशिल्प की बनावट 100% सुरक्षित रहती है" to "Handmade texture fidelity is 100% preserved"
        ),
        voiceNarrationText = "पहला चरण: शिल्प फोटोग्राफी। अपने उत्पाद की साफ़ तस्वीर लें। एआई स्टूडियो आपके उत्पाद की रोशनी और बैकग्राउंड को ई-कॉमर्स के लिए तैयार करेगा।",
        stepVisualType = "PHOTO"
    ),
    OnboardingCarouselSlide(
        stepNumber = 2,
        titleHindi = "२. मातृभाषा में आवाज़ से विवरण",
        titleEnglish = "Step 2: Mother-Tongue Voice Description",
        subtitleHindi = "टाइपिंग मुक्त • 12+ क्षेत्रीय भाषाएं",
        subtitleEnglish = "Zero typing • 12+ regional Indian languages",
        icon = Icons.Default.Mic,
        primaryColor = Color(0xFFDC2626),
        containerColor = Color(0xFFFEE2E2),
        explanationHindi = "टाइपिंग की कोई ज़रूरत नहीं! माइक का लाल बटन दबाएं और हिंदी, मराठी, तमिल, गुजराती, बांग्ला या अपनी मातृभाषा में 15-30 सेकंड बोलें। कच्चा माल, बनाने का समय और पारंपरिक तकनीक बताएं। AI अपने आप पेशेवर विवरण तैयार करेगा।",
        explanationEnglish = "Zero typing required! Tap the mic button and speak naturally in Hindi, Marathi, Tamil, Gujarati, Bengali, or 12+ regional languages. Mention raw materials, crafting hours, and heritage techniques.",
        practicalTips = listOf(
            "🎙️ इस्तेमाल की गई प्राकृतिक सामग्री बताएं" to "Mention raw materials (e.g. clay, pure silk, brass)",
            "⏳ बनाने में लगे घंटे व दिन का ज़िक्र करें" to "Mention craft time (e.g. 14 hours of hand embroidery)",
            "🏛️ GI Tag व पुश्तैनी कला का विवरण दें" to "Highlight heritage technique & GI certified cluster"
        ),
        voiceNarrationText = "दूसरा चरण: आवाज़ से विवरण। माइक दबाकर अपनी भाषा में शिल्प के बारे में बोलें। एआई खुद इसका शीर्षक, विवरण और टैग्स तैयार करेगा।",
        stepVisualType = "VOICE"
    ),
    OnboardingCarouselSlide(
        stepNumber = 3,
        titleHindi = "३. निष्पक्ष आजीविका पारिश्रमिक एवं मूल्य",
        titleEnglish = "Step 3: Fair Living Wage & Smart Costing",
        subtitleHindi = "न्यूनतम ₹175/घंटा पारिश्रमिक सुरक्षित",
        subtitleEnglish = "Guaranteed ₹175/hr artisan living wage",
        icon = Icons.Default.Balance,
        primaryColor = ForestSuccess,
        containerColor = ForestContainer,
        explanationHindi = "कच्चे माल की लागत और अपने श्रम के घंटे दर्ज करें। कौशवाणी (KAUSHVANI) न्यूनतम ₹175/घंटा आजीविका पारिश्रमिक को आधार मानकर खुदरा (B2C) और थोक (B2B Bulk) दोनों मूल्यों की पारदर्शी गणना करता है, ताकि आपको अपनी मेहनत का सही दाम मिले।",
        explanationEnglish = "Enter your raw material expenses and handcrafting hours. KAUSHVANI enforces a living wage floor of ₹175/hr and transparently calculates retail MRP and wholesale B2B bulk pricing with zero guesswork.",
        practicalTips = listOf(
            "💰 ₹175/घंटा न्यूनतम आजीविका पारिश्रमिक सुरक्षित" to "Protected ₹175/hour minimum artisan living wage",
            "📦 खुदरा और थोक (B2B MOQ) दोनों दरें तय" to "Transparent pricing for retail and wholesale buyers",
            "📊 सामग्री + श्रम + पैकेजिंग का खुला विवरण" to "Itemized breakdown that builds buyer trust"
        ),
        voiceNarrationText = "तीसरा चरण: उचित मूल्य। कच्चे माल की लागत और अपनी मेहनत के घंटे दर्ज करें। हम न्यूनतम ₹175 प्रति घंटा कारीगरी जोड़कर सही मूल्य तय करते हैं।",
        stepVisualType = "PRICING"
    ),
    OnboardingCarouselSlide(
        stepNumber = 4,
        titleHindi = "४. AI कैटलॉग, डिजिटल पासपोर्ट व निर्यात",
        titleEnglish = "Step 4: AI Catalog, QR Passport & Export",
        subtitleHindi = "1-क्लिक ONDC, Amazon Karigar व WhatsApp",
        subtitleEnglish = "1-Click multi-platform sync & traceability",
        icon = Icons.Default.AutoAwesome,
        primaryColor = IndigoSecondary,
        containerColor = Color(0xFFEEF2FF),
        explanationHindi = "AI द्वारा तैयार की गई सांस्कृतिक कहानी, अंग्रेजी अनुवाद और ONDC टैग्स की समीक्षा करें। 'कैटलॉग प्रकाशित करें' दबाते ही डिजिटल क्राफ्ट पासपोर्ट (QR कोड) बन जाएगा और उत्पाद ONDC व WhatsApp पर शेयर होने के लिए तैयार हो जाएगा।",
        explanationEnglish = "Review the AI-generated cultural story, international English translation, and ONDC taxonomy tags. With 1-click, generate a traceable Digital Craft Passport QR code and sync with buyer networks.",
        practicalTips = listOf(
            "🌐 मातृभाषा और अंतरराष्ट्रीय अंग्रेजी दोनों में कैटलॉग" to "Bilingual catalog in regional tongue and English",
            "📜 डिजिटल क्राफ्ट पासपोर्ट व प्रामाणिकता QR कोड" to "Authentic GI Craft Passport QR Code generated",
            "🚀 ONDC, GeM और WhatsApp पर त्वरित शेयर" to "Instant 1-click sync to global marketplace"
        ),
        voiceNarrationText = "चौथा चरण: समीक्षा और प्रकाशन। एआई द्वारा तैयार कैटलॉग देखें और एक क्लिक में इसे सरकारी ओएनडीसी और वैश्विक बाज़ार में भेजें।",
        stepVisualType = "EXPORT"
    )
)

/**
 * Onboarding Carousel Dialog for the 'Add Product with AI' workflow
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddProductOnboardingCarouselDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onStartAddProduct: () -> Unit,
    onSpeakText: ((String, String) -> Unit)? = null
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("add_product_onboarding_dialog"),
            contentAlignment = Alignment.Center
        ) {
            AddProductOnboardingCarouselCard(
                onDismiss = onDismiss,
                onStartAddProduct = onStartAddProduct,
                onSpeakText = onSpeakText,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
            )
        }
    }
}

/**
 * Standalone / Embedded Onboarding Carousel Component
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddProductOnboardingCarouselCard(
    onDismiss: () -> Unit,
    onStartAddProduct: () -> Unit,
    onSpeakText: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingCarouselSlides.size })
    val coroutineScope = rememberCoroutineScope()
    val currentSlide = OnboardingCarouselSlides[pagerState.currentPage]
    val totalSlides = OnboardingCarouselSlides.size

    Card(
        modifier = modifier
            .shadow(20.dp, RoundedCornerShape(24.dp))
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        currentSlide.primaryColor.copy(alpha = 0.7f),
                        ArtisanCardBorder
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Workflow Tag, Page Indicator & Close Button
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "AI उत्पाद मार्गदर्शिका (AI Guide)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = ArtisanSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / $totalSlides",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("onboarding_carousel_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ArtisanTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Swipeable Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_horizontal_pager")
            ) { pageIndex ->
                val slide = OnboardingCarouselSlides[pageIndex]
                OnboardingSlideContent(
                    slide = slide,
                    onSpeak = {
                        onSpeakText?.invoke(slide.voiceNarrationText, "hi")
                    }
                )
            }

            // Carousel Navigation Dots Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalSlides) { idx ->
                    val isSelected = idx == pagerState.currentPage
                    val isPast = idx < pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(7.dp)
                            .width(if (isSelected) 28.dp else 10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> currentSlide.primaryColor
                                    isPast -> ForestSuccess
                                    else -> ArtisanCardBorder
                                }
                            )
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(idx)
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Bottom Actions: [Prev / Skip] & [Next / Start Adding Product]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("onboarding_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("पिछला (Prev)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text("छोड़ें (Skip)", fontSize = 12.sp, color = ArtisanTextMuted)
                    }
                }

                if (pagerState.currentPage < totalSlides - 1) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentSlide.primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text("आगे बढ़ें (Next)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            onDismiss()
                            onStartAddProduct()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("onboarding_start_add_product_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "उत्पाद जोड़ना शुरू करें ✨",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Slide Page Layout
 */
@Composable
private fun OnboardingSlideContent(
    slide: OnboardingCarouselSlide,
    onSpeak: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Step Hero Icon & Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(slide.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = null,
                    tint = slide.primaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slide.titleHindi,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )
                Text(
                    text = slide.titleEnglish,
                    fontSize = 11.sp,
                    color = ArtisanTextSecondary
                )
            }

            // Voice Narration Button for Audio Accessibility
            IconButton(
                onClick = onSpeak,
                modifier = Modifier
                    .size(40.dp)
                    .background(TerracottaContainer, CircleShape)
                    .testTag("slide_audio_button_${slide.stepNumber}")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Listen to step instructions",
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Interactive Visual Mockup / Step Demonstration Card
        StepInteractiveVisualMockup(slide = slide)

        // Detailed Explanation
        Surface(
            color = ArtisanSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = slide.explanationHindi,
                    fontSize = 12.sp,
                    color = ArtisanTextPrimary,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = slide.explanationEnglish,
                    fontSize = 10.sp,
                    color = ArtisanTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        // Practical Tips Checklist
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "💡 मुख्य सुझाव (Best Practices):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = slide.primaryColor
            )

            slide.practicalTips.forEach { (hindiTip, engTip) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ForestSuccess,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(13.dp)
                    )
                    Column {
                        Text(
                            text = hindiTip,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = engTip,
                            fontSize = 9.sp,
                            color = ArtisanTextMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual Mockup Demonstrating the Step Concept
 */
@Composable
private fun StepInteractiveVisualMockup(slide: OnboardingCarouselSlide) {
    Surface(
        color = Color(0xFFF9F7F2),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        when (slide.stepVisualType) {
            "PHOTO" -> {
                // Photography Before/After Studio Visual
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Raw Photo Box
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFE0E0E0))
                                .border(1.dp, Color.Gray, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Camera, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                                Text("वर्कशॉप फ़ोटो", fontSize = 8.sp, color = Color.DarkGray)
                            }
                        }
                        Text("1. कच्ची तस्वीर", fontSize = 10.sp, color = ArtisanTextSecondary)
                    }

                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))

                    // AI Studio Enhanced Box
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.verticalGradient(listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))))
                                .border(1.5.dp, TerracottaPrimary, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(24.dp))
                                Text("AI स्टूडियो", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                            }
                        }
                        Text("2. ई-कॉमर्स रेडी ✨", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    }
                }
            }
            "VOICE" -> {
                // Regional Voice Visual with Waveform & Language Badges
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = CircleShape,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "\"यह शुद्ध काली मिट्टी का बना फूलदान है...\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ArtisanTextPrimary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    // Regional languages chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("हिन्दी", "मराठी", "தமிழ்", "ગુજરાતી", "English").forEach { lang ->
                            Surface(
                                color = ArtisanSurfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = lang,
                                    fontSize = 9.sp,
                                    color = ArtisanTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            "PRICING" -> {
                // Fair Living Wage Visual
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("कारीगरी आजीविका", fontSize = 9.sp, color = ArtisanTextSecondary)
                        Text("₹175/घंटा", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                    }
                    Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArtisanTextMuted)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("कच्चा माल + पैकेजिंग", fontSize = 9.sp, color = ArtisanTextSecondary)
                        Text("लागत मूल्य", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                    }
                    Text("=", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArtisanTextMuted)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("उचित खुदरा दर", fontSize = 9.sp, color = TerracottaPrimary)
                        Text("100% निष्पक्ष MRP", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TerracottaPrimary)
                    }
                }
            }
            "EXPORT" -> {
                // ONDC & Digital QR Export Visual
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = IndigoSecondary, modifier = Modifier.size(16.dp))
                            Text("Craft Passport QR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                        }
                    }

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = ForestSuccess, modifier = Modifier.size(16.dp))
                            Text("ONDC & GeM Live", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                        }
                    }
                }
            }
        }
    }
}
