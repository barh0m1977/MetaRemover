package com.ibrahim.metaremover.picker

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

expect class ImagePicker() {
    @Composable
    fun pickImage(result: (ByteArray?) -> Unit):()-> Unit
}