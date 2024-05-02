package com.t34400.questscreencapture.server

import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

const val SERVER_SOCKET_TIMEOUT = 2000
const val SERVER_SOCKET_ACCEPT_INTERVAL = 100L

class ImageProcessServer(
    private val port: Int,
    private val imageProvider: IImageProvider,
    private val imageProcessor: IImageProcessor,
) {
    private var isRunning = false
    fun run() {
        try {
            ServerSocket(port).use { serverSocket ->
                serverSocket.soTimeout = SERVER_SOCKET_TIMEOUT
                isRunning = true
                println("Run server. port=$port")
                runServerLoop(serverSocket)
            }
        } catch (e: IOException) {
            println("Failed to start a server (port=$port): ${e.message}")
        }
    }

    private fun runServerLoop(serverSocket: ServerSocket) {
        while (isRunning) {
            try {
                System.out.flush()
                serverSocket.accept().use { clientSocket ->
                    println("Accept a client socket. remotePort=" + clientSocket.port)
                    runScanLoop(clientSocket)
                }
            } catch (e: SocketTimeoutException) {
                Thread.sleep(SERVER_SOCKET_ACCEPT_INTERVAL)
                continue
            } catch (e: IOException) {
                println("Failed to accept a client socket: ${e.message}")
            }
        }
    }

    private fun runScanLoop(clientSocket: Socket) {
        try {
            InputStreamReader(clientSocket.getInputStream()).use { inputStreamReader ->
                clientSocket.getOutputStream().use { outputStream ->
                    DataOutputStream(outputStream).use { dataOutputStream ->
                        var isScanning = false
                        while (isRunning) {
                            if (inputStreamReader.ready()) {
                                val c = inputStreamReader.read()
                                isScanning = if (c == -1) {
                                    break
                                } else c == 0
                            }

                            if (!isScanning) {
                                try {
                                    Thread.sleep(50)
                                    continue
                                } catch (e: InterruptedException) {
                                    return
                                }
                            }

                            val image = imageProvider.getLatestImage()
                                ?: try {
                                    Thread.sleep(50)
                                    continue
                                } catch (e: InterruptedException) {
                                    return
                                }

                            imageProcessor.processImage(image.pixels, image.width, image.height, image.unixTime, image.cropRect)
                                ?.writePacket(dataOutputStream)
                                ?: run {
                                // Check if client's alive
                                dataOutputStream.writeInt(-1)
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            println("Scan loop interrupted: ${e.message}")
        }
    }
}