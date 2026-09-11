package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.ai.MinimalistBackgroundStyle
import com.example.data.image.CraftImageStorageManager
import com.example.ui.theme.ArtisanCardBorder
import com.example.ui.theme.ArtisanSurface
import com.example.ui.theme.ArtisanTextMuted
import com.example.ui.theme.ArtisanTextPrimary
import com.example.ui.theme.ArtisanTextSecondary
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.DeepNavyContainer
import com.example.ui.theme.ForestContainer
import com.example.ui.theme.ForestSuccess
import com.example.ui.theme.MarigoldAccent
import com.example.ui.theme.MarigoldContainer
import com.example.ui.theme.MarigoldTertiary
import com.example.ui.theme.TerracottaContainer
import com.example.ui.theme.TerracottaDark
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.WarmIvoryBackground
import com.example.ui.theme.WarmIvoryCard
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.HunarSetuViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/**
 * Screen providing an integrated live Camera and Google Imagen 3 workflow:
 * 1. Live camera viewfinder with grid, level guide, and sample artisan crafts fallback.
 * 2. Automatic craft segmentation to isolate handcrafted items from workshop clutter.
 * 3. Imagen 3 integration to generate professional, minimalist background backdrops.
 * 4. Interactive Before/After split slider showing raw workshop photo vs AI minimalist studio result.
 * 5. Lighting, warmth, and shadow controls for natural product integration.
 * 6. Direct saving to Room database catalog or forwarding to Smart Catalog Wizard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraImagenStudioScreen(
    viewModel: HunarSetuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State from ViewModel
    val capturedPhotoUri by viewModel.imagenCapturedPhotoUri.collectAsStateWithLifecycle()
    val compositedUri by viewModel.imagenCompositedUri.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingImagen.collectAsStateWithLifecycle()
    val generationStep by viewModel.imagenGenerationStep.collectAsStateWithLifecycle()
    val selectedStyle by viewModel.selectedMinimalistStyle.collectAsStateWithLifecycle()
    val customPrompt by viewModel.customImagenPrompt.collectAsStateWithLifecycle()
    val studioCategory by viewModel.studioCategory.collectAsStateWithLifecycle()
    val shadowIntensity by viewModel.imagenShadowIntensity.collectAsStateWithLifecycle()
    val warmth by viewModel.imagenWarmth.collectAsStateWithLifecycle()
    val exposure by viewModel.imagenExposure.collectAsStateWithLifecycle()
    val beforeAfterPos by viewModel.imagenBeforeAfterPosition.collectAsStateWithLifecycle()

    // Permissions
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "कैमरा अनुमति अस्वीकार की गई (Camera permission denied)", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo picker launcher (zero permission Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImagenPhotoSelected(uri.toString())
        }
    }

    // Modal Sheet State for Quick Catalog Save
    var showSaveDialog by remember { mutableStateOf(false) }
    var showHelpSheet by remember { mutableStateOf(false) }
    var showFineTuneSheet by remember { mutableStateOf(false) }

    val helpSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tuneSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Camera & Imagen Studio",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MarigoldContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Imagen 3",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MarigoldTertiary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "शिल्प फ़ोटो व AI मिनिमलिस्ट बैकग्राउंड",
                            fontSize = 12.sp,
                            color = ArtisanTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppNavTab.ARTISAN_HOME) },
                        modifier = Modifier.testTag("imagen_studio_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = DeepNavy
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.speakText(
                                "कैमरा व Imagen स्टूडियो में आपका स्वागत है। अपने हस्तशिल्प की फ़ोटो लें और Imagen 3 से पेशेवर व सुरुचिपूर्ण मिनिमलिस्ट बैकग्राउंड तैयार करें।",
                                "hi"
                            )
                        },
                        modifier = Modifier.testTag("listen_studio_instructions")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen to Instructions",
                            tint = TerracottaPrimary
                        )
                    }
                    IconButton(
                        onClick = { showHelpSheet = true },
                        modifier = Modifier.testTag("imagen_studio_help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Studio Help",
                            tint = DeepNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmIvoryBackground)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WarmIvoryBackground)
        ) {
            if (capturedPhotoUri.isBlank()) {
                // MODE 1: LIVE CAMERA VIEWFINDER & CAPTURE
                CameraViewfinderStage(
                    hasPermission = hasCameraPermission,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onPhotoCaptured = { uri -> viewModel.onImagenPhotoCaptured(uri) },
                    onPickFromGallery = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onSelectSampleCraft = { sampleKey ->
                        viewModel.onImagenPhotoSelected(sampleKey)
                    },
                    onSpeakAdvice = { text -> viewModel.speakHelpText(text) }
                )
            } else {
                // MODE 2: AI IMAGEN MINIMALIST STUDIO WORKFLOW
                ImagenStudioEnhancerStage(
                    rawPhotoUri = capturedPhotoUri,
                    compositedPhotoUri = compositedUri,
                    isGenerating = isGenerating,
                    generationStep = generationStep,
                    selectedStyle = selectedStyle,
                    customPrompt = customPrompt,
                    craftCategory = studioCategory,
                    sliderPosition = beforeAfterPos,
                    onSliderChange = { pos -> viewModel.imagenBeforeAfterPosition.value = pos },
                    onSelectStyle = { style -> viewModel.generateImagenBackground(style) },
                    onUpdateCustomPrompt = { prompt -> viewModel.customImagenPrompt.value = prompt },
                    onGenerateCustomPrompt = { prompt ->
                        viewModel.generateImagenBackground(MinimalistBackgroundStyle.CUSTOM, prompt)
                    },
                    onOpenFineTuning = { showFineTuneSheet = true },
                    onRetakePhoto = { viewModel.retakeCameraPhoto() },
                    onOpenSaveDialog = { showSaveDialog = true },
                    onNavigateToWizard = {
                        viewModel.wizardImageUri.value = compositedUri.ifBlank { capturedPhotoUri }
                        viewModel.navigateTo(AppNavTab.SMART_CATALOG_WIZARD)
                    },
                    onSpeakText = { text -> viewModel.speakHelpText(text) }
                )
            }
        }
    }

    // Modal Bottom Sheet: Lighting & Contact Shadow Tuning
    if (showFineTuneSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFineTuneSheet = false },
            sheetState = tuneSheetState,
            containerColor = ArtisanSurface
        ) {
            FineTuneBlendControlsSheet(
                shadowIntensity = shadowIntensity,
                warmth = warmth,
                exposure = exposure,
                onApply = { s, w, e ->
                    viewModel.updateImagenBlendSettings(s, w, e)
                },
                onClose = {
                    scope.launch {
                        tuneSheetState.hide()
                        showFineTuneSheet = false
                    }
                }
            )
        }
    }

    // Modal Bottom Sheet: Help & Tips
    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = helpSheetState,
            containerColor = ArtisanSurface
        ) {
            StudioHelpBottomSheet(
                onClose = {
                    scope.launch {
                        helpSheetState.hide()
                        showHelpSheet = false
                    }
                }
            )
        }
    }

    // Quick Save Dialog: Save directly to Catalog
    if (showSaveDialog) {
        QuickSaveToCatalogDialog(
            defaultCategory = studioCategory,
            onDismiss = { showSaveDialog = false },
            onConfirm = { title, category, price ->
                showSaveDialog = false
                viewModel.saveImagenEnhancedProductToCatalog(
                    title = title,
                    price = price,
                    category = category
                )
            }
        )
    }
}

// =============================================================================
// STAGE 1: LIVE CAMERA VIEWFINDER & HUD
// =============================================================================

@Composable
private fun CameraViewfinderStage(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onPhotoCaptured: (String) -> Unit,
    onPickFromGallery: () -> Unit,
    onSelectSampleCraft: (String) -> Unit,
    onSpeakAdvice: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var isGridEnabled by remember { mutableStateOf(true) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var isShutterTriggered by remember { mutableStateOf(false) }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }

    if (!hasPermission) {
        // Permission Request Fallback View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(TerracottaContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "कैमरा अनुमति आवश्यक है",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "हस्तशिल्प की लाइव फ़ोटो खींचने और AI Imagen से आधुनिक मिनिमलिस्ट बैकग्राउंड जनरेट करने के लिए कैमरा अनुमति दें।",
                        fontSize = 13.sp,
                        color = ArtisanTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("grant_camera_permission_button")
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("कैमरा शुरू करें (Grant Permission)", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onPickFromGallery,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("upload_gallery_fallback_button")
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = DeepNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("या गैलरी से फ़ोटो चुनें (Pick Photo)", color = DeepNavy)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Sample Crafts Section
            Text(
                text = "या त्वरित परीक्षण के लिए नमूना शिल्प चुनें:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ArtisanTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            SampleCraftsPickerRow(onSelect = onSelectSampleCraft)
        }
        return
    }

    // Active CameraX Live Viewfinder
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val capture = ImageCapture.Builder()
                            .setFlashMode(flashMode)
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
                        cameraControl = camera.cameraControl
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "कैमरा त्रुटि: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Rule of Thirds Minimalist Grid Overlay
        if (isGridEnabled) {
            CameraFramingGrid(modifier = Modifier.fillMaxSize())
        }

        // Shutter White Flash Animation
        AnimatedVisibility(
            visible = isShutterTriggered,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.85f)))
        }

        // Top Floating HUD Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Status Tag
            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ForestSuccess, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "लाइव कैमरा तैयार",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Controls: Flash, Torch, Flip, Grid
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Flash Toggle
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                            else -> ImageCapture.FLASH_MODE_AUTO
                        }
                        imageCapture?.flashMode = flashMode
                    },
                    modifier = Modifier.testTag("camera_flash_button")
                ) {
                    Icon(
                        imageVector = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                            else -> Icons.Default.FlashAuto
                        },
                        contentDescription = "Toggle Flash",
                        tint = Color.White
                    )
                }

                // Grid Toggle
                IconButton(
                    onClick = { isGridEnabled = !isGridEnabled },
                    modifier = Modifier.testTag("camera_grid_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Toggle Grid",
                        tint = if (isGridEnabled) MarigoldAccent else Color.White
                    )
                }

                // Flip Camera
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier.testTag("camera_flip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip Camera",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Camera Action Controls & Shutter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Advice Floating Pill
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = MarigoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "शिल्प को केंद्र में रखें और प्राकृतिक रोशनी में फ़ोटो लें",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Main Capture Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Pick Button
                IconButton(
                    onClick = onPickFromGallery,
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .testTag("camera_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Choose from Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Main Circular Shutter Capture Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .background(TerracottaPrimary, CircleShape)
                        .clickable {
                            val cap = imageCapture
                            if (cap != null) {
                                isShutterTriggered = true
                                val tempFile = CraftImageStorageManager.createTempImageFile(context, "imagen_craft_")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

                                cap.takePicture(
                                    outputOptions,
                                    cameraExecutor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            ContextCompat.getMainExecutor(context).execute {
                                                isShutterTriggered = false
                                                val uri = android.net.Uri.fromFile(tempFile).toString()
                                                onPhotoCaptured(uri)
                                            }
                                        }

                                        override fun onError(exc: ImageCaptureException) {
                                            ContextCompat.getMainExecutor(context).execute {
                                                isShutterTriggered = false
                                                Toast.makeText(context, "फ़ोटो कैप्चर विफल: ${exc.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            } else {
                                // Fallback mock capture for emulator environments
                                isShutterTriggered = true
                                onPhotoCaptured("sample_terracotta")
                            }
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture Photo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Sample Craft Quick Toggle Button
                IconButton(
                    onClick = { onSelectSampleCraft("sample_terracotta") },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .testTag("camera_quick_sample_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Use Sample Craft",
                        tint = MarigoldAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Quick Samples Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "नमूना शिल्प:",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(end = 8.dp)
                )
                SampleCraftsPickerRow(onSelect = onSelectSampleCraft, isDarkTheme = true)
            }
        }
    }
}

/**
 * Grid overlay drawing rule of thirds.
 */
@Composable
private fun CameraFramingGrid(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val linePaint = Color.White.copy(alpha = 0.25f)
        val stroke = 1.dp.toPx()

        // Vertical lines (rule of thirds)
        drawLine(linePaint, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = stroke)
        drawLine(linePaint, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth = stroke)

        // Horizontal lines (rule of thirds)
        drawLine(linePaint, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = stroke)
        drawLine(linePaint, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth = stroke)

        // Centered horizon leveler indicator
        val centerY = h * 0.5f
        drawLine(
            Color.Green.copy(alpha = 0.45f),
            Offset(w * 0.42f, centerY),
            Offset(w * 0.58f, centerY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Horizontal row of sample craft quick chips for instant testing.
 */
@Composable
private fun SampleCraftsPickerRow(
    onSelect: (String) -> Unit,
    isDarkTheme: Boolean = false
) {
    val samples = listOf(
        Pair("sample_terracotta", "टेराकोटा (Pottery)"),
        Pair("sample_handloom", "बनारसी साड़ी (Silk)"),
        Pair("sample_dhokra", "ढोकरा पीतल (Brass)"),
        Pair("sample_woodcraft", "काष्ठ खिलौना (Wood)")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(samples) { (key, label) ->
            Surface(
                color = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else TerracottaContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clickable { onSelect(key) }
                    .testTag("sample_craft_chip_$key")
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (isDarkTheme) Color.White else TerracottaDark,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

// =============================================================================
// STAGE 2: AI IMAGEN MINIMALIST STUDIO WORKFLOW
// =============================================================================

@Composable
private fun ImagenStudioEnhancerStage(
    rawPhotoUri: String,
    compositedPhotoUri: String,
    isGenerating: Boolean,
    generationStep: String,
    selectedStyle: MinimalistBackgroundStyle,
    customPrompt: String,
    craftCategory: String,
    sliderPosition: Float,
    onSliderChange: (Float) -> Unit,
    onSelectStyle: (MinimalistBackgroundStyle) -> Unit,
    onUpdateCustomPrompt: (String) -> Unit,
    onGenerateCustomPrompt: (String) -> Unit,
    onOpenFineTuning: () -> Unit,
    onRetakePhoto: () -> Unit,
    onOpenSaveDialog: () -> Unit,
    onNavigateToWizard: () -> Unit,
    onSpeakText: (String) -> Unit
) {
    var customPromptInput by remember { mutableStateOf(customPrompt) }
    val displayPhoto = compositedPhotoUri.ifBlank { rawPhotoUri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Visual Area: Interactive Before / After Comparison Slider
        Card(
            colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header of Comparison Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "फ़ोटो तुलना (Before / After Comparison)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                    }

                    IconButton(
                        onClick = onOpenFineTuning,
                        modifier = Modifier.testTag("open_fine_tuning_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Fine Tune Shadows & Lighting",
                            tint = DeepNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Split Slider View
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9))
                        .testTag("before_after_slider")
                ) {
                    val boxWidth = maxWidth
                    val boxHeight = maxHeight
                    val dividerX = boxWidth * sliderPosition

                    // Right Side: AI Imagen Enhanced Result
                    AsyncImage(
                        model = displayPhoto,
                        contentDescription = "AI Imagen Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Left Side: Raw Workshop Original Photo (clipped by divider)
                    Box(
                        modifier = Modifier
                            .width(dividerX)
                            .fillMaxHeight()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    bottomStart = 14.dp,
                                    topEnd = 0.dp,
                                    bottomEnd = 0.dp
                                )
                            )
                    ) {
                        AsyncImage(
                            model = rawPhotoUri,
                            contentDescription = "Original Raw Workshop Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(boxWidth)
                                .fillMaxHeight()
                        )
                    }

                    // Draggable Vertical Divider Handle
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .offset(x = (dividerX - 16.dp).coerceAtLeast(0.dp))
                            .width(32.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newPos = (sliderPosition + (dragAmount.x / size.width.toFloat()))
                                        .coerceIn(0.05f, 0.95f)
                                    onSliderChange(newPos)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Vertical white separator line
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .fillMaxHeight()
                                .background(Color.White)
                        )

                        // Circular Touch Knob Handle
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .border(2.dp, TerracottaPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⇄",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary
                            )
                        }
                    }

                    // Floating Comparison Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "मूल फ़ोटो (Raw)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = TerracottaPrimary.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "✨ Imagen 3 मिनिमलिस्ट",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Slider Hint text
                Text(
                    text = "हैंडल (⇄) को बाएँ-दाएँ खिसकाकर मूल और AI बैकग्राउंड की तुलना करें",
                    fontSize = 11.sp,
                    color = ArtisanTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // AI Generation Progress Status (when generating)
        if (isGenerating) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MarigoldContainer),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MarigoldAccent.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MarigoldTertiary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = "Imagen 3 बैकग्राउंड तैयार कर रहा है...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                    }

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = MarigoldTertiary,
                        trackColor = Color.White.copy(alpha = 0.6f)
                    )

                    Text(
                        text = generationStep.ifBlank { "Google Imagen 3 से बैकग्राउंड सिंथेसाइज़ हो रहा है..." },
                        fontSize = 12.sp,
                        color = ArtisanTextSecondary
                    )
                }
            }
        }

        // Minimalist Background Presets Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "मिनिमलिस्ट बैकग्राउंड स्टाइल चुनें",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
                Text(
                    text = "AI Presets",
                    fontSize = 12.sp,
                    color = TerracottaPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(MinimalistBackgroundStyle.values()) { style ->
                    val isSelected = selectedStyle == style
                    MinimalistStyleCard(
                        style = style,
                        isSelected = isSelected,
                        onClick = { onSelectStyle(style) }
                    )
                }
            }
        }

        // Custom AI Prompt Field (Active when CUSTOM is selected)
        if (selectedStyle == MinimalistBackgroundStyle.CUSTOM) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisanSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisanCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "कस्टम AI बैकग्राउंड प्रॉम्प्ट (Describe Scene)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )

                    OutlinedTextField(
                        value = customPromptInput,
                        onValueChange = {
                            customPromptInput = it
                            onUpdateCustomPrompt(it)
                        },
                        placeholder = {
                            Text(
                                "उदा. बेज मार्बल पेडेस्टल पर सुबह की कोमल धूप और पत्तों की परछाई",
                                fontSize = 12.sp,
                                color = ArtisanTextMuted
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onSpeakText("अपनी पसंद का बैकग्राउंड बोलें, जैसे: मार्बल पोडियम या लकड़ी का टेबल")
                                },
                                modifier = Modifier.testTag("custom_prompt_voice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Dictate",
                                    tint = TerracottaPrimary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = ArtisanCardBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_prompt_input")
                    )

                    // Quick Prompt Suggestion Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val suggestions = listOf(
                            "सफेद मार्बल (Marble)",
                            "सुबह की खिड़की की धूप (Sunlight)",
                            "लकड़ी का स्लैब (Teak Slab)"
                        )
                        suggestions.forEach { suggestion ->
                            Surface(
                                color = TerracottaContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    customPromptInput = suggestion
                                    onUpdateCustomPrompt(suggestion)
                                }
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 10.sp,
                                    color = TerracottaDark,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onGenerateCustomPrompt(customPromptInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("generate_custom_imagen_button"),
                        enabled = !isGenerating
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Imagen 3 से जनरेट करें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Action Buttons: Save to Catalog / Wizard / Retake
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Primary Action: Save to Catalog
            Button(
                onClick = onOpenSaveDialog,
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_to_catalog_button"),
                enabled = !isGenerating
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "कैटलॉग में सुरक्षित करें (Save to Catalog)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Secondary Action: Forward to Smart Catalog Wizard
            OutlinedButton(
                onClick = onNavigateToWizard,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepNavy),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepNavyContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("continue_to_smart_wizard_button")
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "स्मार्ट विज़ार्ड में विवरण जोड़ें (Add Voice Details)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Retake Camera Photo
            TextButton(
                onClick = onRetakePhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("retake_photo_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = ArtisanTextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "फ़ोटो पुनः खींचें (Retake Photo)",
                    fontSize = 13.sp,
                    color = ArtisanTextSecondary
                )
            }
        }
    }
}

/**
 * Visual preset card for each minimalist background style.
 */
@Composable
private fun MinimalistStyleCard(
    style: MinimalistBackgroundStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TerracottaContainer else ArtisanSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) TerracottaPrimary else ArtisanCardBorder
        ),
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .testTag("style_card_${style.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Style Color Palette Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (style) {
                            MinimalistBackgroundStyle.STUDIO_WHITE -> Brush.verticalGradient(
                                listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                            )
                            MinimalistBackgroundStyle.HERITAGE_TERRACOTTA -> Brush.verticalGradient(
                                listOf(Color(0xFFFED7AA), Color(0xFFC04A26))
                            )
                            MinimalistBackgroundStyle.RAW_LINEN -> Brush.linearGradient(
                                listOf(Color(0xFFF5EBE0), Color(0xFFD5BDAF))
                            )
                            MinimalistBackgroundStyle.DARK_TEAKWOOD -> Brush.radialGradient(
                                listOf(Color(0xFF5C3D2E), Color(0xFF1E140F))
                            )
                            MinimalistBackgroundStyle.TRAVERTINE_STONE -> Brush.linearGradient(
                                listOf(Color(0xFFF8F6F0), Color(0xFFD8D2C2))
                            )
                            MinimalistBackgroundStyle.CUSTOM -> Brush.radialGradient(
                                listOf(Color(0xFFFEF3C7), Color(0xFFD97706))
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(TerracottaPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = style.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = style.hindiTitle,
                fontSize = 10.sp,
                color = TerracottaDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Surface(
                color = if (isSelected) TerracottaPrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = style.tag,
                    fontSize = 9.sp,
                    color = if (isSelected) TerracottaPrimary else ArtisanTextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// =============================================================================
// MODAL SHEETS & DIALOGS
// =============================================================================

/**
 * Modal bottom sheet for fine-tuning contact shadows, warmth, and exposure.
 */
@Composable
private fun FineTuneBlendControlsSheet(
    shadowIntensity: Float,
    warmth: Float,
    exposure: Float,
    onApply: (shadow: Float, warmth: Float, exposure: Float) -> Unit,
    onClose: () -> Unit
) {
    var shadowVal by remember { mutableFloatStateOf(shadowIntensity) }
    var warmthVal by remember { mutableFloatStateOf(warmth) }
    var exposureVal by remember { mutableFloatStateOf(exposure) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "प्रकाश व परछाई नियंत्रण (Shadow & Lighting)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        // Contact Shadow Slider
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "परछाई की गहराई (Contact Shadow)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ArtisanTextPrimary)
                Text(text = "${(shadowVal * 100).toInt()}%", fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = shadowVal,
                onValueChange = { shadowVal = it },
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = TerracottaPrimary,
                    activeTrackColor = TerracottaPrimary
                ),
                modifier = Modifier.testTag("shadow_slider")
            )
        }

        // Warmth / Color Temperature Slider
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "रंग का तापमान (Warmth / Coolness)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ArtisanTextPrimary)
                Text(
                    text = if (warmthVal > 0) "+${(warmthVal * 100).toInt()}% Warm" else "${(warmthVal * 100).toInt()}% Cool",
                    fontSize = 12.sp,
                    color = TerracottaPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = warmthVal,
                onValueChange = { warmthVal = it },
                valueRange = -0.5f..0.5f,
                colors = SliderDefaults.colors(
                    thumbColor = TerracottaPrimary,
                    activeTrackColor = TerracottaPrimary
                ),
                modifier = Modifier.testTag("warmth_slider")
            )
        }

        // Exposure Slider
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "उजाला / एक्सपोज़र (Brightness)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ArtisanTextPrimary)
                Text(
                    text = "${(exposureVal * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = TerracottaPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = exposureVal,
                onValueChange = { exposureVal = it },
                valueRange = -0.4f..0.4f,
                colors = SliderDefaults.colors(
                    thumbColor = TerracottaPrimary,
                    activeTrackColor = TerracottaPrimary
                ),
                modifier = Modifier.testTag("exposure_slider")
            )
        }

        Button(
            onClick = {
                onApply(shadowVal, warmthVal, exposureVal)
                onClose()
            },
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("apply_fine_tune_button")
        ) {
            Text("परिवर्तन लागू करें (Apply Settings)", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Help and guidance sheet for the artisan.
 */
@Composable
private fun StudioHelpBottomSheet(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Imagen Studio गाइड (How It Works)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        val tips = listOf(
            Triple("1. प्राकृतिक रोशनी", "खिड़की के पास बैठकर फ़ोटो खींचें ताकि शिल्प की नक्काशी व रंग स्पष्ट दिखें।", Icons.Default.WbSunny),
            Triple("2. AI बैकग्राउंड सेपरेशन", "Imagen AI आपके शिल्प को पहचानकर कार्यशाला की अव्यवस्था हटा देता है।", Icons.Default.AutoAwesome),
            Triple("3. मिनिमलिस्ट बैकग्राउंड", "अमेज़न, ओएनडीसी व सोशल मीडिया के लिए साफ, सादा व सुंदर स्टूडियो बैकग्राउंड तैयार होता है।", Icons.Default.CheckCircle),
            Triple("4. प्राकृतिक परछाई (Contact Shadow)", "शिल्प हवा में तैरता हुआ नहीं लगता बल्कि यथार्थवादी सतह पर रखा हुआ दिखता है।", Icons.Default.Tune)
        )

        tips.forEach { (title, desc, icon) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(TerracottaContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                    Text(text = desc, fontSize = 12.sp, color = ArtisanTextSecondary, lineHeight = 18.sp)
                }
            }
        }

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("समझ गया (Got It)", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Dialog allowing quick entry of title, category, and price before saving.
 */
@Composable
private fun QuickSaveToCatalogDialog(
    defaultCategory: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, price: Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(defaultCategory) }
    var priceStr by remember { mutableStateOf("1250") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "कैटलॉग में जोड़ें (Save to Catalog)",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "AI Imagen से तैयार उत्पाद को अपने कैटलॉग में प्रकाशित करने के लिए बुनियादी जानकारी भरें:",
                    fontSize = 12.sp,
                    color = ArtisanTextSecondary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("उत्पाद का नाम (Title)") },
                    placeholder = { Text("उदा. हस्तनिर्मित टेराकोटा सुराही") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_dialog_title_input")
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("मूल्य ₹ (Price)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_dialog_price_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "Handcrafted $category Art" }
                    val finalPrice = priceStr.toDoubleOrNull() ?: 1250.0
                    onConfirm(finalTitle, category, finalPrice)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_dialog_confirm_button")
            ) {
                Text("सुरक्षित करें (Save)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें (Cancel)", color = ArtisanTextSecondary)
            }
        }
    )
}
