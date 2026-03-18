package ru.gr05307.net

import java.net.ServerSocket
import kotlin.concurrent.thread

class Server(port: Int) {
    private val serverSocket = ServerSocket(port)


    var isActive = false
        private set


    fun start() {
        thread {
            isActive = true
            while (isActive) {
                ConnectedClient(serverSocket.accept()).start()
            }
        }
    }
}