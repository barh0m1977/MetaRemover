package com.ibrahim.metaremover

import androidx.compose.ui.window.ComposeUIViewController
import com.ibrahim.metaremover.data.IosImageCleaner
import com.ibrahim.metaremover.data.IosImageGallerySaver
import com.ibrahim.metaremover.data.IosMetadataAnalyzer
import com.ibrahim.metaremover.di.AppModule

fun MainViewController() = ComposeUIViewController {
    val appModule = AppModule(
        analyzer = IosMetadataAnalyzer(),
        cleaner = IosImageCleaner(),
        saver = IosImageGallerySaver()
    )
    MainEntryPoint(appModule)
}
