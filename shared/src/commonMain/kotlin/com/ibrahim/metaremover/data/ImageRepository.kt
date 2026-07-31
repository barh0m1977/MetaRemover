package com.ibrahim.metaremover.data

import com.ibrahim.metaremover.domain.CleanResult
import com.ibrahim.metaremover.domain.ImageMetadata
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import com.ibrahim.metaremover.domain.MetadataCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageRepository(
    private val analyzer: MetadataAnalyzer,
    private val cleaner: MetadataCleaner
) {
    suspend fun analyze(bytes: ByteArray): ImageMetadata = withContext(Dispatchers.Default) {
        analyzer.analyze(bytes)
    }

    suspend fun clean(bytes: ByteArray): CleanResult = withContext(Dispatchers.Default) {
        cleaner.clean(bytes)
    }
}
