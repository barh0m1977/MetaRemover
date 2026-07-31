package com.ibrahim.metaremover.core.ai.detectors

import com.ibrahim.metaremover.core.ai.AIProviderDetector
import com.ibrahim.metaremover.domain.AiProvider
import com.ibrahim.metaremover.domain.model.AIAnalysis

class OpenAIDetector : AIProviderDetector {
    override fun detect(text: String, bytes: ByteArray): AIAnalysis? {
        val lowerText = text.lowercase()
        if (lowerText.contains("dall-e") || lowerText.contains("openai")) {
            return AIAnalysis(
                isAIGenerated = true,
                provider = AiProvider.OPENAI,
                confidence = 1.0f
            )
        }
        return null
    }
}
