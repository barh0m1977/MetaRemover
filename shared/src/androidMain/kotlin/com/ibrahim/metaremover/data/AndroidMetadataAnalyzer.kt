package com.ibrahim.metaremover.data

import androidx.exifinterface.media.ExifInterface
import com.ibrahim.metaremover.domain.ImageMetadata
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import java.io.ByteArrayInputStream

class AndroidMetadataAnalyzer : MetadataAnalyzer {
    
    private val aiKeywords = listOf("dall-e", "midjourney", "stable diffusion", "firefly", "generative ai", "c2pa")
    private val webKeywords = listOf(
        "http", "www.", ".com", ".net", ".org", "facebook", "instagram", "twitter", "whatsapp", 
        "telegram", "browser", "chrome", "safari", "google", "bing", "pinterest", "unsplash", 
        "pixabay", "pexels", "adobe", "flickr", "screenshot", "download", "optimized"
    )

    override suspend fun analyze(bytes: ByteArray): ImageMetadata {
        return try {
            val inputStream = ByteArrayInputStream(bytes)
            val exif = ExifInterface(inputStream)

            val latLong = exif.latLong
            val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE)
            val model = exif.getAttribute(ExifInterface.TAG_MODEL)
            val make = exif.getAttribute(ExifInterface.TAG_MAKE)
            val userComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT)
            val artist = exif.getAttribute(ExifInterface.TAG_ARTIST)
            val copyright = exif.getAttribute(ExifInterface.TAG_COPYRIGHT)
            val xmp = exif.getAttribute(ExifInterface.TAG_XMP)

            val metadataText = "$software $model $make $userComment $artist $copyright $xmp".lowercase()
            val isAI = aiKeywords.any { it in metadataText } || bytes.containsKeyword("c2pa")
            
            // Web Detection
            val webMatch = webKeywords.find { it in metadataText } ?: webKeywords.find { bytes.containsKeyword(it) }
            val isWeb = webMatch != null
            val webSource = if (isWeb) {
                extractUrl(metadataText) ?: extractUrl(bytes.toRawString()) ?: "Web Source Found ($webMatch)"
            } else null

            ImageMetadata(
                width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).takeIf { it > 0 },
                height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).takeIf { it > 0 },
                
                latitude = latLong?.get(0)?.toString(),
                longitude = latLong?.get(1)?.toString(),
                altitude = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE),
                gpsProcessingMethod = exif.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD),

                cameraMake = make,
                cameraModel = model,
                lensModel = exif.getAttribute(ExifInterface.TAG_LENS_MODEL),
                software = software,

                fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER),
                exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),
                isoSpeedRatings = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS),
                flash = exif.getAttribute(ExifInterface.TAG_FLASH),

                dateTimeOriginal = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL),
                dateTimeDigitized = exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED),

                isAIOrSecured = isAI,
                c2paSoftwareAgent = software,
                copyright = exif.getAttribute(ExifInterface.TAG_COPYRIGHT),
                artist = artist,
                userComment = userComment,

                orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION),
                fileSize = "${bytes.size / 1024} KB",

                hasThumbnail = exif.hasThumbnail(),
                makerNotes = if (exif.getAttribute(ExifInterface.TAG_MAKER_NOTE) != null) "Detected" else null,
                xmpData = if (xmp != null) "Detected" else null,
                
                isFromWeb = isWeb,
                webSource = webSource
            )
        } catch (e: Exception) {
            ImageMetadata()
        }
    }
    
    private fun ByteArray.containsKeyword(keyword: String): Boolean {
        return toRawString().contains(keyword, ignoreCase = true)
    }

    private fun ByteArray.toRawString(): String {
        return this.map { if (it in 32..126) it.toInt().toChar() else ' ' }.joinToString("")
    }

    private fun extractUrl(text: String): String? {
        val regex = Regex("(https?://[\\w\\d./?=#&%\\-]+)")
        return regex.find(text)?.value
    }
}
