package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SelectedCategoryMapping
import com.example.data.repository.CraftTaxonomyRepository
import com.example.ui.components.HierarchicalCategoryBottomSheet
import com.example.ui.components.HierarchicalCategoryPickerContent
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel

/**
 * Hierarchical Category Selection & Industry Standards Mapping Screen.
 * Provides artisans with an interactive chip-based interface to categorize their craft,
 * discover accurate HSN export codes, and link to Geographical Indication (GI) registries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HierarchicalCategorySelectionScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val currentMapping by viewModel.hierarchicalCategoryMapping.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "मानक शिल्प वर्गीकरण",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "Industry-Standard Craft Taxonomy & HSN",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_HOME) },
                        modifier = Modifier.testTag("btn_back_from_category_screen")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = ArtisanTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.testTag("btn_open_category_bottom_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Taxonomy",
                            tint = TerracottaPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ArtisanSurface)
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("hierarchical_category_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Informational Government & Industry Standards Banner
            Surface(
                color = IndigoSecondaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IndigoSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "उद्योग एवं निर्यात मानक (EPCH & ONDC Ready)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoSecondary
                        )
                        Text(
                            text = "सही HSN कोड व जीआई टैग मैपिंग से B2B खरीदार और सरकारी पोर्टल पर विश्वास बढ़ता है।",
                            fontSize = 11.sp,
                            color = ArtisanTextPrimary
                        )
                    }
                }
            }

            // Embedded Hierarchical Picker with full 3-tier chips
            HierarchicalCategoryPickerContent(
                initialMapping = currentMapping,
                onApply = { newMapping ->
                    viewModel.applyHierarchicalCategoryMapping(newMapping)
                },
                onClose = {
                    viewModel.navigateTo(AppNavTab.ARTISAN_HOME)
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom Sheet modal if triggered from top bar
        if (showBottomSheet) {
            HierarchicalCategoryBottomSheet(
                initialMapping = currentMapping,
                onDismiss = { showBottomSheet = false },
                onCategorySelected = { newMapping ->
                    viewModel.applyHierarchicalCategoryMapping(newMapping)
                    showBottomSheet = false
                }
            )
        }
    }
}
