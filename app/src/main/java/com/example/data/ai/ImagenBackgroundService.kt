package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.image.CraftImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Minimalist background presets designed specifically for Indian handicraft product photography.
 */
enum class MinimalistBackgroundStyle(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val description: String,
    val hindiDescription: String,
    val tag: String,
    val basePrompt: String
) {
    STUDIO_WHITE(
        id = "studio_white",
        title = "Minimalist Studio White",
        hindiTitle = "क्लीन स्टूडियो व्हाइट",
        description = "Pure seamless white cyclorama with soft ambient ground shadow, e-commerce catalog ready",
        hindiDescription = "अमेज़न व ओएनडीसी मानकों के अनुरूप स्वच्छ सफेद बैकग्राउंड",
        tag = "Amazon • ONDC Standard",
        basePrompt = "A pristine minimalist e-commerce studio background, pure warm white and subtle cream gradient cyclorama, soft diffused ambient morning lighting, clean seamless floor with gentle natural shadow falloff, empty centered podium area, professional commercial 8k product photography backdrop, high aesthetic, no clutter"
    ),
    HERITAGE_TERRACOTTA(
        id = "heritage_terracotta",
        title = "Warm Heritage Terracotta",
        hindiTitle = "पारंपरिक टेराकोटा मिट्टी",
        description = "Warm terracotta clay backdrop with gentle natural sunlight, earthy minimalist podium",
        hindiDescription = "मिट्टी, टेराकोटा व पारंपरिक शिल्पों के लिए प्राकृतिक गर्म रंग",
        tag = "Pottery • Terracotta",
        basePrompt = "A warm minimalist Indian heritage studio backdrop, earthy terracotta sun-baked clay podium with soft textured plaster wall behind, gentle natural sunbeam light, warm organic beige and terracotta tones, empty center for handcrafted items, 8k professional studio shot, clean aesthetic"
    ),
    RAW_LINEN(
        id = "raw_linen",
        title = "Organic Raw Linen & Jute",
        hindiTitle = "ऑर्गेनिक खादी व लिनन",
        description = "Natural woven raw linen fabric texture with soft side window illumination",
        hindiDescription = "हथकरघा वस्त्रों व साड़ियों के लिए सौम्य लिनन बनावट",
        tag = "Handloom • Textiles",
        basePrompt = "A minimalist handcrafted artisan backdrop, raw organic beige linen fabric draped on a clean studio table, soft diffused window light, delicate woven texture, neutral ivory tones, empty center stage, professional macro studio product backdrop, warm neutral minimal"
    ),
    DARK_TEAKWOOD(
        id = "dark_teakwood",
        title = "Dark Teakwood Showcase",
        hindiTitle = "पॉलिश टीकवुड लकड़ी",
        description = "Minimalist dark walnut wooden surface with soft diffused spotlight",
        hindiDescription = "पीतल, धातु व आभूषणों को उभारने के लिए गहरा लकड़ी का बेस",
        tag = "Brass • Metal • Jewelry",
        basePrompt = "A minimalist luxury studio backdrop, dark polished teakwood pedestal surface, moody directional soft studio spotlight, deep rich walnut wood grain with subtle vignetting, empty center, 8k product showcase, dramatic contrast for metal crafts"
    ),
    TRAVERTINE_STONE(
        id = "travertine_stone",
        title = "Travertine Stone Podium",
        hindiTitle = "ट्रैवर्टीन मार्बल पोडियम",
        description = "Minimalist architectural travertine stone block with subtle morning shadows",
        hindiDescription = "आधुनिक कलाकृतियों व पत्थर शिल्पों के लिए सुरुचिपूर्ण मार्बल",
        tag = "Artifacts • Modern Minimal",
        basePrompt = "A minimalist contemporary product photography backdrop, smooth neutral travertine stone block platform, soft pale sand and cream stone texture, gentle diffused architectural shadow, empty center, 8k commercial photography, serene gallery setting"
    ),
    CUSTOM(
        id = "custom_prompt",
        title = "Custom AI Prompt",
        hindiTitle = "कस्टम एआई निर्देश",
        description = "Describe your dream aesthetic background in your own words or voice",
        hindiDescription = "अपनी पसंद का बैकग्राउंड बोलकर या लिखकर तैयार करें",
        tag = "Custom AI",
        basePrompt = ""
    )
}

/**
 * Result data holder for generated backgrounds and composited images.
 */
data class ImagenGenerationResult(
    val backgroundUri: String,
    val compositedUri: String,
    val style: MinimalistBackgroundStyle,
    val usedPrompt: String,
    val isAiGenerated: Boolean,
    val message: String
)

/**
 * Service orchestrating Google Imagen 3 API calls, procedural fallbacks,
 * craft segmentation, and seamless contact-shadow compositing.
 */
object ImagenBackgroundService {
    private const val TAG = "ImagenBackgroundService"
    private const val IMAGEN_MODEL_ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Generates a professional minimalist background using Imagen 3 REST API.
     * Falls back to a procedural minimalist bitmap if offline or if no API key is available.
     */
    suspend fun generateMinimalistBackground(
        context: Context,
        style: MinimalistBackgroundStyle,
        customPromptText: String = "",
        craftCategory: String = "Handicraft"
    ): Pair<Bitmap, Boolean> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val finalPrompt = buildPrompt(style, customPromptText, craftCategory)

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "YOUR_API_KEY") {
            try {
                Log.d(TAG, "Calling Imagen 3 with prompt: $finalPrompt")
                val bitmap = callImagenApi(apiKey, finalPrompt)
                if (bitmap != null) {
                    return@withContext Pair(bitmap, true)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Imagen API call failed, falling back to procedural engine: ${e.message}")
            }
        } else {
            Log.d(TAG, "Using procedural minimalist backdrop (offline/keyless mode)")
        }

        // Procedural high-fidelity minimalist renderer fallback
        val proceduralBitmap = renderProceduralMinimalistBackdrop(style, 1080, 1080)
        Pair(proceduralBitmap, false)
    }

    /**
     * Builds an optimized prompt with professional commercial studio photography guardrails.
     */
    fun buildPrompt(
        style: MinimalistBackgroundStyle,
        customPromptText: String,
        craftCategory: String
    ): String {
        return if (style == MinimalistBackgroundStyle.CUSTOM && customPromptText.isNotBlank()) {
            "Professional minimalist commercial product photography background, $customPromptText, empty center stage for $craftCategory product showcase, soft natural ambient lighting, clean aesthetic, photorealistic 8k, pristine composition, no text, no clutter"
        } else {
            val base = if (style.basePrompt.isNotBlank()) style.basePrompt else MinimalistBackgroundStyle.STUDIO_WHITE.basePrompt
            "$base, tailored for Indian handcrafted $craftCategory, empty center pedestal with soft realistic contact shadow area"
        }
    }

    /**
     * Executes the HTTP POST request to the Imagen 3 API endpoint.
     */
    private fun callImagenApi(apiKey: String, prompt: String): Bitmap? {
        val url = "$IMAGEN_MODEL_ENDPOINT?key=$apiKey"
        val requestJson = JSONObject().apply {
            val instancesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("prompt", prompt)
                })
            }
            put("instances", instancesArray)

            val parametersObj = JSONObject().apply {
                put("sampleCount", 1)
                put("aspectRatio", "1:1")
                val outputOptions = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                }
                put("outputOptions", outputOptions)
            }
            put("parameters", parametersObj)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e(TAG, "Imagen API returned HTTP ${response.code}: $responseBody")
            return null
        }

        val jsonResponse = JSONObject(responseBody)
        val predictions = jsonResponse.optJSONArray("predictions")
        if (predictions != null && predictions.length() > 0) {
            val firstPrediction = predictions.getJSONObject(0)
            val base64Bytes = firstPrediction.optString("bytesBase64Encoded")
            if (base64Bytes.isNotBlank()) {
                val imageBytes = Base64.decode(base64Bytes, Base64.DEFAULT)
                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }
        }
        return null
    }

    /**
     * Procedural high-fidelity minimalist renderer.
     * Generates studio-grade textured gradients, pedestals, and lighting falloffs.
     */
    fun renderProceduralMinimalistBackdrop(
        style: MinimalistBackgroundStyle,
        width: Int = 1080,
        height: Int = 1080
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val w = width.toFloat()
        val h = height.toFloat()

        when (style) {
            MinimalistBackgroundStyle.STUDIO_WHITE -> {
                // Pristine warm white & cream cyclorama
                paint.shader = LinearGradient(
                    w * 0.5f, 0f, w * 0.5f, h,
                    Color.rgb(255, 255, 255),
                    Color.rgb(243, 244, 246),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Soft circular ambient highlight in center
                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.45f, w * 0.55f,
                    intArrayOf(Color.argb(40, 255, 255, 255), Color.argb(0, 255, 255, 255)),
                    null,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Minimalist subtle floor horizon line
                paint.shader = null
                paint.color = Color.argb(12, 0, 0, 0)
                paint.strokeWidth = 2f
                canvas.drawLine(0f, h * 0.72f, w, h * 0.72f, paint)

                // Floor gradient
                paint.shader = LinearGradient(
                    0f, h * 0.72f, 0f, h,
                    Color.argb(10, 0, 0, 0),
                    Color.argb(25, 0, 0, 0),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.72f, w, h, paint)
            }

            MinimalistBackgroundStyle.HERITAGE_TERRACOTTA -> {
                // Warm sun-baked terracotta clay background
                paint.shader = LinearGradient(
                    0f, 0f, w, h,
                    Color.rgb(254, 243, 235),
                    Color.rgb(243, 218, 199),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Sunbeam light accent
                paint.shader = RadialGradient(
                    w * 0.35f, h * 0.25f, w * 0.7f,
                    intArrayOf(Color.rgb(255, 250, 240), Color.rgb(238, 196, 170), Color.rgb(212, 142, 102)),
                    floatArrayOf(0f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Clay pedestal surface at bottom
                paint.shader = LinearGradient(
                    0f, h * 0.70f, 0f, h,
                    Color.rgb(196, 115, 75),
                    Color.rgb(168, 92, 54),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(RectF(w * 0.1f, h * 0.70f, w * 0.9f, h * 0.88f), 24f, 24f, paint)
            }

            MinimalistBackgroundStyle.RAW_LINEN -> {
                // Organic neutral linen fabric weave
                paint.shader = LinearGradient(
                    0f, 0f, 0f, h,
                    Color.rgb(246, 241, 233),
                    Color.rgb(232, 222, 209),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Soft window shadow diagonals
                paint.shader = null
                paint.color = Color.argb(14, 110, 95, 75)
                paint.strokeWidth = 36f
                for (i in -4..8) {
                    val startX = i * 160f
                    canvas.drawLine(startX, 0f, startX + 380f, h, paint)
                }

                // Linen cloth tabletop fold
                paint.shader = LinearGradient(
                    0f, h * 0.68f, 0f, h,
                    Color.argb(30, 200, 185, 170),
                    Color.argb(60, 170, 150, 130),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, h * 0.68f, w, h, paint)
            }

            MinimalistBackgroundStyle.DARK_TEAKWOOD -> {
                // Deep walnut and teakwood surface with spotlight
                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.40f, w * 0.65f,
                    intArrayOf(Color.rgb(74, 52, 38), Color.rgb(42, 28, 20), Color.rgb(18, 12, 8)),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Wood grain lines
                paint.shader = null
                paint.color = Color.argb(16, 255, 230, 200)
                paint.strokeWidth = 3f
                for (y in (h * 0.68f).toInt()..h.toInt() step 28) {
                    canvas.drawLine(0f, y.toFloat(), w, y.toFloat() + 6f, paint)
                }
            }

            MinimalistBackgroundStyle.TRAVERTINE_STONE -> {
                // Architectural travertine marble podium
                paint.shader = LinearGradient(
                    0f, 0f, w * 0.3f, h,
                    Color.rgb(248, 246, 242),
                    Color.rgb(230, 226, 218),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)

                // Stone block pedestal
                paint.shader = LinearGradient(
                    w * 0.15f, h * 0.66f, w * 0.85f, h * 0.86f,
                    Color.rgb(222, 216, 204),
                    Color.rgb(198, 191, 178),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRoundRect(RectF(w * 0.12f, h * 0.66f, w * 0.88f, h * 0.85f), 16f, 16f, paint)

                // Soft podium contact shadow
                paint.shader = null
                paint.color = Color.argb(35, 30, 30, 30)
                canvas.drawOval(RectF(w * 0.08f, h * 0.82f, w * 0.92f, h * 0.90f), paint)
            }

            MinimalistBackgroundStyle.CUSTOM -> {
                // Elegant soft neutral studio gradient
                paint.shader = RadialGradient(
                    w * 0.5f, h * 0.45f, w * 0.65f,
                    intArrayOf(Color.rgb(255, 253, 250), Color.rgb(239, 233, 225), Color.rgb(218, 210, 198)),
                    floatArrayOf(0f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
            }
        }

        return bitmap
    }

    /**
     * Composites an artisan product photo onto the Imagen background:
     * 1. Segments foreground craft item using color-distance and center-weighted thresholding.
     * 2. Renders realistic ambient occlusion & directional contact shadows.
     * 3. Scales and aligns the craft onto the minimalist backdrop.
     * 4. Applies tone-matching (exposure, warmth, contrast) so the product naturally matches the background.
     */
    suspend fun compositeProductOntoImagenBackground(
        context: Context,
        productBitmap: Bitmap,
        backgroundBitmap: Bitmap,
        shadowIntensity: Float = 0.65f,
        warmth: Float = 0.0f,
        exposure: Float = 0.0f
    ): Bitmap = withContext(Dispatchers.IO) {
        val targetW = backgroundBitmap.width
        val targetH = backgroundBitmap.height

        // 1. Extract foreground mask of the craft
        val (segmentedCraft, _) = extractForegroundCraft(productBitmap, targetW, targetH)

        // 2. Prepare composite canvas
        val resultBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Draw Imagen background
        val bgSrcRect = Rect(0, 0, backgroundBitmap.width, backgroundBitmap.height)
        val bgDstRect = Rect(0, 0, targetW, targetH)
        canvas.drawBitmap(backgroundBitmap, bgSrcRect, bgDstRect, paint)

        // 3. Realistic contact & ambient drop shadow
        val effectiveShadowAlpha = (shadowIntensity * 120f).toInt().coerceIn(0, 180)
        if (effectiveShadowAlpha > 0) {
            // Broad soft ambient occlusion shadow
            paint.color = Color.argb(effectiveShadowAlpha / 2, 20, 15, 12)
            canvas.drawOval(
                RectF(targetW * 0.18f, targetH * 0.72f, targetW * 0.82f, targetH * 0.86f),
                paint
            )

            // Tighter core contact shadow under the base of the product
            paint.color = Color.argb(effectiveShadowAlpha, 15, 10, 8)
            canvas.drawOval(
                RectF(targetW * 0.28f, targetH * 0.74f, targetW * 0.72f, targetH * 0.82f),
                paint
            )
        }

        // 4. Tone match craft: apply subtle warmth/exposure if needed
        val toneMatchedCraft = if (warmth != 0.0f || exposure != 0.0f) {
            applyToneMatching(segmentedCraft, warmth, exposure)
        } else {
            segmentedCraft
        }

        // 5. Draw the segmented craft product
        paint.color = Color.WHITE
        canvas.drawBitmap(toneMatchedCraft, 0f, 0f, paint)

        if (toneMatchedCraft != segmentedCraft) {
            toneMatchedCraft.recycle()
        }
        if (segmentedCraft != productBitmap) {
            segmentedCraft.recycle()
        }

        resultBitmap
    }

    /**
     * Extracts the foreground craft using color boundary difference & center proximity.
     */
    private fun extractForegroundCraft(
        src: Bitmap,
        targetW: Int,
        targetH: Int
    ): Pair<Bitmap, ByteArray> {
        // Resize source to fit target nicely
        val scale = min(targetW * 0.82f / src.width, targetH * 0.82f / src.height)
        val scaledW = (src.width * scale).toInt().coerceAtLeast(1)
        val scaledH = (src.height * scale).toInt().coerceAtLeast(1)

        val scaledSrc = Bitmap.createScaledBitmap(src, scaledW, scaledH, true)

        val total = scaledW * scaledH
        val pixels = IntArray(total)
        scaledSrc.getPixels(pixels, 0, scaledW, 0, 0, scaledW, scaledH)

        // Corner background sampling
        val c1 = pixels[0]
        val c2 = pixels[scaledW - 1]
        val c3 = pixels[(scaledH - 1) * scaledW]
        val c4 = pixels[total - 1]

        val bgR = (((c1 shr 16) and 0xFF) + ((c2 shr 16) and 0xFF) + ((c3 shr 16) and 0xFF) + ((c4 shr 16) and 0xFF)) / 4
        val bgG = (((c1 shr 8) and 0xFF) + ((c2 shr 8) and 0xFF) + ((c3 shr 8) and 0xFF) + ((c4 shr 8) and 0xFF)) / 4
        val bgB = ((c1 and 0xFF) + (c2 and 0xFF) + (c3 and 0xFF) + (c4 and 0xFF)) / 4

        val mask = ByteArray(total)
        val cx = scaledW * 0.5f
        val cy = scaledH * 0.5f
        val maxDist = sqrt((cx * cx + cy * cy).toDouble())

        for (y in 0 until scaledH) {
            for (x in 0 until scaledW) {
                val idx = y * scaledW + x
                val c = pixels[idx]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                val dr = (r - bgR).toDouble()
                val dg = (g - bgG).toDouble()
                val db = (b - bgB).toDouble()
                val colorDist = sqrt(dr * dr + dg * dg + db * db)

                val distFromCenter = sqrt(((x - cx) * (x - cx) + (y - cy) * (y - cy)).toDouble())
                val centerWeight = (1.0 - (distFromCenter / maxDist)).coerceIn(0.0, 1.0)

                val threshold = 34.0 + (centerWeight * 40.0)

                val alpha = when {
                    colorDist > threshold + 18.0 -> 255
                    colorDist < threshold -> 0
                    else -> (((colorDist - threshold) / 18.0) * 255.0).toInt().coerceIn(0, 255)
                }

                val newColor = (alpha shl 24) or (r shl 16) or (g shl 8) or b
                pixels[idx] = newColor
                mask[idx] = alpha.toByte()
            }
        }

        // Place on target-sized transparent bitmap
        val isolatedProduct = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val isolatedCanvas = Canvas(isolatedProduct)

        val posX = (targetW - scaledW) / 2f
        val posY = (targetH - scaledH) * 0.44f // Center slightly above bottom contact plane

        val tempBitmap = Bitmap.createBitmap(scaledW, scaledH, Bitmap.Config.ARGB_8888)
        tempBitmap.setPixels(pixels, 0, scaledW, 0, 0, scaledW, scaledH)

        val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        isolatedCanvas.drawBitmap(tempBitmap, posX, posY, p)

        tempBitmap.recycle()
        if (scaledSrc != src) scaledSrc.recycle()

        return Pair(isolatedProduct, mask)
    }

    /**
     * Tone matches product: adjustments for exposure and warm/cool temperature.
     */
    private fun applyToneMatching(src: Bitmap, warmth: Float, exposure: Float): Bitmap {
        val w = src.width
        val h = src.height
        val total = w * h
        val pixels = IntArray(total)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        val expFactor = 1.0f + exposure
        val warmR = (warmth * 24f).toInt()
        val warmB = (-warmth * 24f).toInt()

        for (i in 0 until total) {
            val c = pixels[i]
            val a = (c shr 24) and 0xFF
            if (a == 0) continue

            var r = (((c shr 16) and 0xFF) * expFactor).toInt() + warmR
            var g = (((c shr 8) and 0xFF) * expFactor).toInt()
            var b = ((c and 0xFF) * expFactor).toInt() + warmB

            r = r.coerceIn(0, 255)
            g = g.coerceIn(0, 255)
            b = b.coerceIn(0, 255)

            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, w, 0, 0, w, h)
        return result
    }

    /**
     * Complete pipeline: generates Imagen background, composites craft, and saves both to disk.
     */
    suspend fun processProductWithImagen(
        context: Context,
        productPhotoUri: String,
        style: MinimalistBackgroundStyle,
        customPrompt: String = "",
        craftCategory: String = "Handicraft",
        shadowIntensity: Float = 0.65f,
        warmth: Float = 0.0f,
        exposure: Float = 0.0f
    ): ImagenGenerationResult = withContext(Dispatchers.IO) {
        // Load original product photo
        val productBitmap = CraftImageStorageManager.loadBitmap(context, productPhotoUri, 1200)
            ?: throw IllegalArgumentException("Cannot load photo from URI: $productPhotoUri")

        // 1. Generate or synthesize background
        val (bgBitmap, isAi) = generateMinimalistBackground(context, style, customPrompt, craftCategory)

        // Save background to disk
        val bgFile = File(CraftImageStorageManager.getEnhancedDir(context), "bg_${style.id}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(bgFile).use { out ->
            bgBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        val bgUri = Uri.fromFile(bgFile).toString()

        // 2. Composite craft onto the minimalist background
        val compositedBitmap = compositeProductOntoImagenBackground(
            context = context,
            productBitmap = productBitmap,
            backgroundBitmap = bgBitmap,
            shadowIntensity = shadowIntensity,
            warmth = warmth,
            exposure = exposure
        )

        // 3. Save final enhanced composite
        val compositedUri = CraftImageStorageManager.saveEnhancedImage(context, compositedBitmap, 92)

        productBitmap.recycle()
        bgBitmap.recycle()
        compositedBitmap.recycle()

        val promptUsed = buildPrompt(style, customPrompt, craftCategory)
        val msg = if (isAi) {
            "Imagen 3 ने आपके शिल्प के लिए हाई-रिज़ॉल्यूशन मिनिमलिस्ट बैकग्राउंड तैयार किया।"
        } else {
            "मिनिमलिस्ट स्टूडियो बैकग्राउंड सफलतापूर्वक लागू किया गया।"
        }

        ImagenGenerationResult(
            backgroundUri = bgUri,
            compositedUri = compositedUri,
            style = style,
            usedPrompt = promptUsed,
            isAiGenerated = isAi,
            message = msg
        )
    }
}
