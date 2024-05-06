package com.t34400.questscreencapture.test

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.t34400.questscreencapture.ImageUtils
import com.t34400.questscreencapture.screenshot.ScreenShotData
import com.t34400.questscreencapture.screenshot.ScreenShotTaker
import com.t34400.questscreencapture.zxing.BarcodeScanner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class ZxingTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val inputType = args.getOrNull(0) ?: "SCREENSHOT"

            val screenshotData = when (inputType.uppercase()) {
                "SCREENSHOT" -> ScreenShotTaker.takeScreenShot()
                "FILE" -> ScreenShotData(
                    System.currentTimeMillis(),
                    BitmapFactory.decodeFile("/sdcard/Oculus/Screenshots/qr.png")
                )
                else -> null
            }
            screenshotData?.let { data ->
                run(data)
            }
        }

        private fun run(screenshotData: ScreenShotData) {
            val filePath = "/sdcard/Oculus/Screenshots/original.jpg"
            val file = File(filePath)
            FileOutputStream(file).use { fos ->
                screenshotData.screenShot.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
            }

            val scanner = BarcodeScanner()
            val inputImage = ImageUtils.convertBitmapToPixels(screenshotData.screenShot)
            scanner.processImage(
                inputImage,
                screenshotData.screenShot.width,
                screenshotData.screenShot.height,
                screenshotData.unixTime,
                null
            )?.let { packetWriter ->
                val baos = ByteArrayOutputStream()
                DataOutputStream(baos).use { dos ->
                    packetWriter.writePacket(dos)
                }

                val packet = baos.toByteArray()
                val bais = ByteArrayInputStream(packet)
                val dis = DataInputStream(bais)

                val rawValueSize = dis.readInt()
                val cornerCount = dis.readInt()
                val unixTime = dis.readLong()
                println("Raw Value Size: $rawValueSize")
                println("Corner Count: $cornerCount")
                println("Unix Time: $unixTime")

                val rawValueBytes = ByteArray(rawValueSize)
                dis.read(rawValueBytes)
                val rawValue = String(rawValueBytes, StandardCharsets.UTF_8)
                println("Raw Value: $rawValue")

                if (cornerCount == 0) {
                    return
                }

                val cornerPoints: MutableList<Pair<Float, Float>> = mutableListOf()
                for (i in 0 until cornerCount) {
                    val x = dis.readFloat()
                    val y = dis.readFloat()
                    cornerPoints.add(Pair(x, y))
                    println("Corner $i: ($x, $y)")
                }

                outputResultBitmap(screenshotData.screenShot, cornerPoints)
            }
        }

        private fun outputResultBitmap(bitmap: Bitmap, cornerPoints: List<Pair<Float, Float>>) {
            val screenShot = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val bitmapCanvas = Canvas(screenShot)
            val linePaint = Paint().apply  {
                color = Color.GREEN
                strokeWidth = 1.0f
            }
            val pointPaint = Paint().apply  {
                color = Color.RED
                strokeWidth = 2.0f
            }

            when (cornerPoints.size) {
                0 -> return
                1 -> {
                    val point = cornerPoints.first()
                    bitmapCanvas.drawPoint(point.first, point.second, pointPaint)
                }
                else -> {
                    val startPoint = cornerPoints.first()
                    val endpoint = cornerPoints.last()

                    var previousPoint = endpoint
                    for (i in 0 until cornerPoints.count()) {
                        linePaint.color = if (i % 2 == 0) Color.GREEN else Color.CYAN
                        val currentPoint = cornerPoints[i]
                        bitmapCanvas.drawLine(previousPoint.first, previousPoint.second, currentPoint.first, currentPoint.second, linePaint)
                        previousPoint = currentPoint
                    }

                    pointPaint.color = Color.RED
                    bitmapCanvas.drawPoint(startPoint.first, startPoint.second, pointPaint)
                    pointPaint.color = Color.BLUE
                    bitmapCanvas.drawPoint(endpoint.first, endpoint.second, pointPaint)
                }
            }

            val resultFilePath = "/sdcard/Oculus/Screenshots/result.jpg"
            FileOutputStream(File(resultFilePath)).use { fos ->
                screenShot.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
            }
        }
    }
}