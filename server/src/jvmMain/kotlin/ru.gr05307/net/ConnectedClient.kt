package ru.gr05307.net

import java.net.Socket

class ConnectedClient(private val socket: Socket) {
    companion object {
        private val clients = mutableListOf<ConnectedClient>()
    }
    private val communicator = Communicator(socket)
    private var username: String? = null

    init {
        communicator.addDataListener { parseData(it) }
        clients.add(this)
        sendData("${InfoType.INFORMATION}:Введите своё имя")
    }

    private fun notifyLeave(client: ConnectedClient) {
        val cName = client.username
        if (cName != null) {
            val leaveMsg = "${InfoType.INFORMATION}:Пользователь $cName покинул чат."
            val aliveClients = clients.filter {it !== client && !it.isDisconnected()}.toList()
            aliveClients.forEach { alive ->
                try {
                    alive.sendData(leaveMsg)
                } catch (_: Exception) {
                    // dead client cleaned by parseData call
                }
            }
        }
    }

    private fun handleDisconnect() {
        notifyLeave(this)
        clients.remove(this)
        stop()
        try {
            socket.close()
        } catch (_: Exception) {
        }
    }

    private fun parseData(data: String) {
        // clean up disconnected clients
        val disconnectedClients = clients.filter { it.isDisconnected() }.toList()
        disconnectedClients.forEach { client ->
            notifyLeave(client)
            clients.remove(client)
        }

        if (username == null) {
            when {
                data.isBlank() -> {
                    sendData("${InfoType.WARNING}:Имя не может быть пустым. Введите имя.")
                }
                data.trim() != data -> {
                    sendData("${InfoType.WARNING}:Имя не должно содержать пробелов в начале или конце. Введите другое имя.")
                }
                clients.any { it !== this && it.username == data } -> {
                    sendData("${InfoType.WARNING}:Такое имя уже использовано. Введите другое имя.")
                }
                else -> {
                    username = data
                    sendData("${InfoType.INFORMATION}:Добро пожаловать, $data!")
                    clients.forEach {
                        if (it !== this) {
                            it.sendData("${InfoType.INFORMATION}:Пользователь $data присоединился к чату.")
                        }
                    }
                }
            }
        } else {
            clients.forEach { if (it.username != null) it.sendData("${InfoType.MESSAGE}:$username: $data") }
        }
    }

    fun start() = communicator.start()
    fun sendData(data: String) {
        try {
            communicator.sendData(data)
        } catch (_: Exception) {
            handleDisconnect()
        }
    }
    fun stop() = communicator.stop()

    fun isDisconnected(): Boolean {
        return socket.isClosed || !socket.isConnected
    }
}