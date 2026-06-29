package com.ibrahim.metaremover.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ibrahim.metaremover.domain.ImageCleaner
import java.io.ByteArrayOutputStream

class AndroidImageCleaner : ImageCleaner {
    override suspend fun clean(bytes: ByteArray): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
        val outputStream = ByteArrayOutputStream()
        // Compression is a CPU intensive task
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        return outputStream.toByteArray()
    }
}
