package ru.gr05307

import ru.gr05307.net.Server

class MainViewModel(
    val server: Server,
) {
    fun start(){
        server.start()
    }
}