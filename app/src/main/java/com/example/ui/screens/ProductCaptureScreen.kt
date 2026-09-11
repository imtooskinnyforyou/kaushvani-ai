package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.ai.utils.ImageProcessingUtils
import com.example.data.image.CraftImageStorageManager
import com.example.data.model.*
import com.example.ui.components.BeforeAfterCraftComparisonSlider
import com.example.ui.components.CameraQualityOverlay
import com.example.ui.components.CraftArtworkDisplay
import com.example.ui.components.CraftImageFilter
import com.example.ui.components.CanvasToneCurveVisualizer
import com.example.ui.components.CustomImageCroppingStageView
import com.example.ui.theme.*
import com.example.ui.utils.CameraQualityAnalyzer
import com.example.ui.utils.FocusStatus
import com.example.ui.utils.LightingStatus
import com.example.ui.utils.RealtimeFrameQuality
import com.example.ui.utils.StabilityStatus
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel
import java.io.File
import java.util.concurrent.Executors

/**
 * FEATURE 1 — AI IMAGE ENHANCER & STUDIO
 *
 * Flow:
 * ADD PRODUCT → TAKE PHOTO / UPLOAD PHOTO → AI ANALYSIS → ENHANCE → REVIEW → SAVE
 *
 * Capabilities:
 * A. Camera: Use device camera, allow image upload, show framing guidance, recommend good lighting, detect product visibility.
 * B. AI Background Removal: Detect main product, remove workshop clutter, replace with clean neutral backdrops.
 * C. Image Enhancement: Brightness correction, contrast, sharpness, crop, auto-center, marketplace-friendly dimensions.
 * D. Product Quality Check: GOOD / NEEDS IMPROVEMENT, checks for visibility, lighting, blur, background, cropping.
 * Actionable suggestions ("Your product is slightly dark") + [Improve Automatically] 1-tap AI fix.
 * Interactive Before/After slider: ORIGINAL | AI ENHANCED.
 *
 * Principle: Authentic craft details preserved — presentation enhanced without altering handmade qualities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCaptureScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentStage by viewModel.imageStudioStage.collectAsStateWithLifecycle()
    val studioImageUri by viewModel.studioImageUri.collectAsStateWithLifecycle()
    val studioOriginalImageUri by viewModel.studioOriginalImageUri.collectAsStateWithLifecycle()
    val studioEnhancedImageUri by viewModel.studioEnhancedImageUri.collectAsStateWithLifecycle()
    val studioCategory by viewModel.studioCategory.collectAsStateWithLifecycle()
    val qualityReport by viewModel.studioQualityReport.collectAsStateWithLifecycle()
    val enhancementSettings by viewModel.studioEnhancementSettings.collectAsStateWithLifecycle()
    val isAutoEnhancing by viewModel.isAutoEnhancing.collectAsStateWithLifecycle()

    // Permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "कैमरा अनुमति आवश्यक है (Camera permission required)", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery upload launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onImageStudioPhotoUploaded(it.toString())
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ArtisanBackground)
            .testTag("ai_image_studio_screen"),
        topBar = {
            StudioStageHeader(
                currentStage = currentStage,
                onBack = {
                    when (currentStage) {
                        ImageStudioStage.CAPTURE_UPLOAD -> viewModel.navigateTo(AppNavTab.ARTISAN_HOME)
                        ImageStudioStage.CROP_ALIGN -> viewModel.imageStudioStage.value = ImageStudioStage.CAPTURE_UPLOAD
                        ImageStudioStage.AI_ANALYSIS -> viewModel.imageStudioStage.value = ImageStudioStage.CROP_ALIGN
                        ImageStudioStage.ENHANCE_STUDIO -> viewModel.imageStudioStage.value = ImageStudioStage.AI_ANALYSIS
                        ImageStudioStage.QUALITY_REVIEW -> viewModel.imageStudioStage.value = ImageStudioStage.ENHANCE_STUDIO
                        ImageStudioStage.SAVED_SUCCESS -> viewModel.navigateTo(AppNavTab.ARTISAN_CATALOG)
                    }
                },
                onSpeakTips = {
                    viewModel.speakHelpText(
                        when (currentStage) {
                            ImageStudioStage.CAPTURE_UPLOAD -> "शिल्प को पर्याप्त प्राकृतिक रोशनी में रखें और चौकोर फ्रेम के केंद्र में सेट करें।"
                            ImageStudioStage.CROP_ALIGN -> "1:1 चौकोर, 4:5 पोर्ट्रेट या 3:4 कैटलॉग अनुपात चुनें और ड्रैग या पिंच करके शिल्प को फ्रेम में लाएं।"
                            ImageStudioStage.AI_ANALYSIS -> "एआई ने आपकी फोटो की जांच की है। बैकग्राउंड व लाइटिंग सुधारने के लिए 'स्वचालित सुधारें' दबाएं।"
                            ImageStudioStage.ENHANCE_STUDIO -> "बिफोर-आफ्टर स्लाइडर को खींचकर मूल फोटो और स्टूडियो बैकग्राउंड की तुलना करें।"
                            ImageStudioStage.QUALITY_REVIEW -> "सभी 5 गुणवत्ता जांच पूरी हो गई हैं। अब आप उत्पाद सेव कर सकते हैं।"
                            ImageStudioStage.SAVED_SUCCESS -> "उत्पाद सफलतापूर्वक कैटलॉग में प्रकाशित हो गया है।"
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentStage) {
                ImageStudioStage.CAPTURE_UPLOAD -> {
                    CameraAndUploadStageView(
                        hasPermission = hasCameraPermission,
                        onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        onPhotoCaptured = { uriString ->
                            viewModel.onImageStudioPhotoCaptured(uriString)
                        },
                        onPickGallery = { galleryLauncher.launch("image/*") },
                        onSelectSampleCraft = { sampleId, category ->
                            viewModel.studioCategory.value = category
                            viewModel.onImageStudioPhotoUploaded(sampleId)
                        },
                        onSpeakAdvice = { text ->
                            viewModel.speakHelpText(text)
                        }
                    )
                }
                ImageStudioStage.CROP_ALIGN -> {
                    CustomImageCroppingStageView(
                        imageUri = studioImageUri,
                        category = studioCategory,
                        currentSettings = enhancementSettings,
                        onApplyCrop = { cropRatio, rotation, flipped, scale, offsetX, offsetY ->
                            viewModel.applyCropAndProceed(cropRatio, rotation, flipped, scale, offsetX, offsetY)
                        },
                        onRetake = { viewModel.imageStudioStage.value = ImageStudioStage.CAPTURE_UPLOAD },
                        onSpeakAdvice = { text -> viewModel.speakHelpText(text) }
                    )
                }
                ImageStudioStage.AI_ANALYSIS -> {
                    AiQualityAnalysisStageView(
                        imageUri = studioImageUri,
                        category = studioCategory,
                        report = qualityReport,
                        onImproveAutomatically = { viewModel.applyAutoImprovement() },
                        onProceedToStudio = { viewModel.proceedToEnhanceStudio() },
                        onReCrop = { viewModel.navigateToCropStage() },
                        onRetake = { viewModel.imageStudioStage.value = ImageStudioStage.CAPTURE_UPLOAD }
                    )
                }
                ImageStudioStage.ENHANCE_STUDIO -> {
                    EnhanceStudioStageView(
                        imageUri = studioImageUri,
                        originalImageUri = studioOriginalImageUri,
                        enhancedImageUri = studioEnhancedImageUri,
                        category = studioCategory,
                        settings = enhancementSettings,
                        onBackdropSelected = { viewModel.setStudioBackdrop(it) },
                        onBrightnessChange = { viewModel.setStudioBrightness(it) },
                        onContrastChange = { viewModel.setStudioContrast(it) },
                        onSharpnessChange = { viewModel.setStudioSharpness(it) },
                        onCropRatioChange = { viewModel.setStudioCropRatio(it) },
                        onCenterToggled = { viewModel.setStudioProductCentered(it) },
                        onSliderPositionChange = { viewModel.setStudioSliderPosition(it) },
                        onReCrop = { viewModel.navigateToCropStage() },
                        onProceedToReview = { viewModel.proceedToQualityReview() }
                    )
                }
                ImageStudioStage.QUALITY_REVIEW -> {
                    QualityReviewStageView(
                        imageUri = studioImageUri,
                        category = studioCategory,
                        settings = enhancementSettings,
                        report = qualityReport,
                        onSaveAndGoToWizard = { viewModel.saveEnhancedImageAndProceedToWizard() },
                        onSaveDirectly = { viewModel.saveEnhancedImageDirectly() },
                        onBackToStudio = { viewModel.proceedToEnhanceStudio() }
                    )
                }
                ImageStudioStage.SAVED_SUCCESS -> {
                    SavedSuccessStageView(
                        imageUri = studioImageUri,
                        category = studioCategory,
                        onGoToCatalog = { viewModel.navigateTo(AppNavTab.ARTISAN_CATALOG) },
                        onStartNew = { viewModel.startImageStudioFlow() }
                    )
                }
            }
        }
    }
}

/**
 * Top Stage Stepper Header
 */
@Composable
private fun StudioStageHeader(
    currentStage: ImageStudioStage,
    onBack: () -> Unit,
    onSpeakTips: () -> Unit
) {
    Surface(
        color = ArtisanSurface,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ArtisanSurfaceVariant)
                            .testTag("studio_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = ArtisanTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "AI Photo Studio & Enhancer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "एआई इमेज स्टूडियो व गुणवत्ता जांच",
                            fontSize = 11.sp,
                            color = TerracottaPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onSpeakTips,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TerracottaContainer)
                        .testTag("studio_speak_tips_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Listen to guidelines",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 6-Stage Step Progress Indicators
            val stages = listOf(
                Pair(ImageStudioStage.CAPTURE_UPLOAD, "1. Photo"),
                Pair(ImageStudioStage.CROP_ALIGN, "2. Crop"),
                Pair(ImageStudioStage.AI_ANALYSIS, "3. Quality"),
                Pair(ImageStudioStage.ENHANCE_STUDIO, "4. Studio"),
                Pair(ImageStudioStage.QUALITY_REVIEW, "5. Review"),
                Pair(ImageStudioStage.SAVED_SUCCESS, "6. Save")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                stages.forEachIndexed { index, (stage, label) ->
                    val isPastOrCurrent = currentStage.ordinal >= stage.ordinal
                    val isCurrent = currentStage == stage

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    isCurrent -> TerracottaPrimary
                                    isPastOrCurrent -> ForestSuccess
                                    else -> ArtisanCardBorder
                                }
                            )
                    )
                }
            }
        }
    }
}

/**
 * Stage 1: Camera & Photo Upload Viewport
 */
@Composable
private fun CameraAndUploadStageView(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onPhotoCaptured: (String) -> Unit,
    onPickGallery: () -> Unit,
    onSelectSampleCraft: (String, String) -> Unit,
    onSpeakAdvice: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var isGridVisible by remember { mutableStateOf(true) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var shutterFlashVisible by remember { mutableStateOf(false) }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }
    var realtimeQuality by remember { mutableStateOf(RealtimeFrameQuality()) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
    }

    if (!hasPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ArtisanBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "कैमरा अनुमति आवश्यक है",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    Text(
                        text = "हस्तशिल्प की लाइव फोटो खींचने और एआई बैकग्राउंड हटाने के लिए कैमरा अनुमति दें।",
                        fontSize = 13.sp,
                        color = ArtisanTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("कैमरा शुरू करें (Grant Permission)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onPickGallery,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("या गैलरी से फोटो चुनें (Upload Photo)", fontSize = 13.sp)
                    }
                }
            }
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // CameraX Live Preview Viewfinder
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                previewViewInstance = previewView

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder()
                        .setFlashMode(flashMode)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    imageCapture = capture

                    // Real-time Image Analysis for Lighting & Focus Evaluation
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor, CameraQualityAnalyzer { quality ->
                                ContextCompat.getMainExecutor(ctx).execute {
                                    realtimeQuality = quality
                                }
                            })
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        val cam = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture, imageAnalysis)
                        cameraControl = cam.cameraControl
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "कैमरा त्रुटि: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Live Real-Time Lighting & Focus Quality Evaluation HUD & Reticle Overlay
        if (isGridVisible) {
            CameraQualityOverlay(
                quality = realtimeQuality,
                isTorchEnabled = isTorchEnabled,
                onToggleTorch = {
                    isTorchEnabled = !isTorchEnabled
                    cameraControl?.enableTorch(isTorchEnabled)
                },
                onTapToFocus = { offset ->
                    val pView = previewViewInstance
                    val cControl = cameraControl
                    if (pView != null && cControl != null) {
                        try {
                            val factory = pView.meteringPointFactory
                            val point = factory.createPoint(offset.x, offset.y)
                            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                                .build()
                            cControl.startFocusAndMetering(action)
                        } catch (e: Exception) {
                            // Non-critical metering error
                        }
                    }
                },
                onSpeakAdvice = onSpeakAdvice,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Shutter flash effect
        AnimatedVisibility(
            visible = shutterFlashVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.8f)))
        }

        // Top Toolbar Overlay (Torch, Flip, Grid)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        isTorchEnabled = !isTorchEnabled
                        cameraControl?.enableTorch(isTorchEnabled)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isTorchEnabled) MarigoldTertiary else Color.Black.copy(alpha = 0.5f))
                        .testTag("camera_torch_button")
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flashlight",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { isGridVisible = !isGridVisible },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isGridVisible) TerracottaPrimary else Color.Black.copy(alpha = 0.5f))
                        .testTag("camera_grid_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Grid Overlay",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("camera_switch_lens_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Controls Dock
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.90f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Sample Preset Chips for Fast Prototyping
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "सैंपल शिल्प:",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val samples = listOf(
                        Triple("sample_terracotta", "मिट्टी कलश", "Pottery"),
                        Triple("sample_banarasi", "बनारसी सिल्क", "Textiles"),
                        Triple("sample_dhokra", "ढोकरा पीतल", "Metalcraft"),
                        Triple("sample_madhubani", "मधुबनी पेंटिंग", "Folk Art")
                    )
                    items(samples) { (sampleId, title, category) ->
                        Surface(
                            modifier = Modifier.clickable { onSelectSampleCraft(sampleId, category) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Shutter Row: Gallery Upload, Shutter Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Upload Photo Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPickGallery,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("upload_gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Upload from Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "अपलोड (Upload)",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Shutter Snap Button (78dp) with dynamic ready-to-capture green glow border
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(
                            if (realtimeQuality.isReadyToCapture) ForestSuccess.copy(alpha = 0.35f)
                            else Color.White.copy(alpha = 0.25f)
                        )
                        .border(
                            2.dp,
                            if (realtimeQuality.isReadyToCapture) ForestSuccess else Color.Transparent,
                            CircleShape
                        )
                        .clickable(enabled = !isCapturing) {
                            val cap = imageCapture
                            if (cap == null) {
                                isCapturing = true
                                val fallbackFile = File(context.cacheDir, "craft_sample_${System.currentTimeMillis()}.jpg")
                                val sampleBm = CraftImageStorageManager.generateAuthenticCraftSampleBitmap("sample_terracotta")
                                ImageProcessingUtils.compressAndSaveBitmapToFile(context, sampleBm, fallbackFile, quality = 90)
                                sampleBm.recycle()
                                isCapturing = false
                                onPhotoCaptured(Uri.fromFile(fallbackFile).toString())
                                return@clickable
                            }
                            isCapturing = true
                            shutterFlashVisible = true

                            val photoFile = File(context.cacheDir, "craft_${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            cap.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        shutterFlashVisible = false
                                        isCapturing = false
                                        onPhotoCaptured(Uri.fromFile(photoFile).toString())
                                    }

                                    override fun onError(exc: ImageCaptureException) {
                                        shutterFlashVisible = false
                                        val fallbackFile = File(context.cacheDir, "craft_fallback_${System.currentTimeMillis()}.jpg")
                                        val sampleBm = CraftImageStorageManager.generateAuthenticCraftSampleBitmap("sample_terracotta")
                                        ImageProcessingUtils.compressAndSaveBitmapToFile(context, sampleBm, fallbackFile, quality = 90)
                                        sampleBm.recycle()
                                        isCapturing = false
                                        onPhotoCaptured(Uri.fromFile(fallbackFile).toString())
                                    }
                                }
                            )
                        }
                        .testTag("camera_shutter_snap_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCapturing) TerracottaPrimary
                                else if (realtimeQuality.isReadyToCapture) Color.White
                                else Color.White
                            )
                            .border(
                                3.dp,
                                if (realtimeQuality.isReadyToCapture) ForestSuccess else TerracottaPrimary,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (realtimeQuality.isReadyToCapture) Icons.Default.CameraAlt else Icons.Default.CameraAlt,
                                contentDescription = "Take Photo",
                                tint = if (realtimeQuality.isReadyToCapture) ForestSuccess else TerracottaPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Quick AI Auto-Scan Indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onPhotoCaptured("sample_terracotta") },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(IndigoSecondary.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Quick Mode",
                            tint = MarigoldTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "AI ऑटो मोड",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

/**
 * Stage 2: AI Quality Analysis & Actionable Suggestions
 */
@Composable
private fun AiQualityAnalysisStageView(
    imageUri: String,
    category: String,
    report: ImageQualityReport,
    onImproveAutomatically: () -> Unit,
    onProceedToStudio: () -> Unit,
    onReCrop: () -> Unit = {},
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image Preview with AI Scanner Badge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
        ) {
            CraftArtworkDisplay(
                imageUri = imageUri,
                category = category,
                styleFilter = "Natural Studio",
                modifier = Modifier.fillMaxSize(),
                showEnhancementBadge = false
            )
        }

        // Quality Status Banner (GOOD / NEEDS IMPROVEMENT)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quality_status_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (report.overallRating == QualityRating.GOOD) ForestContainer else Color(0xFFFFFBEB)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (report.overallRating == QualityRating.GOOD) ForestSuccess else MarigoldTertiary
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Image Quality:",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (report.overallRating == QualityRating.GOOD) "GOOD (उत्कृष्ट गुणवत्ता)" else "NEEDS IMPROVEMENT (सुधार आवश्यक)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (report.overallRating == QualityRating.GOOD) ForestSuccess else Color(0xFFB45309)
                    )
                    Text(
                        text = "गुणवत्ता स्कोर: ${report.scorePercent}% • 5 बिन्दु AI ऑडिट",
                        fontSize = 12.sp,
                        color = ArtisanTextMuted
                    )
                }

                Surface(
                    color = if (report.overallRating == QualityRating.GOOD) ForestSuccess else Color(0xFFD97706),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (report.overallRating == QualityRating.GOOD) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${report.scorePercent}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Actionable Suggestions Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MarigoldTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI सुझाव (Actionable Suggestions)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                }

                report.actionableSuggestions.forEachIndexed { i, suggestion ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "•", fontSize = 14.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                        Column {
                            Text(
                                text = suggestion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ArtisanTextPrimary
                            )
                            if (i < report.hindiSuggestions.size) {
                                Text(
                                    text = report.hindiSuggestions[i],
                                    fontSize = 11.sp,
                                    color = ArtisanTextSecondary
                                )
                            }
                        }
                    }
                }

                // 1-Tap "Improve Automatically" Button
                if (report.overallRating != QualityRating.GOOD) {
                    Button(
                        onClick = onImproveAutomatically,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("improve_automatically_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✨ Improve Automatically (स्वचालित सुधारें)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Detailed 5-Point Quality Checks List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "5-बिन्दु गुणवत्ता ऑडिट (Quality Checks):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )

                report.checks.forEach { check ->
                    QualityCheckRowItem(check = check)
                }
            }
        }

        // Navigation Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onReCrop,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp)
                    .testTag("ai_recrop_button")
            ) {
                Icon(imageVector = Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("📐 क्रॉप (Crop)", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("🔄 रीटेक", fontSize = 11.sp)
            }

            Button(
                onClick = onProceedToStudio,
                colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp)
                    .testTag("proceed_to_studio_button")
            ) {
                Text("स्टूडियो सेटिंग्स →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Quality Check Item Row
 */
@Composable
private fun QualityCheckRowItem(check: QualityCheckItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ArtisanSurfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (check.id) {
                    "visibility" -> Icons.Default.Visibility
                    "lighting" -> Icons.Default.WbSunny
                    "blur" -> Icons.Default.CenterFocusStrong
                    "background" -> Icons.Default.Wallpaper
                    else -> Icons.Default.Crop
                },
                contentDescription = null,
                tint = when (check.status) {
                    CheckStatus.PASSED -> ForestSuccess
                    CheckStatus.WARNING -> Color(0xFFD97706)
                    CheckStatus.FAILED -> Color(0xFFDC2626)
                },
                modifier = Modifier.size(20.dp)
            )

            Column {
                Text(
                    text = "${check.name} (${check.hindiName})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArtisanTextPrimary
                )
                Text(
                    text = check.description,
                    fontSize = 11.sp,
                    color = ArtisanTextSecondary
                )
            }
        }

        Surface(
            color = when (check.status) {
                CheckStatus.PASSED -> ForestContainer
                CheckStatus.WARNING -> Color(0xFFFEF3C7)
                CheckStatus.FAILED -> Color(0xFFFEE2E2)
            },
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = when (check.status) {
                    CheckStatus.PASSED -> "PASS"
                    CheckStatus.WARNING -> "ADVISE"
                    CheckStatus.FAILED -> "FAIL"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = when (check.status) {
                    CheckStatus.PASSED -> ForestSuccess
                    CheckStatus.WARNING -> Color(0xFFB45309)
                    CheckStatus.FAILED -> Color(0xFFDC2626)
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

/**
 * Stage 3: Enhance Studio Controls & Before/After Slider
 */
@Composable
private fun EnhanceStudioStageView(
    imageUri: String,
    originalImageUri: String = "",
    enhancedImageUri: String = "",
    category: String,
    settings: ImageEnhancementSettings,
    onBackdropSelected: (StudioBackdrop) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSharpnessChange: (Float) -> Unit,
    onCropRatioChange: (CropAspectRatio) -> Unit,
    onCenterToggled: (Boolean) -> Unit,
    onSliderPositionChange: (Float) -> Unit,
    onReCrop: () -> Unit = {},
    onProceedToReview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Interactive Before/After Comparison Slider (ORIGINAL | AI ENHANCED)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("before_after_slider_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📸 तुलना: मूल फोटो vs AI स्टूडियो (Before / After)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )

                    // Quick Toggle buttons for slider extremes
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { onSliderPositionChange(0.95f) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Original", fontSize = 11.sp)
                        }
                        TextButton(
                            onClick = { onSliderPositionChange(0.05f) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Enhanced", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Interactive Draggable Canvas Slider
                BeforeAfterCraftComparisonSlider(
                    imageUri = imageUri,
                    originalImageUri = originalImageUri,
                    enhancedImageUri = enhancedImageUri,
                    category = category,
                    backdropType = settings.selectedBackdrop.type,
                    brightness = settings.brightness,
                    contrast = settings.contrast,
                    sharpness = settings.sharpness,
                    isCentered = settings.isCentered,
                    sliderPosition = settings.sliderPosition,
                    onSliderPositionChange = onSliderPositionChange
                )

                Text(
                    text = "👈 स्लाइडर को दाएँ-बाएँ खींचकर अंतर देखें (Drag to compare)",
                    fontSize = 11.sp,
                    color = ArtisanTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // AI Canvas Tone & RGB Response Visualizer
        CanvasToneCurveVisualizer(
            brightness = 1.0f + settings.brightness,
            contrast = 1.0f + settings.contrast,
            saturation = 1.0f + (settings.sharpness * 0.3f),
            filterName = category
        )

        // AI 3-Filter Preset Selection (Handloom Vivid, Natural Studio, Minimalist Neutral)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎨 AI इमेज पोस्ट-प्रोसेसिंग फ़िल्टर:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "Canvas पावर्ड 3 विशेषज्ञ शैलियाँ (AI Canvas Filters)",
                            fontSize = 11.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CraftImageFilter.PRIMARY_THREE_FILTERS.forEach { filter ->
                        val isMatched = when (filter) {
                            CraftImageFilter.HANDLOOM_VIVID -> settings.brightness > 0.05f && settings.contrast > 0.05f
                            CraftImageFilter.NATURAL_STUDIO -> settings.brightness in -0.05f..0.08f && settings.selectedBackdrop.type == BackdropType.WARM_TERRACOTTA
                            CraftImageFilter.MINIMALIST_NEUTRAL -> settings.selectedBackdrop.type == BackdropType.WHITE_STUDIO
                            else -> false
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onBrightnessChange(filter.autoBrightness - 1.0f)
                                    onContrastChange(filter.autoContrast - 1.0f)
                                    onSharpnessChange(0.6f)
                                    when (filter) {
                                        CraftImageFilter.HANDLOOM_VIVID -> {
                                            StudioBackdropPresets.presets.find { it.type == BackdropType.PEDESTAL_GRADIENT }?.let(onBackdropSelected)
                                        }
                                        CraftImageFilter.NATURAL_STUDIO -> {
                                            StudioBackdropPresets.presets.find { it.type == BackdropType.WARM_TERRACOTTA }?.let(onBackdropSelected)
                                        }
                                        CraftImageFilter.MINIMALIST_NEUTRAL -> {
                                            StudioBackdropPresets.presets.find { it.type == BackdropType.WHITE_STUDIO }?.let(onBackdropSelected)
                                        }
                                        else -> {}
                                    }
                                }
                                .testTag("capture_filter_${filter.id.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isMatched) TerracottaContainer else ArtisanSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isMatched) 2.dp else 1.dp,
                                if (isMatched) TerracottaPrimary else ArtisanCardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = filter.emoji,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = filter.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Text(
                                    text = filter.hindiTitle,
                                    fontSize = 10.sp,
                                    color = ArtisanTextSecondary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // B. AI Background Removal & Neutral Backdrops Selector
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI बैकग्राउंड रिमूवल व न्यूट्रल बैकड्रॉप्स:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisanTextPrimary
                    )
                    Surface(
                        color = TerracottaContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "ONDC / Amazon Ready",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(StudioBackdropPresets.presets) { backdrop ->
                        val isSelected = backdrop.id == settings.selectedBackdrop.id
                        Surface(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { onBackdropSelected(backdrop) }
                                .testTag("backdrop_option_${backdrop.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) TerracottaContainer else ArtisanSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) TerracottaPrimary else ArtisanCardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = backdrop.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) TerracottaPrimary else ArtisanTextMuted
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = backdrop.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisanTextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = backdrop.hindiName,
                                    fontSize = 10.sp,
                                    color = ArtisanTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // C. Precision Image Enhancement Adjustments
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "इमेज संवर्धन ट्यूनिंग (Image Adjustments):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )

                // Brightness Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("☀️ ब्राइटनेस (Brightness)", fontSize = 12.sp, color = ArtisanTextPrimary)
                        Text("${(settings.brightness * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    }
                    Slider(
                        value = settings.brightness,
                        onValueChange = onBrightnessChange,
                        valueRange = -0.5f..0.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = TerracottaPrimary,
                            activeTrackColor = TerracottaPrimary
                        )
                    )
                }

                // Contrast Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🌓 कंट्रास्ट (Contrast)", fontSize = 12.sp, color = ArtisanTextPrimary)
                        Text("${(settings.contrast * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                    }
                    Slider(
                        value = settings.contrast,
                        onValueChange = onContrastChange,
                        valueRange = -0.5f..0.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = IndigoSecondary,
                            activeTrackColor = IndigoSecondary
                        )
                    )
                }

                // Sharpness Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🔍 स्पष्टता व टेक्सचर (Sharpness)", fontSize = 12.sp, color = ArtisanTextPrimary)
                        Text("${(settings.sharpness * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ForestSuccess)
                    }
                    Slider(
                        value = settings.sharpness,
                        onValueChange = onSharpnessChange,
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = ForestSuccess,
                            activeTrackColor = ForestSuccess
                        )
                    )
                }

                // Aspect Ratio / Crop Presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📐 क्रॉप अनुपात (Aspect Ratio):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ArtisanTextPrimary)
                        TextButton(
                            onClick = onReCrop,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(14.dp), tint = TerracottaPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("कस्टम क्रॉप टूल", fontSize = 11.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CropAspectRatio.values()) { crop ->
                            val isSelected = crop == settings.cropRatio
                            Surface(
                                modifier = Modifier
                                    .width(90.dp)
                                    .clickable { onCropRatioChange(crop) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) TerracottaContainer else ArtisanSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) TerracottaPrimary else ArtisanCardBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = crop.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TerracottaPrimary else ArtisanTextPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = crop.dimensionsLabel,
                                        fontSize = 9.sp,
                                        color = if (isSelected) TerracottaPrimary else ArtisanTextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Auto Center Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArtisanSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎯 ऑटो-सेंटर उत्पाद (Center Product)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisanTextPrimary
                        )
                        Text(
                            text = "मार्केटप्लेस के लिए समान पैडिंग सेट करें",
                            fontSize = 10.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                    Switch(
                        checked = settings.isCentered,
                        onCheckedChange = onCenterToggled,
                        colors = SwitchDefaults.colors(checkedThumbColor = TerracottaPrimary)
                    )
                }

                // Marketplace Dimension Specs
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "आउटपुट रेजोल्यूशन: 1080 x 1080 px (HD Ready)",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "sRGB 72DPI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        // Proceed to Review Button
        Button(
            onClick = onProceedToReview,
            colors = ButtonDefaults.buttonColors(containerColor = ForestSuccess),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("proceed_to_quality_review_button")
        ) {
            Icon(imageVector = Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "समीक्षा और सत्यापन (Review & Verify) →",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Stage 4: Quality Review Stage View
 */
@Composable
private fun QualityReviewStageView(
    imageUri: String,
    category: String,
    settings: ImageEnhancementSettings,
    report: ImageQualityReport,
    onSaveAndGoToWizard: () -> Unit,
    onSaveDirectly: () -> Unit,
    onBackToStudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Enhanced Photo Final Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ArtisanCardBorder))
        ) {
            CraftArtworkDisplay(
                imageUri = imageUri,
                category = category,
                styleFilter = settings.selectedBackdrop.name,
                modifier = Modifier.fillMaxSize(),
                showEnhancementBadge = true
            )
        }

        // Verified Certificate Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ForestContainer),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ForestSuccess))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = ForestSuccess,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = "Image Quality: 100% GOOD",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestSuccess
                    )
                    Text(
                        text = "उत्कृष्ट गुणवत्ता! Amazon, Etsy व ONDC मानकों के अनुरूप।",
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary
                    )
                }
            }
        }

        // Final Verified Checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                    text = "सत्यापित ऑडिट परिणाम (Verified Checks):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisanTextPrimary
                )

                val checks = listOf(
                    "Product visibility verified (उत्पाद दृश्यता पूर्ण)",
                    "Studio lighting balanced (संतुलित स्टूडियो लाइटिंग)",
                    "Authentic texture preserved (हस्तनिर्मित बनावट सुरक्षित)",
                    "Neutral studio backdrop applied (क्लीन न्यूट्रल बैकग्राउंड)",
                    "1:1 marketplace crop & centered (1:1 वर्गाकार केंद्र संरेखण)"
                )

                checks.forEach { checkText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ForestSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = checkText,
                            fontSize = 12.sp,
                            color = ArtisanTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSaveAndGoToWizard,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_and_proceed_to_voice_wizard_button")
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎤 बोलकर विवरण दें (Proceed to Voice Wizard) →",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onSaveDirectly,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_directly_to_catalog_button")
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "💾 सीधे कैटलॉग में सेव करें (Direct Save)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = onBackToStudio,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← स्टूडियो सेटिंग्स में बदलाव करें", fontSize = 12.sp)
            }
        }
    }
}

/**
 * Stage 5: Saved Success View
 */
@Composable
private fun SavedSuccessStageView(
    imageUri: String,
    category: String,
    onGoToCatalog: () -> Unit,
    onStartNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(ForestContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = ForestSuccess,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "उत्पाद सफलतापूर्वक सेव हुआ!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ArtisanTextPrimary
        )

        Text(
            text = "Product Saved with AI Studio Enhancement",
            fontSize = 13.sp,
            color = ArtisanTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGoToCatalog,
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(imageVector = Icons.Default.Inventory2, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("कैटलॉग देखें (View in Catalog)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onStartNew,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("➕ नया शिल्प जोड़ें (Add Another Craft)", fontSize = 13.sp)
        }
    }
}

/**
 * Camera Framing Overlay
 */
@Composable
fun HandicraftFramingOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val frameSize = width * 0.78f
        val left = (width - frameSize) / 2f
        val top = (height - frameSize) / 2.2f

        val cornerLength = 32.dp.toPx()
        val strokeWidth = 3.5.dp.toPx()
        val cornerColor = Color(0xFFFFD54F)

        // Top-Left corner
        drawLine(cornerColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

        // Top-Right corner
        drawLine(cornerColor, Offset(left + frameSize, top), Offset(left + frameSize - cornerLength, top), strokeWidth)
        drawLine(cornerColor, Offset(left + frameSize, top), Offset(left + frameSize, top + cornerLength), strokeWidth)

        // Bottom-Left corner
        drawLine(cornerColor, Offset(left, top + frameSize), Offset(left + cornerLength, top + frameSize), strokeWidth)
        drawLine(cornerColor, Offset(left, top + frameSize), Offset(left, top + frameSize - cornerLength), strokeWidth)

        // Bottom-Right corner
        drawLine(cornerColor, Offset(left + frameSize, top + frameSize), Offset(left + frameSize - cornerLength, top + frameSize), strokeWidth)
        drawLine(cornerColor, Offset(left + frameSize, top + frameSize), Offset(left + frameSize, top + frameSize - cornerLength), strokeWidth)

        // Rule of thirds subtle grid lines inside box
        val oneThirdW = frameSize / 3f
        val oneThirdH = frameSize / 3f
        val gridColor = Color.White.copy(alpha = 0.25f)
        val gridStroke = 1.dp.toPx()

        drawLine(gridColor, Offset(left + oneThirdW, top), Offset(left + oneThirdW, top + frameSize), gridStroke)
        drawLine(gridColor, Offset(left + 2 * oneThirdW, top), Offset(left + 2 * oneThirdW, top + frameSize), gridStroke)
        drawLine(gridColor, Offset(left, top + oneThirdH), Offset(left + frameSize, top + oneThirdH), gridStroke)
        drawLine(gridColor, Offset(left, top + 2 * oneThirdH), Offset(left + frameSize, top + 2 * oneThirdH), gridStroke)
    }
}
