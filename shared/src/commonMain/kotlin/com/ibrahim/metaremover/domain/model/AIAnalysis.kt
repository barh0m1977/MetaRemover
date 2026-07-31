package com.ibrahim.metaremover.domain.model

import com.ibrahim.metaremover.domain.AiProvider

data class AIAnalysis(
    val isAIGenerated: Boolean = false,
    val provider: AiProvider? = null,
    val model: String? = null,
    val confidence: Float? = null,
    val prompt: String? = null,
    val negativePrompt: String? = null,
    val params: Map<String, String> = emptyMap()
)
