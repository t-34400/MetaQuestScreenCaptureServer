package com.t34400.questscreencapture.server

data class Rect(val left: Int, val top: Int, val width: Int, val height: Int)
data class InputImage(val pixels: IntArray, val width: Int, val height: Int, val unixTime: Long, val cropRect: Rect? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InputImage

        if (!pixels.contentEquals(other.pixels)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (unixTime != other.unixTime) return false
        return cropRect == other.cropRect
    }

    override fun hashCode(): Int {
        var result = pixels.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + unixTime.hashCode()
        result = 31 * result + (cropRect?.hashCode() ?: 0)
        return result
    }
}