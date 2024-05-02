package com.t34400.questscreencapture.screenshot

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScreenShotData(val unixTime: Long, val screenShot: Bitmap)

object ScreenShotTaker {
    private const val TAKE_SCREENSHOT_COMMAND =
        "am startservice " +
                "-n com.oculus.metacam/.capture.CaptureService " +
                "-a TAKE_SCREENSHOT " +
                "--ei screenshot_height 1024 " +
                "--ei screenshot_width 1024 "

    @SuppressLint("SdCardPath")
    private const val SCREENSHOT_DIRECTORY_PATH = "/sdcard/Oculus/Screenshots/"
    private const val SCREENSHOT_PREFIX = "com.oculus.vrshell-"

    private const val SCREENSHOT_SAVE_WAIT_TIMEOUT = 2000
    private const val SCREENSHOT_SAVE_CHECK_INTERVAL = 100L

    fun takeScreenShot(): ScreenShotData? {
        val originalScreenShotCount = getCurrentScreenShotFiles()?.size ?: 0
        val captured = captureScreen()
        if (captured) {
            val timestamp = System.currentTimeMillis()
            waitForScreenshotSave(originalScreenShotCount)?.let { screenshotFile ->
                val bitmap = loadScreenShotFile(screenshotFile)
                screenshotFile.delete()
                bitmap?.let {
                    return ScreenShotData(timestamp, bitmap)
                }
            }
        }

        return null
    }

    private fun getCurrentScreenShotFiles(): Array<File>? {
        val directory = File(SCREENSHOT_DIRECTORY_PATH)
        return directory.listFiles { _, name ->
            name.startsWith(SCREENSHOT_PREFIX) && name.endsWith(
                ".jpg"
            )
        }
    }

    private fun captureScreen(): Boolean {
        val process = Runtime.getRuntime().exec(TAKE_SCREENSHOT_COMMAND)
        val exitCode = process.waitFor()
        return exitCode == 0
    }

    private fun waitForScreenshotSave(originalScreenShotCount: Int): File? {
        val startTime = System.currentTimeMillis()
        var screenshotFiles = getCurrentScreenShotFiles()
        var currentCount = screenshotFiles?.size ?: 0

        while (currentCount <= originalScreenShotCount) {
            if (System.currentTimeMillis() - startTime >= SCREENSHOT_SAVE_WAIT_TIMEOUT) {
                println("Timeout occurred. Screenshots were not saved.")
                return null
            }
            screenshotFiles = getCurrentScreenShotFiles()
            currentCount = screenshotFiles?.size ?: 0

            Thread.sleep(SCREENSHOT_SAVE_CHECK_INTERVAL)
        }


        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        dateFormat.isLenient = false
        var latestScreenshot: File? = null
        var latestDate: Date? = null

        screenshotFiles?.forEach { file ->
            val fileName = file.nameWithoutExtension
            val date = try {
                dateFormat.parse(fileName.substringAfter("-"))
            } catch (e: Exception) {
                null
            }
            if (date != null && (latestDate == null || date.after(latestDate))) {
                latestDate = date
                latestScreenshot = file
            }
        }

        return latestScreenshot
    }

    private fun loadScreenShotFile(screenshotFile: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(screenshotFile.absolutePath) ?: run {
                println("Failed to decode screenshot.")
                null
            }
        } catch (e: Exception) {
            println("Error occurred while processing screenshot: ${e.message}")
            null
        }
    }
}