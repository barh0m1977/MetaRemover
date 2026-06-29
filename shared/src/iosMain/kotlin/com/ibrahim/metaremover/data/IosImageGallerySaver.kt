package com.ibrahim.metaremover.data

import com.ibrahim.metaremover.domain.ImageGallerySaver
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume

class IosImageGallerySaver : ImageGallerySaver {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun saveImage(bytes: ByteArray, fileName: String): Boolean = suspendCancellableCoroutine { cont ->
        if (bytes.isEmpty()) {
            cont.resume(false)
            return@suspendCancellableCoroutine
        }

        val nsData = bytes.usePinned {
            NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong())
        }

        val uiImage = nsData?.let { UIImage.imageWithData(it) }
        if (uiImage == null) {
            cont.resume(false)
            return@suspendCancellableCoroutine
        }

        PHPhotoLibrary.sharedPhotoLibrary().performChanges(
            {
                PHAssetChangeRequest.creationRequestForAssetFromImage(uiImage)
            },
            completionHandler = { success, error ->
                cont.resume(success)
            }
        )
    }
}
