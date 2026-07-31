package com.ibrahim.metaremover

import androidx.compose.ui.window.ComposeUIViewController
import com.ibrahim.metaremover.data.IosImageCleaner
import com.ibrahim.metaremover.data.IosImageGallerySaver
import com.ibrahim.metaremover.data.IosMetadataAnalyzer
import com.ibrahim.metaremover.di.initKoin
import com.ibrahim.metaremover.domain.ImageGallerySaver
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import com.ibrahim.metaremover.domain.MetadataCleaner
import org.koin.dsl.module

fun MainViewController() = ComposeUIViewController {
    val iosModule = module {
        single<MetadataAnalyzer> { IosMetadataAnalyzer() }
        single<MetadataCleaner> { IosImageCleaner(get()) }
        single<ImageGallerySaver> { IosImageGallerySaver() }
        single<AppUpdateManager> { 
            object : AppUpdateManager {
                override fun checkForUpdates(onUpdateAvailable: () -> Unit) {}
                override fun startFlexibleUpdate() {}
            }
        }
        single<AppReviewManager> {
            object : AppReviewManager {
                override fun launchReviewFlow() {}
            }
        }
    }
    
    try {
        initKoin(iosModule)
    } catch (e: Exception) {}

    MainEntryPoint()
}
