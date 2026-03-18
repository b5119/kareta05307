package ru.gr05307.kareta05307.ui

import ru.gr05307.net.InfoType
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