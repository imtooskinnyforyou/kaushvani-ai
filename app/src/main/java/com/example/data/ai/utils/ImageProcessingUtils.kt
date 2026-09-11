package com.example.data.ai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Standard aspect ratio presets required for professional craft e-commerce catalogs.
 */
enum class CatalogAspectRatio(
    val ratioWidth: Int,
    val ratioHeight: Int,
    val ratioValue: Float, // width / height
    val displayName: String,
    val description: String
) {
    SQUARE_1_1(1, 1, 1.0f, "1:1 Square", "Amazon, Flipkart & ONDC Marketplace Primary Hero Image"),
    PORTRAIT_4_5(4, 5, 0.8f, "4:5 Portrait", "Social Commerce, Instagram Showcase & Mobile Feed"),
    PORTRAIT_3_4(3, 4, 0.75f, "3:4 Catalog", "Handloom Textiles, Saree, Apparel & Tall Craft"),
    LANDSCAPE_4_3(4, 3, 1.3333334f, "4:3 Landscape", "Wide Dhokra Art, Sculptures & Workshop Displays"),
    BANNER_16_9(16, 9, 1.7777778f, "16:9 Banner", "Storefront Banner & Craft Story Header")
}

/**
 * Real image processing utility for KAARIGAR.
 *
 * Implements direct on-device pixel-manipulation techniques:
 * 1. Brightness Adjustment (LUT-based offset clamping).
 * 2. Contrast Adjustment (Midpoint S-curve and linear expansion).
 * 3. Saturation & Vibrance (Luma-weighted chroma enhancement).
 * 4. Unsharp Masking (3x3 Laplacian edge convolution filter).
 * 5. Auto-Crop to 1:1, 4:5, 3:4 using subject saliency / center-of-mass detection.
 * 6. EXIF orientation correction, downscaling, and Base64 serialization.
 */
object ImageProcessingUtils {

    /**
     * Adjusts image brightness using direct pixel-level manipulation.
     * @param brightnessOffset Range: -1.0f (darkest) to 1.0f (brightest), where 0.0f is unchanged.
     */
    fun adjustBrightness(source: Bitmap, brightnessOffset: Float): Bitmap {
        if (brightnessOffset == 0f) return source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)

        val width = source.width
        val height = source.height
        val totalPixels = width * height
        val pixels = IntArray(totalPixels)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val delta = (brightnessOffset * 255f).roundToInt()
        val lut = IntArray(256)
        for (i in 0..255) {
            lut[i] = (i + delta).coerceIn(0, 255)
        }

        for (i in 0 until totalPixels) {
            val c = pixels[i]
            val a = (c ushr 24) and 0xFF
            val r = lut[(c shr 16) and 0xFF]
            val g = lut[(c shr 8) and 0xFF]
            val b = lut[c and 0xFF]
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Adjusts image contrast centered around middle gray (128) using direct pixel-level manipulation.
     * @param contrastFactor Range: -1.0f (flat gray) to 1.0f (high dynamic range), where 0.0f is unchanged.
     */
    fun adjustContrast(source: Bitmap, contrastFactor: Float): Bitmap {
        if (contrastFactor == 0f) return source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)

        val width = source.width
        val height = source.height
        val totalPixels = width * height
        val pixels = IntArray(totalPixels)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val factor = 1.0f + contrastFactor
        val lut = IntArray(256)
        for (i in 0..255) {
            val normalized = (i - 128) * factor + 128
            lut[i] = normalized.roundToInt().coerceIn(0, 255)
        }

        for (i in 0 until totalPixels) {
            val c = pixels[i]
            val a = (c ushr 24) and 0xFF
            val r = lut[(c shr 16) and 0xFF]
            val g = lut[(c shr 8) and 0xFF]
            val b = lut[c and 0xFF]
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Adjusts brightness, contrast, and saturation simultaneously in a high-performance single pixel pass.
     *
     * @param brightness -1.0f to 1.0f (0.0f = neutral)
     * @param contrast -1.0f to 1.0f (0.0f = neutral)
     * @param saturation 0.0f (monochrome) to 2.0f (vivid), where 1.0f is neutral
     */
    fun adjustBrightnessAndContrast(
        source: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float = 1.0f
    ): Bitmap {
        val width = source.width
        val height = source.height
        val totalPixels = width * height
        val pixels = IntArray(totalPixels)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val contrastFactor = 1.0f + contrast
        val brightnessDelta = (brightness * 255f)
        val lut = IntArray(256)
        for (i in 0..255) {
            val contrasted = (i - 128) * contrastFactor + 128 + brightnessDelta
            lut[i] = contrasted.roundToInt().coerceIn(0, 255)
        }

        val applySaturation = abs(saturation - 1.0f) > 0.01f

        for (i in 0 until totalPixels) {
            val c = pixels[i]
            val a = (c ushr 24) and 0xFF
            var r = lut[(c shr 16) and 0xFF]
            var g = lut[(c shr 8) and 0xFF]
            var b = lut[c and 0xFF]

            if (applySaturation) {
                val luma = (0.299f * r + 0.587f * g + 0.114f * b)
                r = (luma + saturation * (r - luma)).roundToInt().coerceIn(0, 255)
                g = (luma + saturation * (g - luma)).roundToInt().coerceIn(0, 255)
                b = (luma + saturation * (b - luma)).roundToInt().coerceIn(0, 255)
            }

            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Applies a 3x3 unsharp mask convolution kernel to enhance edges and micro-textures in handcrafted goods.
     * @param amount 0.0f (no sharpening) to 1.0f (maximum crispness)
     */
    fun applyUnsharpMask(source: Bitmap, amount: Float): Bitmap {
        if (amount <= 0.02f) return source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)

        val width = source.width
        val height = source.height
        val totalPixels = width * height
        val srcPixels = IntArray(totalPixels)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)

        val dstPixels = IntArray(totalPixels)

        val weight = amount * 0.5f
        val center = 1.0f + 4.0f * weight

        for (y in 1 until height - 1) {
            val rowOffset = y * width
            val upOffset = (y - 1) * width
            val downOffset = (y + 1) * width

            for (x in 1 until width - 1) {
                val idx = rowOffset + x
                val c = srcPixels[idx]
                val u = srcPixels[upOffset + x]
                val d = srcPixels[downOffset + x]
                val l = srcPixels[rowOffset + (x - 1)]
                val r = srcPixels[rowOffset + (x + 1)]

                val a = (c ushr 24) and 0xFF

                val cR = (c shr 16) and 0xFF
                val cG = (c shr 8) and 0xFF
                val cB = c and 0xFF

                val uR = (u shr 16) and 0xFF
                val uG = (u shr 8) and 0xFF
                val uB = u and 0xFF

                val dR = (d shr 16) and 0xFF
                val dG = (d shr 8) and 0xFF
                val dB = d and 0xFF

                val lR = (l shr 16) and 0xFF
                val lG = (l shr 8) and 0xFF
                val lB = l and 0xFF

                val rR = (r shr 16) and 0xFF
                val rG = (r shr 8) and 0xFF
                val rB = r and 0xFF

                val sharpR = (center * cR - weight * (uR + dR + lR + rR)).roundToInt().coerceIn(0, 255)
                val sharpG = (center * cG - weight * (uG + dG + lG + rG)).roundToInt().coerceIn(0, 255)
                val sharpB = (center * cB - weight * (uB + dB + lB + rB)).roundToInt().coerceIn(0, 255)

                dstPixels[idx] = (a shl 24) or (sharpR shl 16) or (sharpG shl 8) or sharpB
            }
        }

        // Copy borders
        for (x in 0 until width) {
            dstPixels[x] = srcPixels[x]
            dstPixels[(height - 1) * width + x] = srcPixels[(height - 1) * width + x]
        }
        for (y in 0 until height) {
            dstPixels[y * width] = srcPixels[y * width]
            dstPixels[y * width + (width - 1)] = srcPixels[y * width + (width - 1)]
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(dstPixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Calculates the normalized center of mass (cx, cy) in range [0.0..1.0] of the craft artifact
     * by evaluating pixel chrominance and contrast from background edges.
     */
    fun detectSubjectCenterOfMass(source: Bitmap): Pair<Float, Float> {
        val width = source.width
        val height = source.height
        val totalPixels = width * height

        // Downsample step for fast calculation
        val step = max(1, width / 200)
        val pixels = IntArray(totalPixels)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Corner background color estimate
        val corner1 = pixels[0]
        val corner2 = pixels[width - 1]
        val corner3 = pixels[(height - 1) * width]
        val corner4 = pixels[totalPixels - 1]

        val bgR = (((corner1 shr 16) and 0xFF) + ((corner2 shr 16) and 0xFF) + ((corner3 shr 16) and 0xFF) + ((corner4 shr 16) and 0xFF)) / 4
        val bgG = (((corner1 shr 8) and 0xFF) + ((corner2 shr 8) and 0xFF) + ((corner3 shr 8) and 0xFF) + ((corner4 shr 8) and 0xFF)) / 4
        val bgB = ((corner1 and 0xFF) + (corner2 and 0xFF) + (corner3 and 0xFF) + (corner4 and 0xFF)) / 4

        var sumMassX = 0.0
        var sumMassY = 0.0
        var totalMass = 0.0

        for (y in 0 until height step step) {
            val rowOffset = y * width
            for (x in 0 until width step step) {
                val c = pixels[rowOffset + x]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                val dr = (r - bgR).toDouble()
                val dg = (g - bgG).toDouble()
                val db = (b - bgB).toDouble()
                val dist = sqrt(dr * dr + dg * dg + db * db)

                if (dist > 30.0) {
                    val mass = dist - 30.0
                    sumMassX += x * mass
                    sumMassY += y * mass
                    totalMass += mass
                }
            }
        }

        if (totalMass <= 0.0) {
            return Pair(0.5f, 0.5f)
        }

        val normX = ((sumMassX / totalMass) / width).toFloat().coerceIn(0.15f, 0.85f)
        val normY = ((sumMassY / totalMass) / height).toFloat().coerceIn(0.15f, 0.85f)
        return Pair(normX, normY)
    }

    /**
     * Automatically crops an image to standard e-commerce catalog aspect ratios (1:1, 4:5, 3:4, etc.)
     * centered intelligently around the subject's center of mass.
     *
     * @param source The input photo.
     * @param ratioPreset The target catalog aspect ratio (e.g. SQUARE_1_1, PORTRAIT_4_5, PORTRAIT_3_4).
     * @param targetResolution Standard catalog resolution (e.g. 1080px for standard e-commerce).
     */
    fun autoCropToAspectRatio(
        source: Bitmap,
        ratioPreset: CatalogAspectRatio,
        targetResolution: Int = 1080
    ): Bitmap {
        val (centerXNorm, centerYNorm) = detectSubjectCenterOfMass(source)
        return cropWithFocalPoint(source, ratioPreset.ratioValue, centerXNorm, centerYNorm, targetResolution)
    }

    /**
     * Crops image to target aspect ratio using specified normalized focal point (0.0 to 1.0).
     */
    fun cropWithFocalPoint(
        source: Bitmap,
        aspectRatioValue: Float, // width / height
        focalPointXNorm: Float = 0.5f,
        focalPointYNorm: Float = 0.5f,
        targetResolution: Int = 1080
    ): Bitmap {
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()
        val srcRatio = srcW / srcH

        val cropW: Float
        val cropH: Float

        if (srcRatio > aspectRatioValue) {
            // Source is wider than target ratio: crop horizontally
            cropH = srcH
            cropW = srcH * aspectRatioValue
        } else {
            // Source is taller than target ratio: crop vertically
            cropW = srcW
            cropH = srcW / aspectRatioValue
        }

        // Center around focal point, clamped safely inside original bitmap bounds
        val desiredLeft = (focalPointXNorm * srcW) - (cropW / 2f)
        val desiredTop = (focalPointYNorm * srcH) - (cropH / 2f)

        val cropLeft = desiredLeft.coerceIn(0f, max(0f, srcW - cropW))
        val cropTop = desiredTop.coerceIn(0f, max(0f, srcH - cropH))

        val srcRect = Rect(
            cropLeft.toInt(),
            cropTop.toInt(),
            (cropLeft + cropW).toInt(),
            (cropTop + cropH).toInt()
        )

        val outWidth: Int
        val outHeight: Int
        if (aspectRatioValue >= 1.0f) {
            outWidth = targetResolution
            outHeight = (targetResolution / aspectRatioValue).roundToInt()
        } else {
            outHeight = targetResolution
            outWidth = (targetResolution * aspectRatioValue).roundToInt()
        }

        val outBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val dstRect = Rect(0, 0, outWidth, outHeight)

        canvas.drawBitmap(source, srcRect, dstRect, paint)
        return outBitmap
    }

    /**
     * Complete one-stop catalog processing pipeline:
     * 1. Auto-crop to 1:1, 4:5, or 3:4 aspect ratio around artifact center-of-mass.
     * 2. Pixel brightness, contrast, and saturation tone enhancement.
     * 3. Texture unsharp mask sharpening.
     */
    fun processCatalogImage(
        source: Bitmap,
        ratioPreset: CatalogAspectRatio,
        brightness: Float = 0.0f,
        contrast: Float = 0.0f,
        sharpness: Float = 0.0f,
        targetResolution: Int = 1080
    ): Bitmap {
        // Step 1: Auto-Crop to ratio
        val cropped = autoCropToAspectRatio(source, ratioPreset, targetResolution)

        // Step 2: Brightness & Contrast & Vibrance
        val adjusted = if (brightness != 0f || contrast != 0f) {
            val enhanced = adjustBrightnessAndContrast(
                source = cropped,
                brightness = brightness,
                contrast = contrast,
                saturation = 1.0f + (contrast * 0.3f)
            )
            if (enhanced != cropped) cropped.recycle()
            enhanced
        } else {
            cropped
        }

        // Step 3: Sharpness
        val finalResult = if (sharpness > 0.02f) {
            val sharpened = applyUnsharpMask(adjusted, sharpness)
            if (sharpened != adjusted) adjusted.recycle()
            sharpened
        } else {
            adjusted
        }

        return finalResult
    }

    /**
     * Safely decodes and downscales a Bitmap from a URI or File path.
     * Keeps max dimension around 1024px to balance Gemini multimodal fidelity with payload speed.
     */
    fun loadAndOptimizeBitmap(context: Context, uriString: String?, maxDimension: Int = 1024): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            
            // First pass: inspect dimensions
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return null

            // Calculate sample size
            val maxSide = max(srcWidth, srcHeight)
            var sampleSize = 1
            while (maxSide / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }

            // Second pass: decode sample
            inputStream = context.contentResolver.openInputStream(uri)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val sampledBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (sampledBitmap == null) return null

            // Correct orientation from EXIF if needed
            val orientation = getExifOrientation(context, uri)
            val rotatedBitmap = rotateBitmapIfNeeded(sampledBitmap, orientation)

            // Final scaling if still larger than maxDimension
            scaleBitmapDown(rotatedBitmap, maxDimension)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Compresses and saves a Bitmap to a persistent local file with specified compression quality.
     * Replaces dummy in-memory placeholders with real filesystem persistence.
     */
    fun compressAndSaveBitmapToFile(
        context: Context,
        bitmap: Bitmap,
        targetFile: java.io.File? = null,
        quality: Int = 88,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): java.io.File {
        val destFile = targetFile ?: run {
            val uploadDir = java.io.File(context.cacheDir, "catalog_uploads").apply { if (!exists()) mkdirs() }
            java.io.File(uploadDir, "upload_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}.jpg")
        }

        java.io.FileOutputStream(destFile).use { outStream ->
            bitmap.compress(format, quality.coerceIn(30, 100), outStream)
            outStream.flush()
        }
        return destFile
    }

    /**
     * Reads an image URI, corrects orientation, downscales to maximum dimension,
     * compresses to a high-quality JPEG file, and returns the persisted File object ready for upload.
     */
    fun compressImageForUpload(
        context: Context,
        sourceUriString: String?,
        maxDimension: Int = 1200,
        quality: Int = 88
    ): java.io.File? {
        if (sourceUriString.isNullOrBlank()) return null
        val bitmap = loadAndOptimizeBitmap(context, sourceUriString, maxDimension) ?: return null
        return try {
            compressAndSaveBitmapToFile(context, bitmap, quality = quality)
        } finally {
            bitmap.recycle()
        }
    }

    /**
     * Converts a Bitmap to a base64 encoded JPEG string.
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 80): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxSide = max(width, height)
        if (maxSide <= maxDimension) return bitmap

        val ratio = maxDimension.toFloat() / maxSide
        val targetWidth = (width * ratio).toInt()
        val targetHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun getExifOrientation(context: Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()
            orientation
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

