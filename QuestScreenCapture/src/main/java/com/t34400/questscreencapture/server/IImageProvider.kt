package com.t34400.questscreencapture.server

interface IImageProvider {
    fun getLatestImage() : InputImage?
}