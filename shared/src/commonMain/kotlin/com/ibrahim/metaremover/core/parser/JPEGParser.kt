package com.ibrahim.metaremover.core.parser

import com.ibrahim.metaremover.domain.model.ImageAnalysis

class JPEGParser : MetadataParser {
    override fun canParse(bytes: ByteArray): Boolean =
        bytes.size > 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()

    override suspend fun parse(bytes: ByteArray): ImageAnalysis {
        // Implement detailed JPEG parsing or delegate to specialized analyzers
        return ImageAnalysis()
    }

    override suspend fun clean(bytes: ByteArray): ByteArray {
        val result = mutableListOf<Byte>()
        result.add(0xFF.toByte()); result.add(0xD8.toByte())
        var i = 2
        while (i < bytes.size - 1) {
            if (bytes[i] == 0xFF.toByte()) {
                val marker = bytes[i + 1].toInt() and 0xFF
                if (marker == 0x00 || marker == 0xFF) { i++; continue }
                if (marker == 0xD9) { result.add(0xFF.toByte()); result.add(0xD9.toByte()); break }
                if (marker == 0xDA) {
                    result.addAll(bytes.slice(i until bytes.size).map { it })
                    break
                }
                val length = ((bytes[i + 2].toInt() and 0xFF) shl 8) or (bytes[i + 3].toInt() and 0xFF)
                if (marker in 0xE0..0xEF || marker == 0xFE) { i += length + 2 } 
                else {
                    for (j in 0 until length + 2) if (i + j < bytes.size) result.add(bytes[i + j])
                    i += length + 2
                }
            } else i++
        }
        return result.toByteArray()
    }
}
