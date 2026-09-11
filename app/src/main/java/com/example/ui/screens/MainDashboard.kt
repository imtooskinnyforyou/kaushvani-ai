package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.data.model.UserRole
import com.example.ui.components.AppHeaderOfflineSyncBar
import com.example.ui.components.GlobalLoadingOverlay
import com.example.ui.components.HierarchicalCategoryBottomSheet
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel

/**
 * Navigation item specification for the accessible Bottom Navigation bar
 */
data class DashboardBottomNavItem(
    val tab: AppNavTab,
    val title: String,
    val hindiTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val contentDescription: String,
    val isPrimaryAction: Boolean = false
)

/**
 * MainDashboard Composable
 *
 * Implements an accessible Scaffold layout with high-contrast BottomNavigation
 * hosting the 5 primary tabs for artisans:
 * 1. Home (होम)
 * 2. Catalog (कैटलॉग)
 * 3. Add Product (शिल्प जोड़ें / AI Voice Wizard)
 * 4. Inquiries & Orders (पूछताछ व ऑर्डर्स)
 * 5. Profile & Settings (कारीगर प्रोफ़ाइल)
 *
 * Fully conforms to Android Accessibility standards (>=48dp touch targets, TalkBack semantics,
 * and high-contrast Material Design 3 styling).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MainDashboard(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val inquiries by viewModel.allInquiries.collectAsStateWithLifecycle()
    val globalLoadingState by viewModel.globalLoadingState.collectAsStateWithLifecycle()
    val isCategoryHierarchySheetVisible by viewModel.isCategoryHierarchySheetVisible.collectAsStateWithLifecycle()
    val hierarchicalCategoryMapping by viewModel.hierarchicalCategoryMapping.collectAsStateWithLifecycle()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val newInquiriesCount = dashboardMetrics.newInquiriesCount

    // Sync state flows for offline indicator
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsStateWithLifecycle()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsStateWithLifecycle()
    val draftProductsCount by viewModel.draftProductsCount.collectAsStateWithLifecycle()
    val unsyncedEventsCount by viewModel.unsyncedEventsCount.collectAsStateWithLifecycle()
    val totalPendingDraftsCount by viewModel.totalPendingDraftsCount.collectAsStateWithLifecycle()
    val pendingDraftProducts by viewModel.pendingDraftProducts.collectAsStateWithLifecycle()
    val pendingCatalogQueueCount by viewModel.pendingCatalogQueueCount.collectAsStateWithLifecycle()
    val pendingAiTasksCount by viewModel.pendingAiTasksCount.collectAsStateWithLifecycle()

    // 5 Core Accessible Navigation Destinations for Artisans
    val artisanNavItems = listOf(
        DashboardBottomNavItem(
            tab = AppNavTab.ARTISAN_HOME,
            title = "Home",
            hindiTitle = "होम",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            testTag = "bottom_nav_home",
            contentDescription = "Home dashboard, shows earnings, AI quick actions, and recent activity"
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.ARTISAN_CATALOG,
            title = "Catalog",
            hindiTitle = "कैटलॉग",
            selectedIcon = Icons.Filled.Inventory2,
            unselectedIcon = Icons.Outlined.Inventory2,
            testTag = "bottom_nav_catalog",
            contentDescription = "Craft catalog, manage products, stock, and digital showroom"
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.SMART_CATALOG_WIZARD,
            title = "Add Product",
            hindiTitle = "शिल्प जोड़ें",
            selectedIcon = Icons.Filled.AddCircle,
            unselectedIcon = Icons.Outlined.AddCircleOutline,
            testTag = "bottom_nav_add_product",
            contentDescription = "Add new craft product with AI voice wizard and photo scanner",
            isPrimaryAction = true
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.INQUIRIES_ORDERS,
            title = "Inquiries",
            hindiTitle = "पूछताछ",
            selectedIcon = Icons.Filled.MarkChatUnread,
            unselectedIcon = Icons.Outlined.ChatBubbleOutline,
            testTag = "bottom_nav_inquiries",
            contentDescription = "Buyer inquiries and purchase orders. $newInquiriesCount new messages"
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.ARTISAN_PROFILE,
            title = "Profile",
            hindiTitle = "प्रोफ़ाइल",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.PersonOutline,
            testTag = "bottom_nav_profile",
            contentDescription = "Artisan profile, passport ID, language settings, and bank details"
        )
    )

    // Buyer Role Navigation items
    val buyerNavItems = listOf(
        DashboardBottomNavItem(
            tab = AppNavTab.BUYER_MARKETPLACE,
            title = "Marketplace",
            hindiTitle = "शिल्प बाज़ार",
            selectedIcon = Icons.Filled.Storefront,
            unselectedIcon = Icons.Outlined.Storefront,
            testTag = "bottom_nav_marketplace",
            contentDescription = "Explore direct artisan crafts and verified master artisans"
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.INQUIRIES_ORDERS,
            title = "My Orders",
            hindiTitle = "मेरी पूछताछ",
            selectedIcon = Icons.Filled.ShoppingBag,
            unselectedIcon = Icons.Outlined.ShoppingBag,
            testTag = "bottom_nav_buyer_orders",
            contentDescription = "My active bulk orders and quotation discussions"
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.VYAPAR_MITRA_CHAT,
            title = "AI Advisor",
            hindiTitle = "AI मित्र",
            selectedIcon = Icons.Filled.AutoAwesome,
            unselectedIcon = Icons.Outlined.AutoAwesome,
            testTag = "bottom_nav_ai_advisor",
            contentDescription = "Vyapar Mitra AI advisory chat"
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.ARTISAN_PROFILE,
            title = "Artisans",
            hindiTitle = "कारीगर",
            selectedIcon = Icons.Filled.Badge,
            unselectedIcon = Icons.Outlined.Badge,
            testTag = "bottom_nav_buyer_profile",
            contentDescription = "Verified artisan directory and heritage profiles"
        )
    )

    // Admin Role Navigation items
    val adminNavItems = listOf(
        DashboardBottomNavItem(
            tab = AppNavTab.ADMIN_DASHBOARD,
            title = "Console",
            hindiTitle = "प्रशासन",
            selectedIcon = Icons.Filled.AdminPanelSettings,
            unselectedIcon = Icons.Outlined.AdminPanelSettings,
            testTag = "bottom_nav_admin_console",
            contentDescription = "Platform administration, telemetry, and artisan verification"
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.BUYER_MARKETPLACE,
            title = "Catalog",
            hindiTitle = "मार्केटप्लेस",
            selectedIcon = Icons.Filled.Storefront,
            unselectedIcon = Icons.Outlined.Storefront,
            testTag = "bottom_nav_admin_catalog",
            contentDescription = "Global marketplace catalog review"
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.INQUIRIES_ORDERS,
            title = "Orders",
            hindiTitle = "आर्डर्स",
            selectedIcon = Icons.Filled.ReceiptLong,
            unselectedIcon = Icons.Outlined.ReceiptLong,
            testTag = "bottom_nav_admin_orders",
            contentDescription = "Platform trade volume and dispute monitoring"
        ),
        DashboardBottomNavItem(
            tab = AppNavTab.VYAPAR_MITRA_CHAT,
            title = "AI Telemetry",
            hindiTitle = "AI लॉग्स",
            selectedIcon = Icons.Filled.Psychology,
            unselectedIcon = Icons.Outlined.Psychology,
            testTag = "bottom_nav_admin_ai",
            contentDescription = "Gemini LLM model latency and token telemetry"
        )
    )

    val currentNavItems = when (currentRole) {
        UserRole.ARTISAN -> artisanNavItems
        UserRole.BUYER -> buyerNavItems
        UserRole.ADMIN -> adminNavItems
    }

    val isFullscreenFlow = currentTab == AppNavTab.SMART_CATALOG_WIZARD || 
        currentTab == AppNavTab.VOICE_CATALOG ||
        currentTab == AppNavTab.PRODUCT_CAPTURE || 
        currentTab == AppNavTab.PRODUCT_DETAIL ||
        currentTab == AppNavTab.LANGUAGE_PREFERENCES

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(ArtisanBackground)
                .testTag("main_dashboard_scaffold"),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (!isFullscreenFlow && currentTab != AppNavTab.VYAPAR_MITRA_CHAT) {
                    Surface(
                        color = ArtisanSurface,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_dashboard_top_bar")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Offline Sync Status Indicator Bar with Retry Sync Button
                            AppHeaderOfflineSyncBar(
                                pendingDraftsCount = totalPendingDraftsCount,
                                draftProductsCount = draftProductsCount,
                                unsyncedEventsCount = unsyncedEventsCount,
                                isSyncing = isSyncing,
                                isNetworkAvailable = isNetworkAvailable,
                                syncState = syncState,
                                lastSyncTimestamp = lastSyncTimestamp,
                                pendingDraftProducts = pendingDraftProducts,
                                onRetrySync = { viewModel.retrySyncAll() },
                                onPublishDraft = { draft -> viewModel.publishDraftProduct(draft) },
                                pendingCatalogSyncCount = pendingCatalogQueueCount,
                                pendingAiTasksCount = pendingAiTasksCount
                            )

                            // Accessible Role Switcher Segmented Control
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArtisanSurfaceVariant)
                                    .padding(4.dp)
                                    .testTag("role_switcher_bar"),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RoleTabItem(
                                    title = "👨‍🎨 कारीगर",
                                    subtitle = "Artisan",
                                    isSelected = currentRole == UserRole.ARTISAN,
                                    selectedColor = TerracottaPrimary,
                                    testTag = "role_tab_artisan",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.setUserRole(UserRole.ARTISAN) }
                                )
                                RoleTabItem(
                                    title = "🛍️ खरीदार",
                                    subtitle = "Buyer",
                                    isSelected = currentRole == UserRole.BUYER,
                                    selectedColor = IndigoSecondary,
                                    testTag = "role_tab_buyer",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.setUserRole(UserRole.BUYER) }
                                )
                                RoleTabItem(
                                    title = "🛡️ एडमिन",
                                    subtitle = "Admin",
                                    isSelected = currentRole == UserRole.ADMIN,
                                    selectedColor = ForestSuccess,
                                    testTag = "role_tab_admin",
                                    modifier = Modifier.weight(1f),
                                    onClick = { viewModel.setUserRole(UserRole.ADMIN) }
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (!isFullscreenFlow) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_dashboard_bottom_navigation"),
                        containerColor = ArtisanSurface,
                        tonalElevation = 8.dp
                    ) {
                        currentNavItems.forEach { item ->
                            val isSelected = currentTab == item.tab

                            NavigationBarItem(
                                modifier = Modifier
                                    .testTag(item.testTag)
                                    .semantics {
                                        contentDescription = item.contentDescription
                                    },
                                selected = isSelected,
                                onClick = {
                                    if (item.tab == AppNavTab.SMART_CATALOG_WIZARD) {
                                        viewModel.startNewProductWizard()
                                    } else {
                                        viewModel.navigateTo(item.tab)
                                    }
                                },
                                icon = {
                                    if (item.isPrimaryAction) {
                                        // Highlighted Center Action Item for "Add Product"
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) TerracottaPrimary else TerracottaContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.title,
                                                tint = if (isSelected) Color.White else TerracottaPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    } else {
                                        BadgedBox(
                                            badge = {
                                                if (item.tab == AppNavTab.INQUIRIES_ORDERS && newInquiriesCount > 0) {
                                                    Badge(
                                                        containerColor = Color(0xFFDC2626),
                                                        contentColor = Color.White
                                                    ) {
                                                        Text(
                                                            text = "$newInquiriesCount",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.title,
                                                tint = if (isSelected) TerracottaPrimary else ArtisanTextMuted,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.hindiTitle,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TerracottaPrimary else ArtisanTextMuted,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = if (item.isPrimaryAction) Color.Transparent else TerracottaContainer,
                                    selectedIconColor = TerracottaPrimary,
                                    selectedTextColor = TerracottaPrimary,
                                    unselectedIconColor = ArtisanTextMuted,
                                    unselectedTextColor = ArtisanTextMuted
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
        SharedTransitionLayout {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tabTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { tab ->
                CompositionLocalProvider(
                    com.example.ui.utils.LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    com.example.ui.utils.LocalAnimatedVisibilityScope provides this@AnimatedContent
                ) {
                    when (tab) {
                        AppNavTab.ARTISAN_HOME -> ArtisanHomeScreen(viewModel = viewModel)
                        AppNavTab.ARTISAN_CATALOG -> ArtisanCatalogScreen(viewModel = viewModel)
                        AppNavTab.ARTISAN_STUDIO -> ArtisanStudioScreen(viewModel = viewModel)
                        AppNavTab.VOICE_CATALOG -> MultilingualVoiceCatalogScreen(viewModel = viewModel)
                        AppNavTab.SMART_CATALOG_WIZARD -> SmartCatalogWizardScreen(viewModel = viewModel)
                        AppNavTab.PRODUCT_CAPTURE -> CameraImagenStudioScreen(viewModel = viewModel)
                        AppNavTab.PRODUCT_DETAIL -> ProductDetailScreen(viewModel = viewModel)
                        AppNavTab.BUYER_MARKETPLACE -> BuyerMarketplaceScreen(viewModel = viewModel)
                        AppNavTab.INQUIRIES_ORDERS -> InquiriesOrdersScreen(viewModel = viewModel)
                        AppNavTab.VYAPAR_MITRA_CHAT -> VyaparMitraChatScreen(viewModel = viewModel)
                        AppNavTab.ARTISAN_PROFILE -> ArtisanProfileScreen(viewModel = viewModel)
                        AppNavTab.DYNAMIC_PRICING_ASSISTANT -> DynamicPricingAssistantScreen(viewModel = viewModel)
                        AppNavTab.ARTISAN_SALES_ANALYTICS -> ArtisanSalesAnalyticsDashboardScreen(viewModel = viewModel)
                        AppNavTab.PRICING_ADVISOR -> PricingAdvisorScreen(hunarViewModel = viewModel)
                        AppNavTab.CRAFT_TAXONOMY -> HierarchicalCategorySelectionScreen(viewModel = viewModel)
                        AppNavTab.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel = viewModel)
                        AppNavTab.BUYER_SAVED -> BuyerMarketplaceScreen(viewModel = viewModel)
                        AppNavTab.BUYER_BULK_REQUIREMENTS -> BuyerMarketplaceScreen(viewModel = viewModel)
                        AppNavTab.LANGUAGE_PREFERENCES -> LanguagePreferencesScreen(
                            viewModel = viewModel,
                            onNavigateBack = { viewModel.navigateTo(AppNavTab.ARTISAN_HOME) }
                        )
                    }
                }
            }
        }
        }

        // Global Hierarchical Category Bottom Sheet
        if (isCategoryHierarchySheetVisible) {
            HierarchicalCategoryBottomSheet(
                initialMapping = hierarchicalCategoryMapping,
                onDismiss = { viewModel.dismissCategoryHierarchySheet() },
                onCategorySelected = { newMapping ->
                    viewModel.applyHierarchicalCategoryMapping(newMapping)
                }
            )
        }

        // Global High-Craft AI Processing Loading Overlay
        GlobalLoadingOverlay(
            state = globalLoadingState,
            onCancel = { viewModel.cancelGlobalLoading() }
        )
    }
}

@Composable
private fun RoleTabItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    selectedColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .semantics {
                role = Role.Tab
                selected = isSelected
                contentDescription = "$title: $subtitle"
                stateDescription = if (isSelected) "चयनित (Selected)" else "गैर-चयनित (Unselected)"
            }
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) selectedColor else Color.Transparent,
        shape = RoundedCornerShape(9.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else ArtisanTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.85f) else ArtisanTextMuted
            )
        }
    }
}
