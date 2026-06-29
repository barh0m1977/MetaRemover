package com.ibrahim.metaremover.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.ibrahim.metaremover.domain.ImageGallerySaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidImageGallerySaver(private val context: Context) : ImageGallerySaver {
    override suspend fun saveImage(bytes: ByteArray, fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MetaRemover")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext false

            resolver.openOutputStream(imageUri)?.use { outputStream ->
                outputStream.write(bytes)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
