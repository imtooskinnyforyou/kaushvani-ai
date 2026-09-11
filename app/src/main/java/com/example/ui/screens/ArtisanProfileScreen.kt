package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ArtisanProfile
import com.example.ui.components.RegionalLanguage
import com.example.ui.components.SupportedRegionalLanguages
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtisanProfileScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.artisanProfile.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSwitchArtisanDialog by remember { mutableStateOf(false) }
    var showFirebaseAuthDialog by remember { mutableStateOf(false) }

    if (showFirebaseAuthDialog) {
        FirebaseAuthDialog(
            viewModel = viewModel,
            onDismiss = { showFirebaseAuthDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "शिल्पकार प्रोफ़ाइल व डिजिटल पासपोर्ट",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_header_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = TerracottaPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArtisanSurface,
                    titleContentColor = ArtisanTextPrimary
                )
            )
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
            // Digital Artisan Identity Card (Gold/Bronze Border)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("artisan_passport_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoSecondary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(IndigoSecondary, Color(0xFF0F1B30))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Top Row: Gov Emblem / HunarSetu Badge
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
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = MarigoldTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "KAUSHVANI ARTISAN PASSPORT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MarigoldTertiary,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Surface(
                                    color = ForestSuccess,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "✓ VERIFIED MASTER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Artisan Profile Main Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MarigoldTertiary)
                                        .border(2.dp, Color(0xFFFFD54F), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = profile.craftSpecialty,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFD54F),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "📍 ${profile.villageOrCluster}, ${profile.state}",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                            // ID Badges and Numbers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "PM विश्वकर्मा ID:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text(text = profile.artisanCardNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "अनुभव (Experience):", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text(text = "${profile.experienceYears} वर्ष (Years)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // AI Translation & Transcription Language Selection Card
            item {
                LanguageSelectionSection(
                    profile = profile,
                    viewModel = viewModel
                )
            }

            // Firebase Cloud Firestore & Auth Multi-User Section
            item {
                FirebaseAuthCloudSection(
                    viewModel = viewModel,
                    onOpenAuthDialog = { showFirebaseAuthDialog = true }
                )
            }

            // Share Digital Showroom QR CTA
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("artisan_qr_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "डिजिटल शोरूम QR कोड",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "खरीदारों को व्हाट्सएप या मेलों में स्कैन कराएं",
                                    fontSize = 12.sp,
                                    color = ArtisanTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Simulated QR Code Frame
                        Surface(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(2.dp, IndigoSecondary)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Showroom QR",
                                    modifier = Modifier.size(110.dp),
                                    tint = IndigoSecondary
                                )
                            }
                        }

                        Text(
                            text = "https://kaushvani.app/artisan/${profile.name.lowercase().replace(" ", "-")}",
                            fontSize = 11.sp,
                            color = TerracottaPrimary,
                            fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Namaste! View my authentic handmade craft catalog directly on KAUSHVANI: https://kaushvani.app/artisan/${profile.name.lowercase().replace(" ", "-")}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Digital Catalog"))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_digital_catalog_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("डिजिटल कैटलॉग शेयर करें (Share Catalog)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Direct Payment & Bank UPI Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "बैंकिंग एवं डायरेक्ट पेमेंट (Direct UPI)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )

                        Surface(
                            color = ArtisanSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "UPI ID (Direct Artisan Bank Transfer):", fontSize = 11.sp, color = ArtisanTextSecondary)
                                    Text(text = profile.upiId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                                }
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = ForestSuccess
                                )
                            }
                        }
                    }
                }
            }

            // Profile Edit & Register New Artisan Button
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_profile_dialog_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("प्रोफ़ाइल संपादित करें", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showSwitchArtisanDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("switch_artisan_dialog_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("कारीगर बदलें / रजिस्टर", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Edit Profile Modal
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(profile.name) }
        var craft by remember { mutableStateOf(profile.craftSpecialty) }
        var exp by remember { mutableStateOf(profile.experienceYears.toString()) }
        var village by remember { mutableStateOf(profile.villageOrCluster) }
        var state by remember { mutableStateOf(profile.state) }
        var phone by remember { mutableStateOf(profile.phone) }
        var upi by remember { mutableStateOf(profile.upiId) }
        var cardNo by remember { mutableStateOf(profile.artisanCardNo) }
        var selectedLang by remember {
            mutableStateOf(
                SupportedRegionalLanguages.find {
                    it.name.equals(profile.preferredLanguage, ignoreCase = true) || it.code.equals(profile.preferredLanguage, ignoreCase = true)
                } ?: SupportedRegionalLanguages.first()
            )
        }
        var langDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("शिल्पकार प्रोफ़ाइल संपादित करें (Edit Profile)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    item {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("शिल्पकार का नाम (Name)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    item {
                        OutlinedTextField(value = craft, onValueChange = { craft = it }, label = { Text("हस्तकला विशेषता (Craft Specialty)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    item {
                        // Language Dropdown in Dialog
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "पसंदीदा AI व अनुवाद भाषा (Language & AI Locale):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ArtisanTextPrimary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { langDropdownExpanded = !langDropdownExpanded }
                                        .testTag("dialog_language_selector"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = ArtisanSurfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
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
                                                    text = "${selectedLang.nativeName} (${selectedLang.name})",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ArtisanTextPrimary
                                                )
                                                Text(
                                                    text = "AI Locale: ${selectedLang.localeTag}",
                                                    fontSize = 10.sp,
                                                    color = TerracottaPrimary
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (langDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = ArtisanTextSecondary
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = langDropdownExpanded,
                                    onDismissRequest = { langDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    SupportedRegionalLanguages.forEach { lang ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "${lang.nativeName} (${lang.name})",
                                                            fontWeight = if (lang == selectedLang) FontWeight.Bold else FontWeight.Normal,
                                                            fontSize = 13.sp
                                                        )
                                                        Text(
                                                            text = "Locale: ${lang.localeTag}",
                                                            fontSize = 10.sp,
                                                            color = ArtisanTextSecondary
                                                        )
                                                    }
                                                    if (lang == selectedLang) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = ForestSuccess,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedLang = lang
                                                langDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = exp, onValueChange = { exp = it }, label = { Text("अनुभव (वर्ष)") }, modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("राज्य (State)") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                    }
                    item {
                        OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("गाँव / क्लस्टर (Village/Cluster)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    item {
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("फ़ोन नंबर (Mobile)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    item {
                        OutlinedTextField(value = upi, onValueChange = { upi = it }, label = { Text("UPI ID (बैंक भुगतान के लिए)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                    item {
                        OutlinedTextField(value = cardNo, onValueChange = { cardNo = it }, label = { Text("PM Vishwakarma / Artisan Card No.") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateArtisanProfile(
                            name = name,
                            craftSpecialty = craft,
                            experienceYears = exp.toIntOrNull() ?: 10,
                            villageOrCluster = village,
                            state = state,
                            phone = phone,
                            upiId = upi,
                            artisanCardNo = cardNo,
                            preferredLanguage = selectedLang.name,
                            localePreference = selectedLang.localeTag
                        )
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    modifier = Modifier.testTag("save_profile_dialog_confirm_button")
                ) {
                    Text("सहेजें (Save Profile)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("रद्द करें") }
            }
        )
    }

    // Switch Artisan / Register Modal
    if (showSwitchArtisanDialog) {
        val sampleArtisans = listOf(
            Triple("Ram Prasad Prajapati", "Terracotta & Pottery", "Bhiti Rawat, Gorakhpur, Uttar Pradesh"),
            Triple("Noor Jahan Begum", "Banarasi Brocade & Silk", "Madanpura, Varanasi, Uttar Pradesh"),
            Triple("Mangal Murmu", "Dhokra Brass Casting", "Kondagaon, Bastar, Chhattisgarh"),
            Triple("Sita Devi Jha", "Madhubani Folk Painting", "Ranti, Madhubani, Bihar")
        )

        AlertDialog(
            onDismissRequest = { showSwitchArtisanDialog = false },
            title = { Text("कारीगर खाता चुनें (Switch Artisan Account)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sampleArtisans) { (artisanName, artisanCraft, artisanCluster) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateArtisanProfile(
                                        name = artisanName,
                                        craftSpecialty = artisanCraft,
                                        experienceYears = 18,
                                        villageOrCluster = artisanCluster.substringBeforeLast(","),
                                        state = artisanCluster.substringAfterLast(",").trim(),
                                        phone = "+91 98765 43210",
                                        upiId = "${artisanName.lowercase().replace(" ", "")}@upi",
                                        artisanCardNo = "PMV-${artisanName.take(2).uppercase()}-2024-8890",
                                        preferredLanguage = profile.preferredLanguage,
                                        localePreference = profile.localePreference
                                    )
                                    showSwitchArtisanDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = ArtisanSurfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = artisanName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ArtisanTextPrimary)
                                Text(text = artisanCraft, fontSize = 12.sp, color = TerracottaPrimary)
                                Text(text = "📍 $artisanCluster", fontSize = 10.sp, color = ArtisanTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchArtisanDialog = false }) {
                    Text("बंद करें")
                }
            }
        )
    }
}

/**
 * Language Selection & AI Locale Preference Configuration Component
 * Saves language preference in Jetpack DataStore and updates SpeechRecognizer locale
 * for the voice-cataloging workflow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionSection(
    profile: ArtisanProfile,
    viewModel: HunarSetuViewModel
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val currentSelectedLang = remember(profile.preferredLanguage) {
        SupportedRegionalLanguages.find {
            it.name.equals(profile.preferredLanguage, ignoreCase = true) ||
            it.code.equals(profile.preferredLanguage, ignoreCase = true)
        } ?: SupportedRegionalLanguages.first()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("language_selection_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Title, DataStore status badge, and TTS Voice Preview
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TerracottaContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "भाषा व स्थानीयकरण (Language Switcher)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Surface(
                                color = ForestSuccess.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.testTag("datastore_sync_badge")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = ForestSuccess,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "DataStore",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestSuccess
                                    )
                                }
                            }
                        }
                        Text(
                            text = "SpeechRecognizer व AI अनुवाद वरीयता (DataStore Persisted)",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                // Voice Test Button
                IconButton(
                    onClick = {
                        val greeting = when (currentSelectedLang.code) {
                            "hi" -> "नमस्ते! आपकी पसंदीदा भाषा हिन्दी चुनी गई है। DataStore में वरीयता सहेज ली गई है और स्पीच रिकॉग्नाइज़र सक्रिय है।"
                            "mr" -> "नमस्कार! तुमची भाषा मराठी निवडली आहे. डेटास्टोअरमध्ये भाषा सेव्ह केली असून व्हॉइस कॅटलॉगर तयार आहे."
                            "gu" -> "નમસ્તે! તમારી ભાષા ગુજરાતી પસંદ કરવામાં આવી છે. DataStore સેટિંગ્સ અપડેટ થયેલ છે."
                            "bn" -> "নমস্কার! আপনার ভাষা বাংলা সেট করা হয়েছে। ভয়েস রিকগনাইজার সক্রিয়।"
                            "ta" -> "வணக்கம்! உங்கள் மொழி தமிழ் தேர்ந்தெடுக்கப்பட்டது. DataStore மற்றும் குரல் பட்டியல் அமைக்கப்பட்டது."
                            "te" -> "నమస్కారం! మీ భాష తెలుగు ఎంచుకోబడింది. స్పీచ్ రికగ్నైజర్ సిద్ధంగా ఉంది."
                            "kn" -> "ನಮಸ್ಕಾರ! ನಿಮ್ಮ ಭಾಷೆ ಕನ್ನಡ ಹೊಂದಿಸಲಾಗಿದೆ. ಧ್ವನಿ ವಿವರಣೆ ಸಿದ್ಧವಾಗಿದೆ."
                            "or" -> "ନମସ୍କାର! ଆପଣଙ୍କ ଭାଷା ଓଡ଼ିଆ ସେଟ୍ ହୋଇଛି।"
                            "pa" -> "ਸਤਿ ਸ਼੍ਰੀ ਅਕਾਲ! ਤੁਹਾਡੀ ਭਾਸ਼ਾ ਪੰਜਾਬੀ ਚੁਣੀ ਗਈ ਹੈ।"
                            else -> "Namaste! Language preference is saved in DataStore and SpeechRecognizer locale is updated to ${currentSelectedLang.localeTag}."
                        }
                        viewModel.speakText(greeting, currentSelectedLang.code)
                    },
                    modifier = Modifier.testTag("test_language_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Test AI Voice in selected language",
                        tint = TerracottaPrimary
                    )
                }
            }

            HorizontalDivider(color = ArtisanCardBorder.copy(alpha = 0.5f))

            // 1. Language Dropdown Selector
            Text(
                text = "ड्रॉपडाउन से भाषा चुनें (Select from Dropdown):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ArtisanTextSecondary
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDropdownExpanded = !isDropdownExpanded }
                        .testTag("language_dropdown_trigger"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = ArtisanSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TerracottaPrimary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = TerracottaPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = currentSelectedLang.nativeName.take(2),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${currentSelectedLang.nativeName} (${currentSelectedLang.name})",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                    Surface(
                                        color = ForestSuccess.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestSuccess,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "BCP-47 SpeechRecognizer STT: ${currentSelectedLang.localeTag}",
                                    fontSize = 11.sp,
                                    color = TerracottaPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Language Dropdown",
                            tint = TerracottaPrimary
                        )
                    }
                }

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(ArtisanSurface)
                        .testTag("language_dropdown_menu")
                ) {
                    SupportedRegionalLanguages.forEach { lang ->
                        val isCurrent = lang.name.equals(profile.preferredLanguage, ignoreCase = true)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            color = if (isCurrent) TerracottaPrimary else ArtisanSurfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = lang.nativeName.take(2),
                                                color = if (isCurrent) Color.White else ArtisanTextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "${lang.nativeName} (${lang.name})",
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp,
                                                color = if (isCurrent) TerracottaPrimary else ArtisanTextPrimary
                                            )
                                            Text(
                                                text = "STT Locale: ${lang.localeTag}",
                                                fontSize = 10.sp,
                                                color = ArtisanTextSecondary
                                            )
                                        }
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.setArtisanPreferredLanguage(lang.name, lang.localeTag)
                                isDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("language_option_${lang.code}")
                        )
                    }
                }
            }

            // 2. Quick 1-Tap Toggle Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "त्वरित भाषा टॉगल (Quick 1-Tap Switch):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArtisanTextSecondary
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SupportedRegionalLanguages) { lang ->
                        val isSelected = lang.name.equals(profile.preferredLanguage, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .clickable {
                                    viewModel.setArtisanPreferredLanguage(lang.name, lang.localeTag)
                                }
                                .testTag("quick_lang_chip_${lang.code}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TerracottaPrimary else ArtisanSurfaceVariant,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = lang.nativeName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else ArtisanTextPrimary
                                )
                                Text(
                                    text = lang.name,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else ArtisanTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 3. SpeechRecognizer & DataStore AI Pipeline Configuration Summary
            Surface(
                color = ArtisanSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("speech_recognizer_locale_badge")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "AI व स्पीच रिकॉग्नाइज़र कॉन्फ़िगरेशन (Active Pipeline)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• DataStore Preference:", fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(text = "Saved (${currentSelectedLang.code.uppercase()})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• SpeechRecognizer STT Locale:", fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(text = "${currentSelectedLang.name} (${currentSelectedLang.localeTag})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = IndigoSecondary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• Voice-Cataloging Workflow:", fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(text = "${currentSelectedLang.nativeName} ➔ English / ONDC Multilingual", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ForestSuccess)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• Vyapar Mitra Assistant:", fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(text = "${currentSelectedLang.nativeName} Dialogue Active", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TerracottaPrimary)
                    }
                }
            }

            // Button to open Full Language Preferences & SpeechRecognizer Locale Screen
            Button(
                onClick = { viewModel.navigateTo(AppNavTab.LANGUAGE_PREFERENCES) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_language_preferences_screen_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "सभी भाषाएं और स्पीच प्राथमिकताएं देखें (All Languages)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FirebaseAuthCloudSection(
    viewModel: HunarSetuViewModel,
    onOpenAuthDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isFirestoreSyncEnabled by viewModel.isFirestoreSyncEnabled.collectAsStateWithLifecycle()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("firebase_cloud_section_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(0xFFFF9800).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "क्लाउड डेटा व मल्टी-यूज़र सिंक",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "Firebase Auth & Cloud Firestore",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                Surface(
                    color = if (currentUser != null) ForestSuccess.copy(alpha = 0.15f) else TerracottaPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (currentUser != null) "● ONLINE" else "○ GUEST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentUser != null) ForestSuccess else TerracottaPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // User Identity Information Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ArtisanSurfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "खाता उपयोगकर्ता (User Identity):", fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(
                            text = currentUser?.displayName ?: "Guest Session",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoSecondary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Auth UID:", fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(
                            text = currentUser?.uid?.take(16)?.let { "$it..." } ?: "offline-local",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = ArtisanTextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "लॉगिन प्रकार (Auth Type):", fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(
                            text = if (currentUser?.isAnonymous == true) "1-Tap Guest Access" else (currentUser?.email ?: "Local Offline"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestSuccess
                        )
                    }
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenAuthDialog,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_firebase_auth_dialog_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("लॉगिन / स्विच", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.syncAllToCloudFirestore() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sync_to_firestore_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Firestore सिंक", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FirebaseAuthDialog(
    viewModel: HunarSetuViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 1-Tap Guest, 1: Email Login, 2: Register, 3: Reset
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("ARTISAN") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TerracottaPrimary)
                Text("सुरक्षित कारीगर खाता व सत्र", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = if (selectedTab > 2) 1 else selectedTab,
                    containerColor = ArtisanSurfaceVariant,
                    contentColor = TerracottaPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("1-टैप", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1 || selectedTab == 3,
                        onClick = { selectedTab = 1 },
                        text = { Text("लॉगिन", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("पंजीकरण", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // Quick 1-Tap Anonymous Login
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "बिना पासवर्ड के तुरंत शिल्पकार या खरीदार के रूप में सुरक्षित सत्र शुरू करें:",
                                fontSize = 12.sp,
                                color = ArtisanTextSecondary
                            )

                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("आपका नाम (Display Name)") },
                                placeholder = { Text("उदा. राम प्रसाद प्रजापति") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ArtisanTextSecondary) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("anonymous_name_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRole == "ARTISAN",
                                    onClick = { selectedRole = "ARTISAN" },
                                    label = { Text("शिल्पकार (Artisan)") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = selectedRole == "BUYER",
                                    onClick = { selectedRole = "BUYER" },
                                    label = { Text("खरीदार (Buyer)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Button(
                                onClick = {
                                    val name = displayName.ifBlank { if (selectedRole == "ARTISAN") "Master Artisan" else "Verified Buyer" }
                                    viewModel.signInAnonymously(name, selectedRole)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_anonymous_login_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("1-टैप क्लाउड सत्र शुरू करें")
                            }
                        }
                    }
                    1 -> {
                        // Email Login
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("ईमेल पता (Email)") },
                                placeholder = { Text("artisan@kaushvani.in") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ArtisanTextSecondary) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_login_input")
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("पासवर्ड (Password)") },
                                placeholder = { Text("कम से कम 6 अक्षर") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ArtisanTextSecondary) },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("password_login_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { selectedTab = 3 }) {
                                    Text("पासवर्ड भूल गए?", fontSize = 11.sp, color = TerracottaPrimary)
                                }
                            }

                            Button(
                                onClick = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        viewModel.signInWithEmail(email.trim(), password) { success, _ ->
                                            if (success) onDismiss()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_email_login_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary)
                            ) {
                                Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("लॉगिन करें (Sign In)")
                            }
                        }
                    }
                    2 -> {
                        // Register
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRole == "ARTISAN",
                                    onClick = { selectedRole = "ARTISAN" },
                                    label = { Text("शिल्पकार") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = selectedRole == "BUYER",
                                    onClick = { selectedRole = "BUYER" },
                                    label = { Text("खरीदार") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("पूरा नाम (Full Name)") },
                                placeholder = { Text("उदा. राम प्रसाद") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ArtisanTextSecondary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("register_name_input")
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("ईमेल पता (Email)") },
                                placeholder = { Text("artisan@gmail.com") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ArtisanTextSecondary) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth().testTag("register_email_input")
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("मोबाइल नंबर (Mobile Phone)") },
                                placeholder = { Text("+91 98765 43210") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = ArtisanTextSecondary) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("पासवर्ड (Password - कम से कम 6 अक्षर)") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ArtisanTextSecondary) },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("register_password_input")
                            )

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("पासवर्ड पुष्टि करें (Confirm Password)") },
                                leadingIcon = { Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = ArtisanTextSecondary) },
                                trailingIcon = {
                                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (isConfirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("register_confirm_password_input")
                            )

                            Button(
                                onClick = {
                                    if (email.isNotBlank() && password.length >= 6 && password == confirmPassword) {
                                        viewModel.registerWithEmail(
                                            email = email.trim(),
                                            pass = password,
                                            displayName = displayName.ifBlank { "Artisan" },
                                            role = selectedRole,
                                            phone = phone.ifBlank { null }
                                        ) { success, _ ->
                                            if (success) onDismiss()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_register_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess)
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("नया खाता बनाएं (Register)")
                            }
                        }
                    }
                    3 -> {
                        // Forgot Password Form
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "पासवर्ड रीसेट लिंक प्राप्त करने के लिए ईमेल दर्ज करें:",
                                fontSize = 12.sp,
                                color = ArtisanTextSecondary
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("पंजीकृत ईमेल") },
                                placeholder = { Text("artisan@kaushvani.in") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ArtisanTextSecondary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("reset_password_email_input")
                            )

                            Button(
                                onClick = {
                                    if (email.isNotBlank()) {
                                        viewModel.sendPasswordResetEmail(email.trim())
                                        selectedTab = 1
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("send_reset_link_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("रीसेट लिंक भेजें")
                            }

                            TextButton(onClick = { selectedTab = 1 }, modifier = Modifier.fillMaxWidth()) {
                                Text("← वापस लॉगिन पर जाएं", fontSize = 11.sp, color = IndigoSecondary)
                            }
                        }
                    }
                }

                if (currentUser != null) {
                    HorizontalDivider(color = ArtisanCardBorder)
                    OutlinedButton(
                        onClick = {
                            viewModel.signOutUser()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("profile_modal_signout_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("सत्र समाप्त करें (Sign Out)")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("बंद करें (Close)")
            }
        }
    )
}

