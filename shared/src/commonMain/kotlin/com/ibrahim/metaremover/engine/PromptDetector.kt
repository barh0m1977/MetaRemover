package com.ibrahim.metaremover.engine

class PromptDetector {
    fun detectPrompt(text: String): String? {
        // Simple heuristic for prompt detection in metadata strings
        val promptMarkers = listOf("prompt:", "positive_prompt:", "parameters:", "description:")
        val lowerText = text.lowercase()
        
        for (marker in promptMarkers) {
            val index = lowerText.indexOf(marker)
            if (index != -1) {
                val start = index + marker.length
                val end = text.indexOf("\n", start).let { if (it == -1) text.length else it }
                return text.substring(start, end).trim().trim('"', '{', '}')
            }
        }
        return null
    }

    fun detectNegativePrompt(text: String): String? {
        val markers = listOf("negative_prompt:", "negative prompt:", "uc:")
        val lowerText = text.lowercase()
        for (marker in markers) {
            val index = lowerText.indexOf(marker)
            if (index != -1) {
                val start = index + marker.length
                val end = text.indexOf("\n", start).let { if (it == -1) text.length else it }
                return text.substring(start, end).trim().trim('"', '{', '}')
            }
        }
        return null
    }
}
