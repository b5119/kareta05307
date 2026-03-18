package ru.gr05307.kareta05307

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.gr05307.kareta05307.net.Client
import ru.gr05307.kareta05307.ui.ConsoleUI
import ru.gr05307.kareta05307.ui.UI

class Main(
    val client: Client,
    val ui: UI,
) {
    fun start(){
        client.addMessageListener { author, message ->
            ui.showMessage(author,message)
        }
        client.addInfoListener { message, msgType ->
            ui.showInfo(message,msgType)
        }

        client.start()
        ui.addUserDataListener {
            client.sendData(it)
        }
        ui.start()
    }
}

fun main() = application {
    val ui = GraphicsUI()
    val client: Client
    var main: Main? = null
    try {
        client = Client()
        main = Main(client, ui)
    } catch (_: Exception) {

    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Карета 05-307",
    ) {
        App(ui)
        main?.start()
    }
}