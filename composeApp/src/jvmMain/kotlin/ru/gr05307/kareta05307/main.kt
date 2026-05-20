// ru.gr05307.kareta05307/Main.kt (Modified)
package ru.gr05307.kareta05307

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.gr05307.kareta05307.net.Client
import javax.swing.SwingUtilities

class Main(
    val client: Client,
    val ui: GraphicsUI,
) {
    private var started = false

    fun start() {
        if (started) return
        started = true

        client.addMessageListener { author, message ->
            SwingUtilities.invokeLater {
                ui.showMessage(author, message)
            }
        }

        client.addPrivateMessageListener { sender, message ->
            SwingUtilities.invokeLater {
                ui.receivePrivateMessage(sender, message)
            }
        }

        client.addUserListListener { users ->
            SwingUtilities.invokeLater {
                ui.updateOnlineUsers(users)
            }
        }

        client.addInfoListener { message, msgType ->
            SwingUtilities.invokeLater {
                ui.showInfo(message, msgType)
            }
        }
        client.addDisconnectListener {
            SwingUtilities.invokeLater {
                ui.setDisconnected()
            }
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
    main?.start()

    Window(
        onCloseRequest = {
            client?.stop()
            exitApplication()
        },
        title = "Карета 05-307",
    ) {
        App(ui)
    }
}
