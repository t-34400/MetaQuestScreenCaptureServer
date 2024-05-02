package com.t34400.questscreencapture.zxing

import android.graphics.PointF
import com.t34400.questscreencapture.server.IPacketWriter
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

data class BarcodeDataPacketWriter(val unixTime: Long, val cornerPoints: Array<PointF>, val rawValue: String) : IPacketWriter {
    override fun writePacket(outputStream: DataOutputStream) {
        val rawValueBytes = rawValue.toByteArray(StandardCharsets.UTF_8)
        val rawValueSize = rawValueBytes.size
        val cornerPointCount = cornerPoints.size

        val totalSize = META_DATA_SIZE + rawValueSize + 4 * 2 * cornerPointCount
        val outputBytes = ByteArray(totalSize)

        val buffer = ByteBuffer.wrap(outputBytes)
            .putInt(rawValueSize)
            .putInt(cornerPointCount)
            .putLong(unixTime)
            .put(rawValueBytes)
        cornerPoints.forEach { cornerPoint ->
            buffer.putFloat(cornerPoint.x).putFloat(cornerPoint.y)
        }

        outputStream.write(outputBytes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BarcodeDataPacketWriter

        if (unixTime != other.unixTime) return false
        if (!cornerPoints.contentEquals(other.cornerPoints)) return false
        return rawValue == other.rawValue
    }

    override fun hashCode(): Int {
        var result = unixTime.hashCode()
        result = 31 * result + cornerPoints.contentHashCode()
        result = 31 * result + rawValue.hashCode()
        return result
    }

    companion object {
        const val META_DATA_SIZE = 16 // 4 (totalSize) + 4 (cornerCount) + 8 (unixTime)
    }
}
