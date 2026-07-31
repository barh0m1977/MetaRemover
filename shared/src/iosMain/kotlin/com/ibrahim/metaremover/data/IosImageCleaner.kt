package com.ibrahim.metaremover.data

import com.ibrahim.metaremover.domain.CleanResult
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import com.ibrahim.metaremover.domain.MetadataCleaner
import com.ibrahim.metaremover.engine.SimpleMetadataEngine

class IosImageCleaner(private val analyzer: MetadataAnalyzer) : MetadataCleaner {
    private val simpleEngine = SimpleMetadataEngine()

    override suspend fun clean(bytes: ByteArray): CleanResult {
        val beforeMeta = analyzer.analyze(bytes)
        val cleanedBytes = simpleEngine.clean(bytes)
        val afterMeta = analyzer.analyze(cleanedBytes)

        return CleanResult(
            cleanedBytes = cleanedBytes,
            removedItems = listOf("EXIF", "GPS", "XMP", "IPTC"),
            remainingItems = emptyList(),
            privacyScoreBefore = beforeMeta.calculatePrivacyScore(),
            privacyScoreAfter = afterMeta.calculatePrivacyScore()
        )
    }
}
