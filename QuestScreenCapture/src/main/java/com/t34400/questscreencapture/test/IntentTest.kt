package com.t34400.questscreencapture.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PixelFormat
import android.media.ImageReader
import android.os.Binder
import android.os.Bundle
import android.os.IInterface


@SuppressLint("PrivateApi")
class IntentTest {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val imageReader = ImageReader.newInstance(1024, 1024, PixelFormat.RGBA_8888, 2);

            val intent = Intent("com.oculus.systemactivities.SCREENSHOT").apply {
                putExtra("delayInSeconds", 0)
                putExtra("intent_pkg", "com.android.shell")
                putExtra("surface", imageReader.surface)
                setPackage("com.oculus.companion.server")
            }

            sendBroadcast(intent)

            for (i in 0 until 300) {
                val image = imageReader.acquireLatestImage()
                println("Image is null? ${image == null}")
                Thread.sleep(100L)
            }
        }

        private fun sendBroadcast(intent: Intent) {
            val iActivityManagerClass = Class.forName("android.app.IActivityManager")
            val iApplicationThreadClass = Class.forName("android.app.IApplicationThread")
            val iIntentReceiverClass = Class.forName("android.content.IIntentReceiver")

            /*
            val sendBroadcastMethod = iActivityManagerClass.getMethod(
                "broadcastIntent",
                iApplicationThreadClass,
                Intent::class.java,
                String::class.java,
                iIntentReceiverClass,
                Int::class.java,
                String::class.java,
                Bundle::class.java,
                String::class.java,
                Int::class.java,
                Bundle::class.java,
                Boolean::class.java,
                Boolean::class.java,
                Int::class.java
            )
             */
            val sendBroadcastMethod = iActivityManagerClass.methods.find { method -> method.name == "broadcastIntent" }
            if (sendBroadcastMethod == null) {
                println("Fail")
                return
            }

            val manager = getIActivityManager()

            sendBroadcastMethod.invoke(
                manager,
                null,
                intent,
                null,
                null,
                0,
                null,
                null,
                null,
                -1,
                null,
                true,
                false,
                android.os.Process.myUid() / 100000
            )
        }

        @SuppressLint("DiscouragedPrivateApi")
        private fun getIActivityManager(): IInterface? {
            val activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative")
            val getDefaultMethod = activityManagerNativeClass.getDeclaredMethod("getDefault")
            return getDefaultMethod.invoke(null) as IInterface
        }
    }
}