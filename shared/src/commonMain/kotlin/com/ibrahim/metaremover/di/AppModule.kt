package com.ibrahim.metaremover.di

import com.ibrahim.metaremover.AppReviewManager
import com.ibrahim.metaremover.AppUpdateManager
import com.ibrahim.metaremover.data.ImageRepository
import com.ibrahim.metaremover.domain.ImageGallerySaver
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import com.ibrahim.metaremover.domain.MetadataCleaner

class AppModule(
    val analyzer: MetadataAnalyzer,
    val cleaner: MetadataCleaner,
    val saver: ImageGallerySaver,
    val updateManager: AppUpdateManager,
    val reviewManager: AppReviewManager
) {
    val repository: ImageRepository by lazy {
        ImageRepository(analyzer, cleaner)
    }
}
