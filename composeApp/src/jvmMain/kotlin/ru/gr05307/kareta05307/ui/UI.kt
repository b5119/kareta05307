package ru.gr05307.kareta05307.ui

import ru.gr05307.net.InfoType


interface UI {
    fun showMessage(author: String, msg: String)
    fun showInfo(msg: String, msgType: InfoType)

    fun addUserDataListener(listener: (String) -> Unit)
    fun removeUserDataListener(listener: (String) -> Unit)

    fun start()
}