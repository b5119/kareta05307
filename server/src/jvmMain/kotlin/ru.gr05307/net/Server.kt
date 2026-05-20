package ru.gr05307.net

import ru.gr05307.database.ChatMessageService
import java.net.ServerSocket
import kotlin.concurrent.thread

class Server(
    private val chatMessageService: ChatMessageService,
    private val port: Int = 5307,
) {
    private val clients = mutableListOf<ConnectedClient>()
    private lateinit var serverSocket: ServerSocket

    fun start() {
        serverSocket = ServerSocket(port)
        println("Server listening on port $port")

        while (true) {
            val socket = serverSocket.accept()
            thread {
                val client = ConnectedClient(socket, clients, chatMessageService)
                synchronized(clients) { clients.add(client) }
                client.handle()
                synchronized(clients) { clients.remove(client) }
            }
        }
    }

    fun stop() {
        if (::serverSocket.isInitialized) serverSocket.close()
    }
}
