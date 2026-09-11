package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.ChatMessage
import com.example.data.ai.VerifiedOfficialInfo
import com.example.data.model.ProductEntity
import com.example.ui.components.RegionalLanguage
import com.example.ui.components.SupportedRegionalLanguages
import com.example.ui.theme.*
import com.example.ui.theme.NotoDevanagariFontFamily
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel

enum class BusinessDomain(val id: String, val icon: String, val hindiLabel: String, val englishLabel: String) {
    ALL("all", "✨", "सभी विषय", "All Topics"),
    PRICING("pricing", "💰", "मूल्य निर्धारण", "Pricing"),
    DESCRIPTION("description", "✍️", "उत्पाद विवरण", "Description"),
    PHOTOGRAPHY("photography", "📸", "फोटो सुधार", "Photography"),
    PACKAGING("packaging", "📦", "पैकेजिंग", "Packaging"),
    BULK_ORDERS("bulk_order", "🤝", "थोक बिक्री", "Bulk Orders"),
    BUYER_COMM("buyer_comm", "💬", "खरीदार संवाद", "Buyer Comm"),
    DIGITAL_SELLING("digital_selling", "🌐", "डिजिटल बिक्री", "Digital Selling"),
    GOVT_SCHEMES("govt_schemes", "🏛️", "सरकारी योजनाएं", "Govt Schemes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VyaparMitraChatScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val profile by viewModel.artisanProfile.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProductForAssistant.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isTtsSpeaking.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    var inputMessage by remember { mutableStateOf("") }
    var isVoiceListening by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showProductPickerSheet by remember { mutableStateOf(false) }
    var selectedDomain by remember { mutableStateOf(BusinessDomain.ALL) }

    val activeLocale = profile.localePreference.ifBlank { "hi-IN" }
    val activeLangName = profile.preferredLanguage.ifBlank { "Hindi" }
    val activeLangCode: String = SupportedRegionalLanguages.find { lang: RegionalLanguage ->
        lang.name.equals(activeLangName, ignoreCase = true) || lang.localeTag == activeLocale
    }?.code ?: "hi"

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isVoiceListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                inputMessage = spoken
                viewModel.sendVyaparMitraMessage(spoken)
            }
        }
    }

    // Auto scroll to bottom on new message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Dynamic prompt questions based on domain and pinned craft
    val domainQuestions = remember(selectedDomain, selectedProduct) {
        val craftTitle = selectedProduct?.title ?: "मेरे शिल्प"
        when (selectedDomain) {
            BusinessDomain.ALL -> listOf(
                "💰 $craftTitle का सही दाम क्या तय करें?",
                "📸 बिना स्टूडियो के अच्छी फोटो कैसे लें?",
                "✍️ उत्पाद विवरण में क्या कहानी लिखें?",
                "📦 कूरियर में टूटने से बचाने की पैकिंग?",
                "🤝 थोक खरीदारों (Bulk Buyers) को कैसे बेचें?",
                "🏛️ पीएम विश्वकर्मा योजना के ₹15,000 टूलकिट कैसे पाएं?"
            )
            BusinessDomain.PRICING -> listOf(
                "💰 $craftTitle के लिए कारीगर मजदूरी (₹175/घंटा) कैसे जोड़ें?",
                "थोक (Wholesale) और खुदरा (Retail) भाव में क्या अंतर रखें?",
                "कच्चा माल महंगा होने पर दाम कैसे बढ़ाएं?",
                "मोलभाव करने वाले खरीदार को विनम्रता से क्या कहें?"
            )
            BusinessDomain.DESCRIPTION -> listOf(
                "✍️ $craftTitle के लिए 4 लाइनों की पारंपरिक कहानी बनाएं",
                "शिल्प की देखभाल (Care Instructions) में क्या लिखें?",
                "शुद्ध प्राकृतिक और हाथ से बनी शुद्धता कैसे उजागर करें?",
                "ऑनलाइन सर्च के लिए मुख्य कीवर्ड्स (Tags) क्या रखें?"
            )
            BusinessDomain.PHOTOGRAPHY -> listOf(
                "📸 $craftTitle की मोबाइल से सही रोशनी में फोटो कैसे लें?",
                "शिल्प के लिए सादा व सुंदर बैकग्राउंड कैसा रखें?",
                "1:1 वर्गाकार फ्रेम में कौन सा एंगल सबसे अच्छा है?",
                "बारीक नक्काशी या बुनाई की क्लोज-अप फोटो टिप्स"
            )
            BusinessDomain.PACKAGING -> listOf(
                "📦 $craftTitle को कूरियर में सुरक्षित रखने की 5-प्लाई पैकिंग?",
                "इको-फ्रेंडली हनीकॉम्ब पेपर पैकिंग कैसे करें?",
                "डिब्बे में हस्तकला प्रमाण पत्र व धन्यवाद पत्र कैसे रखें?",
                "पार्सल पर 'सावधानी / Fragile' लेबल कैसे लगाएं?"
            )
            BusinessDomain.BULK_ORDERS -> listOf(
                "🤝 50 से 100 पीस के थोक ऑर्डर पर कितना डिस्काउंट दें?",
                "कच्चा माल खरीदने के लिए 50% पेशगी (Advance) कैसे मांगें?",
                "थोक आर्डर शुरू करने से पहले 1 नमूना (Sample) कैसे भेजें?",
                "हाथ से बने सामान की डिलीवरी समय सीमा कैसे तय करें?"
            )
            BusinessDomain.BUYER_COMM -> listOf(
                "💬 कॉर्पोरेट गिफ्टिंग और होटल खरीदार से बातचीत का फॉर्मेट?",
                "50% भारी छूट मांगने वाले खरीदार को क्या संदेश भेजें?",
                "कैटलॉग भेजने के बाद पेशेवर फॉलो-अप संदेश?",
                "डिलीवरी के बाद खरीदार से रिव्यू मांगने का तरीका"
            )
            BusinessDomain.DIGITAL_SELLING -> listOf(
                "🌐 व्हाट्सएप बिजनेस पर कैटलॉग कैसे शेयर करें?",
                "ONDC और अमेजन कारीगर पर अपना शिल्प कैसे बेचें?",
                "इंस्टाग्राम पर शिल्प निर्माण की रील वीडियो कैसे बनाएं?",
                "सुरक्षित UPI QR कोड से भुगतान कैसे स्वीकार करें?"
            )
            BusinessDomain.GOVT_SCHEMES -> listOf(
                "🏛️ पीएम विश्वकर्मा में ₹15,000 टूलकिट अनुदान कैसे पाएं?",
                "पहचान कारीगर कार्ड (Pehchan Card) कैसे बनवाएं?",
                "मुद्रा लोन (PMMY) बिना गारंटी 5% ब्याज पर कैसे लें?",
                "सूरजकुंड व दिल्ली हाट में सरकारी स्टॉल कैसे आरक्षित करें?"
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = ArtisanSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Single Responsive Row: Back → AI icon → “Kaarigar Vyapar Sahayak” → Refresh → Language
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Back Button (min touch target 48dp)
                        IconButton(
                            onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_HOME) },
                            modifier = Modifier
                                .size(48.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "होम स्क्रीन पर वापस जाएं (Back to Home)"
                                }
                                .testTag("chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = ArtisanTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // AI Icon Badge
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

                        // Title (1–2 lines, responsive, prevents clipping)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "KAUSHVANI Vyapar Sahayak",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                color = ArtisanTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "कौशवाणी व्यापार सहायक • Gemini AI",
                                fontFamily = NotoDevanagariFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = TerracottaPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Reset Chat Action (min touch target 48dp)
                        IconButton(
                            onClick = { viewModel.clearVyaparMitraChat() },
                            modifier = Modifier
                                .size(48.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "बातचीत रीसेट करें (Reset Chat)"
                                }
                                .testTag("chat_reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = ArtisanTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Language Selector Badge (min touch target 48dp)
                        Box {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = TerracottaContainer,
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 48.dp, minHeight = 36.dp)
                                    .minimumInteractiveComponentSize()
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "भाषा बदलें: वर्तमान में $activeLangName चयनित है (Change language)"
                                    }
                                    .clickable { showLanguageMenu = true }
                                    .testTag("chat_language_selector_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "🌐 $activeLangCode",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracottaPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false }
                            ) {
                                val langList: List<RegionalLanguage> = SupportedRegionalLanguages.take(6)
                                for (lang in langList) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${lang.nativeName} (${lang.name})",
                                                fontWeight = if (lang.name == activeLangName) FontWeight.Bold else FontWeight.Normal,
                                                fontFamily = NotoDevanagariFontFamily
                                            )
                                        },
                                        onClick = {
                                            viewModel.setArtisanPreferredLanguage(lang.name, lang.localeTag)
                                            showLanguageMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2. Assistant description directly below header
                    Surface(
                        color = WarmIvoryCard.copy(alpha = 0.65f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ForestContainer
                            ) {
                                Text(
                                    text = "Gemini AI",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestSuccess,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "स्मार्ट मूल्य निर्धारण, फोटो सुधार, विपणन व सरकारी योजना सहायता",
                                fontFamily = NotoDevanagariFontFamily,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = ArtisanTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 3. Compact & Aligned “Select Product” control
                    Surface(
                        color = if (selectedProduct != null) MarigoldContainer.copy(alpha = 0.45f) else ArtisanSurfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (selectedProduct != null) MarigoldAccent.copy(alpha = 0.4f) else ArtisanCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (selectedProduct != null) Icons.Default.CheckCircle else Icons.Default.Category,
                                    contentDescription = null,
                                    tint = if (selectedProduct != null) ForestSuccess else TerracottaPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (selectedProduct != null) {
                                        "🎯 ${selectedProduct?.title} (₹${selectedProduct?.retailPrice?.toInt()})"
                                    } else {
                                        "🎯 शिल्प संदर्भ (विशिष्ट उत्पाद सलाह हेतु चुनें)"
                                    },
                                    fontFamily = NotoDevanagariFontFamily,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ArtisanTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (selectedProduct != null) {
                                    IconButton(
                                        onClick = { viewModel.selectProductForAssistant(null) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "शिल्प संदर्भ हटाएं",
                                            tint = ArtisanTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = TerracottaPrimary,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 32.dp)
                                        .minimumInteractiveComponentSize()
                                        .clickable { showProductPickerSheet = true }
                                        .testTag("chat_pick_craft_button")
                                ) {
                                    Text(
                                        text = if (selectedProduct != null) "बदलें" else "शिल्प चुनें",
                                        fontFamily = NotoDevanagariFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = ArtisanSurface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Horizontally scrollable Domain Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(BusinessDomain.values()) { domain ->
                            val isSelected = selectedDomain == domain
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) TerracottaPrimary else ArtisanSurfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) TerracottaPrimary else ArtisanCardBorder),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 36.dp)
                                    .minimumInteractiveComponentSize()
                                    .clickable { selectedDomain = domain }
                                    .testTag("domain_chip_${domain.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = domain.icon, fontSize = 11.sp)
                                    Text(
                                        text = domain.hindiLabel,
                                        fontFamily = NotoDevanagariFontFamily,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else ArtisanTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // 2. Horizontally scrollable Suggested Questions Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(domainQuestions) { q ->
                            Surface(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 36.dp)
                                    .minimumInteractiveComponentSize()
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "सुझावित प्रश्न: $q"
                                    }
                                    .clickable { viewModel.sendVyaparMitraMessage(q) }
                                    .testTag("quick_question_chip_${q.take(10)}"),
                                shape = RoundedCornerShape(12.dp),
                                color = ArtisanSurfaceVariant,
                                border = BorderStroke(1.dp, ArtisanCardBorder)
                            ) {
                                Text(
                                    text = q,
                                    fontFamily = NotoDevanagariFontFamily,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = ArtisanTextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 3. Compact aligned Input Row: Mic (48dp) + TextField (weight 1) + Send (48dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Voice Mic Button (48dp touch target)
                        IconButton(
                            onClick = {
                                isVoiceListening = true
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, activeLocale)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, activeLocale)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "कौशवाणी व्यापार सहायक से बोलकर पूछें ($activeLangName)...")
                                }
                                speechLauncher.launch(intent)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isVoiceListening) TerracottaPrimary else TerracottaContainer)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "माइक से बोलकर प्रश्न पूछें (Voice Search in $activeLangName)"
                                }
                                .testTag("chat_voice_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isVoiceListening) Color.White else TerracottaPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Responsive Text Field
                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            placeholder = {
                                Text(
                                    text = "व्यापार प्रश्न पूछें या बोलें...",
                                    fontFamily = NotoDevanagariFontFamily,
                                    fontSize = 13.sp,
                                    color = ArtisanTextMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "व्यापार सहायक से बातचीत के लिए संदेश लिखें"
                                }
                                .testTag("chat_text_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TerracottaPrimary,
                                unfocusedBorderColor = ArtisanCardBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = WarmIvoryBackground
                            )
                        )

                        // Send Button (48dp touch target)
                        IconButton(
                            onClick = {
                                if (inputMessage.isNotBlank()) {
                                    val msg = inputMessage
                                    inputMessage = ""
                                    viewModel.sendVyaparMitraMessage(msg)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (inputMessage.isNotBlank()) IndigoSecondary else ArtisanSurfaceVariant)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "संदेश भेजें (Send Message)"
                                }
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = if (inputMessage.isNotBlank()) Color.White else ArtisanTextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .background(ArtisanBackground)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Friendly compact welcome banner when chat is empty
            if (messages.isEmpty()) {
                item {
                    Surface(
                        color = ArtisanSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ArtisanCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "नमस्ते! आपका कौशवाणी व्यापार सहायक तैयार है",
                                fontFamily = NotoDevanagariFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Text(
                                text = "अपने हस्तशिल्प का सही दाम, ऑनलाइन बिक्री, उत्पाद विवरण या सरकारी योजनाओं की जानकारी के लिए नीचे दिए गए विषय चुनें अथवा माइक दबाकर बोलें।",
                                fontFamily = NotoDevanagariFontFamily,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = ArtisanTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    activeLangCode = activeLangCode,
                    isCurrentlySpeaking = isSpeaking,
                    onSpeak = {
                        val speakContent = msg.aiGuidanceText ?: msg.text
                        viewModel.speakText(speakContent, activeLangCode)
                    },
                    onStopSpeech = {
                        viewModel.stopSpeech()
                    },
                    onQuickPromptClick = { prompt ->
                        viewModel.sendVyaparMitraMessage(prompt)
                    },
                    onRetry = { prompt ->
                        viewModel.retryVyaparMitraMessage(prompt)
                    },
                    onOpenUrl = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                )
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = TerracottaPrimary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "जेमिनी AI से व्यापार परामर्श प्राप्त हो रहा है...",
                            fontFamily = NotoDevanagariFontFamily,
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }
            }
        }
    }

    // Product Picker Bottom Sheet
    if (showProductPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProductPickerSheet = false },
            containerColor = ArtisanSurface
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
                    Text(
                        text = "परामर्श हेतु शिल्प चुनें (Select Craft)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    if (selectedProduct != null) {
                        TextButton(onClick = {
                            viewModel.selectProductForAssistant(null)
                            showProductPickerSheet = false
                        }) {
                            Text("हटाएं (Clear)", color = TerracottaPrimary)
                        }
                    }
                }

                if (allProducts.isEmpty()) {
                    Text(
                        text = "कैटलॉग में अभी कोई उत्पाद नहीं है। पहले 'नया शिल्प जोड़ें' पर जाएं।",
                        fontSize = 13.sp,
                        color = ArtisanTextSecondary
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allProducts) { prod ->
                            val isCurrent = selectedProduct?.id == prod.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) TerracottaContainer else ArtisanSurfaceVariant,
                                border = BorderStroke(1.dp, if (isCurrent) TerracottaPrimary else ArtisanCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectProductForAssistant(prod)
                                        showProductPickerSheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.Category,
                                        contentDescription = null,
                                        tint = if (isCurrent) TerracottaPrimary else ArtisanTextMuted
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArtisanTextPrimary
                                        )
                                        Text(
                                            text = "${prod.category} • खुदरा: ₹${prod.retailPrice.toInt()} • थोक: ₹${prod.wholesalePrice.toInt()}",
                                            fontSize = 12.sp,
                                            color = ArtisanTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    activeLangCode: String,
    isCurrentlySpeaking: Boolean,
    onSpeak: () -> Unit,
    onStopSpeech: () -> Unit,
    onQuickPromptClick: (String) -> Unit,
    onRetry: (String) -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val isUser = message.sender == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .semantics {
                    contentDescription = if (isUser) "आपका संदेश: ${message.text}" else "व्यापार सहायक का उत्तर: ${message.text}"
                },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isUser -> IndigoSecondary
                    message.isError -> Color(0xFFFEE2E2)
                    else -> ArtisanSurface
                }
            ),
            border = if (isUser) null else BorderStroke(1.dp, if (message.isError) Color(0xFFEF4444) else ArtisanCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header (for Assistant messages)
                if (!isUser) {
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
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "कारीगर व्यापार सहायक",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                            if (message.referencedProductTitle != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MarigoldContainer
                                ) {
                                    Text(
                                        text = "🎯 ${message.referencedProductTitle.take(12)}...",
                                        fontSize = 9.sp,
                                        color = ArtisanTextPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // TTS Read / Stop Button
                        if (!message.isError) {
                            IconButton(
                                onClick = {
                                    if (isCurrentlySpeaking) onStopSpeech() else onSpeak()
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrentlySpeaking) Color(0xFFFEE2E2) else TerracottaContainer)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = if (isCurrentlySpeaking) "ऑडियो रोकें" else "उत्तर बोलकर सुनें"
                                    }
                                    .testTag("tts_speak_button")
                            ) {
                                Icon(
                                    imageVector = if (isCurrentlySpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = if (isCurrentlySpeaking) Color(0xFFDC2626) else TerracottaPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Error State Rendering with Retry
                if (message.isError) {
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        color = Color(0xFFB91C1C),
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { onRetry(message.rawUserPrompt ?: "व्यापार सलाह") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("पुनः प्रयास करें (Retry)", fontSize = 12.sp, color = Color.White)
                    }
                } else {
                    // VERIFIED OFFICIAL INFORMATION CARD (Strictly distinguished)
                    if (message.verifiedOfficialInfo != null) {
                        val official = message.verifiedOfficialInfo
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, ForestSuccess),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = ForestSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "🏛️ आधिकारिक प्रमाणित सरकारी जानकारी",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestSuccess
                                    )
                                }

                                Text(
                                    text = official.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )

                                Text(
                                    text = "प्राधिकरण: ${official.authorityOrScheme}",
                                    fontSize = 10.sp,
                                    color = ArtisanTextSecondary
                                )

                                if (official.summary.isNotBlank()) {
                                    Text(
                                        text = official.summary,
                                        fontSize = 11.sp,
                                        color = ArtisanTextPrimary
                                    )
                                }

                                // Verified Benefits
                                if (official.benefits.isNotEmpty()) {
                                    Text(
                                        text = "मुख्य प्रमाणित लाभ:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                    official.benefits.forEach { b ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text("✓", fontSize = 11.sp, color = ForestSuccess, fontWeight = FontWeight.Bold)
                                            Text(b, fontSize = 11.sp, color = ArtisanTextPrimary)
                                        }
                                    }
                                }

                                if (official.eligibility != null) {
                                    Text(
                                        text = "पात्रता: ${official.eligibility}",
                                        fontSize = 10.sp,
                                        color = ArtisanTextSecondary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (official.helpline != null) {
                                        Text(
                                            text = "📞 ${official.helpline}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ArtisanTextPrimary
                                        )
                                    }
                                    if (official.officialPortal != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = ForestSuccess,
                                            modifier = Modifier.clickable { onOpenUrl(official.officialPortal) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text("आधिकारिक पोर्टल", fontSize = 10.sp, color = Color.White)
                                                Icon(
                                                    imageVector = Icons.Default.OpenInNew,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    text = "📌 यह जानकारी आधिकारिक सरकारी दिशानिर्देशों पर आधारित है।",
                                    fontSize = 9.sp,
                                    color = ForestSuccess
                                )
                            }
                        }
                    }

                    // AI GUIDANCE SECTION
                    val guidanceText = message.aiGuidanceText ?: message.text
                    if (guidanceText.isNotBlank()) {
                        if (!isUser && message.verifiedOfficialInfo != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "🤖 AI व्यापार रणनीति व मार्गदर्शन:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            }
                        }

                        Text(
                            text = guidanceText,
                            fontSize = 13.sp,
                            color = if (isUser) Color.White else ArtisanTextPrimary,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        // Suggested prompts under AI message
        if (!isUser && message.suggestedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.widthIn(max = 340.dp)
            ) {
                items(message.suggestedActions) { action ->
                    Surface(
                        modifier = Modifier
                            .semantics {
                                role = Role.Button
                                contentDescription = "सुझाव: $action"
                            }
                            .clickable { onQuickPromptClick(action) },
                        shape = RoundedCornerShape(8.dp),
                        color = MarigoldContainer
                    ) {
                        Text(
                            text = "💡 $action",
                            fontSize = 10.sp,
                            color = ArtisanTextPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
