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

    fun addDataListener(listener: (String)->Unit) {
        dataListeners.add(listener)
    }

    fun removeDataListener(listener: (String)->Unit) {
        dataListeners.remove(listener)
    }

    var isActive = false
        private set

    fun sendData(data: String) {
        DataOutputStream(socket.getOutputStream()).let { dos ->
            dos.writeUTF(data)
            dos.flush()
        }
    }

    fun start(){
        thread {
            isActive = true
            DataInputStream(socket.getInputStream()).let { dis ->
                while (isActive) {
                    val userData = dis.readUTF()
                    dataListeners.forEach {
                        it(userData)
                    }
                }
            }
        }
    }

    fun stop(){isActive = false}
}