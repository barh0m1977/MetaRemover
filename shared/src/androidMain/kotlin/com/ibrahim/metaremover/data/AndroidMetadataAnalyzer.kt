package com.ibrahim.metaremover.data

import androidx.exifinterface.media.ExifInterface
import com.ibrahim.metaremover.domain.ImageMetadata
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import com.ibrahim.metaremover.engine.AIProviderDetector
import com.ibrahim.metaremover.engine.PromptDetector
import java.io.ByteArrayInputStream

class AndroidMetadataAnalyzer : MetadataAnalyzer {
    
    private val aiDetector = AIProviderDetector()
    private val promptDetector = PromptDetector()
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
            val aiProvider = aiDetector.detect(metadataText) ?: aiDetector.detect(bytes.toRawString())
            val prompt = promptDetector.detectPrompt(metadataText) ?: promptDetector.detectPrompt(bytes.toRawString())
            val negativePrompt = promptDetector.detectNegativePrompt(metadataText) ?: promptDetector.detectNegativePrompt(bytes.toRawString())
            
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

                isAIGenerated = isAI || aiProvider != null,
                isAIOrSecured = isAI || aiProvider != null,
                aiProvider = aiProvider,
                prompt = prompt,
                negativePrompt = negativePrompt,
                c2paSoftwareAgent = software,
                cameraSerialNumber = exif.getAttribute(ExifInterface.TAG_BODY_SERIAL_NUMBER),
                lensSerialNumber = exif.getAttribute(ExifInterface.TAG_LENS_SERIAL_NUMBER),
                copyright = exif.getAttribute(ExifInterface.TAG_COPYRIGHT),
                artist = artist,
                userComment = userComment,

                orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION),
                fileSize = "${bytes.size / 1024} KB",

                rawMetadata = extractAllTags(exif),

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
    
    /** Dump every EXIF/TIFF/GPS tag ExifInterface can read into a raw key -> value map. */
    private fun extractAllTags(exif: ExifInterface): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        for (tag in ALL_EXIF_TAGS) {
            val value = exif.getAttribute(tag)
            if (!value.isNullOrBlank()) map[tag] = value
        }
        return map
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

    private companion object {
        // Every standard EXIF/TIFF/GPS tag ExifInterface exposes, grouped by category
        // (matching MetaRemoverDataType.txt). Non-null values are dumped into rawMetadata.
        val ALL_EXIF_TAGS = listOf(
            // GPS & Location
            ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE, ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_ALTITUDE, ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_TIMESTAMP, ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_SPEED, ExifInterface.TAG_GPS_SPEED_REF,
            ExifInterface.TAG_GPS_TRACK, ExifInterface.TAG_GPS_TRACK_REF,
            ExifInterface.TAG_GPS_IMG_DIRECTION, ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
            ExifInterface.TAG_GPS_DEST_LATITUDE, ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
            ExifInterface.TAG_GPS_DEST_LONGITUDE, ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
            ExifInterface.TAG_GPS_DEST_BEARING, ExifInterface.TAG_GPS_DEST_BEARING_REF,
            ExifInterface.TAG_GPS_DEST_DISTANCE, ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD, ExifInterface.TAG_GPS_AREA_INFORMATION,
            ExifInterface.TAG_GPS_DIFFERENTIAL, ExifInterface.TAG_GPS_H_POSITIONING_ERROR,
            ExifInterface.TAG_GPS_DOP, ExifInterface.TAG_GPS_MEASURE_MODE,
            ExifInterface.TAG_GPS_MAP_DATUM, ExifInterface.TAG_GPS_SATELLITES,
            ExifInterface.TAG_GPS_STATUS, ExifInterface.TAG_GPS_VERSION_ID,
            // Camera Information
            ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL,
            ExifInterface.TAG_CAMERA_OWNER_NAME, ExifInterface.TAG_BODY_SERIAL_NUMBER,
            ExifInterface.TAG_LENS_MAKE, ExifInterface.TAG_LENS_MODEL,
            ExifInterface.TAG_LENS_SERIAL_NUMBER, ExifInterface.TAG_LENS_SPECIFICATION,
            // Camera Settings
            ExifInterface.TAG_EXPOSURE_TIME, ExifInterface.TAG_SHUTTER_SPEED_VALUE,
            ExifInterface.TAG_APERTURE_VALUE, ExifInterface.TAG_F_NUMBER,
            ExifInterface.TAG_ISO_SPEED, ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
            ExifInterface.TAG_EXPOSURE_PROGRAM, ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
            ExifInterface.TAG_EXPOSURE_MODE, ExifInterface.TAG_METERING_MODE,
            ExifInterface.TAG_WHITE_BALANCE, ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FLASH_ENERGY, ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, ExifInterface.TAG_MAX_APERTURE_VALUE,
            ExifInterface.TAG_SUBJECT_DISTANCE, ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
            ExifInterface.TAG_DIGITAL_ZOOM_RATIO, ExifInterface.TAG_BRIGHTNESS_VALUE,
            ExifInterface.TAG_CONTRAST, ExifInterface.TAG_SATURATION,
            ExifInterface.TAG_SHARPNESS, ExifInterface.TAG_GAIN_CONTROL,
            ExifInterface.TAG_LIGHT_SOURCE, ExifInterface.TAG_SCENE_CAPTURE_TYPE,
            ExifInterface.TAG_SCENE_TYPE, ExifInterface.TAG_SENSING_METHOD,
            ExifInterface.TAG_COLOR_SPACE, ExifInterface.TAG_CUSTOM_RENDERED,
            ExifInterface.TAG_SUBJECT_AREA, ExifInterface.TAG_SUBJECT_LOCATION,
            // Time Information
            ExifInterface.TAG_DATETIME, ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_DATETIME_DIGITIZED, ExifInterface.TAG_OFFSET_TIME,
            ExifInterface.TAG_OFFSET_TIME_ORIGINAL, ExifInterface.TAG_OFFSET_TIME_DIGITIZED,
            ExifInterface.TAG_SUBSEC_TIME, ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
            ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
            // Image Properties
            ExifInterface.TAG_IMAGE_WIDTH, ExifInterface.TAG_IMAGE_LENGTH,
            ExifInterface.TAG_ORIENTATION, ExifInterface.TAG_X_RESOLUTION,
            ExifInterface.TAG_Y_RESOLUTION, ExifInterface.TAG_RESOLUTION_UNIT,
            ExifInterface.TAG_COMPRESSION, ExifInterface.TAG_SAMPLES_PER_PIXEL,
            ExifInterface.TAG_BITS_PER_SAMPLE, ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION,
            ExifInterface.TAG_PLANAR_CONFIGURATION, ExifInterface.TAG_Y_CB_CR_POSITIONING,
            ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING, ExifInterface.TAG_PIXEL_X_DIMENSION,
            ExifInterface.TAG_PIXEL_Y_DIMENSION,
            // Device / Author & Copyright
            ExifInterface.TAG_SOFTWARE, ExifInterface.TAG_ARTIST, ExifInterface.TAG_COPYRIGHT,
            // Comments & Notes
            ExifInterface.TAG_USER_COMMENT, ExifInterface.TAG_IMAGE_DESCRIPTION,
            // Security & Identifiers / proprietary
            ExifInterface.TAG_IMAGE_UNIQUE_ID, ExifInterface.TAG_MAKER_NOTE, ExifInterface.TAG_XMP
        )
    }
}
