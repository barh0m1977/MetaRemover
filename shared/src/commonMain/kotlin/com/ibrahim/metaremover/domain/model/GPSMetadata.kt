package com.ibrahim.metaremover.domain.model

data class GPSMetadata(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val processingMethod: String? = null,
    val dateStamp: String? = null,
    val timeStamp: String? = null
)
