package com.ibrahim.metaremover

import android.content.Context
import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

class AndroidAppReviewManager(private val context: Context) : AppReviewManager {
    private val manager = ReviewManagerFactory.create(context)

    override fun launchReviewFlow() {
        val activity = context as? Activity ?: return
        val request = manager.requestReviewFlow()

        request.addOnSuccessListener { reviewInfo ->
            // This launches the official native Google Play rating dialog card
            manager.launchReviewFlow(activity, reviewInfo)
        }
    }
}
