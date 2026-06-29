package com.ibrahim.metaremover.data

import com.ibrahim.metaremover.domain.ImageCleaner
import com.ibrahim.metaremover.domain.ImageMetadata
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageRepository(
    private val analyzer: MetadataAnalyzer,
    private val cleaner: ImageCleaner
) {
    suspend fun analyze(bytes: ByteArray): ImageMetadata = withContext(Dispatchers.Default) {
        analyzer.analyze(bytes)
    }

    suspend fun clean(bytes: ByteArray): ByteArray = withContext(Dispatchers.Default) {
        cleaner.clean(bytes)
    }
}
