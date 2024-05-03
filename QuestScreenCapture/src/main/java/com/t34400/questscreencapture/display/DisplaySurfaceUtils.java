package com.t34400.questscreencapture.display;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Size;
import android.view.Surface;

import com.genymobile.scrcpy.DisplayInfo;
import com.genymobile.scrcpy.wrappers.DisplayManager;
import com.genymobile.scrcpy.wrappers.SurfaceControl;

import java.lang.reflect.Method;

@SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
public class DisplaySurfaceUtils {
    static {
        Looper.prepareMainLooper();
    }
    private DisplaySurfaceUtils() {}

    public static DisplayInfo getDisplayInfo() throws Exception {
        Class<?> clazz = Class.forName("android.hardware.display.DisplayManagerGlobal");
        Method getInstanceMethod = clazz.getDeclaredMethod("getInstance");
        Object dmg = getInstanceMethod.invoke(null);
        DisplayManager displayManager = new DisplayManager(dmg);

        return displayManager.getDisplayInfo(0);
    }

    public static IBinder createDisplay() {
        boolean secure = Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                || (Build.VERSION.SDK_INT == Build.VERSION_CODES.R
                && !"S".equals(Build.VERSION.CODENAME));
        return SurfaceControl.createDisplay("ScreenCopy", secure);
    }

    public static void setDisplaySurface(IBinder display, Surface surface, int width, int height, DisplayInfo displayInfo) {
        Size displaySize = displayInfo.getSize();
        Rect displayRect = new Rect(0, 0, displaySize.getWidth(), displaySize.getHeight());
        Rect surfaceRect = new Rect(0, 0, width, height);

        SurfaceControl.openTransaction();
        try {
            SurfaceControl.setDisplaySurface(display, surface);
            SurfaceControl.setDisplayProjection(display, displayInfo.getRotation(), displayRect, surfaceRect);
            SurfaceControl.setDisplayLayerStack(display, displayInfo.getLayerStack());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            SurfaceControl.closeTransaction();
        }
    }

    // Test
    public static void main(String[] args) {
        try {
            DisplayInfo displayInfo = getDisplayInfo();
            Size size = displayInfo.getSize();
            float aspect = (float) size.getWidth() / size.getHeight();

            int inputImageWidth = (int) (aspect * 640);

            try (ImageReader imageReader = ImageReader.newInstance(inputImageWidth, 640, PixelFormat.RGBA_8888, 2)) {
                IBinder displayToken = createDisplay();
                setDisplaySurface(displayToken, imageReader.getSurface(), inputImageWidth, 640, displayInfo);

                for (int i = 0; i < 30; ++i) {
                    Image image = imageReader.acquireLatestImage();
                    if (image == null) {
                        System.out.println("Failed to capture the display.");
                    } else {
                        System.out.println("Display captured: (" + image.getWidth() + ", " + image.getHeight() + ")");
                        image.close();
                    }
                    Thread.sleep(200L);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
