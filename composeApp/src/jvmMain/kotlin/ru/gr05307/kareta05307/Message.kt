package ru.gr05307.kareta05307

import ru.gr05307.net.InfoType

data class Message (
    val author: String,
    val msg: String,
    val isFromMe: Boolean = false,
    val messageType: InfoType = InfoType.MESSAGE,
)
