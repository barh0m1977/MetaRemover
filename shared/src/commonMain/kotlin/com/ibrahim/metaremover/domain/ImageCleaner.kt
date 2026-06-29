package com.ibrahim.metaremover.domain

interface ImageCleaner {
    suspend fun clean(bytes: ByteArray): ByteArray
}
