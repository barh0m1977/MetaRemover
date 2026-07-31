package com.ibrahim.metaremover.domain.model

data class CameraMetadata(
    val make: String? = null,
    val model: String? = null,
    val software: String? = null,
    val cameraSerialNumber: String? = null,
    val bodySerialNumber: String? = null,
    val lensMake: String? = null,
    val lensModel: String? = null,
    val lensSerialNumber: String? = null
)
