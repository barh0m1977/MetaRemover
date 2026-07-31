package com.ibrahim.metaremover.data

import com.ibrahim.metaremover.domain.ImageMetadata
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import com.ibrahim.metaremover.engine.AIProviderDetector
import com.ibrahim.metaremover.engine.PromptDetector
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFDataRef
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.create
import platform.ImageIO.*

class IosMetadataAnalyzer : MetadataAnalyzer {
    private val aiDetector = AIProviderDetector()
    private val promptDetector = PromptDetector()

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun analyze(bytes: ByteArray): ImageMetadata {
        return try {
            val nsData = bytes.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            }

            val source = CGImageSourceCreateWithData(nsData as CFDataRef, null) ?: return ImageMetadata()
            val properties = CGImageSourceCopyPropertiesAtIndex(source, 0u, null) as? NSDictionary
                ?: return ImageMetadata()

            val exif = properties.objectForKey(kCGImagePropertyExifDictionary) as? NSDictionary
            val tiff = properties.objectForKey(kCGImagePropertyTIFFDictionary) as? NSDictionary
            val gps = properties.objectForKey(kCGImagePropertyGPSDictionary) as? NSDictionary
            val iptc = properties.objectForKey(kCGImagePropertyIPTCDictionary) as? NSDictionary

            val rawString = bytes.decodeToStringSafe()
            val isAI = rawString.lowercase().contains("c2pa")
            val aiProvider = aiDetector.detect(rawString)
            val prompt = promptDetector.detectPrompt(rawString)
            val negativePrompt = promptDetector.detectNegativePrompt(rawString)

            // Aggressive Web Source Detection
            val webSource = iptc?.objectForKey(kCGImagePropertyIPTCSource)?.toString()
                ?: iptc?.objectForKey(kCGImagePropertyIPTCCredit)?.toString()
                ?: tiff?.objectForKey(kCGImagePropertyTIFFSoftware)?.toString()
                ?: tiff?.objectForKey(kCGImagePropertyTIFFCopyright)?.toString()
                ?: properties.objectForKey("Author")?.toString()
            
            val webKeywords = listOf("http", "www.", ".com", ".net", "facebook", "instagram", "twitter", "whatsapp", "telegram", "google", "bing", "pinterest", "unsplash", "pixabay", "screenshot", "download")
            
            val isWeb = webSource?.lowercase()?.let { s -> 
                webKeywords.any { it in s }
            } ?: webKeywords.any { it in rawString }

            ImageMetadata(
                width = properties.objectForKey(kCGImagePropertyPixelWidth) as? Int,
                height = properties.objectForKey(kCGImagePropertyPixelHeight) as? Int,
                fileSize = "${bytes.size / 1024} KB",
                
                latitude = gps?.objectForKey(kCGImagePropertyGPSLatitude)?.toString(),
                longitude = gps?.objectForKey(kCGImagePropertyGPSLongitude)?.toString(),

                cameraMake = tiff?.objectForKey(kCGImagePropertyTIFFMake)?.toString(),
                cameraModel = tiff?.objectForKey(kCGImagePropertyTIFFModel)?.toString(),
                software = tiff?.objectForKey(kCGImagePropertyTIFFSoftware)?.toString(),
                
                dateTimeOriginal = exif?.objectForKey(kCGImagePropertyExifDateTimeOriginal)?.toString(),

                isAIGenerated = isAI || aiProvider != null,
                isAIOrSecured = isAI || aiProvider != null,
                aiProvider = aiProvider,
                prompt = prompt,
                negativePrompt = negativePrompt,

                copyright = tiff?.objectForKey(kCGImagePropertyTIFFCopyright)?.toString(),
                artist = tiff?.objectForKey(kCGImagePropertyTIFFArtist)?.toString(),
                userComment = exif?.objectForKey(kCGImagePropertyExifUserComment)?.toString(),

                hasThumbnail = rawString.contains("thumbnail"),
                makerNotes = if (rawString.contains("apple")) "Detected" else null,
                xmpData = if (rawString.contains("<?xpacket")) "Detected" else null,
                iptcKeywords = iptc?.objectForKey(kCGImagePropertyIPTCKeywords)?.toString(),

                rawMetadata = extractAllTags(properties, exif, tiff, gps, iptc),

                isFromWeb = isWeb,
                webSource = webSource ?: if (isWeb) "Web Stream Detected" else null
            )
        } catch (e: Exception) {
            ImageMetadata()
        }
    }

    /** Flatten every ImageIO property dictionary into a raw key -> value map. */
    @Suppress("UNCHECKED_CAST")
    private fun extractAllTags(vararg dicts: NSDictionary?): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        for (dict in dicts) {
            // Kotlin/Native bridges NSDictionary to kotlin.Map.
            val bridged = dict as? Map<Any?, Any?> ?: continue
            for ((key, value) in bridged) {
                val keyName = key?.toString() ?: continue
                // Skip nested dictionaries; only flatten scalar leaves.
                if (value == null || value is NSDictionary) continue
                val valueStr = value.toString()
                if (valueStr.isNotBlank() && !map.containsKey(keyName)) map[keyName] = valueStr
            }
        }
        return map
    }

    private fun ByteArray.decodeToStringSafe(): String {
        return this.map { if (it in 32..126) it.toInt().toChar() else ' ' }.joinToString("")
    }
}
