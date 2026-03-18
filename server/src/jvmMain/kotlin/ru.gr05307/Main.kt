package ru.gr05307

import ru.gr05307.net.Server

fun main() {
    MainViewModel(Server(5307)).start()
}