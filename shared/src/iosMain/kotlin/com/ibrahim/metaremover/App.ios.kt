package com.ibrahim.metaremover

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image

actual fun ByteArray.toImageBitmap(): ImageBitmap {
    return try {
        val skiaImage = Image.makeFromEncoded(this)
        val skiaBitmap = Bitmap.makeFromImage(skiaImage)
        skiaBitmap.asComposeImageBitmap()
    } catch (e: Exception) {
        ImageBitmap(1, 1)
    }
}
