package com.ibrahim.metaremover.data

import com.ibrahim.metaremover.domain.ImageMetadata
import com.ibrahim.metaremover.domain.MetadataAnalyzer
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

            val rawString = bytes.decodeToStringSafe().lowercase()
            val isAI = rawString.contains("c2pa")

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

                isAIOrSecured = isAI,
                copyright = tiff?.objectForKey(kCGImagePropertyTIFFCopyright)?.toString(),
                artist = tiff?.objectForKey(kCGImagePropertyTIFFArtist)?.toString(),
                userComment = exif?.objectForKey(kCGImagePropertyExifUserComment)?.toString(),

                hasThumbnail = rawString.contains("thumbnail"),
                makerNotes = if (rawString.contains("apple")) "Detected" else null,
                xmpData = if (rawString.contains("<?xpacket")) "Detected" else null,
                iptcKeywords = iptc?.objectForKey(kCGImagePropertyIPTCKeywords)?.toString(),
                
                isFromWeb = isWeb,
                webSource = webSource ?: if (isWeb) "Web Stream Detected" else null
            )
        } catch (e: Exception) {
            ImageMetadata()
        }
    }

    private fun ByteArray.decodeToStringSafe(): String {
        return this.map { if (it in 32..126) it.toInt().toChar() else ' ' }.joinToString("")
    }
}
