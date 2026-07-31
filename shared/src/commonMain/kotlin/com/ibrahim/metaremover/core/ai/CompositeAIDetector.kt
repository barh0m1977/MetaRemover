package com.ibrahim.metaremover.core.ai

import com.ibrahim.metaremover.domain.model.AIAnalysis

class CompositeAIDetector(private val detectors: List<AIProviderDetector>) {
    fun detect(text: String, bytes: ByteArray): AIAnalysis {
        for (detector in detectors) {
            val result = detector.detect(text, bytes)
            if (result != null) return result
        }
        return AIAnalysis()
    }
}
