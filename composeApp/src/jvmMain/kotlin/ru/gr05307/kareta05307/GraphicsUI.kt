// ru.gr05307.kareta05307/GraphicsUI.kt
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

    // NEW: User list and private message state
    private val _onlineUsers = mutableStateListOf<String>()
    val onlineUsers: List<String> = _onlineUsers

    var selectedPrivateUser by mutableStateOf<String?>(null)
        private set

    // Store messages per user (for conversation view)
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

    // NEW: Update online users list
    fun updateOnlineUsers(users: List<String>) {
        _onlineUsers.clear()
        _onlineUsers.addAll(users)
    }

    // NEW: Receive private message
    fun receivePrivateMessage(sender: String, content: String) {
        val conversation = privateMessages.getOrPut(sender) { mutableListOf() }
        conversation.add(sender to content)

        // If this conversation is currently selected, show in main messages
        if (selectedPrivateUser == sender) {
            messages.add(Message(sender, content, isFromMe = false, InfoType.PRIVATE))
        }
    }

    // NEW: Send private message
    fun sendPrivateMessage(recipient: String, content: String) {
        // Send via command
        listeners.forEach { it("/pm $recipient $content") }
        // Add to local conversation
        val conversation = privateMessages.getOrPut(recipient) { mutableListOf() }
        conversation.add("Вы" to content)
        // Show in main messages if this conversation is selected
        if (selectedPrivateUser == recipient) {
            messages.add(Message("Вы → $recipient", content, isFromMe = true, InfoType.PRIVATE))
        }
        userText = ""
    }

    // NEW: Select user for private chat
    fun selectPrivateUser(user: String?) {
        selectedPrivateUser = user
        if (user != null) {
            // Clear current messages and show selected conversation
            messages.clear()
            // Add system message
            messages.add(Message("", "=== Приватный чат с $user ===", false, InfoType.INFORMATION))
            // Load conversation history
            privateMessages[user]?.forEach { (author, content) ->
                val isFromMe = author == "Вы"
                val displayAuthor = if (isFromMe) "Вы → $user" else author
                messages.add(Message(displayAuthor, content, isFromMe, InfoType.PRIVATE))
            }
        } else {
            // Back to public chat
            messages.clear()
            messages.add(Message("", "=== Публичный чат ===", false, InfoType.INFORMATION))
            // Public messages are handled separately via showMessage
        }
    }

    override fun showMessage(author: String, msg: String) {
        // Only add to public chat if not in private chat mode
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
        } else {
            // Already connected - send message
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
}