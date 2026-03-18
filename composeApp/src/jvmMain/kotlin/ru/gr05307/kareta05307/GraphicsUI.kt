package ru.gr05307.kareta05307

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import ru.gr05307.kareta05307.ui.UI
import ru.gr05307.net.InfoType

class GraphicsUI : ViewModel(), UI {

    var userText by mutableStateOf("")
    val messages = mutableStateListOf<Message>()

    override fun showMessage(author: String, msg: String) {
        TODO("Not yet implemented")
    }

    override fun showInfo(msg: String, msgType: InfoType) {
        TODO("Not yet implemented")
    }

    override fun addUserDataListener(listener: (String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun removeUserDataListener(listener: (String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun start() {
        TODO("Not yet implemented")
    }
}