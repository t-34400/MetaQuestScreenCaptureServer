package com.t34400.questscreencapture.zxing

import android.graphics.PointF
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.common.HybridBinarizer
import com.t34400.questscreencapture.server.IImageProcessor
import com.t34400.questscreencapture.server.IPacketWriter
import com.t34400.questscreencapture.server.Rect

class BarcodeScanner : IImageProcessor {
    private val reader: MultiFormatReader = MultiFormatReader()

    init {
        val possibleFormats = listOf(BarcodeFormat.QR_CODE)
        val hints: HashMap<DecodeHintType?, List<BarcodeFormat?>?> =
            object : HashMap<DecodeHintType?, List<BarcodeFormat?>?>() {
                init {
                    put(DecodeHintType.POSSIBLE_FORMATS, possibleFormats)
                }
            }
        reader.setHints(hints)
    }

    private fun scanBarcode(source: LuminanceSource?): Result? {
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val result: Result = try {
            reader.decodeWithState(binaryBitmap)
        } catch (e: NotFoundException) {
            println("Barcode Not Found")
            return null
        }
        return result
    }

    override fun processImage(
        pixels: IntArray,
        width: Int,
        height: Int,
        unixTime: Long,
        cropRect: Rect?
    ): IPacketWriter? {
        val source = RGBLuminanceSource(width, height, pixels).apply {
            cropRect?.let { rect ->
                this.crop(rect.left, rect.top, rect.width, rect.height)
            }
        }

        return scanBarcode(source)?.let { result ->
            val cornerPoints = convertResultPointsToPointFs(result.resultPoints)
            BarcodeDataPacketWriter(unixTime, cornerPoints, result.text)
        }
    }

    private fun convertResultPointsToPointFs(resultPoints: Array<ResultPoint>): Array<PointF> {
        return resultPoints.map { resultPoint ->
            PointF(resultPoint.x, resultPoint.y)
        }.toTypedArray()
    }
}