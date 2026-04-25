package ru.gr05307.net

import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class Communicator(
    private val socket: Socket,
) {

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

    fun sendData(data: String) {
        try {
            DataOutputStream(socket.getOutputStream()).let { dos ->
                dos.writeUTF(data)
                dos.flush()
            }
        } catch (_: Exception) {
            // Connection likely broken, will be detected in read loop
        }
    }

    fun start(){
        thread {
            isActive = true
            try {
                DataInputStream(socket.getInputStream()).let { dis ->
                    while (isActive) {
                        val userData = dis.readUTF()
                        dataListeners.forEach {
                            it(userData)
                        }
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