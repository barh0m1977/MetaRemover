package com.ibrahim.metaremover.di

import com.ibrahim.metaremover.data.ImageRepository
import com.ibrahim.metaremover.domain.ImageCleaner
import com.ibrahim.metaremover.domain.ImageGallerySaver
import com.ibrahim.metaremover.domain.MetadataAnalyzer

class AppModule(
    val analyzer: MetadataAnalyzer,
    val cleaner: ImageCleaner,
    val saver: ImageGallerySaver
) {
    val repository: ImageRepository by lazy {
        ImageRepository(analyzer, cleaner)
    }
}
