package ru.gr05307.net

import java.net.Socket

class ConnectedClient(private val socket: Socket) {
    companion object {
        private val clients = mutableListOf<ConnectedClient>()
    }
    private val communicator = Communicator(socket)
    private var username : String? = null

    init{
        communicator.addDataListener { parseData(it) }
        clients.add(this)
        sendData("${InfoType.INFORMATION}:Введите своё имя")
    }

    private fun parseData(data: String) {
        if (username == null) {
            if (! clients.any { it.username == data })
                username = data
            else {
                sendData("${InfoType.WARNING}:Такое имя уже использовано.Введите другое имя.")
            }

        }
        else
            clients.forEach { it.sendData("${InfoType.MESSAGE}:$username: $data") }
    }

    fun start() = communicator.start()
    fun sendData(data: String) = communicator.sendData(data)
    fun stop() = communicator.stop()
}