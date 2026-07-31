package com.ibrahim.metaremover.engine

import com.ibrahim.metaremover.core.parser.MetadataParser
import com.ibrahim.metaremover.domain.model.ImageAnalysis
import com.ibrahim.metaremover.domain.model.BasicMetadata
import com.ibrahim.metaremover.domain.model.AIAnalysis

class SimpleMetadataEngine : MetadataParser {

    private val aiSignatures = listOf(
        "c2pa", "contentcredentials", "com.adobe.c2pa",
        "trainedAlgorithmicMedia", "compositeWithTrainedAlgorithmicMedia",
        "dall-e", "midjourney", "stable diffusion", "stablediffusion",
        "ai generated", "artificial intelligence", "generative ai",
        "softwareAgent: gpt", "adobe firefly", "bing image creator"
    )

    override fun canParse(bytes: ByteArray): Boolean = bytes.isJpeg() || bytes.isPng()

    override suspend fun parse(bytes: ByteArray): ImageAnalysis {
        val text = bytes.decodeToStringSafe()
        val lowerText = text.lowercase()
        
        val isAI = aiSignatures.any { it.lowercase() in lowerText }
        
        return ImageAnalysis(
            basic = BasicMetadata(
                fileSize = "${bytes.size / 1024} KB"
            ),
            ai = AIAnalysis(
                isAIGenerated = isAI
            )
        )
    }

    override suspend fun clean(bytes: ByteArray): ByteArray {
        return try {
            when {
                bytes.isJpeg() -> bytes.stripJpegMetadata()
                bytes.isPng() -> bytes.stripPngMetadata()
                else -> bytes
            }
        } catch (e: Exception) {
            bytes
        }
    }

    private fun ByteArray.isJpeg() = size > 2 && this[0] == 0xFF.toByte() && this[1] == 0xD8.toByte()
    private fun ByteArray.isPng() = size > 8 && this[0] == 0x89.toByte() && this[1] == 0x50.toByte()

    private fun ByteArray.stripJpegMetadata(): ByteArray {
        val result = mutableListOf<Byte>()
        result.add(0xFF.toByte()); result.add(0xD8.toByte())
        var i = 2
        while (i < size - 1) {
            if (this[i] == 0xFF.toByte()) {
                val marker = this[i + 1].toInt() and 0xFF
                if (marker == 0x00 || marker == 0xFF) { i++; continue }
                if (marker == 0xD9) { result.add(0xFF.toByte()); result.add(0xD9.toByte()); break }
                if (marker == 0xDA) {
                    val remaining = size - i
                    val buffer = ByteArray(remaining)
                    this.copyInto(buffer, 0, i, size)
                    result.addAll(buffer.toList())
                    break
                }
                val length = ((this[i + 2].toInt() and 0xFF) shl 8) or (this[i + 3].toInt() and 0xFF)
                if (marker in 0xE0..0xEF || marker == 0xFE) { i += length + 2 } 
                else {
                    val buffer = ByteArray(length + 2)
                    this.copyInto(buffer, 0, i, i + length + 2)
                    result.addAll(buffer.toList())
                    i += length + 2
                }
            } else i++
        }
        return result.toByteArray()
    }

    private fun ByteArray.stripPngMetadata(): ByteArray {
        val result = mutableListOf<Byte>()
        val signature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        result.addAll(signature.toList())
        var i = 8
        while (i < size - 8) {
            val length = ((this[i].toInt() and 0xFF) shl 24) or ((this[i + 1].toInt() and 0xFF) shl 16) or
                         ((this[i + 2].toInt() and 0xFF) shl 8) or (this[i + 3].toInt() and 0xFF)
            val type = this.sliceArray(i + 4 until i + 8).decodeToString()
            if (type[0].isLowerCase()) { i += length + 12 } 
            else {
                val buffer = ByteArray(length + 12)
                this.copyInto(buffer, 0, i, i + length + 12)
                result.addAll(buffer.toList())
                i += length + 12
                if (type == "IEND") break
            }
        }
        return result.toByteArray()
    }

    private fun ByteArray.decodeToStringSafe(): String {
        val scanSize = size.coerceAtMost(1024 * 512) // Limit to 512KB
        val chars = CharArray(scanSize)
        for (i in 0 until scanSize) {
            val b = this[i].toInt() and 0xFF
            chars[i] = if (b in 32..126) b.toChar() else ' '
        }
        return chars.concatToString()
    }
}
