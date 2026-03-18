package ru.gr05307.kareta05307

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "kareta05307",
    ) {
        App()
    }
}