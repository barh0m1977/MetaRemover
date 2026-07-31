package com.ibrahim.metaremover.core.parser

import com.ibrahim.metaremover.domain.model.ImageAnalysis

class PNGParser : MetadataParser {
    override fun canParse(bytes: ByteArray): Boolean =
        bytes.size > 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte()

    override suspend fun parse(bytes: ByteArray): ImageAnalysis {
        return ImageAnalysis()
    }

    override suspend fun clean(bytes: ByteArray): ByteArray {
        val result = mutableListOf<Byte>()
        val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        result.addAll(signature.map { it })
        var i = 8
        while (i < bytes.size - 8) {
            val length = ((bytes[i].toInt() and 0xFF) shl 24) or ((bytes[i + 1].toInt() and 0xFF) shl 16) or
                         ((bytes[i + 2].toInt() and 0xFF) shl 8) or (bytes[i + 3].toInt() and 0xFF)
            val type = bytes.sliceArray(i + 4 until i + 8).decodeToString()
            if (type[0].isLowerCase()) { i += length + 12 } 
            else {
                for (j in 0 until length + 12) if (i + j < bytes.size) result.add(bytes[i + j])
                i += length + 12
                if (type == "IEND") break
            }
        }
        return result.toByteArray()
    }
}
