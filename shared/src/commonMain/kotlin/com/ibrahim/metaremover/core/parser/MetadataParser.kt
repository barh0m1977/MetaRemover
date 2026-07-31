package com.ibrahim.metaremover.core.parser

import com.ibrahim.metaremover.domain.model.ImageAnalysis

interface MetadataParser {
    fun canParse(bytes: ByteArray): Boolean
    suspend fun parse(bytes: ByteArray): ImageAnalysis
    suspend fun clean(bytes: ByteArray): ByteArray
}
