package com.ibrahim.metaremover.domain

interface MetadataAnalyzer {
    suspend fun analyze(bytes: ByteArray): ImageMetadata
}
