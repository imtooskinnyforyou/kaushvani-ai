package com.example.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BuyerInquiryEntity
import com.example.data.model.ProductEntity
import com.example.data.repository.ProductPerformanceItem
import com.example.ui.components.AddProductOnboardingCarouselDialog
import com.example.ui.components.CraftArtworkDisplay
import com.example.ui.components.OfflineActivityTrackingCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ArtisanHomeScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val inquiries by viewModel.allInquiries.collectAsStateWithLifecycle()
    val profile by viewModel.artisanProfile.collectAsStateWithLifecycle()
    val activityEvents by viewModel.allActivityEvents.collectAsStateWithLifecycle()
    val unsyncedEventsCount by viewModel.unsyncedEventsCount.collectAsStateWithLifecycle()
    val isSyncingEvents by viewModel.isSyncingEvents.collectAsStateWithLifecycle()
    val isOnboardingCarouselVisible by viewModel.isOnboardingCarouselVisible.collectAsStateWithLifecycle()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()

    val newInquiriesCount = dashboardMetrics.newInquiriesCount
    val publishedProductsCount = dashboardMetrics.publishedProducts
    val draftProductsCount = dashboardMetrics.draftProducts

    val productsListedCount = dashboardMetrics.totalProducts
    val totalInquiriesCount = dashboardMetrics.totalInquiries
    val totalViewsCount = dashboardMetrics.totalViewsCount
    val estimatedCatalogValue = dashboardMetrics.estimatedCatalogValue
    val totalSalesRevenue = dashboardMetrics.totalSalesRevenue
    val totalSalesCount = dashboardMetrics.totalSalesCount

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ArtisanBackground)
            .testTag("artisan_home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Artisan Welcome & Greeting Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("artisan_greeting_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoSecondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Namaste, ${profile.name}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "नमस्ते, ${profile.name} 🙏",
                                fontSize = 14.sp,
                                color = MarigoldTertiary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${profile.craftSpecialty} • ${profile.villageOrCluster}, ${profile.state}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        // Vishwakarma Badge & Audio Button
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = ForestSuccess,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "PM Vishwakarma",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "PM विश्वकर्मा",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    viewModel.speakText(
                                        "नमस्ते ${profile.name} जी! हुनरसेतु AI में आपका स्वागत है। आपके कैटलॉग में ${products.size} शिल्प दर्ज हैं और $newInquiriesCount नई खरीदार पूछताछ आई हैं।",
                                        "hi"
                                    )
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .semantics {
                                        role = Role.Button
                                        contentDescription = "नमस्ते अभिवादन और दैनिक सारांश सुनें (Listen to Greeting and Daily Summary)"
                                    },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. "Complete your next product listing" Prominent AI CTA Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("complete_listing_ai_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TerracottaContainer),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(TerracottaPrimary, MarigoldTertiary)
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                            Column {
                                Text(
                                    text = "Complete your next product listing",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "अपनी अगली शिल्प सूची AI से पूरी करें",
                                    fontSize = 12.sp,
                                    color = ArtisanTextSecondary
                                )
                            }
                        }

                        // Guide / Carousel Trigger Button
                        Surface(
                            color = TerracottaPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "शिल्प जोड़ने की 4-चरणीय सचित्र मार्गदर्शिका खोलें (Open 4-Step Listing Guide)"
                                }
                                .clickable { viewModel.showOnboardingCarousel() }
                                .testTag("home_onboarding_guide_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "मार्गदर्शिका",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = "बस शिल्प का फोटो लें और अपनी मातृभाषा में बोलें — AI आपके लिए व्यावसायिक विवरण, उचित मजदूरी दर (Fair Wage) और B2B टैग्स खुद बनाएगा।",
                        fontSize = 12.sp,
                        color = ArtisanTextPrimary,
                        lineHeight = 17.sp
                    )

                    // Step Pills (Clickable to open carousel)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                role = Role.Button
                                contentDescription = "कदम 1 फोटो लें, कदम 2 बोलकर बताएं, कदम 3 उचित मूल्य, कदम 4 प्रकाशित करें. विवरण देखने के लिए टैप करें"
                            }
                            .clickable { viewModel.showOnboardingCarousel() },
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ListingStepPill(number = "1", title = "फोटो लें", modifier = Modifier.weight(1f))
                        ListingStepPill(number = "2", title = "बोलकर बताएं", modifier = Modifier.weight(1f))
                        ListingStepPill(number = "3", title = "उचित मूल्य", modifier = Modifier.weight(1f))
                        ListingStepPill(number = "4", title = "प्रकाशित करें", modifier = Modifier.weight(1f))
                    }

                    // Prominent CTA Button
                    Button(
                        onClick = { viewModel.startNewProductWizard() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "AI द्वारा नया शिल्प जोड़ें (Add Product with AI)"
                            }
                            .testTag("add_product_with_ai_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Product with AI",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(AI से शिल्प जोड़ें)",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // 3. Quick Actions
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "त्वरित क्रियाएँ (Quick Actions)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArtisanQuickActionCard(
                        title = "Camera & Imagen",
                        hindiTitle = "कैमरा व AI बैकग्राउंड",
                        icon = Icons.Default.CameraEnhance,
                        iconColor = TerracottaPrimary,
                        containerColor = TerracottaContainer,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_action_photo_studio",
                        onClick = { viewModel.navigateTo(AppNavTab.PRODUCT_CAPTURE) }
                    )

                    ArtisanQuickActionCard(
                        title = "Voice Catalog",
                        hindiTitle = "बोलकर कैटलॉग",
                        icon = Icons.Default.Mic,
                        iconColor = IndigoSecondary,
                        containerColor = IndigoSecondaryContainer,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_action_voice_catalog",
                        onClick = { viewModel.navigateTo(AppNavTab.VOICE_CATALOG) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArtisanQuickActionCard(
                        title = "AI Fair Pricing",
                        hindiTitle = "उचित मूल्य कैलकुलेटर",
                        icon = Icons.Default.PriceChange,
                        iconColor = ForestSuccess,
                        containerColor = ForestContainer,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_action_pricing_assistant",
                        onClick = { viewModel.openDynamicPricingAssistant() }
                    )

                    ArtisanQuickActionCard(
                        title = "Vyapar Mitra AI",
                        hindiTitle = "व्यापार मित्र सलाहकार",
                        icon = Icons.Default.SupportAgent,
                        iconColor = Color(0xFFD97706),
                        containerColor = MarigoldContainer,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_action_ai_assistant",
                        onClick = { viewModel.navigateTo(AppNavTab.VYAPAR_MITRA_CHAT) }
                    )
                }

                // Dynamic Pricing Quick Action Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            role = Role.Button
                            contentDescription = "Dynamic Pricing Assistant (मूल्य सहायक). लागत, समय और टैग्स के आधार पर AI से उचित मूल्य जानें"
                        }
                        .clickable { viewModel.openDynamicPricingAssistant() }
                        .testTag("quick_action_pricing_assistant"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoSecondary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(IndigoSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PriceChange,
                                    contentDescription = null,
                                    tint = IndigoSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Dynamic Pricing Assistant (मूल्य सहायक)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "लागत, समय और टैग्स के आधार पर AI से उचित मूल्य जानें",
                                    fontSize = 11.sp,
                                    color = ArtisanTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = IndigoSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // D3 Sales & Pricing Analytics Interactive Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            role = Role.Button
                            contentDescription = "D3 Sales & Pricing Analytics (बिक्री व मूल्य रुझान D3 चार्ट्स). मासिक बिक्री, उत्पाद श्रेणी वितरण व मूल्य इतिहास देखें"
                        }
                        .clickable { viewModel.openSalesAnalyticsDashboard() }
                        .testTag("quick_action_d3_analytics"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = TerracottaContainer.copy(alpha = 0.45f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(TerracottaPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "D3 Sales & Pricing Analytics",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "मासिक बिक्री, उत्पाद श्रेणी वितरण व मूल्य रुझान (D3 Charts)",
                                    fontSize = 11.sp,
                                    color = TerracottaPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            color = TerracottaPrimary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "D3 चार्ट्स",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Pricing Advisor (Room DB + Material Cost based Competitive Pricing)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            role = Role.Button
                            contentDescription = "शिल्प मूल्य सलाहकार (Pricing Advisor). Room डेटाबेस और कच्चा माल लागत से नए उत्पादों के लिए प्रतिस्पर्धी मूल्य सीमाएं जानें"
                        }
                        .clickable { viewModel.openPricingAdvisor() }
                        .testTag("quick_action_pricing_advisor"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoSecondaryContainer.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoSecondary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = IndigoSecondary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PriceCheck,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "शिल्प मूल्य सलाहकार (Pricing Advisor)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary
                                )
                                Text(
                                    text = "Room डेटाबेस समकक्ष शिल्पों से प्रतिस्पर्धी मूल्य सीमाएं",
                                    fontSize = 11.sp,
                                    color = IndigoSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            color = IndigoSecondary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "सलाह लें",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Dashboard Cards (4 Key Metrics)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "डैशबोर्ड आँकड़े (Dashboard Analytics)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )

                    Surface(
                        color = TerracottaContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { viewModel.openSalesAnalyticsDashboard() }
                            .semantics {
                                role = Role.Button
                                contentDescription = "विस्तृत D3 चार्ट्स सारांश देखें (View Detailed D3 Charts Summary)"
                            }
                            .testTag("home_view_d3_charts_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "D3 चार्ट्स",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ArtisanDashboardMetricCard(
                        title = "Products Listed",
                        hindiTitle = "सूचीबद्ध शिल्प",
                        value = "$productsListedCount",
                        subValue = "$publishedProductsCount प्रकाशित • $draftProductsCount ड्राफ्ट",
                        icon = Icons.Default.Inventory2,
                        accentColor = TerracottaPrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "card_products_listed",
                        onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_CATALOG) }
                    )

                    ArtisanDashboardMetricCard(
                        title = "Buyer Inquiries",
                        hindiTitle = "खरीदार पूछताछ",
                        value = "$totalInquiriesCount",
                        subValue = if (newInquiriesCount > 0) "$newInquiriesCount नई मांगें!" else "सभी पूछताछ उत्तरित",
                        icon = Icons.Default.MarkChatUnread,
                        accentColor = ForestSuccess,
                        highlight = newInquiriesCount > 0,
                        modifier = Modifier.weight(1f),
                        testTag = "card_buyer_inquiries",
                        onClick = { viewModel.navigateTo(AppNavTab.INQUIRIES_ORDERS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ArtisanDashboardMetricCard(
                        title = "Products Viewed",
                        hindiTitle = "शिल्प देखे गए",
                        value = "$totalViewsCount+",
                        subValue = "थोक व खुदरा खरीदारों द्वारा",
                        icon = Icons.Default.Visibility,
                        accentColor = IndigoSecondary,
                        modifier = Modifier.weight(1f),
                        testTag = "card_products_viewed",
                        onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_CATALOG) }
                    )

                    ArtisanDashboardMetricCard(
                        title = "Estimated Value",
                        hindiTitle = "कैटलॉग अनुमानित मूल्य",
                        value = currencyFormatter.format(estimatedCatalogValue),
                        subValue = "स्टॉक व थोक मूल्य आधारित",
                        icon = Icons.Default.CurrencyRupee,
                        accentColor = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                        testTag = "card_estimated_catalog_value",
                        onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_CATALOG) }
                    )
                }
            }
        }

        // 5. Real-Time Product Performance & Sales Insights Card
        if (dashboardMetrics.productPerformanceList.isNotEmpty() || dashboardMetrics.totalInquiries > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_product_performance_insights"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
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
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = ForestSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "उत्पाद प्रदर्शन व बिक्री (Live Performance)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisanTextPrimary
                                    )
                                }
                                Text(
                                    text = "Real-time Firestore Analytics & Conversion Funnel",
                                    fontSize = 10.sp,
                                    color = ArtisanTextSecondary
                                )
                            }

                            Surface(
                                color = ForestSuccess.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ForestSuccess)
                                    )
                                    Text(
                                        text = "LIVE SYNCED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestSuccess
                                    )
                                }
                            }
                        }

                        // Aggregate Stats Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ArtisanSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "कुल बिक्री राजस्व", fontSize = 10.sp, color = ArtisanTextMuted)
                                Text(
                                    text = currencyFormatter.format(totalSalesRevenue),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestSuccess
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "स्वीकृत आर्डर्स", fontSize = 10.sp, color = ArtisanTextMuted)
                                Text(
                                    text = "${dashboardMetrics.acceptedOrdersCount} ऑर्डर्स",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "रूपांतरण दर (Conversion)", fontSize = 10.sp, color = ArtisanTextMuted)
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f%%", dashboardMetrics.inquiryConversionRate),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaPrimary
                                )
                            }
                        }

                        // Top performing crafts list preview
                        if (dashboardMetrics.topPerformingProducts.isNotEmpty()) {
                            Text(
                                text = "🌟 शीर्ष प्रदर्शन करने वाले शिल्प (Top Performing Crafts):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                dashboardMetrics.topPerformingProducts.take(3).forEachIndexed { idx, item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(ArtisanSurface, RoundedCornerShape(8.dp))
                                            .border(1.dp, ArtisanCardBorder, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                color = if (idx == 0) MarigoldTertiary else IndigoSecondaryContainer,
                                                shape = CircleShape,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "#${idx + 1}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (idx == 0) Color.Black else IndigoSecondary
                                                    )
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ArtisanTextPrimary,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${item.category} • ${item.viewCount} दृश्य • ${item.inquiryCount} पूछताछ",
                                                    fontSize = 10.sp,
                                                    color = ArtisanTextSecondary
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            if (item.revenueGenerated > 0) {
                                                Text(
                                                    text = currencyFormatter.format(item.revenueGenerated),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ForestSuccess
                                                )
                                            }
                                            Text(
                                                text = when (item.stockStatus) {
                                                    "OUT_OF_STOCK" -> "स्टॉक समाप्त"
                                                    "LOW_STOCK" -> "कम स्टॉक (${item.stockAvailable})"
                                                    else -> "स्टॉक में (${item.stockAvailable})"
                                                },
                                                fontSize = 9.sp,
                                                color = when (item.stockStatus) {
                                                    "OUT_OF_STOCK" -> TerracottaPrimary
                                                    "LOW_STOCK" -> Color(0xFFD97706)
                                                    else -> ForestSuccess
                                                },
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Offline Business Activity & Performance Tracking (Room DB + Auto Sync)
        item {
            OfflineActivityTrackingCard(
                events = activityEvents,
                unsyncedCount = unsyncedEventsCount,
                isSyncing = isSyncingEvents,
                onSyncClick = { viewModel.syncActivityEvents() },
                onLogSampleEvent = {
                    viewModel.logCustomActivityEvent(
                        eventType = "PRICING_CALCULATED",
                        title = "मैन्युअल गतिविधि दर्ज (Manual Activity Logged)",
                        description = "Artisan verified inventory & calculated fair price locally in offline mode",
                        category = "PRICING"
                    )
                }
            )
        }

        // 6. Recent Live Inquiries Preview
        if (inquiries.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "हालिया खरीदार पूछताछ (Recent Inquiries)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    TextButton(onClick = { viewModel.navigateTo(AppNavTab.INQUIRIES_ORDERS) }) {
                        Text("सभी देखें (${inquiries.size})", fontSize = 12.sp, color = TerracottaPrimary)
                    }
                }
            }

            items(inquiries.take(2)) { inquiry ->
                HomeInquiryPreviewCard(
                    inquiry = inquiry,
                    onReplyClick = { viewModel.navigateTo(AppNavTab.INQUIRIES_ORDERS) }
                )
            }
        }

        // 6. Recent Products in Catalog Preview
        if (products.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "आपके सूचीबद्ध शिल्प (Recent Crafts)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    TextButton(onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_CATALOG) }) {
                        Text("कैटलॉग खोलें (${products.size})", fontSize = 12.sp, color = IndigoSecondary)
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products.take(4)) { product ->
                        HomeProductMiniCard(
                            product = product,
                            onCardClick = { viewModel.openProductDetail(product) },
                            onTogglePublish = { viewModel.toggleProductPublishStatus(product.id) },
                            onPlayAudio = {
                                val textToSpeak = product.regionalDescription.ifBlank { product.description }
                                viewModel.speakText(textToSpeak, "hi")
                            }
                        )
                    }
                }
            }
        }

        // 7. Vyapar Mitra Advisory Tip
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        contentDescription = "व्यापार मित्र की सलाह (AI Tip): आगामी त्योहारी सीजन के लिए 25+ पीस के थोक आर्डर पर 15% छूट देने से B2B खरीदारों के आर्डर 3 गुना बढ़ते हैं। चैट खोलने के लिए टैप करें।"
                    }
                    .clickable { viewModel.navigateTo(AppNavTab.VYAPAR_MITRA_CHAT) },
                shape = RoundedCornerShape(14.dp),
                color = MarigoldContainer.copy(alpha = 0.5f),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "व्यापार मित्र की सलाह (AI Tip)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "आगामी त्योहारी सीजन के लिए 25+ पीस के थोक आर्डर पर 15% छूट देने से B2B खरीदारों के आर्डर 3 गुना बढ़ते हैं।",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Chat",
                        tint = ArtisanTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Interactive Onboarding Carousel for 'Add Product with AI' Workflow
    AddProductOnboardingCarouselDialog(
        isOpen = isOnboardingCarouselVisible,
        onDismiss = { viewModel.dismissOnboardingCarousel() },
        onStartAddProduct = { viewModel.startNewProductWizard() },
        onSpeakText = { text, lang -> viewModel.speakText(text, lang) }
    )
}

@Composable
fun ListingStepPill(
    number: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(TerracottaPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = ArtisanTextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ArtisanQuickActionCard(
    title: String,
    hindiTitle: String,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    testTag: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .semantics {
                role = Role.Button
                contentDescription = "$title ($hindiTitle)${if (badgeCount > 0) ", $badgeCount new items" else ""}"
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (badgeCount > 0) {
                            Badge(containerColor = Color(0xFFDC2626)) {
                                Text("$badgeCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )
                Text(
                    text = hindiTitle,
                    fontSize = 11.sp,
                    color = ArtisanTextSecondary
                )
            }
        }
    }
}

@Composable
fun ArtisanDashboardMetricCard(
    title: String,
    hindiTitle: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    testTag: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .semantics {
                role = Role.Button
                contentDescription = "$title ($hindiTitle): $value. $subValue"
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArtisanTextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Color(0xFFDC2626) else ArtisanTextPrimary
            )

            Text(
                text = hindiTitle,
                fontSize = 11.sp,
                color = ArtisanTextSecondary
            )

            HorizontalDivider(color = ArtisanCardBorder.copy(alpha = 0.5f))

            Text(
                text = subValue,
                fontSize = 10.sp,
                color = if (highlight) ForestSuccess else ArtisanTextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HomeInquiryPreviewCard(
    inquiry: BuyerInquiryEntity,
    onReplyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = "खरीदार पूछताछ: ${inquiry.buyerName}, शिल्प: ${inquiry.productTitle}, मांग: ${inquiry.requestedQuantity} पीस${if (inquiry.status == "NEW") ", स्थिति: नया" else ""}"
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(IndigoSecondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = inquiry.buyerName.take(1),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = IndigoSecondary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = inquiry.buyerName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    if (inquiry.status == "NEW") {
                        Surface(
                            color = Color(0xFFDC2626),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "नया (NEW)",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${inquiry.productTitle} • ${inquiry.requestedQuantity} पीस मांग",
                    fontSize = 11.sp,
                    color = ArtisanTextSecondary
                )
            }

            FilledTonalButton(
                onClick = onReplyClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.semantics {
                    role = Role.Button
                    contentDescription = "${inquiry.buyerName} की पूछताछ का उत्तर दें"
                }
            ) {
                Text("उत्तर दें", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun HomeProductMiniCard(
    product: ProductEntity,
    onCardClick: () -> Unit = {},
    onTogglePublish: () -> Unit,
    onPlayAudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .semantics {
                role = Role.Button
                contentDescription = "${product.title}, खुदरा मूल्य ₹${product.retailPrice.toInt()}, B2B मूल्य ₹${product.wholesalePrice.toInt()}, स्थिति ${if (product.status == "PUBLISHED") "प्रकाशित (Live)" else "ड्राफ्ट (Draft)"}"
            }
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                CraftArtworkDisplay(
                    imageUri = product.imageUri,
                    category = product.category,
                    styleFilter = product.imageStyleFilter,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    color = if (product.status == "PUBLISHED") ForestSuccess else Color(0xFF6B7280),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = if (product.status == "PUBLISHED") "Live" else "Draft",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "₹${product.retailPrice.toInt()} • B2B ₹${product.wholesalePrice.toInt()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TerracottaPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier
                            .size(24.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "${product.title} का ऑडियो विवरण सुनें"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = IndigoSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    TextButton(
                        onClick = onTogglePublish,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier.semantics {
                            role = Role.Button
                            contentDescription = if (product.status == "PUBLISHED") "${product.title} को कैटलॉग से हटाएं (Unpublish)" else "${product.title} को ऑनलाइन प्रकाशित करें (Publish)"
                        }
                    ) {
                        Text(
                            text = if (product.status == "PUBLISHED") "हटाएं" else "प्रकाशित करें",
                            fontSize = 10.sp,
                            color = if (product.status == "PUBLISHED") Color(0xFFDC2626) else ForestSuccess
                        )
                    }
                }
            }
        }
    }
}
