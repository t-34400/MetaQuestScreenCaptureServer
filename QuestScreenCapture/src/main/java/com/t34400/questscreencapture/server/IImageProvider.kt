package com.t34400.questscreencapture.server

import java.io.Closeable

interface IImageProvider : Closeable {
    fun getLatestImage() : InputImage?
}