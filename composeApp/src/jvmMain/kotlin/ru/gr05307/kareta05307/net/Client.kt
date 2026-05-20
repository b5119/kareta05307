package ru.gr05307.kareta05307.net

import ru.gr05307.net.Communicator
import ru.gr05307.net.InfoType
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class Client(
    host: String = "127.0.0.1",
    port: Int = 5307,
) {
    private val messageListeners = CopyOnWriteArrayList<(String, String) -> Unit>()
    private val privateMessageListeners = CopyOnWriteArrayList<(String, String) -> Unit>()
    private val infoListeners = CopyOnWriteArrayList<(String, InfoType) -> Unit>()
    private val disconnectListeners = CopyOnWriteArrayList<() -> Unit>()
    private val userListListeners = CopyOnWriteArrayList<(List<String>) -> Unit>()
    private val communicator: Communicator

    val isActive: Boolean
        get() = communicator.isActive

    init {
        communicator = Communicator(Socket(host, port))
        communicator.addDataListener { parseData(it) }
        communicator.addDisconnectListener { onDisconnect() }
    }

    fun addMessageListener(listener: (String, String) -> Unit) {
        messageListeners.add(listener)
    }

    fun removeMessageListener(listener: (String, String) -> Unit) {
        messageListeners.remove(listener)
    }

    fun addPrivateMessageListener(listener: (String, String) -> Unit) {
        privateMessageListeners.add(listener)
    }

    fun removePrivateMessageListener(listener: (String, String) -> Unit) {
        privateMessageListeners.remove(listener)
    }

    fun addUserListListener(listener: (List<String>) -> Unit) {
        userListListeners.add(listener)
    }

    fun removeUserListListener(listener: (List<String>) -> Unit) {
        userListListeners.remove(listener)
    }

    fun addInfoListener(listener: (String, InfoType) -> Unit) {
        infoListeners.add(listener)
    }

    fun removeInfoListener(listener: (String, InfoType) -> Unit) {
        infoListeners.remove(listener)
    }

    fun addDisconnectListener(listener: () -> Unit) {
        disconnectListeners.add(listener)
    }

    fun removeDisconnectListener(listener: () -> Unit) {
        disconnectListeners.remove(listener)
    }

    fun sendData(data: String) {
        communicator.sendData(data)
    }

    fun parseData(data: String) {
        val msg = data.split(":", limit = 2)
        if (msg.size != 2) return

        val type = runCatching { InfoType.valueOf(msg[0]) }.getOrNull() ?: return
        when (type) {
            InfoType.INFORMATION,
            InfoType.WARNING,
            InfoType.ERROR -> infoListeners.forEach { it(msg[1], type) }

            InfoType.MESSAGE -> {
                val authMsg = msg[1].split(":", limit = 2)
                if (authMsg.size == 2) {
                    messageListeners.forEach { it(authMsg[0], authMsg[1]) }
                }
            }

            InfoType.PRIVATE -> {
                val privateMsg = msg[1].split(":", limit = 2)
                if (privateMsg.size == 2) {
                    privateMessageListeners.forEach { it(privateMsg[0], privateMsg[1]) }
                }
            }

            InfoType.USERLIST -> {
                val users = msg[1].split(",").filter { it.isNotEmpty() }
                userListListeners.forEach { it(users) }
            }
        }
    }

    private fun onDisconnect() {
        disconnectListeners.forEach { it() }
    }

    fun start() {
        communicator.start()
    }

    fun stop() {
        communicator.stop()
    }
}
