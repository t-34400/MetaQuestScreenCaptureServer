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

    private const val SCREENSHOT_SAVE_WAIT_TIMEOUT = 5000
    private const val SCREENSHOT_SAVE_CHECK_INTERVAL = 200L

    private const val CAPTURE_INTERVAL = 1000L

    fun takeScreenShot(): ScreenShotData? {
        val originalScreenShotCount = getCurrentScreenShotFiles()?.size ?: 0
        val captured = captureScreen()
        if (captured) {
            return loadLatestScreenShot(originalScreenShotCount)
        }

        Thread.sleep(CAPTURE_INTERVAL)
        return null
    }

    private fun getCurrentScreenShotFiles(): Array<File>? {
        val directory = File(SCREENSHOT_DIRECTORY_PATH)
        return directory.listFiles { _, name -> name.endsWith(".jpg") }
    }

    private fun captureScreen(): Boolean {
        val process = Runtime.getRuntime().exec(TAKE_SCREENSHOT_COMMAND)
        val exitCode = process.waitFor().also {
            if (it != 0) {
                println("Failed to take a screenshot: Exit code = $it")
            }
        }
        return exitCode == 0
    }

    private fun loadLatestScreenShot(originalScreenshotCount: Int): ScreenShotData? {
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).apply {
            isLenient = false
        }

        val startTime = System.currentTimeMillis()
        var captureTimestamp = startTime
        var latestScreenshotFile: File? = null
        var latestScreenshotBitmap: Bitmap? = null

        while (latestScreenshotBitmap == null) {
            if (System.currentTimeMillis() - startTime >= SCREENSHOT_SAVE_WAIT_TIMEOUT) {
                println("Timeout occurred. Screenshots were not saved.")
                return null
            }

            latestScreenshotFile?.let {
                latestScreenshotBitmap = loadScreenShotFile(it)
            } ?: run {
                latestScreenshotFile = getLatestScreenShotFile(originalScreenshotCount, dateFormat)?.also {
                    captureTimestamp = System.currentTimeMillis()
                }
            }

            Thread.sleep(SCREENSHOT_SAVE_CHECK_INTERVAL)
        }

        latestScreenshotFile?.delete()
        return latestScreenshotBitmap?.let { bitmap ->
            ScreenShotData(captureTimestamp, bitmap)
        }
    }

    private fun getLatestScreenShotFile(originalScreenshotCount: Int, dateFormat: SimpleDateFormat) : File? {
        val screenshotFiles = getCurrentScreenShotFiles()
        val currentScreenshotCount = screenshotFiles?.size ?: 0
        var latestScreenshotFile: File? = null
        var latestDate: Date? = null

        if (currentScreenshotCount > originalScreenshotCount) {
            screenshotFiles?.forEach { file ->
                val fileName = file.nameWithoutExtension
                val date = try {
                    dateFormat.parse(fileName.substringAfter("-"))
                } catch (e: Exception) {
                    null
                }
                if (date != null && (latestDate == null || date.after(latestDate))) {
                    latestDate = date
                    latestScreenshotFile = file
                }
            }
        }

        return latestScreenshotFile
    }

    private fun loadScreenShotFile(screenshotFile: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(screenshotFile.absolutePath)
        } catch (e: Exception) {
            println("Error occurred while processing screenshot: ${e.message}")
            null
        }
    }
}