package com.ibrahim.metaremover.domain.model

data class BasicMetadata(
    val fileName: String? = null,
    val fileSize: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val format: String? = null,
    val dateTimeOriginal: String? = null,
    val dateTimeDigitized: String? = null
)
