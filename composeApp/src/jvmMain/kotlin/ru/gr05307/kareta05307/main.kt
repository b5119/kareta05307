package ru.gr05307.kareta05307

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.gr05307.kareta05307.net.Client
import ru.gr05307.kareta05307.ui.ConsoleUI
import ru.gr05307.kareta05307.ui.UI

class Main(
    val client: Client,
    val ui: GraphicsUI,
) {
    fun start(){
        client.addMessageListener { author, message ->
            ui.showMessage(author,message)
        }
        client.addInfoListener { message, msgType ->
            ui.showInfo(message,msgType)
        }
        client.addDisconnectListener {
            ui.setDisconnected()
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
    var client: Client? = null
    var main: Main? = null

    fun connect() {
        try {
            client = Client()
            main = Main(client!!, ui)
            ui.clearConnectionError()
        } catch (e: Exception) {
            client = null
            main = null
            ui.setConnectionErrorMessage(
                "Не удалось подключиться к серверу.\n\nПроверьте, что сервер запущен, и попробуйте снова."
            )
        }
    }

    connect()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Карета 05-307",
    ) {
        App(ui)
        main?.start()
    }
}