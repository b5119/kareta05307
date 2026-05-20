package ru.gr05307.net

import ru.gr05307.database.ChatMessageService
import java.net.Socket

class ConnectedClient(
    socket: Socket,
    private val clients: MutableList<ConnectedClient>,
    private val chatMessageService: ChatMessageService,
) {
    private val communicator = Communicator(socket)
    private var username: String? = null
    private var joined = false

    fun handle() {
        try {
            negotiateUsername()
            if (username == null) return
            joined = true

            sendHistory()

            val joinMsg = "Пользователь $username присоединился к чату."
            broadcast(InfoType.INFORMATION, joinMsg, exceptSelf = true)
            chatMessageService.saveMessage("SYSTEM", joinMsg, InfoType.INFORMATION.name)
            broadcastUserList()

            while (true) {
                val raw = communicator.receive() ?: break
                val (type, payload) = parseIncoming(raw) ?: continue

                when (type) {
                    InfoType.MESSAGE -> {
                        val formatted = "$username: $payload"
                        broadcast(InfoType.MESSAGE, formatted, exceptSelf = false)
                        chatMessageService.saveMessage(username!!, payload, InfoType.MESSAGE.name)
                    }

                    InfoType.PRIVATE -> handlePrivateMessage(payload)

                    InfoType.INFORMATION,
                    InfoType.WARNING,
                    InfoType.ERROR,
                    InfoType.USERLIST -> {}
                }
            }
        } catch (_: Exception) {
            // connection closed / reset
        } finally {
            if (joined) {
                val leaveMsg = "Пользователь $username покинул чат."
                broadcast(InfoType.INFORMATION, leaveMsg, exceptSelf = true)
                chatMessageService.saveMessage("SYSTEM", leaveMsg, InfoType.INFORMATION.name)
            }

            synchronized(clients) {
                clients.remove(this)
            }

            if (joined) {
                broadcastUserList()
            }

            communicator.stop()
        }
    }

    fun sendMessage(type: InfoType, text: String) {
        communicator.send("${type.name}:$text")
    }

    private fun broadcast(type: InfoType, text: String, exceptSelf: Boolean) {
        val snapshot = synchronized(clients) { clients.toList() }
        snapshot.forEach { client ->
            if (!exceptSelf || client !== this) {
                try {
                    client.sendMessage(type, text)
                } catch (_: Exception) {
                    // ignore broken client sockets during broadcast
                }
            }
        }
    }

    private fun broadcastUserList() {
        val snapshot = synchronized(clients) { clients.toList() }
        val payload = snapshot.mapNotNull { it.username }.sorted().joinToString(",")
        snapshot.forEach { client ->
            try {
                client.sendMessage(InfoType.USERLIST, payload)
            } catch (_: Exception) {
                // ignore broken client sockets during user list broadcast
            }
        }
    }

    private fun sendHistory() {
        val history = chatMessageService.getRecentMessages()
        if (history.isEmpty()) return

        sendMessage(InfoType.INFORMATION, "── История чата (последние ${history.size} сообщений) ──")
        history.forEach { msg ->
            val infoType = runCatching { InfoType.valueOf(msg.messageType) }
                .getOrDefault(InfoType.MESSAGE)
            val text = if (msg.messageType == InfoType.MESSAGE.name) {
                "${msg.senderName}: ${msg.content}"
            } else {
                msg.content
            }
            sendMessage(infoType, text)
        }
        sendMessage(InfoType.INFORMATION, "── Конец истории ──")
    }

    private fun negotiateUsername() {
        communicator.send("${InfoType.INFORMATION.name}:Введите своё имя")
        while (true) {
            val raw = communicator.receive() ?: return
            val candidate = raw.trim()

            when {
                candidate.isBlank() ->
                    communicator.send("${InfoType.WARNING.name}:Имя не может быть пустым")

                candidate.contains(' ') ->
                    communicator.send("${InfoType.WARNING.name}:Имя не должно содержать пробелы")

                else -> {
                    val reserved = synchronized(clients) {
                        if (clients.any { it !== this && it.username == candidate }) {
                            false
                        } else {
                            username = candidate
                            true
                        }
                    }

                    if (!reserved) {
                        communicator.send("${InfoType.WARNING.name}:Имя уже занято")
                    } else {
                        communicator.send("${InfoType.INFORMATION.name}:Добро пожаловать, $candidate!")
                        return
                    }
                }
            }
        }
    }

    private fun parseIncoming(raw: String): Pair<InfoType, String>? {
        if (raw.startsWith("/pm ")) {
            return InfoType.PRIVATE to raw.removePrefix("/pm ").trim()
        }

        val colon = raw.indexOf(':')
        if (colon < 0) return InfoType.MESSAGE to raw

        val typeName = raw.substring(0, colon)
        val payload = raw.substring(colon + 1)
        val type = runCatching { InfoType.valueOf(typeName) }.getOrNull() ?: return null
        return type to payload
    }

    private fun handlePrivateMessage(payload: String) {
        val recipientAndContent = payload.split(":", limit = 2)
        if (recipientAndContent.size != 2) {
            sendMessage(InfoType.WARNING, "Некорректный формат приватного сообщения")
            return
        }

        val recipient = recipientAndContent[0].trim()
        val content = recipientAndContent[1].trim()
        if (recipient.isBlank() || content.isBlank()) {
            sendMessage(InfoType.WARNING, "Некорректный формат приватного сообщения")
            return
        }

        val recipientClient = synchronized(clients) {
            clients.firstOrNull { it.username == recipient }
        }

        if (recipientClient == null) {
            sendMessage(InfoType.WARNING, "Пользователь $recipient не в сети")
            return
        }

        recipientClient.sendMessage(InfoType.PRIVATE, "${username}:${content}")
    }
}
