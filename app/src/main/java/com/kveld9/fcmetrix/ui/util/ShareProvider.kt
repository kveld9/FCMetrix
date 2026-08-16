package com.kveld9.fcmetrix.ui.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ShareProvider {

    /**
     * Shares the bitmap using the standard Android share sheet.
     */
    fun shareBitmap(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "shared_images")
            cachePath.mkdirs()
            val file = File(cachePath, "grl_result.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share GRL Result"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Saves the bitmap to the public Gallery (Pictures folder).
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
        val filename = "FCM_OVR_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/FCMetrix")
                    }
                    imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir = File(context.getExternalFilesDir(null), "FCMetrix")
                if (!imagesDir.exists()) imagesDir.mkdirs()
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
