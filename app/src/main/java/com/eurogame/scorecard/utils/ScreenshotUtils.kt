package com.eurogame.scorecard.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ScreenshotUtils {
    /**
     * Saves an ImageBitmap to the device's Pictures directory
     * @param context Application context
     * @param bitmap The ImageBitmap to save
     * @param fileName Base name for the file (timestamp will be appended)
     * @return Result with success/failure message
     */
    suspend fun saveImageToGallery(
        context: Context,
        bitmap: ImageBitmap,
        fileName: String = "scorecard"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val androidBitmap = bitmap.asAndroidBitmap()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val displayName = "${fileName}_$timestamp.png"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.WIDTH, androidBitmap.width)
                put(MediaStore.Images.Media.HEIGHT, androidBitmap.height)

                // For Android 10 and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/EuroGameScorecard")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val imageUri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return@withContext Result.failure(
                IOException("Failed to create MediaStore entry")
            )

            // Write the bitmap to the output stream
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            } ?: return@withContext Result.failure(
                IOException("Failed to open output stream")
            )

            // Mark as not pending (for Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }

            Result.success("Scorecard saved to Pictures/EuroGameScorecard/$displayName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
