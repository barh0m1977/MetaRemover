package com.ibrahim.metaremover.core.parser

import com.ibrahim.metaremover.domain.model.ImageAnalysis

class ParserManager(private val parsers: List<MetadataParser>) {
    
    suspend fun analyze(bytes: ByteArray): ImageAnalysis {
        val parser = parsers.find { it.canParse(bytes) }
        return parser?.parse(bytes) ?: ImageAnalysis()
    }

    suspend fun clean(bytes: ByteArray): ByteArray {
        val parser = parsers.find { it.canParse(bytes) }
        return parser?.clean(bytes) ?: bytes
    }
}
