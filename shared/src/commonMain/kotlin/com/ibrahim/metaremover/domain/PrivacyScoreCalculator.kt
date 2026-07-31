package com.ibrahim.metaremover.domain

object PrivacyScoreCalculator {
    fun calculate(meta: ImageMetadata): Int {
        var score = 100
        
        // GPS
        if (meta.latitude != null || meta.longitude != null) score -= 30
        
        // Camera information
        if (meta.cameraMake != null || meta.cameraModel != null) score -= 10
        
        // Serial numbers
        if (meta.cameraSerialNumber != null || meta.lensSerialNumber != null || meta.bodySerialNumber != null) score -= 15
        
        // AI prompts
        if (meta.prompt != null) score -= 10
        
        // XMP
        if (meta.xmpData != null) score -= 5
        
        // IPTC
        if (meta.iptcKeywords != null) score -= 5
        
        // MakerNotes
        if (meta.makerNotes != null) score -= 10
        
        // Thumbnail
        if (meta.hasThumbnail) score -= 5
        
        // Copyright
        if (meta.copyright != null || meta.artist != null) score -= 5
        
        // Comments
        if (meta.userComment != null) score -= 5

        return score.coerceIn(0, 100)
    }
}
