package com.ibrahim.metaremover.picker

import android.content.Context
import android.net.Uri
import android.util.Log.e
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class ImagePicker {
    @Composable
    actual fun pickImage(result: (ByteArray?) -> Unit): () -> Unit {
        val context = LocalContext.current

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) result(readBytesFromUri(context, uri)) else result(null)
        }

        return {
            try {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (e: Exception) {
                e.printStackTrace()
                result(null)
            }
        }
    }

    private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            android.util.Log.e("ImagePicker", "Failed to read image", e)
            null
        }
    }
}
