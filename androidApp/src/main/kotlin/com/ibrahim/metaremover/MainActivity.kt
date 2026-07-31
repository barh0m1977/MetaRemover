package com.ibrahim.metaremover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ibrahim.metaremover.data.AndroidImageCleaner
import com.ibrahim.metaremover.data.AndroidImageGallerySaver
import com.ibrahim.metaremover.data.AndroidMetadataAnalyzer
import com.ibrahim.metaremover.di.initKoin
import com.ibrahim.metaremover.domain.ImageGallerySaver
import com.ibrahim.metaremover.domain.MetadataAnalyzer
import com.ibrahim.metaremover.domain.MetadataCleaner
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val updateManager = AndroidAppUpdateManager(this)
        val reviewManager = AndroidAppReviewManager(this)

        val androidModule = module {
            single<MetadataAnalyzer> { AndroidMetadataAnalyzer() }
            single<MetadataCleaner> { AndroidImageCleaner(get()) }
            single<ImageGallerySaver> { AndroidImageGallerySaver(this@MainActivity) }
            single<AppUpdateManager> { updateManager }
            single<AppReviewManager> { reviewManager }
        }

        try {
            initKoin(androidModule)
        } catch (e: Exception) {
            // Koin might already be started in some environments
        }

        setContent {
            MainEntryPoint()
        }
    }
}
