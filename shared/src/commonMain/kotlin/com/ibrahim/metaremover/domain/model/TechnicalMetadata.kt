package com.ibrahim.metaremover.domain.model

data class TechnicalMetadata(
    val colorSpace: String? = null,
    val compression: String? = null,
    val orientation: String? = null,
    val bitDepth: Int? = null,
    val jpegQuality: Int? = null,
    val isProgressive: Boolean? = null,
    val exposureTime: String? = null,
    val fNumber: String? = null,
    val isoSpeedRatings: String? = null,
    val focalLength: String? = null
)
