package ru.gr05307.net

import ru.gr05307.database.ChatMessageService
import java.net.ServerSocket
import java.net.SocketException
import kotlin.concurrent.thread

class Server(
    private val chatMessageService: ChatMessageService,
    private val port: Int = 5307,
) {
    private val clients = mutableListOf<ConnectedClient>()
    private lateinit var serverSocket: ServerSocket
    @Volatile
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        serverSocket = ServerSocket(port)
        println("Server listening on port $port")

        try {
            while (isRunning) {
                val socket = serverSocket.accept()
                thread(name = "chat-client-${socket.port}") {
                    val client = ConnectedClient(socket, clients, chatMessageService)
                    synchronized(clients) { clients.add(client) }
                    client.handle()
                }
            }
        } catch (e: SocketException) {
            if (isRunning) throw e
        } finally {
            stop()
        }
    }

    fun stop() {
        if (!isRunning && (!::serverSocket.isInitialized || serverSocket.isClosed)) return
        isRunning = false
        if (::serverSocket.isInitialized && !serverSocket.isClosed) {
            serverSocket.close()
        }
    }
}
