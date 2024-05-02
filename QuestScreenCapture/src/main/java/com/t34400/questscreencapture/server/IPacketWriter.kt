package com.t34400.questscreencapture.server

import java.io.DataOutputStream

interface IPacketWriter {
    fun writePacket(outputStream: DataOutputStream)
}