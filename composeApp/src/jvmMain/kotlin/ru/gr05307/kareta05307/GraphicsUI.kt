package ru.gr05307.kareta05307

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.application
import androidx.lifecycle.ViewModel
import ru.gr05307.kareta05307.ui.UI
import ru.gr05307.net.InfoType
import kotlin.system.exitProcess

class GraphicsUI : ViewModel(), UI {
    private val listeners: MutableList<(String) -> Unit> = mutableListOf()

    var showDialog by mutableStateOf(false)
    var dialogMessage by mutableStateOf("")
    var userText by mutableStateOf("")
    val messages = mutableStateListOf<Message>()

    override fun showMessage(author: String, msg: String) {
        messages.add(Message(author, msg))
    }

    override fun showInfo(msg: String, msgType: InfoType) {
        showDialog = true
        dialogMessage = msg
    }

    override fun addUserDataListener(listener: (String) -> Unit) {
        listeners.add(listener)
    }

    override fun removeUserDataListener(listener: (String) -> Unit) {
        listeners.remove(listener)
    }

    override fun start() {

    }

    fun send(){
        listeners.forEach { it(userText) }
        userText = ""
        showDialog = false
    }

    fun exit(){
        exitProcess(0)
    }
}