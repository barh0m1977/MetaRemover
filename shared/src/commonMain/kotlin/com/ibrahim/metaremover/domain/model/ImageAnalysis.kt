package com.ibrahim.metaremover.domain.model

data class ImageAnalysis(
    val basic: BasicMetadata = BasicMetadata(),
    val camera: CameraMetadata = CameraMetadata(),
    val gps: GPSMetadata = GPSMetadata(),
    val technical: TechnicalMetadata = TechnicalMetadata(),
    val ai: AIAnalysis = AIAnalysis(),
    val privacyScore: Int = 100,
    val rawMetadata: Map<String, String> = emptyMap()
)
