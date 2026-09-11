package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BuyerInquiryEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.HunarSetuViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InquiriesOrdersScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val inquiries by viewModel.allInquiries.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }

    // Dialog state for responding to inquiry
    var respondingInquiry by remember { mutableStateOf<BuyerInquiryEntity?>(null) }
    // Dialog state for rejecting inquiry
    var rejectingInquiry by remember { mutableStateOf<BuyerInquiryEntity?>(null) }

    val filterOptions = listOf(
        Pair("ALL", "सभी (All)"),
        Pair("NEW", "नई पूछताछ (New)"),
        Pair("RESPONDED", "उत्तर दिया (Responded)"),
        Pair("ACCEPTED", "स्वीकृत (Accepted)"),
        Pair("REJECTED", "अस्वीकृत (Rejected)")
    )

    val newInquiriesCount = remember(inquiries) {
        inquiries.count { it.status == "NEW" }
    }

    val filteredInquiries = remember(inquiries, selectedFilter) {
        when (selectedFilter) {
            "ALL" -> inquiries
            "RESPONDED" -> inquiries.filter { it.status == "RESPONDED" || it.status == "QUOTE_SENT" }
            else -> inquiries.filter { it.status == selectedFilter }
        }
    }

    val totalPotentialValue = inquiries.sumOf { it.requestedQuantity * it.offeredPricePerUnit }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ArtisanSurface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "B2B खरीदार पूछताछ (Buyer Inquiries)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "Manage wholesale RFQ quotes and bulk buyer inquiries",
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary
                        )
                    }

                    if (newInquiriesCount > 0) {
                        Surface(
                            color = TerracottaPrimary,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("badge_new_inquiries_count")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$newInquiriesCount नई पूछताछ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Status Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions) { (key, label) ->
                        val isSelected = key == selectedFilter
                        val count = when (key) {
                            "ALL" -> inquiries.size
                            "NEW" -> inquiries.count { it.status == "NEW" }
                            "RESPONDED" -> inquiries.count { it.status == "RESPONDED" || it.status == "QUOTE_SENT" }
                            "ACCEPTED" -> inquiries.count { it.status == "ACCEPTED" }
                            "REJECTED" -> inquiries.count { it.status == "REJECTED" }
                            else -> 0
                        }
                        Surface(
                            modifier = Modifier
                                .semantics {
                                    role = androidx.compose.ui.semantics.Role.Tab
                                    this.selected = isSelected
                                    contentDescription = "$label फ़िल्टर, $count पूछताछ"
                                    stateDescription = if (isSelected) "चयनित (Selected)" else "उपलब्ध (Available)"
                                }
                                .testTag("filter_inquiry_$key")
                                .clickable { selectedFilter = key },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) IndigoSecondary else ArtisanSurfaceVariant,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else ArtisanTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Pipeline Summary & New Buyer Alert Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoSecondary)
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
                            Column {
                                Text(
                                    text = "कुल संभावित B2B ऑर्डर मूल्य (Total Inquiries Pipeline)",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "₹${totalPotentialValue.toInt()}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${inquiries.size} पूछताछ कुल",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        if (newInquiriesCount > 0) {
                            Surface(
                                color = Color(0xFFFFF3CD),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MarkEmailUnread,
                                        contentDescription = null,
                                        tint = Color(0xFF856404),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "New Buyer Inquiry: $newInquiriesCount खरीदारों ने कोटेशन मांगी है। कृपया जल्दी जवाब दें।",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF856404),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredInquiries.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            color = IndigoSecondaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkChatUnread,
                                contentDescription = null,
                                tint = IndigoSecondary,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxSize()
                            )
                        }
                        
                        Text(
                            text = "No Inquiries Yet!",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = ArtisanTextPrimary
                        )
                        
                        Text(
                            text = "Your inbox is waiting. Share your product links on WhatsApp and social media to attract more potential buyers and receive wholesale quotes!\n\n(अपने उत्पादों को व्हाट्सएप पर साझा करें और अधिक खरीदारों को आकर्षित करें!)",
                            fontSize = 13.sp,
                            color = ArtisanTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out our exclusive artisan crafts collection on KAUSHVANI! Place your wholesale inquiries today.")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Catalog")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Products", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(filteredInquiries, key = { it.id }) { inquiry ->
                    BuyerInquiryCard(
                        inquiry = inquiry,
                        onRespond = { respondingInquiry = inquiry },
                        onAccept = {
                            viewModel.updateInquiryStatus(inquiry.id, "ACCEPTED")
                        },
                        onReject = { rejectingInquiry = inquiry },
                        onCallBuyer = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${inquiry.buyerPhone}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        onSpeak = {
                            val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(inquiry.timestamp))
                            val msg = "नई खरीदार पूछताछ। खरीदार ${inquiry.buyerName}, संस्था ${inquiry.buyerCompany}। उत्पाद: ${inquiry.productTitle}। मांगी गई मात्रा: ${inquiry.requestedQuantity} यूनिट। आवश्यक डिलीवरी तिथि: ${inquiry.requiredDate}। सन्देश: ${inquiry.message}"
                            viewModel.speakText(msg, "hi")
                        }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOG: RESPOND TO INQUIRY
    // ==========================================
    respondingInquiry?.let { inquiry ->
        var responseText by remember {
            mutableStateOf(
                if (inquiry.artisanResponse.isNotBlank()) inquiry.artisanResponse
                else "नमस्ते ${inquiry.buyerName} जी! हम आपकी मांगी गई ${inquiry.requestedQuantity} यूनिट्स ${inquiry.requiredDate} तक तैयार कर सकते हैं।"
            )
        }
        var offeredPrice by remember { mutableStateOf(inquiry.offeredPricePerUnit.toInt().toString()) }

        val presetResponses = listOf(
            "हाँ, हम निश्चित समय में उच्च गुणवत्ता के साथ तैयार कर सकते हैं।",
            "कस्टम पैकेजिंग और जीआई प्रामाणिकता प्रमाणपत्र शामिल होगा।",
            "सैंपल पीस तुरंत भेजा जा सकता है, थोक लॉट 15 दिन में तैयार होगा।"
        )

        AlertDialog(
            onDismissRequest = { respondingInquiry = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null,
                        tint = TerracottaPrimary
                    )
                    Text("खरीदार को उत्तर दें (Respond to Inquiry)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Summary info
                    Surface(
                        color = ArtisanSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "खरीदार: ${inquiry.buyerName} (${inquiry.buyerCompany})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Text(
                                text = "उत्पाद: ${inquiry.productTitle}",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary,
                                maxLines = 1
                            )
                            Text(
                                text = "मात्रा: ${inquiry.requestedQuantity} यूनिट • आवश्यक तिथि: ${inquiry.requiredDate}",
                                fontSize = 11.sp,
                                color = TerracottaPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "त्वरित उत्तर सुझाव (Quick Replies):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextSecondary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetResponses) { preset ->
                            Surface(
                                modifier = Modifier.clickable { responseText = preset },
                                shape = RoundedCornerShape(8.dp),
                                color = ArtisanSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 10.sp,
                                    color = ArtisanTextPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = responseText,
                        onValueChange = { responseText = it },
                        label = { Text("आपका उत्तर / कोटेशन सन्देश (Response Message)", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_artisan_response_text"),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.respondToBuyerInquiry(inquiry.id, responseText)
                        respondingInquiry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_confirm_send_response")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("उत्तर भेजें (Send Response)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { respondingInquiry = null }) {
                    Text("रद्द करें (Cancel)")
                }
            }
        )
    }

    // ==========================================
    // DIALOG: REJECT INQUIRY CONFIRMATION
    // ==========================================
    rejectingInquiry?.let { inquiry ->
        var rejectionReason by remember { mutableStateOf("वर्तमान में कार्यशाला क्षमता पूर्ण है (Workshop at capacity)") }

        val rejectionReasons = listOf(
            "वर्तमान में कार्यशाला क्षमता पूर्ण है (Workshop at capacity)",
            "आवश्यक तिथि बहुत निकट है (Lead time too short)",
            "कच्चा माल अनुपलब्ध है (Raw materials unavailable)",
            "मात्रा न्यूनतम आर्डर से कम है (Below MOQ)"
        )

        AlertDialog(
            onDismissRequest = { rejectingInquiry = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                    Text("पूछताछ अस्वीकार करें (Reject Inquiry)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "क्या आप ${inquiry.buyerName} (${inquiry.buyerCompany}) के ${inquiry.requestedQuantity} पीस के अनुरोध को अस्वीकार करना चाहते हैं?",
                        fontSize = 13.sp,
                        color = ArtisanTextPrimary
                    )

                    Text(
                        text = "कारण चुनें (Select Reason):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextSecondary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        rejectionReasons.forEach { reason ->
                            val isSelected = rejectionReason == reason
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { rejectionReason = reason },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFFFEBEE) else ArtisanSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFFDC2626) else ArtisanCardBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { rejectionReason = reason },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFDC2626))
                                    )
                                    Text(
                                        text = reason,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFFDC2626) else ArtisanTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateInquiryStatus(inquiry.id, "REJECTED")
                        rejectingInquiry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_confirm_reject_inquiry")
                ) {
                    Text("अस्वीकार करें (Reject)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectingInquiry = null }) {
                    Text("वापस जाएँ (Cancel)")
                }
            }
        )
    }
}

/**
 * Single B2B Buyer Inquiry Card Component
 * Fully presents:
 * - "New Buyer Inquiry" banner / status
 * - Buyer (Name, Organization, Contact)
 * - Product
 * - Quantity
 * - Message
 * - Date (Received Date + Required Date)
 * - Actions: [Respond], [Accept], [Reject]
 */
@Composable
fun BuyerInquiryCard(
    inquiry: BuyerInquiryEntity,
    onRespond: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCallBuyer: () -> Unit,
    onSpeak: () -> Unit
) {
    val context = LocalContext.current
    val formattedDate = remember(inquiry.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(inquiry.timestamp))
    }

    val isNew = inquiry.status == "NEW"

    val statusColor = when (inquiry.status) {
        "NEW" -> Color(0xFFE53935)
        "RESPONDED", "QUOTE_SENT" -> Color(0xFF1E88E5)
        "ACCEPTED" -> ForestSuccess
        "REJECTED", "DECLINED" -> Color(0xFF757575)
        "COMPLETED" -> IndigoSecondary
        else -> ArtisanTextMuted
    }

    val statusLabel = when (inquiry.status) {
        "NEW" -> "New Buyer Inquiry"
        "RESPONDED", "QUOTE_SENT" -> "उत्तर भेजा गया (Responded)"
        "ACCEPTED" -> "स्वीकृत आर्डर (Accepted)"
        "REJECTED", "DECLINED" -> "अस्वीकृत (Rejected)"
        "COMPLETED" -> "पूर्ण (Completed)"
        else -> inquiry.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = androidx.compose.ui.semantics.Role.Button
                contentDescription = "खरीदार पूछताछ: ${inquiry.buyerName} (${inquiry.buyerCompany}), शिल्प: ${inquiry.productTitle}, मात्रा: ${inquiry.requestedQuantity} यूनिट, स्थिति: $statusLabel, आवश्यक तिथि: ${inquiry.requiredDate}"
            }
            .testTag("inquiry_card_${inquiry.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNew) Color(0xFFFFFDF9) else ArtisanSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isNew) 1.5.dp else 1.dp,
            color = if (isNew) TerracottaPrimary.copy(alpha = 0.5f) else ArtisanCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Buyer Info & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isNew) TerracottaContainer else IndigoSecondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inquiry.buyerName.take(1).uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = if (isNew) TerracottaPrimary else IndigoSecondary
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = inquiry.buyerName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            if (inquiry.buyerCompany.isNotBlank()) {
                                Surface(
                                    color = ArtisanSurfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = inquiry.buyerCompany,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ArtisanTextSecondary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Organization & Contact Line
                        Text(
                            text = "📞 ${inquiry.buyerPhone} • ✉️ ${inquiry.buyerEmail}",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                // Status Badge
                Surface(
                    color = statusColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Product & Quantity Summary Box
            Surface(
                color = ArtisanSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "उत्पाद (Product):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextMuted
                            )
                            Text(
                                text = inquiry.productTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary,
                                maxLines = 2
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "मात्रा (Quantity)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextMuted
                            )
                            Text(
                                text = "${inquiry.requestedQuantity} यूनिट (Pcs)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = IndigoSecondary
                            )
                        }
                    }

                    HorizontalDivider(color = ArtisanCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "प्रस्तावित मूल्य: ₹${inquiry.offeredPricePerUnit.toInt()}/pc • कुल: ₹${(inquiry.requestedQuantity * inquiry.offeredPricePerUnit).toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestSuccess
                        )

                        Text(
                            text = "📍 ${inquiry.buyerCity}",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }
            }

            // Dates Row: Received Date & Required Delivery Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = ArtisanTextMuted, modifier = Modifier.size(13.dp))
                    Text(
                        text = "प्राप्त: $formattedDate",
                        fontSize = 11.sp,
                        color = ArtisanTextSecondary
                    )
                }

                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(12.dp))
                        Text(
                            text = "आवश्यक तिथि: ${inquiry.requiredDate}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17)
                        )
                    }
                }
            }

            // Message from Buyer
            if (inquiry.message.isNotBlank()) {
                Surface(
                    color = Color(0xFFF7F5F0),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "खरीदार का सन्देश (Message):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextMuted
                        )
                        Text(
                            text = "\"${inquiry.message}\"",
                            fontSize = 12.sp,
                            color = ArtisanTextPrimary,
                            lineHeight = 17.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // Artisan previous response (if exists)
            if (inquiry.artisanResponse.isNotBlank()) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ForestSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Column {
                            Text(
                                text = "शिल्पकार का भेजा गया उत्तर:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestSuccess
                            )
                            Text(
                                text = inquiry.artisanResponse,
                                fontSize = 11.sp,
                                color = ArtisanTextPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Actions Row: [Listen Audio] + [Respond] + [Accept] + [Reject]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio Readout Icon
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier
                        .size(38.dp)
                        .background(ArtisanSurfaceVariant, shape = RoundedCornerShape(10.dp))
                        .semantics {
                            role = androidx.compose.ui.semantics.Role.Button
                            contentDescription = "${inquiry.buyerName} की पूछताछ का विवरण ऑडियो में सुनें"
                        }
                        .testTag("btn_speak_inquiry_${inquiry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Quick Call Button
                IconButton(
                    onClick = onCallBuyer,
                    modifier = Modifier
                        .size(38.dp)
                        .background(ArtisanSurfaceVariant, shape = RoundedCornerShape(10.dp))
                        .semantics {
                            role = androidx.compose.ui.semantics.Role.Button
                            contentDescription = "खरीदार ${inquiry.buyerName} को फोन करें: ${inquiry.buyerPhone}"
                        }
                        .testTag("btn_call_buyer_${inquiry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = IndigoSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Action 1: [Respond]
                OutlinedButton(
                    onClick = onRespond,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            role = androidx.compose.ui.semantics.Role.Button
                            contentDescription = "${inquiry.buyerName} को कोटेशन या उत्तर भेजें"
                        }
                        .testTag("btn_respond_inquiry_${inquiry.id}"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Respond", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Action 2: [Accept]
                if (inquiry.status != "ACCEPTED" && inquiry.status != "COMPLETED") {
                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                role = androidx.compose.ui.semantics.Role.Button
                                contentDescription = "${inquiry.buyerName} का ${inquiry.requestedQuantity} यूनिट का आर्डर स्वीकार करें"
                            }
                            .testTag("btn_accept_inquiry_${inquiry.id}"),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Action 3: [Reject]
                if (inquiry.status != "REJECTED" && inquiry.status != "COMPLETED") {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                role = androidx.compose.ui.semantics.Role.Button
                                contentDescription = "${inquiry.buyerName} की पूछताछ अस्वीकार करें"
                            }
                            .testTag("btn_reject_inquiry_${inquiry.id}"),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }
            }
        }
    }
}
