package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.data.sync.SyncState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * OfflineSyncStatusIndicator provides a real-time, accessible, M3-compliant sync status bar
 * in the app header. It indicates when local drafts are pending synchronization and provides
 * a high-visibility manual 'Retry Sync' button for when connection is restored.
 */
@Composable
fun AppHeaderOfflineSyncBar(
    pendingDraftsCount: Int,
    draftProductsCount: Int,
    unsyncedEventsCount: Int,
    isSyncing: Boolean,
    isNetworkAvailable: Boolean,
    syncState: SyncState,
    lastSyncTimestamp: Long,
    onRetrySync: () -> Unit,
    modifier: Modifier = Modifier,
    pendingDraftProducts: List<ProductEntity> = emptyList(),
    onPublishDraft: ((ProductEntity) -> Unit)? = null,
    pendingCatalogSyncCount: Int = 0,
    pendingAiTasksCount: Int = 0
) {
    var showDraftsModal by remember { mutableStateOf(false) }

    // Infinite rotation for spinning sync icon
    val infiniteTransition = rememberInfiniteTransition(label = "SyncRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SyncIconRotation"
    )

    val isError = syncState is SyncState.Error
    val totalWorkManagerQueue = pendingCatalogSyncCount + pendingAiTasksCount
    val effectivePendingCount = pendingDraftsCount + totalWorkManagerQueue
    val hasPendingItems = effectivePendingCount > 0 || !isNetworkAvailable || isError

    // Dynamic background and accent colors based on sync and connection state
    val containerColor = when {
        isSyncing -> Color(0xFFEFF6FF) // Soft Indigo/Blue 50
        isError -> Color(0xFFFEF2F2) // Soft Rose 50
        !isNetworkAvailable -> Color(0xFFFFFBEB) // Amber 50
        effectivePendingCount > 0 -> Color(0xFFFFF7ED) // Warm Orange 50
        else -> Color(0xFFF0FDF4) // Forest Green 50
    }

    val borderColor = when {
        isSyncing -> Color(0xFF93C5FD) // Blue 300
        isError -> Color(0xFFFCA5A5) // Rose 300
        !isNetworkAvailable -> Color(0xFFFCD34D) // Amber 300
        effectivePendingCount > 0 -> Color(0xFFFDBA74) // Orange 300
        else -> Color(0xFF86EFAC) // Green 300
    }

    val iconColor = when {
        isSyncing -> IndigoSecondary
        isError -> Color(0xFFDC2626)
        !isNetworkAvailable -> Color(0xFFD97706)
        effectivePendingCount > 0 -> TerracottaPrimary
        else -> ForestSuccess
    }

    val statusTitle = when {
        isSyncing -> "सिंक जारी है (Syncing in Progress)"
        isError -> "सिंक में रुकावट (Sync Incomplete)"
        !isNetworkAvailable && effectivePendingCount > 0 -> "ऑफलाइन • $effectivePendingCount कार्य कतार में (Offline • $effectivePendingCount Queued)"
        !isNetworkAvailable -> "ऑफलाइन मोड सक्रिय (Offline Mode Active)"
        effectivePendingCount > 0 -> "$effectivePendingCount सिंक कार्य कतार में ($effectivePendingCount Queued for Sync)"
        else -> "सभी डेटा सिंक है (All Drafts Synced)"
    }

    val statusSubtitle = when {
        isSyncing -> "स्थानीय बदलाव क्लाउड व सर्वर से सिंक हो रहे हैं…"
        isError -> (syncState as? SyncState.Error)?.errorMessage ?: "नेटवर्क कनेक्शन जांचें और पुनः प्रयास करें"
        !isNetworkAvailable -> "स्थानीय Room में सुरक्षित • नेटवर्क आने पर WorkManager स्वतः सिंक करेगा"
        totalWorkManagerQueue > 0 -> "WorkManager कतार: $pendingCatalogSyncCount कैटलॉग • $pendingAiTasksCount AI टास्क"
        pendingDraftsCount > 0 -> "$draftProductsCount उत्पाद ड्राफ्ट • $unsyncedEventsCount ऑफलाइन इवेंट्स"
        else -> "अंतिम सफल सिंक: ${formatSyncTime(lastSyncTimestamp)}"
    }

    val statusIcon: ImageVector = when {
        isSyncing -> Icons.Default.Sync
        isError -> Icons.Default.SyncProblem
        !isNetworkAvailable -> Icons.Default.CloudOff
        pendingDraftsCount > 0 -> Icons.Default.CloudQueue
        else -> Icons.Default.CloudDone
    }

    val a11yDescription = "Offline Sync Status: $statusTitle. $statusSubtitle"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_sync_status_header")
            .semantics {
                contentDescription = a11yDescription
            },
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = if (hasPendingItems) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Status Icon and Bilingual Details
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = pendingDraftsCount > 0) {
                        showDraftsModal = true
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "Sync Indicator Icon",
                        tint = iconColor,
                        modifier = Modifier
                            .size(18.dp)
                            .then(
                                if (isSyncing) Modifier.rotate(rotationAngle) else Modifier
                            )
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = statusTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (pendingDraftsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(iconColor)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "$pendingDraftsCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = statusSubtitle,
                        fontSize = 11.sp,
                        color = ArtisanTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Manual 'Retry Sync' Button
            FilledTonalButton(
                onClick = onRetrySync,
                enabled = !isSyncing,
                modifier = Modifier
                    .height(36.dp)
                    .defaultMinSize(minWidth = 88.dp, minHeight = 48.dp)
                    .testTag("retry_sync_button")
                    .semantics {
                        role = Role.Button
                        contentDescription = if (isSyncing) "Syncing in progress" else "Retry synchronization with server and cloud"
                    },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isError) Color(0xFFDC2626) else if (hasPendingItems) TerracottaPrimary else IndigoSecondary,
                    contentColor = Color.White,
                    disabledContainerColor = ArtisanSurfaceVariant,
                    disabledContentColor = ArtisanTextSecondary
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = ArtisanTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "सिंक…",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isError) "Retry" else "Sync",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modal Sheet / Dialog to inspect and manage pending drafts
    if (showDraftsModal) {
        PendingDraftsDetailsModal(
            draftProducts = pendingDraftProducts,
            unsyncedEventsCount = unsyncedEventsCount,
            isNetworkAvailable = isNetworkAvailable,
            isSyncing = isSyncing,
            onDismiss = { showDraftsModal = false },
            onRetrySync = {
                onRetrySync()
                showDraftsModal = false
            },
            onPublishDraft = { product ->
                onPublishDraft?.invoke(product)
            }
        )
    }
}

/**
 * Modal bottom sheet/dialog to inspect pending drafts and unsynced events in detail.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingDraftsDetailsModal(
    draftProducts: List<ProductEntity>,
    unsyncedEventsCount: Int,
    isNetworkAvailable: Boolean,
    isSyncing: Boolean,
    onDismiss: () -> Unit,
    onRetrySync: () -> Unit,
    onPublishDraft: (ProductEntity) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("pending_drafts_modal"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TerracottaContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Pending Offline Drafts",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "स्थानीय ड्राफ्ट व सिंक कतार (Room DB)",
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close modal",
                        tint = ArtisanTextSecondary
                    )
                }
            }

            Divider(color = ArtisanDivider)

            // Connection banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isNetworkAvailable) Color(0xFFF0FDF4) else Color(0xFFFFFBEB))
                    .border(
                        1.dp,
                        if (isNetworkAvailable) Color(0xFF86EFAC) else Color(0xFFFCD34D),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isNetworkAvailable) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = if (isNetworkAvailable) ForestSuccess else Color(0xFFD97706),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isNetworkAvailable) "इन्टरनेट कनेक्टेड है (Internet Online) • सिंक तैयार" else "डिवाइस ऑफ़लाइन है (Device Offline) • ड्राफ्ट सुरक्षित हैं",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isNetworkAvailable) ForestSuccess else Color(0xFFB45309)
                )
            }

            // Summary Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("उत्पाद ड्राफ्ट", fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(
                            "${draftProducts.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                        Text("Draft Products", fontSize = 10.sp, color = ArtisanTextSecondary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ArtisanSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("इवेंट कतार", fontSize = 11.sp, color = ArtisanTextSecondary)
                        Text(
                            "$unsyncedEventsCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoSecondary
                        )
                        Text("Unsynced Logs", fontSize = 10.sp, color = ArtisanTextSecondary)
                    }
                }
            }

            // Product Drafts List
            if (draftProducts.isNotEmpty()) {
                Text(
                    text = "स्थानीय ड्राफ्ट उत्पाद (${draftProducts.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    draftProducts.take(4).forEach { draft ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ArtisanSurfaceVariant)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = draft.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ArtisanTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${draft.category} • ₹${draft.retailPrice} • Draft",
                                    fontSize = 11.sp,
                                    color = ArtisanTextSecondary
                                )
                            }

                            Button(
                                onClick = { onPublishDraft(draft) },
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("publish_draft_button_${draft.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text("Publish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "कोई अप्रकाशित ड्राफ्ट नहीं है (No pending product drafts)",
                        fontSize = 13.sp,
                        color = ArtisanTextSecondary
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("बंद करें (Close)")
                }

                Button(
                    onClick = onRetrySync,
                    enabled = !isSyncing,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("modal_retry_sync_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("सिंक हो रहा है…")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Sync Now")
                    }
                }
            }
        }
    }
}

private fun formatSyncTime(timestamp: Long): String {
    if (timestamp <= 0L) return "हाल ही में (Recently)"
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000L -> "अभी-अभी (Just now)"
        diff < 3600000L -> "${diff / 60000L} मिनट पहले (${diff / 60000L}m ago)"
        else -> {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
