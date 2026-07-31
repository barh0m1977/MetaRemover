package com.ibrahim.metaremover.presentation

import com.ibrahim.metaremover.domain.ImageMetadata
import com.ibrahim.metaremover.domain.model.ImageAnalysis

data class MainState(
    val originalBytes: ByteArray? = null,
    val cleanedBytes: ByteArray? = null,
    val originalMetadata: ImageMetadata? = null,
    val cleanedMetadata: ImageMetadata? = null,
    val analysis: ImageAnalysis? = null,
    val isSaving: Boolean = false,
    val isProcessing: Boolean = false,
    val progressPhase: ProgressPhase? = null,
    val loadingMessage: String? = null,
    val error: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MainState

        if (originalBytes != null) {
            if (other.originalBytes == null) return false
            if (!originalBytes.contentEquals(other.originalBytes)) return false
        } else if (other.originalBytes != null) return false
        if (cleanedBytes != null) {
            if (other.cleanedBytes == null) return false
            if (!cleanedBytes.contentEquals(other.cleanedBytes)) return false
        } else if (other.cleanedBytes != null) return false
        if (originalMetadata != other.originalMetadata) return false
        if (cleanedMetadata != other.cleanedMetadata) return false
        if (analysis != other.analysis) return false
        if (isSaving != other.isSaving) return false
        if (isProcessing != other.isProcessing) return false
        if (progressPhase != other.progressPhase) return false
        if (loadingMessage != other.loadingMessage) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originalBytes?.contentHashCode() ?: 0
        result = 31 * result + (cleanedBytes?.contentHashCode() ?: 0)
        result = 31 * result + (originalMetadata?.hashCode() ?: 0)
        result = 31 * result + (cleanedMetadata?.hashCode() ?: 0)
        result = 31 * result + (analysis?.hashCode() ?: 0)
        result = 31 * result + isSaving.hashCode()
        result = 31 * result + isProcessing.hashCode()
        result = 31 * result + (progressPhase?.hashCode() ?: 0)
        result = 31 * result + (loadingMessage?.hashCode() ?: 0)
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}
