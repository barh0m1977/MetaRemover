package com.ibrahim.metaremover.domain

data class ImageMetadata(
    // Identification & Basic
    val fileName: String? = null,
    val fileSize: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val format: String? = null,

    // Location (GPS)
    val latitude: String? = null,
    val longitude: String? = null,
    val altitude: String? = null,
    val gpsProcessingMethod: String? = null,
    val gpsDateStamp: String? = null,

    // Camera & Lens
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val lensModel: String? = null,
    val lensMake: String? = null,
    val software: String? = null,

    // Exposure & Shooting
    val fNumber: String? = null,
    val exposureTime: String? = null,
    val isoSpeedRatings: String? = null,
    val focalLength: String? = null,
    val flash: String? = null,
    val whiteBalance: String? = null,
    val exposureProgram: String? = null,

    // Date & Time
    val dateTimeOriginal: String? = null,
    val dateTimeDigitized: String? = null,
    val offsetTime: String? = null,

    // Security, AI & Rights
    val isAIOrSecured: Boolean = false,
    val c2paSoftwareAgent: String? = null,
    val c2paDigitalSourceType: String? = null,
    val c2paActions: String? = null,
    val copyright: String? = null,
    val artist: String? = null,
    val userComment: String? = null,

    // Technical Details
    val colorSpace: String? = null,
    val compression: String? = null,
    val orientation: String? = null,
    val bitDepth: Int? = null,
    val colorType: String? = null,
    val filter: String? = null,
    val interlace: String? = null,

    // Deep Privacy / Proprietary
    val hasThumbnail: Boolean = false,
    val makerNotes: String? = null,
    val xmpData: String? = null,
    val iptcKeywords: String? = null,
    val iccProfile: String? = null,
    
    // Web & Source
    val isFromWeb: Boolean = false,
    val webSource: String? = null
)
