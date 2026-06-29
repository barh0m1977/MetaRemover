package com.ibrahim.metaremover.domain

class PrivacyScoreCalculator {
    fun calculate(meta: ImageMetadata): Int {
        var score = 100

        // GPS Info
        if (meta.latitude != null || meta.longitude != null) score -= 40
        
        // Device Info (Model/Manufacturer)
        if (meta.cameraModel != null) score -= 25
        
        // Timestamp
        if (meta.cameraModel != null) score -= 15
        
        // Software/Processing Info
        if (meta.c2paSoftwareAgent != null) score -= 10

        return score.coerceIn(0, 100)
    }
}
