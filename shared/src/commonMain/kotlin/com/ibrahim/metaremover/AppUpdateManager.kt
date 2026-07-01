package com.ibrahim.metaremover

interface AppUpdateManager {
    fun checkForUpdates(onUpdateAvailable: () -> Unit)
    fun startFlexibleUpdate()
}

interface AppReviewManager {
    fun launchReviewFlow()
}