package ru.gr05307

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import ru.gr05307.database.ChatMessageService
import ru.gr05307.net.Server

@SpringBootApplication
class ServerApp

fun main() {
    // Start Spring with NO web server (we use our own TCP socket server)
    val context = SpringApplicationBuilder(ServerApp::class.java)
        .web(WebApplicationType.NONE)
        .run()

    val chatMessageService = context.getBean(ChatMessageService::class.java)
    val server = Server(chatMessageService)

    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop()
            context.close()
        }
    )

    println("Database ready. Starting chat server on port 5307...")
    MainViewModel(server).start()
}
