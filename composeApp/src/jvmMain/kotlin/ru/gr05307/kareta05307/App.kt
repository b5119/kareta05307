package ru.gr05307.kareta05307

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

import kareta05307.composeapp.generated.resources.Res
import kareta05307.composeapp.generated.resources.compose_multiplatform

@Composable
fun App(viewModel: GraphicsUI) {
    MaterialTheme {

        if (viewModel.showDialog) {
            AlertDialog(
                text = {
                    val focusManager = LocalFocusManager.current
                    OutlinedTextField(
                        value = viewModel.userText,
                        onValueChange = { viewModel.userText = it },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.send()
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = true
                    )
                },
                onDismissRequest = { },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.send()
                        }
                    ) {
                        Text("Ок")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            viewModel.exit()
                        }
                    ) {
                        Text("Выйти")
                    }
                },
                title = {
                    Text(viewModel.dialogMessage)
                },
            )
        }

        Column(Modifier
            .fillMaxSize()
            .padding(4.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                reverseLayout = true,
                verticalArrangement = spacedBy(4.dp),
            ) {
                items(viewModel.messages){
                    Box(modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.5f)
                        .background(MaterialTheme.colorScheme.primary),
                    ) {
                        Column(Modifier.fillMaxSize().padding(16.dp)) {
                            Text(it.author, color = MaterialTheme.colorScheme.onPrimary)
                            Text(it.msg, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var textFieldValue by remember { mutableStateOf(viewModel.userText) }
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        viewModel.userText = it
                    },
                    modifier = Modifier.weight(1f).onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Enter) {
                            if (keyEvent.isShiftPressed) {
                                // Shift+Enter: add newline
                                textFieldValue += "\n"
                                viewModel.userText = textFieldValue
                            } else {
                                // Enter alone: send message
                                if (textFieldValue.isNotBlank()) {
                                    viewModel.send()
                                    textFieldValue = ""
                                    viewModel.userText = ""
                                }
                            }
                            true
                        } else {
                            false
                        }
                    },
                    maxLines = 5,
                )
                IconButton(
                    onClick = {
                        if (viewModel.userText.isNotBlank()) {
                            viewModel.send()
                            textFieldValue = ""
                            viewModel.userText = ""
                        }
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = null
                    )
                }
            }
        }
    }
}