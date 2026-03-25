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
    private val infoListeners: MutableList<(String, InfoType) -> Unit> = mutableListOf()
    private val communicator: Communicator

    val isActive: Boolean
        get() = communicator.isActive


    init{
        communicator = Communicator(Socket(host, port))
        //communicator.addDataListener(::parseData)
        communicator.addDataListener { parseData(it) }
    }

    fun addMessageListener(listener: (String, String) -> Unit) {
        messageListeners.add(listener)
    }

    fun removeMessageListener(listener: (String, String) -> Unit) {
        messageListeners.remove(listener)
    }

    fun addInfoListener(listener: (String, InfoType) -> Unit) {
        infoListeners.add(listener)
    }

    fun removeInfoListener(listener: (String, InfoType) -> Unit) {
        infoListeners.remove(listener)
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
            InfoType.ERROR -> infoListeners.forEach {it(msg[1], type)}
            InfoType.MESSAGE -> {
                println(data)
                val authMsg = msg[1].split(":", limit = 2)
                messageListeners.forEach { it(authMsg[0], authMsg[1]) }
            }
        }
    }

    fun start() {
        communicator.start()
    }

    fun stop() {
        communicator.stop()
    }

}