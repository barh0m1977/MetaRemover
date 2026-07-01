package com.ibrahim.metaremover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ibrahim.metaremover.data.AndroidImageCleaner
import com.ibrahim.metaremover.data.AndroidImageGallerySaver
import com.ibrahim.metaremover.data.AndroidMetadataAnalyzer
import com.ibrahim.metaremover.di.AppModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Hide navigation and status bars for Immersive Mode
        hideSystemBars()
        val updateManager = AndroidAppUpdateManager(this)
        val reviewManager = AndroidAppReviewManager(this)
        val appModule = AppModule(
            analyzer = AndroidMetadataAnalyzer(),
            cleaner = AndroidImageCleaner(),
            saver = AndroidImageGallerySaver(this),
            updateManager = updateManager,
            reviewManager = reviewManager
        )

        setContent {
            MainEntryPoint(appModule)
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}
