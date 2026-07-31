package com.ibrahim.metaremover.data.repository

import com.ibrahim.metaremover.core.ai.CompositeAIDetector
import com.ibrahim.metaremover.core.parser.ParserManager
import com.ibrahim.metaremover.domain.CleanResult
import com.ibrahim.metaremover.domain.model.ImageAnalysis
import com.ibrahim.metaremover.domain.repository.ImageRepository
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import com.ibrahim.metaremover.domain.MetadataCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageRepositoryImpl(
    private val parserManager: ParserManager,
    private val aiDetector: CompositeAIDetector,
    private val analyzer: MetadataAnalyzer,
    private val cleaner: MetadataCleaner
) : ImageRepository {

    override suspend fun analyze(bytes: ByteArray): ImageAnalysis = withContext(Dispatchers.Default) {
        val analysis = parserManager.analyze(bytes)
        val platformMeta = analyzer.analyze(bytes)
        
        // Use the pluggable AI detector with efficient text extraction
        val scanSize = bytes.size.coerceAtMost(1024 * 512)
        val chars = CharArray(scanSize)
        for (i in 0 until scanSize) {
            val b = bytes[i].toInt() and 0xFF
            chars[i] = if (b in 32..126) b.toChar() else ' '
        }
        val text = chars.concatToString()
        
        val aiAnalysis = aiDetector.detect(text, bytes)
        
        analysis.copy(
            basic = analysis.basic.copy(
                width = platformMeta.width,
                height = platformMeta.height,
                format = analysis.basic.format ?: "JPEG"
            ),
            camera = analysis.camera.copy(
                make = platformMeta.cameraMake,
                model = platformMeta.cameraModel,
                bodySerialNumber = platformMeta.bodySerialNumber,
                lensSerialNumber = platformMeta.lensSerialNumber
            ),
            gps = analysis.gps.copy(
                latitude = platformMeta.latitude?.toDoubleOrNull(),
                longitude = platformMeta.longitude?.toDoubleOrNull(),
                altitude = platformMeta.altitude?.toDoubleOrNull()
            ),
            technical = analysis.technical.copy(
                isoSpeedRatings = platformMeta.isoSpeedRatings,
                fNumber = platformMeta.fNumber
            ),
            ai = aiAnalysis,
            privacyScore = platformMeta.calculatePrivacyScore(),
            rawMetadata = analysis.rawMetadata + platformMeta.rawMetadata
        )
    }

    override suspend fun clean(bytes: ByteArray): CleanResult = withContext(Dispatchers.Default) {
        val parserCleaned = parserManager.clean(bytes)
        cleaner.clean(parserCleaned)
    }
}
