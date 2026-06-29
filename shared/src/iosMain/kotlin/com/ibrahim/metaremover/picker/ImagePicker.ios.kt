package com.ibrahim.metaremover.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

actual class ImagePicker {
    @Composable
    actual fun pickImage(result: (ByteArray?) -> Unit): () -> Unit {
        // 1. Get the current iOS ViewController holding your Compose view
        val viewController = LocalUIViewController.current

        // 2. Remember the coordinator so it doesn't get garbage collected during the lifecycle
        val coordinator = remember { ImagePickerCoordinator(result) }

        return {
            // 3. Create and configure the native iOS Image Picker Controller
            val imagePickerController = UIImagePickerController().apply {
                sourceType =
                    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                delegate = coordinator
            }

            // 4. Present the native iOS view controller modally
            viewController.presentViewController(
                imagePickerController,
                animated = true,
                completion = null
            )
        }
    }
}

// 5. A Coordinator class acting as the delegate for the iOS Image Picker
private class ImagePickerCoordinator(
    private val onImagePicked: (ByteArray?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        // Get the selected image
        val image =
            didFinishPickingMediaWithInfo[platform.UIKit.UIImagePickerControllerOriginalImage] as? UIImage

        if (image != null) {
            // Convert UIImage to NSData (JPEG format, 1.0 is maximum quality)
            val nsData = UIImageJPEGRepresentation(image, 1.0)

            if (nsData != null) {
                // Convert NSData to Kotlin ByteArray
                val bytes = ByteArray(nsData.length.toInt())

                // Corrected from 'usePin' to 'usePinned'
                bytes.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                }
                onImagePicked(bytes)
            } else {
                onImagePicked(null)
            }
        } else {
            onImagePicked(null)
        }

        // Dismiss the picker UI
        picker.dismissViewControllerAnimated(true, completion = null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onImagePicked(null)
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}