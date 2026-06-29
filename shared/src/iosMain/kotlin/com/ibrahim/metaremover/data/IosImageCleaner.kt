package com.ibrahim.metaremover.data

import com.ibrahim.metaremover.domain.ImageCleaner
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

class IosImageCleaner : ImageCleaner {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun clean(bytes: ByteArray): ByteArray {
        val nsData = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }

        val uiImage = UIImage.imageWithData(nsData) ?: return bytes
        val cleanedNsData = UIImageJPEGRepresentation(uiImage, 0.95) ?: return bytes

        val cleanedBytes = ByteArray(cleanedNsData.length.toInt())
        cleanedBytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), cleanedNsData.bytes, cleanedNsData.length)
        }

        return cleanedBytes
    }
}
