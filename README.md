# Meta Quest Screen Barcode Reader
This repository contains scripts to set up a server that reads barcodes from Meta Quest's screen and sends them to clients. 
It enables barcode reading from the front camera by combining it with pass-through functionality.

## ⚠️ Deprecation Notice ⚠️

As of **August 12, 2024**, Meta has re-enabled the **Media Projection API**, which allows direct screen capture on Meta Quest devices. This means that using ADB for screen capture is no longer necessary. This library was initially created to address the lack of screen capture functionality when the Media Projection API was disabled, but it is now largely obsolete.

**We recommend utilizing the Media Projection API for a more straightforward and efficient solution.**

## Demo
https://github.com/t-34400/MetaQuestScreenBarcodeReader/assets/49368264/3b8a5c36-1c15-481e-8127-53306258a3d8

https://github.com/t-34400/MetaQuestScreenBarcodeReader/assets/49368264/721a8a5b-017a-46c2-8c6d-474358b415a0

## Usage
### Preparation
1. [Enable USB Debugging on Meta Quest](https://developer.oculus.com/documentation/native/android/mobile-device-setup/)
2. [Install ADB and Meta Quest drivers on your PC](https://developer.oculus.com/documentation/native/android/ts-adb/)

### Starting the Server
1. Download the latest APK from the repository's Release.
2. Enable USB Debugging on Meta Quest.
3. Connect your Meta Quest via wired/wireless, and execute the following command to send the APK to the device (first time only).
   ```bash
   adb push <path_to_apk> /data/local/tmp
   ```
4. Execute the following command to start the server.
   ```bash
   adb shell CLASSPATH=/data/local/tmp/build.apk app_process /system/bin com.t34400.questscreencapture.ServerLauncher <port> [<screenshot|display>]
   ```
   - The first option is the TCP port of the server (mandatory).
   - The second option is the input image mode (default: Screenshot).
      - Screenshot:
         - Capture the screen via storage using Meta Quest's screenshot feature.
         - No barrel distortion.
         - System shutter sound is played every time an image is captured.
      - Display:
         - Capture the screen from the display buffer.
         - Barrel distortion is present.

### Connecting to the Server
1. Connect to the TCP port specified when the server was started from the app using the scan results.
2. Write 0 as a 4-byte big-endian integer to start scanning for barcodes, and write 1 to stop scanning (ongoing scans cannot be stopped).
3. Upon each scan completion, the server will send the following data to the client:
   - If the scan succeeds:
      - Size of the barcode's Raw Value (4-byte big-endian integer)
      - Number of detected feature points (4-byte big-endian integer)
      - Unix Time [ms] when the image was captured (8-byte big-endian integer)
      - Raw Value (big-endian, UTF8)
      - Positions x, y of feature points (as many 4-byte big-endian floating-point numbers as detected)
   - If the scan fails, -1 will be sent as a 4-byte big-endian integer.

Unity sample package for client scripts is available for download from the repository's Release. 

## Screen to Camera Coordinate Transformation
To transform the position on the screenshot to real-world coordinates relative to the center camera, the following transformation is applied:
```math
(x_c, y_c, \text{depth}) \mapsto (X, Y, \text{depth})
```

Where:
- $`(x_c, y_c)`$: Coordinates on the screenshot, normalized to the range [0, 1].
- depth: Depth from the center camera.
- X, Y: Vertical relative positions with respect to the camera.

The transformation was empirically measured and fitted with a second-order polynomial, yielding the following equations:

| Polynomial   | Coefficient(X) | Coefficient(Y) |
|--------------|----------------|----------------|
| Constant     | -0.034031      | 0.000808       |
| $x_c$        | -0.002005      | -0.002539      |
| $y_c$        | -0.000078      | 0.002363       |
| $depth$      | -0.834209      | 0.836051       |
| $x_c^2$      | 0.000228       | 0.000794       |
| $x_c * y_c$  | 0.002781       | -0.003242      |
| $x_c * depth$| 1.675339       | 0.003726       |
| $y_c^2$      | 0.000195       | -0.001518      |
| $y_c * depth$| -0.001481      | -1.673221      |
| $depth^2$    | -0.002061      | -0.001475      |

## License
[MIT License](LICENSE)

Please refer to the [Notes](#notes) section for third-party licenses.

## Notes
- This script requires enabling USB debugging in developer mode, as it is executed via ADB shell commands.
- At the moment, only QR codes are detected.
- The [scrcpy](./QuestScreenCapture/src/main/java/com/genymobile/scrcpy) directory contains third-party code distributed under the Apache License. For details, please refer to the [NOTICE](./QuestScreenCapture/src/main/java/com/genymobile/NOTICE) file in this directory.
- Barcode detection is performed using the [Zxing](https://github.com/zxing/zxing) library. Please ensure compliance with its license terms if you use it.

##  Acknowledgements

In this repository, we utilize the source code from [Genymobile/scrcpy](https://github.com/Genymobile/scrcpy) and the [Zxing](https://github.com/zxing/zxing) library. I would like to express my gratitude to the developers for their contributions.

Additionally, the 3D models of the fruits used in the demo video is borrowed from [Quaternius](https://quaternius.com/index.html). I am deeply grateful to the author for their work.

Thank you to the developers and the author for their invaluable contributions.

## Previous Version
[MetaQuestScreenBarcodeReader](https://github.com/t-34400/MetaQuestScreenBarcodeReader)
