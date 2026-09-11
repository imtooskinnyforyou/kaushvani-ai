package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PhotoStudioSelector(
    selectedImageUri: String,
    onImageSelected: (String) -> Unit,
    currentFilter: String,
    onFilterSelected: (String) -> Unit,
    category: String,
    isGiTagged: Boolean,
    onGiTagToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onLaunchCamera: () -> Unit = {},
    onOpenStudioEnhancer: () -> Unit = {}
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it.toString()) }
    }

    val sampleCraftPresets = listOf(
        Pair("sample_terracotta", "मिट्टी कलश (Terracotta)"),
        Pair("sample_banarasi", "बनारसी सिल्क (Banarasi)"),
        Pair("sample_dhokra", "ढोकरा पीतल (Dhokra)"),
        Pair("sample_madhubani", "मधुबनी आर्ट (Mithila)"),
        Pair("sample_woodcraft", "शीशम नक्काशी (Wood)")
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Photo & AI Studio",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "1. उत्पाद फोटो और AI स्टूडियो (Photo & Studio)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                }
            }

            // Big Preview Box with AI Enhancement Studio Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                CraftArtworkDisplay(
                    imageUri = selectedImageUri,
                    category = category,
                    styleFilter = currentFilter,
                    isGiTagged = isGiTagged,
                    modifier = Modifier.fillMaxSize(),
                    showEnhancementBadge = true
                )

                // Quick camera & gallery action buttons at bottom overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "गैलरी (Photo)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onLaunchCamera,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take Photo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "फोटो खींचें (Camera)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Direct AI Studio & Enhancer Launch Button
            Button(
                onClick = onOpenStudioEnhancer,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✨ AI फोटो स्टूडियो और एन्हांसर खोलें (Open Studio)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // AI Studio One-Tap Lighting & Texture Enhancement Filters
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI 1-टैप टेक्सचर व लाइटिंग फ़िल्टर:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ArtisanTextSecondary
                    )
                    Text(
                        text = "ब्राइटनेस व कंट्रास्ट ऑटो-ट्यून्ड",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TerracottaPrimary
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CraftImageFilter.values()) { filter ->
                        val isSelected = filter.id.equals(currentFilter, ignoreCase = true) ||
                                         filter.title.equals(currentFilter, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .semantics {
                                    role = Role.Tab
                                    selected = isSelected
                                    contentDescription = "${filter.hindiTitle} - ${filter.title} फ़िल्टर"
                                    stateDescription = if (isSelected) "लागू फ़िल्टर (Applied Filter)" else "उपलब्ध फ़िल्टर (Available Filter)"
                                }
                                .clickable { onFilterSelected(filter.title) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TerracottaContainer else ArtisanSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) TerracottaPrimary else ArtisanCardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isSelected) TerracottaPrimary else ArtisanTextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = filter.hindiTitle,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TerracottaPrimary else ArtisanTextPrimary
                                    )
                                }
                                Text(
                                    text = filter.title,
                                    fontSize = 9.sp,
                                    color = if (isSelected) TerracottaPrimary.copy(alpha = 0.8f) else ArtisanTextMuted
                                )
                            }
                        }
                    }
                }
            }

            // GI Tag & Preset craft selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Checkbox(
                        checked = isGiTagged,
                        onCheckedChange = onGiTagToggled,
                        colors = CheckboxDefaults.colors(checkedColor = ForestSuccess)
                    )
                    Column {
                        Text(
                            text = "GI Tag Certified Craft",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "भौगोलिक उपदर्शन प्रामाणिक शिल्प",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }
            }

            // Preset Craft Quick Buttons
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "शिल्प नमूना (Preset Craft Presets):",
                    fontSize = 11.sp,
                    color = ArtisanTextMuted
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(sampleCraftPresets) { (uri, label) ->
                        OutlinedButton(
                            onClick = { onImageSelected(uri) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(text = label, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
