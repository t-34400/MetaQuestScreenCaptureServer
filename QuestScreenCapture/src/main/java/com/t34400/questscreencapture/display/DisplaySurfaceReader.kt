package com.t34400.questscreencapture.display

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.media.ImageReader
import android.os.IBinder
import com.t34400.questscreencapture.ImageUtils
import com.t34400.questscreencapture.server.IImageProvider
import com.t34400.questscreencapture.server.InputImage

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
class DisplaySurfaceReader : IImageProvider {
    // Retain a reference to IBinder to keep ImageReader's acquireLatestImage() functioning properly.
    private val displayToken: IBinder
    private val displayReader: ImageReader
    private val cropRect: com.t34400.questscreencapture.server.Rect

    private var pixels = IntArray(0)

    init {
        val displayInfo = DisplaySurfaceUtils.getDisplayInfo()
            ?: run {
                throw IllegalStateException("Failed to obtain display info.")
            }
        println("Display Info: $displayInfo")
        val size = displayInfo.size
        val aspect = size.width.toFloat() / size.height

        val inputImageWidth = (aspect * INPUT_IMAGE_HEIGHT).toInt()
        println("Input Surface Size: ($inputImageWidth, $INPUT_IMAGE_HEIGHT)")
        cropRect = com.t34400.questscreencapture.server.Rect(
            (NORMALIZED_CROP_LEFT * inputImageWidth).toInt(),
            (NORMALIZED_CROP_TOP * INPUT_IMAGE_HEIGHT).toInt(),
            (NORMALIZED_CROP_WIDTH * inputImageWidth).toInt(),
            (NORMALIZED_CROP_HEIGHT * INPUT_IMAGE_HEIGHT).toInt()
        )

        displayToken = DisplaySurfaceUtils.createDisplay()
        displayReader = ImageReader.newInstance(inputImageWidth, INPUT_IMAGE_HEIGHT, PixelFormat.RGBA_8888, 2)
        DisplaySurfaceUtils.setDisplaySurface(
            displayToken,
            displayReader.surface,
            inputImageWidth,
            INPUT_IMAGE_HEIGHT,
            displayInfo
        )
    }
    override fun getLatestImage(): InputImage? {
        return displayReader.acquireLatestImage()?.use { image ->
            pixels = ImageUtils.convertRGBA8888toPixels(image, pixels)
            val unixTime = System.currentTimeMillis()
            val width = image.width
            val height  = image.height

            InputImage(
                pixels,
                width, height,
                unixTime,
                cropRect
            )
        }
    }

    override fun close() {
        displayReader.close()
    }

    companion object {
        private const val INPUT_IMAGE_HEIGHT = 640
        private const val NORMALIZED_CROP_LEFT = 0.05f
        private const val NORMALIZED_CROP_TOP = 0.25f
        private const val NORMALIZED_CROP_WIDTH = 0.4f
        private const val NORMALIZED_CROP_HEIGHT = 0.5f
    }
}