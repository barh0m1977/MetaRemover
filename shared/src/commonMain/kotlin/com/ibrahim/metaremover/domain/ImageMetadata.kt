package com.ibrahim.metaremover.domain

enum class AiProvider {
    OPENAI, GEMINI, MIDJOURNEY, STABLE_DIFFUSION, ADOBE_FIREFLY,
    LEONARDO, IDEOGRAM, BING, CANVA, GROK, UNKNOWN
}

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
    val cameraSerialNumber: String? = null,
    val lensModel: String? = null,
    val lensMake: String? = null,
    val lensSerialNumber: String? = null,
    val bodySerialNumber: String? = null,
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

    // AI Detection
    val isAIGenerated: Boolean = false,
    val aiProvider: AiProvider? = null,
    val aiModel: String? = null,
    val aiConfidence: Float? = null,
    val prompt: String? = null,
    val negativePrompt: String? = null,
    val sampler: String? = null,
    val seed: String? = null,
    val cfgScale: Float? = null,
    val steps: Int? = null,
    val generationSoftware: String? = null,

    // Editing History
    val editingSoftware: String? = null,
    val editingHistory: List<String> = emptyList(),
    val photoshopVersion: String? = null,
    val lightroomVersion: String? = null,

    // Security, AI & Rights
    val isAIOrSecured: Boolean = false,
    val copyright: String? = null,
    val artist: String? = null,
    val userComment: String? = null,

    // C2PA
    val hasC2PA: Boolean = false,
    val c2paGenerator: String? = null,
    val c2paManifestVersion: String? = null,
    val c2paClaimGenerator: String? = null,
    val c2paSoftwareAgent: String? = null,
    val c2paDigitalSourceType: String? = null,
    val c2paActions: String? = null,

    // Technical Details
    val colorSpace: String? = null,
    val compression: String? = null,
    val orientation: String? = null,
    val bitDepth: Int? = null,
    val colorType: String? = null,
    val filter: String? = null,
    val interlace: String? = null,

    // Integrity
    val sha256: String? = null,
    val md5: String? = null,

    // Compression
    val jpegQuality: Int? = null,
    val progressive: Boolean? = null,

    // PNG
    val pngChunks: List<String> = emptyList(),

    // Deep Privacy / Proprietary
    val hasThumbnail: Boolean = false,
    val thumbnailWidth: Int? = null,
    val thumbnailHeight: Int? = null,
    val makerNotes: String? = null,
    val xmpData: String? = null,
    val iptcKeywords: String? = null,
    val iccProfile: String? = null,
    val hiddenMetadata: Map<String, String> = emptyMap(),

    // Full generic dump of every tag the platform analyzer could read (raw key -> value)
    val rawMetadata: Map<String, String> = emptyMap(),

    // Web & Source
    val isFromWeb: Boolean = false,
    val webSource: String? = null
) {
    fun calculatePrivacyScore(): Int = PrivacyScoreCalculator.calculate(this)
}
