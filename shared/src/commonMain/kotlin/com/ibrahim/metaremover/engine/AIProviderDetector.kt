package com.ibrahim.metaremover.engine

import com.ibrahim.metaremover.domain.AiProvider

class AIProviderDetector {
    fun detect(text: String): AiProvider? {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("dall-e") || lowerText.contains("openai") -> AiProvider.OPENAI
            lowerText.contains("midjourney") -> AiProvider.MIDJOURNEY
            lowerText.contains("stable diffusion") || lowerText.contains("stablediffusion") -> AiProvider.STABLE_DIFFUSION
            lowerText.contains("adobe firefly") -> AiProvider.ADOBE_FIREFLY
            lowerText.contains("gemini") || lowerText.contains("imagen") -> AiProvider.GEMINI
            lowerText.contains("leonardo") -> AiProvider.LEONARDO
            lowerText.contains("ideogram") -> AiProvider.IDEOGRAM
            lowerText.contains("bing") -> AiProvider.BING
            lowerText.contains("canva") -> AiProvider.CANVA
            lowerText.contains("grok") -> AiProvider.GROK
            else -> null
        }
    }
}
