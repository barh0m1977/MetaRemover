package com.ibrahim.metaremover.core.ai

import com.ibrahim.metaremover.domain.AiProvider
import com.ibrahim.metaremover.domain.model.AIAnalysis

interface AIProviderDetector {
    fun detect(text: String, bytes: ByteArray): AIAnalysis?
}
