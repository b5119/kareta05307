// ru.gr05307.net/ConnectedClient.kt
package ru.gr05307.net

import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

class ConnectedClient(private val socket: Socket) {
    companion object {
        private val clients = ConcurrentHashMap<String, ConnectedClient>()

        // Broadcast user list to all connected clients
        private fun broadcastUserList() {
            val userList = clients.keys.joinToString(",")
            val message = "${InfoType.USERLIST}:$userList"
            clients.values.forEach { client ->
                if (!client.isDisconnected() && client.username != null) {
                    try {
                        client.sendData(message)
                    } catch (_: Exception) {
                        // Will be cleaned up in parseData
                    }
                }
            }
        }

        // Send private message to specific user
        private fun sendPrivateMessage(sender: String, recipient: String, content: String) {
            val recipientClient = clients[recipient]
            if (recipientClient != null && !recipientClient.isDisconnected()) {
                // Send to recipient
                recipientClient.sendData("${InfoType.PRIVATE}:$sender:$content")
                // Also send back to sender (so they see it in their chat)
                // val senderClient = clients[sender]
                // senderClient?.sendData("${InfoType.PRIVATE}:$recipient:$content")
            } else {
                val senderClient = clients[sender]
                senderClient?.sendData("${InfoType.ERROR}:Пользователь $recipient не в сети")
            }
        }
    }

    private val communicator = Communicator(socket)
    private var username: String? = null

    init {
        communicator.addDataListener { parseData(it) }
        // Don't add to clients until username is validated
        sendData("${InfoType.INFORMATION}:Введите своё имя")
    }

    private fun notifyJoin() {
        val joinMsg = "${InfoType.INFORMATION}:Пользователь ${username} присоединился к чату."
        val aliveClients = clients.values.filter { it !== this && !it.isDisconnected() }
        aliveClients.forEach { it.sendData(joinMsg) }
        broadcastUserList()
    }

    private fun notifyLeave(client: ConnectedClient) {
        val cName = client.username
        if (cName != null) {
            val leaveMsg = "${InfoType.INFORMATION}:Пользователь $cName покинул чат."
            val aliveClients = clients.values.filter { it !== client && !it.isDisconnected() }
            aliveClients.forEach { alive ->
                try {
                    alive.sendData(leaveMsg)
                } catch (_: Exception) {
                    // dead client cleaned by parseData call
                }
            }
            broadcastUserList()
        }
    }

    private fun handleDisconnect() {
        notifyLeave(this)
        if (username != null) {
            clients.remove(username)
        }
        broadcastUserList()
        stop()
        try {
            socket.close()
        } catch (_: Exception) {
        }
    }

    private fun parseData(data: String) {
        // Clean up disconnected clients
        val disconnectedClients = clients.values.filter { it.isDisconnected() }.toList()
        disconnectedClients.forEach { client ->
            notifyLeave(client)
            if (client.username != null) {
                clients.remove(client.username)
            }
        }

        // Handle username registration phase
        if (username == null) {
            when {
                data.isBlank() -> {
                    sendData("${InfoType.WARNING}:Имя не может быть пустым. Введите имя.")
                }
                data.trim() != data -> {
                    sendData("${InfoType.WARNING}:Имя не должно содержать пробелов в начале или конце. Введите другое имя.")
                }
                clients.containsKey(data) -> {
                    sendData("${InfoType.WARNING}:Такое имя уже использовано. Введите другое имя.")
                }
                else -> {
                    username = data
                    clients[data] = this
                    sendData("${InfoType.INFORMATION}:Добро пожаловать, $data!")
                    notifyJoin()
                }
            }
        } else {
            // Already registered - process messages
            processChatMessage(data)
        }
    }

    private fun processChatMessage(data: String) {
        // Check for private message command
        if (data.startsWith("/pm ") || data.startsWith("/private ")) {
            val prefix = if (data.startsWith("/pm ")) "/pm " else "/private "
            val withoutPrefix = data.removePrefix(prefix)
            val firstSpace = withoutPrefix.indexOf(' ')

            if (firstSpace > 0) {
                val recipient = withoutPrefix.substring(0, firstSpace)
                val content = withoutPrefix.substring(firstSpace + 1)
                if (content.isNotBlank()) {
                    // Send private message - server does NOT echo back
                    sendPrivateMessage(username!!, recipient, content)
                } else {
                    // prohibit sending empty messages
                    sendData("${InfoType.WARNING}:Нельзя отправить пустое сообщение")
                }
            } else {
                sendData("${InfoType.WARNING}:Использование: /pm имя_пользователя сообщение")
            }
        }
        // Regular public message
        else {
            clients.values.forEach { client ->
                if (client.username != null && !client.isDisconnected()) {
                    client.sendData("${InfoType.MESSAGE}:${username}: $data")
                }
            }
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