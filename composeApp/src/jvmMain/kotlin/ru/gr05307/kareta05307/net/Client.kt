// ru.gr05307.kareta05307.net/Client.kt
package ru.gr05307.kareta05307.net

import ru.gr05307.net.Communicator
import ru.gr05307.net.InfoType
import java.net.Socket
import kotlin.concurrent.thread

class Client(
    host: String = "127.0.0.1",
    port: Int = 5307,
) {
    private val messageListeners: MutableList<(String, String) -> Unit> = mutableListOf()
    private val privateMessageListeners: MutableList<(String, String) -> Unit> = mutableListOf() // NEW
    private val infoListeners: MutableList<(String, InfoType) -> Unit> = mutableListOf()
    private val disconnectListeners: MutableList<() -> Unit> = mutableListOf()
    private val userListListeners: MutableList<(List<String>) -> Unit> = mutableListOf() // NEW
    private val communicator: Communicator

    val isActive: Boolean
        get() = communicator.isActive


    init{
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

    // NEW: Private message listener
    fun addPrivateMessageListener(listener: (String, String) -> Unit) {
        privateMessageListeners.add(listener)
    }

    fun removePrivateMessageListener(listener: (String, String) -> Unit) {
        privateMessageListeners.remove(listener)
    }

    // NEW: User list listener
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
        val type = InfoType.valueOf(msg[0])
        when (type) {
            InfoType.INFORMATION,
            InfoType.WARNING,
            InfoType.ERROR -> infoListeners.forEach { it(msg[1], type) }

            InfoType.MESSAGE -> {
                val authMsg = msg[1].split(":", limit = 2)
                messageListeners.forEach { it(authMsg[0], authMsg[1]) }
            }

            // NEW: Handle private messages
            InfoType.PRIVATE -> {
                // Format: PRIVATE:sender:content
                val privateMsg = msg[1].split(":", limit = 2)
                if (privateMsg.size == 2) {
                    privateMessageListeners.forEach { it(privateMsg[0], privateMsg[1]) }
                }
            }

            // NEW: Handle user list updates
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