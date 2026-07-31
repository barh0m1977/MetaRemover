package com.ibrahim.metaremover

import androidx.compose.runtime.Composable
import com.ibrahim.metaremover.presentation.MainViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@Composable
fun MainEntryPoint() {
    val mainViewModel: MainViewModel = koinViewModel()
    val updateManager: AppUpdateManager = koinInject()
    val reviewManager: AppReviewManager = koinInject()
    
    App(
        mainViewModel,
        updateManager = updateManager,
        reviewManager = reviewManager
    )
}
