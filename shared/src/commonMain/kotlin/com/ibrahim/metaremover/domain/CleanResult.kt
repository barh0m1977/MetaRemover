package com.ibrahim.metaremover.domain

data class CleanResult(
    val cleanedBytes: ByteArray,
    val removedItems: List<String>,
    val remainingItems: List<String>,
    val privacyScoreBefore: Int,
    val privacyScoreAfter: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CleanResult

        if (!cleanedBytes.contentEquals(other.cleanedBytes)) return false
        if (removedItems != other.removedItems) return false
        if (remainingItems != other.remainingItems) return false
        if (privacyScoreBefore != other.privacyScoreBefore) return false
        if (privacyScoreAfter != other.privacyScoreAfter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cleanedBytes.contentHashCode()
        result = 31 * result + removedItems.hashCode()
        result = 31 * result + remainingItems.hashCode()
        result = 31 * result + privacyScoreBefore
        result = 31 * result + privacyScoreAfter
        return result
    }
}
