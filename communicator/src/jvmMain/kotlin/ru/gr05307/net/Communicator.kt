package ru.gr05307.net

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.concurrent.thread

class Communicator(
    private val socket: Socket,
) {
    private val input by lazy { DataInputStream(socket.getInputStream()) }
    private val output by lazy { DataOutputStream(socket.getOutputStream()) }

    private val dataListeners = mutableListOf<(String)->Unit>()
    private val disconnectListeners = mutableListOf<() -> Unit>()

    fun addDataListener(listener: (String)->Unit) {
        dataListeners.add(listener)
    }

    fun removeDataListener(listener: (String)->Unit) {
        dataListeners.remove(listener)
    }

    fun addDisconnectListener(listener: () -> Unit) {
        disconnectListeners.add(listener)
    }

    fun removeDisconnectListener(listener: () -> Unit) {
        disconnectListeners.remove(listener)
    }

    var isActive = false
        private set

    fun send(data: String) {
        sendData(data)
    }

    fun sendData(data: String) {
        try {
            output.writeUTF(data)
            output.flush()
        } catch (_: Exception) {
            // Connection likely broken, will be detected in read loop
        }
    }

    fun receive(): String? {
        return try {
            input.readUTF()
        } catch (_: Exception) {
            null
        }
    }

    fun start(){
        thread {
            isActive = true
            try {
                while (isActive) {
                    val userData = input.readUTF()
                    dataListeners.forEach {
                        it(userData)
                    }
                }
            } catch (_: Exception) {
                // Socket closed or connection lost
            } finally {
                isActive = false
                disconnectListeners.forEach { it() }
                try {
                    socket.close()
                } catch (_: Exception) {}
            }
        }
    }

    fun stop(){
        isActive = false
        try {
            socket.close()
        } catch (_: Exception) {}
    }
}
