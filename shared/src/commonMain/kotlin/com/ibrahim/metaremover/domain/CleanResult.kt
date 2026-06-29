package com.ibrahim.metaremover.domain

data class CleanResult(
    val cleanedBytes: ByteArray,
    val removedFields: List<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CleanResult

        if (!cleanedBytes.contentEquals(other.cleanedBytes)) return false
        if (removedFields != other.removedFields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cleanedBytes.contentHashCode()
        result = 31 * result + removedFields.hashCode()
        return result
    }
}