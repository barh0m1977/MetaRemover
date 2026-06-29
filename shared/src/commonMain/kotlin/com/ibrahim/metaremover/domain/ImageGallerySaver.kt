package com.ibrahim.metaremover.domain

interface ImageGallerySaver {
    suspend fun saveImage(bytes: ByteArray, fileName: String): Boolean
}
