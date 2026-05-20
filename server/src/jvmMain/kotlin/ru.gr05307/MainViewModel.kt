package ru.gr05307

import ru.gr05307.net.Server

class MainViewModel(private val server: Server) {
    fun start() {
        server.start()   // blocks — runs the accept loop
    }
}
