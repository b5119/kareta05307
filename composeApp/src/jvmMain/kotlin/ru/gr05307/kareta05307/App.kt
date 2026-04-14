package ru.gr05307.kareta05307

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gr05307.net.InfoType

@Composable
fun App(viewModel: GraphicsUI) {
    val listState = rememberLazyListState()

    // Auto-scroll to latest message when messages change
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

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
                        singleLine = true,
                        label = { Text("Имя пользователя") }
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

        Column(
            Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Chat messages area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp),
                reverseLayout = false,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
            ) {
                items(viewModel.messages) { message ->
                    // Handle regular messages and system/join notification
                    if (message.author.isEmpty()) {
                        NotificationBubble(
                            message = message
                        )
                    } else {
                        MessageBubble(
                            message = message,
                            isFromMe = message.isFromMe
                        )
                    }
                }
            }

            // Input area
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var textFieldValue by remember { mutableStateOf(viewModel.userText) }
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        viewModel.userText = it
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.key == Key.Enter) {
                                if (keyEvent.isShiftPressed) {
                                    textFieldValue += "\n"
                                    viewModel.userText = textFieldValue
                                } else {
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
                    placeholder = { Text("Введите сообщение... (Enter - отправить, Shift+Enter - новая строка)") },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                )
                IconButton(
                    onClick = {
                        if (viewModel.userText.isNotBlank()) {
                            viewModel.send()
                            textFieldValue = ""
                            viewModel.userText = ""
                        }
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(50)
                        )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Отправить",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isFromMe: Boolean,
) {
    val bubbleColor = if (isFromMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val alignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (!isFromMe) {
                Text(
                    text = message.author,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = message.msg,
                fontSize = 15.sp,
                color = if (isFromMe) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }
    }
}

@Composable
fun NotificationBubble(
    message: Message
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                message.msg,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
