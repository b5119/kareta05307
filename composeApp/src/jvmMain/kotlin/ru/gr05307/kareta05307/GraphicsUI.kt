package ru.gr05307.kareta05307

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    var username: String? by mutableStateOf(null)
    private var waitingForUsername = true

    var connectionError by mutableStateOf<String?>(null)
        private set

    fun setConnectionErrorMessage(error: String) {
        connectionError = error
    }

    fun clearConnectionError() {
        connectionError = null
    }

    override fun showMessage(author: String, msg: String) {
        messages.add(Message(author, msg, isFromMe = author == username))
    }

    override fun showInfo(msg: String, msgType: InfoType) {
        dialogMessage = msg
        when (msgType) {
            InfoType.INFORMATION -> {
                if (waitingForUsername && msg.startsWith("Добро пожаловать")) {
                    // Username accepted
                    username = extractUsernameFromWelcomeMessage(msg)
                    waitingForUsername = false
                    showDialog = false
                } else if(!waitingForUsername) {
                    messages.add(Message("", msg, false, InfoType.INFORMATION))
                    showDialog = false
                } else {
                    showDialog = true
                }
            }
            InfoType.WARNING -> {
                if (waitingForUsername) {
                    showDialog = true
                } else {
                    messages.add(Message("", msg, false, InfoType.WARNING))
                    showDialog = false
                }
            }
            InfoType.ERROR -> {
                if (waitingForUsername) {
                    showDialog = true
                } else {
                    messages.add(Message("", msg, false, InfoType.ERROR))
                    showDialog = false
                }
            }
            InfoType.MESSAGE -> {}
        }
    }

    private fun extractUsernameFromWelcomeMessage(msg: String): String? {
        // Message format: "Добро пожаловать, username!"
        val match = Regex("Добро пожаловать, (.+)!").find(msg)
        return match?.groupValues?.get(1)
    }

    override fun addUserDataListener(listener: (String) -> Unit) {
        listeners.add(listener)
    }

    override fun removeUserDataListener(listener: (String) -> Unit) {
        listeners.remove(listener)
    }

    override fun start() {
        showDialog = true
        dialogMessage = "Введите своё имя"
    }

    fun send() {
        val text = userText
        if (waitingForUsername) {
            // Validating username
            if (text.isBlank()) {
                dialogMessage = "Имя не может быть пустым или содержать только пробелы"
                return
            }
            if (text.contains(' ')) {
                dialogMessage = "Имя не должно содержать пробелов"
                return
            }
        }
        listeners.forEach { it(text) }
        if (!waitingForUsername) {
            userText = ""
        }
    }

    fun exit() {
        exitProcess(0)
    }
}