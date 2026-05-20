package ru.gr05307.net

import ru.gr05307.database.ChatMessageService
import ru.gr05307.net.Communicator
import ru.gr05307.net.InfoType
import java.net.Socket

class ConnectedClient(
    private val socket: Socket,
    private val clients: MutableList<ConnectedClient>,
    private val chatMessageService: ChatMessageService,
) {
    private val communicator = Communicator(socket)
    private var username: String? = null

    fun handle() {
        try {
            negotiateUsername()
            if (username == null) return

            // ── Send message history to this newly connected client ──
            sendHistory()

            // Announce join
            val joinMsg = "Пользователь $username присоединился к чату."
            broadcast(InfoType.INFORMATION, joinMsg, exceptSelf = true)
            chatMessageService.saveMessage("SYSTEM", joinMsg, InfoType.INFORMATION.name)

            // ── Main read loop ──
            while (true) {
                val raw = communicator.receive() ?: break       // null = disconnected
                val (type, payload) = parseIncoming(raw) ?: continue

                if (type == InfoType.MESSAGE) {
                    val formatted = "$username: $payload"
                    broadcast(InfoType.MESSAGE, formatted, exceptSelf = false)
                    chatMessageService.saveMessage(username!!, payload, InfoType.MESSAGE.name)
                }
            }
        } catch (_: Exception) {
            // connection closed / reset
        } finally {
            val leaveMsg = "Пользователь $username покинул чат."
            broadcast(InfoType.INFORMATION, leaveMsg, exceptSelf = true)
            chatMessageService.saveMessage("SYSTEM", leaveMsg, InfoType.INFORMATION.name)
            socket.close()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Send a formatted message only to THIS client. */
    fun sendMessage(type: InfoType, text: String) {
        communicator.send("${type.name}:$text")
    }

    /** Walk existing clients (under lock) and send to all (or all except self). */
    private fun broadcast(type: InfoType, text: String, exceptSelf: Boolean) {
        synchronized(clients) {
            clients.forEach { c ->
                if (!exceptSelf || c !== this) {
                    c.sendMessage(type, text)
                }
            }
        }
    }

    /** Replay last 50 messages from the database to this client only. */
    private fun sendHistory() {
        val history = chatMessageService.getRecentMessages()
        if (history.isEmpty()) return

        sendMessage(InfoType.INFORMATION, "── История чата (последние ${history.size} сообщений) ──")
        history.forEach { msg ->
            val infoType = runCatching { InfoType.valueOf(msg.messageType) }
                .getOrDefault(InfoType.MESSAGE)
            val text = if (msg.messageType == InfoType.MESSAGE.name)
                "${msg.senderName}: ${msg.content}"
            else
                msg.content
            sendMessage(infoType, text)
        }
        sendMessage(InfoType.INFORMATION, "── Конец истории ──")
    }

    /**
     * Username handshake.
     * Mirrors the existing protocol: server asks, client sends a name,
     * server validates (non-empty, no spaces, unique) and confirms.
     */
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

                synchronized(clients) { clients.any { it.username == candidate } } ->
                    communicator.send("${InfoType.WARNING.name}:Имя уже занято")

                else -> {
                    username = candidate
                    communicator.send("${InfoType.INFORMATION.name}:Добро пожаловать, $candidate!")
                    return
                }
            }
        }
    }

    /** Parse raw wire string into (InfoType, payload) or null if malformed. */
    private fun parseIncoming(raw: String): Pair<InfoType, String>? {
        val colon = raw.indexOf(':')
        if (colon < 0) return null
        val typeName = raw.substring(0, colon)
        val payload = raw.substring(colon + 1)
        val type = runCatching { InfoType.valueOf(typeName) }.getOrNull() ?: return null
        return type to payload
    }
}
