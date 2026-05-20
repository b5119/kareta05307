// ru.gr05307.kareta05307/Main.kt (Modified)
package ru.gr05307.kareta05307

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.gr05307.kareta05307.net.Client

class Main(
    val client: Client,
    val ui: GraphicsUI,
) {
    private var started = false
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun start() {
        if (started) return
        started = true

        client.addMessageListener { author, message ->
            scope.launch {
                ui.showMessage(author, message)
            }
        }

        client.addPrivateMessageListener { sender, message ->
            scope.launch {
                ui.receivePrivateMessage(sender, message)
            }
        }

        client.addUserListListener { users ->
            scope.launch {
                ui.updateOnlineUsers(users)
            }
        }

        client.addInfoListener { message, msgType ->
            scope.launch {
                ui.showInfo(message, msgType)
            }
        }
        client.addDisconnectListener {
            scope.launch {
                ui.setDisconnected()
            }
        }

        client.start()
        ui.addUserDataListener {
            client.sendData(it)
        }
        ui.start()
    }

    fun cancel() {
        scope.cancel()
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
    main?.start()

    Window(
        onCloseRequest = {
            main?.cancel()
            client?.stop()
            exitApplication()
        },
        title = "Карета 05-307",
    ) {
        App(ui)
    }
}
