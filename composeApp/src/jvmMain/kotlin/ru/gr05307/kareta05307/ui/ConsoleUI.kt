package ru.gr05307.ui

import kotlin.concurrent.thread

class ConsoleUI : UI {
    private val listeners: MutableList<(String) -> Unit> = mutableListOf()
    private var isActive: Boolean = false

    override fun showMessage(author: String, msg: String) {
        println("$author: $msg")
    }

    override fun showInfo(msg: String, msgType: InfoType) {
        println("$msgType: $msg")
    }

    override fun showRequest(request: RequestType) {
        when (request) {
            RequestType.USERNAME -> {
                println("Введите ваше имя")
            }
        }
    }

    override fun addUserDataListener(listener: (String) -> Unit) {
        listeners.add(listener)
    }

    override fun removeUserDataListener(listener: (String) -> Unit) {
        listeners.remove(listener)
    }

    override fun start() {
        thread {
            isActive = true
            while (isActive) {
                val userData = readln()
                listeners.forEach {
                    it(userData)
                }
            }
        }
    }

    fun stop() {
        isActive = false
    }
}