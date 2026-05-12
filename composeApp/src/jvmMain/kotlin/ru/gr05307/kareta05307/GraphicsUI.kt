// ru.gr05307.kareta05307/GraphicsUI.kt (Fixed - clear username after registration)
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

    var isDisconnected by mutableStateOf(false)
        private set

    private val _onlineUsers = mutableStateListOf<String>()
    val onlineUsers: List<String> = _onlineUsers

    var selectedPrivateUser by mutableStateOf<String?>(null)
        private set

    private val privateMessages = mutableMapOf<String, MutableList<Pair<String, String>>>()

    fun setConnectionErrorMessage(error: String) {
        connectionError = error
    }

    fun clearConnectionError() {
        connectionError = null
    }

    fun setDisconnected() {
        isDisconnected = true
    }

    fun updateOnlineUsers(users: List<String>) {
        println("DEBUG: Updating online users. New list: $users")
        println("DEBUG: Current online users size: ${_onlineUsers.size}")
        println("DEBUG: Current selected user: $selectedPrivateUser")

        // Clear and add all to trigger UI updates
        _onlineUsers.clear()
        _onlineUsers.addAll(users)

        println("DEBUG: After update, online users size: ${_onlineUsers.size}")

        // If selected user left the chat, clear selection
        val currentSelected = selectedPrivateUser
        if (currentSelected != null && !_onlineUsers.contains(currentSelected)) {
            println("DEBUG: Selected user $currentSelected left the chat. Clearing selection.")
            selectPrivateUser(null)
            // Add notification that user left
            messages.add(Message("", "Пользователь $currentSelected покинул чат", false, InfoType.INFORMATION))
        }
    }

    fun receivePrivateMessage(sender: String, content: String) {
        val conversation = privateMessages.getOrPut(sender) { mutableListOf() }
        conversation.add(sender to content)

        if (selectedPrivateUser == sender) {
            messages.add(Message(sender, content, isFromMe = false, InfoType.PRIVATE))
        }
    }

    fun sendPrivateMessage(recipient: String, content: String) {
        // Check if recipient is still online
        if (!_onlineUsers.contains(recipient) && recipient != username) {
            messages.add(Message("", "Ошибка: Пользователь $recipient не в сети", false, InfoType.WARNING))
            return
        }

        listeners.forEach { it("/pm $recipient $content") }

        val conversation = privateMessages.getOrPut(recipient) { mutableListOf() }
        conversation.add("Вы" to content)

        if (selectedPrivateUser == recipient) {
            messages.add(Message("Вы → $recipient", content, isFromMe = true, InfoType.PRIVATE))
        }

        userText = ""
    }

    fun selectPrivateUser(user: String?) {
        selectedPrivateUser = user
        if (user != null) {
            messages.clear()
            messages.add(Message("", "=== Приватный чат с $user ===", false, InfoType.INFORMATION))
            privateMessages[user]?.forEach { (author, content) ->
                val isFromMe = author == "Вы"
                val displayAuthor = if (isFromMe) "Вы → $user" else author
                messages.add(Message(displayAuthor, content, isFromMe, InfoType.PRIVATE))
            }
        } else {
            messages.clear()
            messages.add(Message("", "=== Публичный чат ===", false, InfoType.INFORMATION))
        }
    }

    fun isInPrivateChat(): Boolean = selectedPrivateUser != null

    fun getCurrentChatTarget(): String? = selectedPrivateUser

    override fun showMessage(author: String, msg: String) {
        if (selectedPrivateUser == null) {
            messages.add(Message(author, msg, isFromMe = author == username))
        }
    }

    override fun showInfo(msg: String, msgType: InfoType) {
        dialogMessage = msg
        when (msgType) {
            InfoType.INFORMATION -> {
                if (waitingForUsername && msg.startsWith("Добро пожаловать")) {
                    username = extractUsernameFromWelcomeMessage(msg)
                    waitingForUsername = false
                    showDialog = false
                    // FIX: Clear the input text after successful registration
                    userText = ""
                } else if(!waitingForUsername) {
                    if (selectedPrivateUser == null) {
                        messages.add(Message("", msg, false, InfoType.INFORMATION))
                    }
                    showDialog = false
                } else {
                    showDialog = true
                }
            }
            InfoType.WARNING -> {
                if (waitingForUsername) {
                    showDialog = true
                } else {
                    if (selectedPrivateUser == null) {
                        messages.add(Message("", msg, false, InfoType.WARNING))
                    }
                    showDialog = false
                }
            }
            InfoType.ERROR -> {
                if (waitingForUsername) {
                    showDialog = true
                } else {
                    if (selectedPrivateUser == null) {
                        messages.add(Message("", msg, false, InfoType.ERROR))
                    }
                    showDialog = false
                }
            }
            InfoType.MESSAGE, InfoType.PRIVATE, InfoType.USERLIST -> {}
        }
    }

    private fun extractUsernameFromWelcomeMessage(msg: String): String? {
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
        // FIX: Clear any existing text when starting
        userText = ""
    }

    fun send() {
        val text = userText
        if (waitingForUsername) {
            if (text.isBlank()) {
                dialogMessage = "Имя не может быть пустым или содержать только пробелы"
                return
            }
            if (text.contains(' ')) {
                dialogMessage = "Имя не должно содержать пробелов"
                return
            }
            listeners.forEach { it(text) }
            // FIX: Don't clear here - wait for successful registration in showInfo
            // The text will be cleared only after successful registration
        } else {
            if (text.isNotBlank()) {
                if (selectedPrivateUser != null) {
                    sendPrivateMessage(selectedPrivateUser!!, text)
                } else {
                    listeners.forEach { it(text) }
                    userText = ""
                }
            }
        }
    }

    fun exit() {
        exitProcess(0)
    }

    fun disconnect() {
        // Handle disconnect logic
        isDisconnected = true
    }
}
