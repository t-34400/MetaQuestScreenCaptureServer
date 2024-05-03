package com.t34400.questscreencapture

import com.t34400.questscreencapture.display.DisplaySurfaceReader
import com.t34400.questscreencapture.screenshot.ScreenShotTaker
import com.t34400.questscreencapture.server.IImageProvider
import com.t34400.questscreencapture.server.ImageProcessServer
import com.t34400.questscreencapture.server.InputImage
import com.t34400.questscreencapture.zxing.BarcodeScanner
import java.util.Locale

class ServerLauncher {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                println("Usage: adb shell CLASSPATH=/data/local/tmp/build.apk app_process /system/bin com.t34400.questscreencapture.ServerLauncher <port> [<inputMethod=screenshot|display>]")
                return
            }

            val port = args.getOrNull(0)?.toIntOrNull() ?: 8010

            val imageProvider = when (args.getOrNull(1)?.uppercase(Locale.getDefault())) {
                "DISPLAY" -> DisplaySurfaceReader()
                else -> {
                    object : IImageProvider {
                        override fun getLatestImage(): InputImage? {
                            return ScreenShotTaker.takeScreenShot()?.let { screenshotData ->
                                val unixTime = screenshotData.unixTime
                                val width = screenshotData.screenShot.width
                                val height = screenshotData.screenShot.height
                                val pixels = ImageUtils.convertBitmapToPixels(screenshotData.screenShot)
                                InputImage(pixels, width, height, unixTime)
                            }
                        }
                        override fun close() {
                        }
                    }
                }
            }

            val imageProcessor = BarcodeScanner()

            println("Starting server on port $port")
            ImageProcessServer(port, imageProvider, imageProcessor).run()
            imageProvider.close()
        }
    }
}
