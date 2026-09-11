package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserActivityEventEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OfflineActivityTrackingCard(
    events: List<UserActivityEventEntity>,
    unsyncedCount: Int,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    onLogSampleEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var isExpanded by remember { mutableStateOf(false) }

    val filteredEvents = remember(events, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            events
        } else {
            events.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_activity_tracking_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title, Subtitle, Sync Status & Button
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
                            .background(if (unsyncedCount > 0) TerracottaContainer else ForestContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (unsyncedCount > 0) Icons.Default.CloudQueue else Icons.Default.CloudDone,
                            contentDescription = "Event Sync Status",
                            tint = if (unsyncedCount > 0) TerracottaPrimary else ForestSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Offline Activity Tracker",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "ऑफ़लाइन गतिविधि व प्रदर्शन ट्रैकिंग (Room DB)",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                // Sync Action Button
                FilledTonalButton(
                    onClick = onSyncClick,
                    enabled = !isSyncing,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("btn_sync_offline_events"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (unsyncedCount > 0) TerracottaPrimary else IndigoSecondary,
                        contentColor = Color.White
                    )
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Syncing...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (unsyncedCount > 0) "सिंक करें ($unsyncedCount)" else "सिंक चेक",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sync Status Pill Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = if (unsyncedCount > 0) TerracottaContainer.copy(alpha = 0.5f) else ForestContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (unsyncedCount > 0) TerracottaPrimary else ForestSuccess)
                        )
                        Text(
                            text = if (unsyncedCount > 0)
                                "$unsyncedCount events saved offline in Room (पेंडिंग सिंक)"
                            else
                                "All ${events.size} business events synchronized (क्लाउड सिंक पूर्ण)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (unsyncedCount > 0) TerracottaPrimary else ForestSuccess
                        )
                    }

                    Text(
                        text = "Total: ${events.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                }
            }

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChipItem(
                    label = "All (${events.size})",
                    isSelected = selectedCategoryFilter == "ALL",
                    onClick = { selectedCategoryFilter = "ALL" }
                )
                FilterChipItem(
                    label = "Catalog",
                    isSelected = selectedCategoryFilter == "CATALOG",
                    onClick = { selectedCategoryFilter = "CATALOG" }
                )
                FilterChipItem(
                    label = "Inquiry",
                    isSelected = selectedCategoryFilter == "INQUIRY",
                    onClick = { selectedCategoryFilter = "INQUIRY" }
                )
                FilterChipItem(
                    label = "Pricing",
                    isSelected = selectedCategoryFilter == "PRICING",
                    onClick = { selectedCategoryFilter = "PRICING" }
                )
            }

            // List of Activity Events
            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "कोई गतिविधि दर्ज नहीं है (No events found)",
                        fontSize = 12.sp,
                        color = ArtisanTextMuted
                    )
                }
            } else {
                val displayList = if (isExpanded) filteredEvents else filteredEvents.take(3)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayList.forEach { event ->
                        ActivityEventRowItem(event = event)
                    }
                }

                // Expand / Collapse & Log Event Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (filteredEvents.size > 3) {
                        TextButton(
                            onClick = { isExpanded = !isExpanded },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (isExpanded) "कम देखें (Show Less)" else "सभी ${filteredEvents.size} इवेंट देखें (View All)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoSecondary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    TextButton(
                        onClick = onLogSampleEvent,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TerracottaPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "इवेंट दर्ज करें (+ Event)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) IndigoSecondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else ArtisanTextSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ActivityEventRowItem(event: UserActivityEventEntity) {
    val dateFormatter = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }
    val formattedTime = remember(event.timestamp) { dateFormatter.format(Date(event.timestamp)) }

    val icon: ImageVector = when (event.eventType) {
        "PRODUCT_CREATED" -> Icons.Default.AddPhotoAlternate
        "PRODUCT_UPDATED" -> Icons.Default.EditNote
        "PRODUCT_DELETED" -> Icons.Default.DeleteOutline
        "INQUIRY_SENT" -> Icons.Default.MarkChatUnread
        "INQUIRY_RESPONDED" -> Icons.Default.QuestionAnswer
        "PRICING_CALCULATED" -> Icons.Default.CurrencyRupee
        "CATALOG_GENERATED" -> Icons.Default.AutoAwesome
        "VOICE_NOTE_RECORDED" -> Icons.Default.Mic
        "OFFLINE_SYNC" -> Icons.Default.Sync
        else -> Icons.Default.History
    }

    val iconContainerColor = when (event.category) {
        "CATALOG" -> TerracottaContainer
        "INQUIRY" -> IndigoSecondaryContainer
        "PRICING" -> ForestContainer
        "VOICE" -> MarigoldContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconColor = when (event.category) {
        "CATALOG" -> TerracottaPrimary
        "INQUIRY" -> IndigoSecondary
        "PRICING" -> ForestSuccess
        "VOICE" -> Color(0xFFD97706)
        else -> ArtisanTextPrimary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    // Synced / Offline pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (event.isSynced) ForestContainer else TerracottaContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (event.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (event.isSynced) ForestSuccess else TerracottaPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = if (event.isSynced) "Synced" else "Offline",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (event.isSynced) ForestSuccess else TerracottaPrimary
                            )
                        }
                    }
                }

                if (event.description.isNotBlank()) {
                    Text(
                        text = event.description,
                        fontSize = 11.sp,
                        color = ArtisanTextSecondary,
                        maxLines = 2
                    )
                }

                Text(
                    text = "$formattedTime • ${event.category}",
                    fontSize = 10.sp,
                    color = ArtisanTextMuted
                )
            }
        }
    }
}
