package com.ibrahim.metaremover

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import android.app.Activity

class AndroidAppUpdateManager(private val context: Context) : AppUpdateManager {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    override fun checkForUpdates(onUpdateAvailable: () -> Unit) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                onUpdateAvailable()
            }
        }
    }

    override fun startFlexibleUpdate() {
        val activity = context as? Activity ?: return
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // This automatically displays Google Play's built-in update sheet over your app
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,
                activity,
                1001
            )
        }
    }
}
