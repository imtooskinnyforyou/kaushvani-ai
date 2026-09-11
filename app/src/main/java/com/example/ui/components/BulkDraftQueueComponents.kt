package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.data.model.ProductEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * Bulk Draft Queue Section rendered in ArtisanCatalogScreen.
 * Displays unprocessed drafts queued for later cataloging and triggers rapid camera capture.
 */
@Composable
fun BulkDraftQueueSection(
    drafts: List<ProductEntity>,
    onSnapBulkPhotosClick: () -> Unit,
    onProcessDraftWithAi: (ProductEntity) -> Unit,
    onDeleteDraft: (Long) -> Unit,
    onBatchProcessAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bulk_draft_queue_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (drafts.isNotEmpty()) TerracottaContainer.copy(alpha = 0.55f) else ArtisanSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (drafts.isNotEmpty()) TerracottaPrimary.copy(alpha = 0.4f) else ArtisanCardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (drafts.isNotEmpty()) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with title, badge and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (drafts.isNotEmpty()) TerracottaPrimary else ArtisanSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = if (drafts.isNotEmpty()) Color.White else IndigoSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "थोक ड्राफ्ट कतार",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            if (drafts.isNotEmpty()) {
                                Surface(
                                    color = TerracottaPrimary,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${drafts.size} ड्राफ्ट",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Bulk Draft Queue • बाद में AI से कैटलॉग बनाएं",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                // Snap Bulk Photos Button
                Button(
                    onClick = onSnapBulkPhotosClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 44.dp)
                        .testTag("btn_bulk_snap_photos")
                        .semantics {
                            role = Role.Button
                            contentDescription = "थोक फोटो खींचे, नए ड्राफ्ट कतार में जोड़ें (Bulk Snap Photos)"
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "फ़ोटो खींचें",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Drafts Queue List or Empty State
            if (drafts.isNotEmpty()) {
                // Batch Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${drafts.size} अनप्रोसेस्ड ड्राफ्ट कतार में सुरक्षित हैं",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ArtisanTextSecondary
                    )

                    FilledTonalButton(
                        onClick = onBatchProcessAll,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = IndigoSecondary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .testTag("btn_batch_process_drafts")
                            .semantics {
                                role = Role.Button
                                contentDescription = "सभी ड्राफ्ट एक साथ AI से तैयार करें (Batch Process All Drafts)"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "सबको AI से भरें",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Horizontal Carousel of Draft Cards
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bulk_drafts_carousel"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(drafts, key = { it.id }) { draft ->
                        BulkDraftCard(
                            draft = draft,
                            onProcessWithAi = { onProcessDraftWithAi(draft) },
                            onDelete = { onDeleteDraft(draft.id) }
                        )
                    }
                }
            } else {
                // Empty state helpful prompt
                Surface(
                    color = ArtisanSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = null,
                            tint = MarigoldTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "त्वरित सीक्वेंस फोटो कैप्चर",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisanTextPrimary
                            )
                            Text(
                                text = "एक साथ 5-10 शिल्प की तस्वीरें खींचें और बाद में आराम से विवरण व मूल्य भरें।",
                                fontSize = 11.sp,
                                color = ArtisanTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Draft Item Card in Bulk Draft Queue
 */
@Composable
fun BulkDraftCard(
    draft: ProductEntity,
    onProcessWithAi: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .testTag("bulk_draft_card_${draft.id}"),
        colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(ArtisanSurfaceVariant)
            ) {
                CraftArtworkDisplay(
                    imageUri = draft.imageUri,
                    category = draft.category,
                    styleFilter = draft.imageStyleFilter,
                    isGiTagged = draft.isGiTagged,
                    showEnhancementBadge = false,
                    modifier = Modifier.fillMaxSize()
                )

                // Category & Unprocessed badge overlay
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = draft.category,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Delete quick button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .padding(2.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .semantics {
                            role = Role.Button
                            contentDescription = "ड्राफ्ट हटाएं (Delete Draft ${draft.title})"
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Pending status badge
                Surface(
                    color = TerracottaPrimary.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(topStart = 8.dp),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "ड्राफ्ट",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Draft Details & Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    Text(
                        text = draft.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "श्रेणी: ${draft.category}",
                        fontSize = 10.sp,
                        color = ArtisanTextSecondary
                    )
                }

                Button(
                    onClick = onProcessWithAi,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("btn_process_draft_${draft.id}")
                        .semantics {
                            role = Role.Button
                            contentDescription = "AI कैटलॉग विज़ार्ड से विवरण तैयार करें (Catalog with AI)"
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI कैटलॉग बनाएं",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Rapid Multi-Photo Snapper Dialog.
 * Allows artisans to quickly snap 5-20 photos in sequence with audio/visual feedback,
 * preview filmstrip, gallery multi-selection, category tagging, and batch draft persistence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkPhotoCaptureModal(
    initialCategory: String = "Pottery",
    onDismiss: () -> Unit,
    onSaveBulkDrafts: (photoUris: List<String>, category: String, craftType: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedCraftType by remember { mutableStateOf("") }
    val snappedPhotos = remember { mutableStateListOf<String>() }

    var isCapturing by remember { mutableStateOf(false) }
    var flashTrigger by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // Multi-photo gallery picker
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 15)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                snappedPhotos.add(uri.toString())
            }
            Toast.makeText(context, "📸 ${uris.size} तस्वीरें गैलरी से जोड़ी गईं!", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "कैमरा अनुमति आवश्यक है। आप गैलरी से भी फोटो चुन सकते हैं।", Toast.LENGTH_LONG).show()
        }
    }

    val categories = listOf("Pottery", "Handloom", "Woodcraft", "Metalcraft", "Jewelry", "Paintings", "Leather")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("bulk_photo_capture_modal"),
            color = DeepNavy
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "त्वरित थोक फोटो कैप्चर",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rapid Sequence Snapper",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }

                    // Live Counter Badge
                    Surface(
                        color = if (snappedPhotos.isNotEmpty()) TerracottaPrimary else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${snappedPhotos.size} फोटो",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Category Selection Strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            color = if (isSelected) TerracottaPrimary else Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color.White else Color.Transparent
                            )
                        ) {
                            Text(
                                text = cat,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Viewfinder Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(2.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                ) {
                    if (hasCameraPermission) {
                        // CameraX Preview View
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val capture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()
                                    imageCapture = capture

                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            capture
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback UI if camera permission not granted
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "कैमरा सक्रिय करें",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "लगातार तस्वीरें खींचने के लिए कैमरा अनुमति दें या नीचे से गैलरी तस्वीरें जोड़ें।",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("कैमरा अनुमति दें (Grant Permission)", fontSize = 12.sp)
                            }
                        }
                    }

                    // Framing overlay guides
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    )

                    // Flash shutter feedback animation
                    if (flashTrigger) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.85f))
                        )
                    }

                    // Shutter action overlay bar
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, start = 20.dp, end = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pick from gallery button
                        IconButton(
                            onClick = {
                                galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                .testTag("btn_gallery_multi_picker")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = "Pick Multiple Photos from Gallery",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Main Shutter Button
                        val shutterScale by animateFloatAsState(
                            targetValue = if (isCapturing) 0.85f else 1.0f,
                            label = "shutterScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .scale(shutterScale)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(TerracottaPrimary)
                                .clickable {
                                    if (isCapturing) return@clickable
                                    isCapturing = true
                                    flashTrigger = true

                                    coroutineScope.launch {
                                        delay(120)
                                        flashTrigger = false
                                    }

                                    val capture = imageCapture
                                    if (capture != null && hasCameraPermission) {
                                        val photoFile = File(
                                            context.cacheDir,
                                            "bulk_${System.currentTimeMillis()}_${snappedPhotos.size + 1}.jpg"
                                        )
                                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                        capture.takePicture(
                                            outputOptions,
                                            ContextCompat.getMainExecutor(context),
                                            object : ImageCapture.OnImageSavedCallback {
                                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                    val savedUri = Uri.fromFile(photoFile).toString()
                                                    snappedPhotos.add(savedUri)
                                                    isCapturing = false
                                                }

                                                override fun onError(exception: ImageCaptureException) {
                                                    // Fallback to procedural sample craft
                                                    val sample = "sample_craft_${(snappedPhotos.size % 4) + 1}"
                                                    snappedPhotos.add(sample)
                                                    isCapturing = false
                                                }
                                            }
                                        )
                                    } else {
                                        // Demo/Fallback snapping for emulator or without camera
                                        val sampleUris = listOf("sample_terracotta", "sample_handloom", "sample_brass", "sample_wood")
                                        val picked = sampleUris[snappedPhotos.size % sampleUris.size]
                                        snappedPhotos.add(picked)
                                        isCapturing = false
                                    }
                                }
                                .testTag("bulk_shutter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Snap Photo",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Instant Demo Craft Sample Button
                        IconButton(
                            onClick = {
                                val demoSamples = listOf("sample_terracotta", "sample_handloom", "sample_brass", "sample_wood")
                                val item = demoSamples[snappedPhotos.size % demoSamples.size]
                                snappedPhotos.add(item)
                                Toast.makeText(context, "✨ डेमो शिल्प फोटो जोड़ी गई!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                .testTag("btn_add_sample_snap")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Add Sample Photo",
                                tint = MarigoldTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Sequence Filmstrip (horizontal thumbnail list of snapped photos)
                if (snappedPhotos.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "कैप्चर की गई तस्वीरें (${snappedPhotos.size}):",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(snappedPhotos) { index, photoUri ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.DarkGray)
                                        .border(1.5.dp, TerracottaPrimary, RoundedCornerShape(10.dp))
                                ) {
                                    CraftArtworkDisplay(
                                        imageUri = photoUri,
                                        category = selectedCategory,
                                        showEnhancementBadge = false,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Photo Index Badge
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(bottomEnd = 6.dp),
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }

                                    // Remove thumbnail button
                                    IconButton(
                                        onClick = { snappedPhotos.removeAt(index) },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Bar to Save All Drafts
                Surface(
                    color = DeepNavySurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(0.4f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("रद्द करें", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (snappedPhotos.isEmpty()) {
                                    Toast.makeText(context, "कृपया कम से कम 1 फोटो खींचें!", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSaveBulkDrafts(
                                        snappedPhotos.toList(),
                                        selectedCategory,
                                        selectedCraftType
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = snappedPhotos.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TerracottaPrimary,
                                disabledContainerColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.6f)
                                .height(48.dp)
                                .testTag("btn_save_bulk_drafts")
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "सभी ${snappedPhotos.size} तस्वीरें ड्राफ्ट में सहेजें (Save All ${snappedPhotos.size} Drafts)"
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "सहेजें (${snappedPhotos.size} ड्राफ्ट)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
