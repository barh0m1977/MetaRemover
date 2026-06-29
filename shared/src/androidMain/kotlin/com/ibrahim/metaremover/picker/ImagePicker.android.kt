package com.ibrahim.metaremover.picker

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

actual class ImagePicker {
    @Composable
    actual fun pickImage(result: (ByteArray?) -> Unit): () -> Unit {
        val context = LocalContext.current

        // 1. Corrected the spelling to 'rememberLauncherForActivityResult'
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                // 2. Read the Uri bytes and pass them to the callback
                val bytes = readBytesFromUri(context, uri)
                result(bytes)
            } else {
                result(null)
            }
        }

        // 3. Return a lambda that triggers the launcher when clicked
        return {
            launcher.launch("image/*")
        }
    }

    // Helper function to convert Android Uri to ByteArray
    private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}