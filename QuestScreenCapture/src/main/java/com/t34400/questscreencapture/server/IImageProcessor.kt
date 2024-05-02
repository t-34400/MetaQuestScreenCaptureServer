package com.t34400.questscreencapture.server

interface IImageProcessor {
    fun processImage(pixels: IntArray, width: Int, height: Int, unixTime: Long, cropRect: Rect?): IPacketWriter?
}