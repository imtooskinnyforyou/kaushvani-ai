package com.example.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import com.example.data.ai.utils.ImageProcessingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * Storage metrics summary for telemetry and storage management.
 */
data class ImageStorageMetrics(
    val originalCount: Int,
    val enhancedCount: Int,
    val thumbnailCount: Int,
    val totalSizeBytes: Long,
    val totalSizeMegaBytes: Double
)

/**
 * Robust on-device persistent image storage manager for KAARIGAR.
 *
 * Handles:
 * 1. Temporary file and URI creation for CameraX and image pickers.
 * 2. High-quality compression (adaptive JPEG quality with EXIF orientation preservation).
 * 3. Immutable original image preservation (`craft_images/original/`).
 * 4. High-resolution studio enhanced image persistence (`craft_images/enhanced/`).
 * 5. Optimized lightweight thumbnail generation (`craft_images/thumbnails/`).
 * 6. Export file generation for marketplace sharing (`craft_images/exports/`).
 * 7. Storage cleanup and garbage collection of stale temporary captures.
 * 8. Guaranteed `file://` URI scheme resolution for local Room and Cloud synchronization.
 */
object CraftImageStorageManager {

    private const val TAG = "CraftImageStorageMgr"
    private const val DIR_ORIGINALS = "craft_images/original"
    private const val DIR_ENHANCED = "craft_images/enhanced"
    private const val DIR_THUMBNAILS = "craft_images/thumbnails"
    private const val DIR_EXPORTS = "craft_images/exports"
    private const val DIR_TEMP = "craft_images/temp"

    fun getOriginalsDir(context: Context): File {
        val dir = File(context.filesDir, DIR_ORIGINALS)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getEnhancedDir(context: Context): File {
        val dir = File(context.filesDir, DIR_ENHANCED)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getThumbnailsDir(context: Context): File {
        val dir = File(context.filesDir, DIR_THUMBNAILS)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getExportsDir(context: Context): File {
        val dir = File(context.filesDir, DIR_EXPORTS)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getTempDir(context: Context): File {
        val dir = File(context.cacheDir, DIR_TEMP)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Creates a temporary image file in cache storage for CameraX capture or image picking.
     */
    fun createTempImageFile(
        context: Context,
        prefix: String = "craft_capture_",
        extension: String = ".jpg"
    ): File {
        val tempDir = getTempDir(context)
        val timestamp = System.currentTimeMillis()
        val randomSuffix = UUID.randomUUID().toString().take(6)
        return File(tempDir, "${prefix}${timestamp}_${randomSuffix}$extension")
    }

    /**
     * Creates a temporary image URI for camera outputs.
     */
    fun createTempImageUri(
        context: Context,
        prefix: String = "craft_capture_"
    ): Uri {
        val file = createTempImageFile(context, prefix)
        return Uri.fromFile(file)
    }

    /**
     * Preserves the original raw capture/upload to persistent internal storage.
     * Guaranteed to never be overwritten.
     */
    suspend fun saveOriginalImage(context: Context, sourceUriString: String): String = withContext(Dispatchers.IO) {
        if (sourceUriString.isBlank()) return@withContext ""
        try {
            val originalsDir = getOriginalsDir(context)
            val fileName = "orig_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val targetFile = File(originalsDir, fileName)

            if (sourceUriString.startsWith("sample_") || !sourceUriString.contains("://")) {
                // Generate and save authentic sample bitmap to disk
                val sampleBitmap = generateAuthenticCraftSampleBitmap(sourceUriString)
                FileOutputStream(targetFile).use { out ->
                    sampleBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    out.flush()
                }
                sampleBitmap.recycle()
                return@withContext Uri.fromFile(targetFile).toString()
            }

            val uri = Uri.parse(sourceUriString)
            if (uri.scheme == "file") {
                val srcFile = File(uri.path ?: "")
                if (srcFile.exists()) {
                    srcFile.copyTo(targetFile, overwrite = true)
                    return@withContext Uri.fromFile(targetFile).toString()
                }
            }

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                FileOutputStream(targetFile).use { out ->
                    inputStream.copyTo(out)
                    out.flush()
                }
                inputStream.close()
                return@withContext Uri.fromFile(targetFile).toString()
            }

            // Fallback: decode & re-compress
            val bitmap = ImageProcessingUtils.loadAndOptimizeBitmap(context, sourceUriString, 1440)
            if (bitmap != null) {
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    out.flush()
                }
                bitmap.recycle()
                return@withContext Uri.fromFile(targetFile).toString()
            }

            sourceUriString
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save original image: ${e.message}", e)
            sourceUriString
        }
    }

    /**
     * Saves the rendered, studio-enhanced bitmap to persistent internal storage.
     * Returns a valid `file://` URI string ready for local DB and cloud synchronization.
     */
    suspend fun saveEnhancedImage(
        context: Context,
        bitmap: Bitmap,
        quality: Int = 92
    ): String = withContext(Dispatchers.IO) {
        try {
            val enhancedDir = getEnhancedDir(context)
            val fileName = "enhanced_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val targetFile = File(enhancedDir, fileName)

            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(50, 100), out)
                out.flush()
            }
            Uri.fromFile(targetFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save enhanced image: ${e.message}", e)
            ""
        }
    }

    /**
     * Generates a lightweight, fast-loading thumbnail for catalog grids and inventory views.
     */
    suspend fun createProductThumbnail(
        context: Context,
        sourceUriString: String,
        maxDimension: Int = 360,
        quality: Int = 85
    ): String = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmap(context, sourceUriString, maxDimension = maxDimension)
                ?: return@withContext sourceUriString

            val thumbDir = getThumbnailsDir(context)
            val fileName = "thumb_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val targetFile = File(thumbDir, fileName)

            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.flush()
            }
            bitmap.recycle()
            Uri.fromFile(targetFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create thumbnail: ${e.message}", e)
            sourceUriString
        }
    }

    /**
     * Exports a pristine, high-resolution copy of a product image for external marketplace sharing (ONDC/WhatsApp/Instagram).
     */
    suspend fun exportCatalogImage(
        context: Context,
        sourceUriString: String,
        quality: Int = 95
    ): File? = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmap(context, sourceUriString, maxDimension = 1920) ?: return@withContext null
            val exportsDir = getExportsDir(context)
            val fileName = "catalog_export_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(exportsDir, fileName)

            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.flush()
            }
            bitmap.recycle()
            targetFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export catalog image: ${e.message}", e)
            null
        }
    }

    /**
     * Loads a Bitmap from file, content resolver, or sample Uri.
     */
    suspend fun loadBitmap(
        context: Context,
        uriString: String,
        maxDimension: Int = 1080
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (uriString.isBlank()) return@withContext null
        if (uriString.startsWith("sample_") || !uriString.contains("://")) {
            return@withContext generateAuthenticCraftSampleBitmap(uriString)
        }
        ImageProcessingUtils.loadAndOptimizeBitmap(context, uriString, maxDimension)
    }

    /**
     * Safely deletes an image file if it is stored in our internal managed directories.
     */
    suspend fun deleteManagedImage(context: Context, uriString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")
                if (file.exists() && file.canonicalPath.startsWith(context.filesDir.canonicalPath)) {
                    return@withContext file.delete()
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image: ${e.message}", e)
            false
        }
    }

    /**
     * Cleans up stale temporary files older than the specified duration.
     */
    suspend fun cleanupTempFiles(
        context: Context,
        olderThanMillis: Long = 24 * 60 * 60 * 1000L
    ): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        try {
            val tempDir = getTempDir(context)
            val cutoff = System.currentTimeMillis() - olderThanMillis
            tempDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoff) {
                    if (file.delete()) deletedCount++
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Temp file cleanup failed: ${e.message}")
        }
        deletedCount
    }

    /**
     * Computes storage usage metrics across originals, enhanced photos, and thumbnails.
     */
    suspend fun getStorageMetrics(context: Context): ImageStorageMetrics = withContext(Dispatchers.IO) {
        var origCount = 0
        var enhancedCount = 0
        var thumbCount = 0
        var totalBytes = 0L

        fun scanDir(dir: File, onFile: (File) -> Unit) {
            dir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    totalBytes += file.length()
                    onFile(file)
                }
            }
        }

        scanDir(getOriginalsDir(context)) { origCount++ }
        scanDir(getEnhancedDir(context)) { enhancedCount++ }
        scanDir(getThumbnailsDir(context)) { thumbCount++ }

        ImageStorageMetrics(
            originalCount = origCount,
            enhancedCount = enhancedCount,
            thumbnailCount = thumbCount,
            totalSizeBytes = totalBytes,
            totalSizeMegaBytes = totalBytes / (1024.0 * 1024.0)
        )
    }

    /**
     * Generates a high-fidelity authentic handicraft sample bitmap when built-in samples are selected.
     */
    fun generateAuthenticCraftSampleBitmap(sampleId: String, width: Int = 1080, height: Int = 1080): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when {
            sampleId.contains("terracotta") || sampleId.contains("pottery") -> {
                // Background: rustic workshop floor
                paint.color = Color.rgb(220, 205, 190)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Workshop shadow
                paint.color = Color.argb(80, 80, 50, 40)
                canvas.drawOval(RectF(width * 0.18f, height * 0.72f, width * 0.82f, height * 0.86f), paint)

                // Terracotta Pot Body
                paint.color = Color.rgb(186, 75, 45)
                val path = Path().apply {
                    moveTo(width * 0.5f, height * 0.22f)
                    cubicTo(width * 0.36f, height * 0.22f, width * 0.28f, height * 0.32f, width * 0.25f, height * 0.44f)
                    cubicTo(width * 0.20f, height * 0.58f, width * 0.25f, height * 0.74f, width * 0.50f, height * 0.76f)
                    cubicTo(width * 0.75f, height * 0.74f, width * 0.80f, height * 0.58f, width * 0.75f, height * 0.44f)
                    cubicTo(width * 0.72f, height * 0.32f, width * 0.64f, height * 0.22f, width * 0.50f, height * 0.22f)
                    close()
                }
                canvas.drawPath(path, paint)

                // Highlights & Texture
                paint.color = Color.rgb(215, 110, 75)
                canvas.drawOval(RectF(width * 0.35f, height * 0.30f, width * 0.55f, height * 0.55f), paint)

                // Pot Rim
                paint.color = Color.rgb(150, 55, 30)
                canvas.drawRoundRect(RectF(width * 0.36f, height * 0.18f, width * 0.64f, height * 0.24f), 16f, 16f, paint)

                // Traditional Carved Belt
                paint.color = Color.rgb(245, 230, 210)
                paint.strokeWidth = 10f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(width * 0.26f, height * 0.48f, width * 0.74f, height * 0.48f, paint)
                paint.style = Paint.Style.FILL
            }
            sampleId.contains("banarasi") || sampleId.contains("textile") || sampleId.contains("silk") -> {
                // Royal Crimson Silk with Gold Zari
                paint.color = Color.rgb(180, 20, 50)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Gold Zari Motifs
                paint.color = Color.rgb(235, 185, 60)
                for (x in 100..width - 100 step 200) {
                    for (y in 100..height - 100 step 200) {
                        canvas.drawCircle(x.toFloat(), y.toFloat(), 35f, paint)
                        canvas.drawOval(RectF(x - 60f, y - 15f, x + 60f, y + 15f), paint)
                    }
                }

                // Zari Brocade Border
                paint.color = Color.rgb(215, 165, 40)
                canvas.drawRect(0f, height * 0.78f, width.toFloat(), height.toFloat(), paint)
            }
            sampleId.contains("dhokra") || sampleId.contains("metal") || sampleId.contains("brass") -> {
                // Antique Bronze Dhokra Figurine
                paint.color = Color.rgb(40, 36, 32)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.color = Color.rgb(210, 160, 50)
                canvas.drawRoundRect(RectF(width * 0.30f, height * 0.30f, width * 0.70f, height * 0.70f), 24f, 24f, paint)
                paint.color = Color.rgb(160, 110, 30)
                canvas.drawCircle(width * 0.5f, height * 0.25f, 70f, paint)
            }
            else -> {
                // Madhubani / Folk Art Canvas
                paint.color = Color.rgb(250, 240, 215)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.color = Color.rgb(180, 40, 30)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 20f
                canvas.drawRoundRect(RectF(40f, 40f, width - 40f, height - 40f), 20f, 20f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.rgb(220, 90, 40)
                canvas.drawCircle(width * 0.5f, height * 0.5f, 180f, paint)
                paint.color = Color.rgb(245, 185, 45)
                canvas.drawCircle(width * 0.5f, height * 0.5f, 120f, paint)
            }
        }

        return bitmap
    }
}

