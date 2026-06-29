package com.ibrahim.metaremover

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibrahim.metaremover.di.AppModule
import com.ibrahim.metaremover.presentation.MainViewModel

@Composable
fun MainEntryPoint(appModule: AppModule) {
    val mainViewModel: MainViewModel = viewModel {
        MainViewModel(appModule.repository, appModule.saver)
    }
    App(mainViewModel)
}
