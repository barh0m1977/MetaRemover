package com.ibrahim.metaremover.domain.repository

import com.ibrahim.metaremover.domain.CleanResult
import com.ibrahim.metaremover.domain.model.ImageAnalysis

interface ImageRepository {
    suspend fun analyze(bytes: ByteArray): ImageAnalysis
    suspend fun clean(bytes: ByteArray): CleanResult
    // suspend fun compare(original: ByteArray, cleaned: ByteArray): ComparisonResult
}
