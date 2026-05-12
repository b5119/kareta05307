// ru.gr05307.kareta05307/App.kt (Lines 80-110 with custom color schemes)
package ru.gr05307.kareta05307

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.gr05307.kareta05307.ui.CustomDarkColorScheme
import ru.gr05307.kareta05307.ui.CustomLightColorScheme
import ru.gr05307.net.InfoType
import kotlin.random.Random


// Custom gradient backgrounds
val CustomLightGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE3F2FD),
        Color(0xFFF5F5F5),
        Color(0xFFE8EAF6)
    )
)

val CustomDarkGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF121212),
        Color(0xFF1E1E2E)
    )
)

@Composable
fun App(viewModel: GraphicsUI) {
    val listState = rememberLazyListState()
    var isDarkTheme by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Auto-scroll to latest message when messages change
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    // Apply custom color schemes based on theme preference
    MaterialTheme(
        colorScheme = if (isDarkTheme) CustomDarkColorScheme else CustomLightColorScheme,
        typography = androidx.compose.material3.Typography(),
        shapes = androidx.compose.material3.Shapes()
    ) {
        // Connection error dialog
        if (viewModel.connectionError != null) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Ошибка подключения") },
                text = { Text(viewModel.connectionError!!) },
                confirmButton = {
                    Button(onClick = { viewModel.exit() }) {
                        Text("Выйти")
                    }
                }
            )
        }

        // Server disconnect dialog
        if (viewModel.isDisconnected) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Соединение разорвано") },
                text = { Text("Сервер недоступен. Соединение было разорвано.\n\nПерезапустите приложение для повторного подключения.") },
                confirmButton = {
                    Button(onClick = { viewModel.exit() }) {
                        Text("Выйти")
                    }
                }
            )
        }

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

        // Main chat UI with enhanced sidebar
        if (viewModel.username != null && !viewModel.showDialog) {
            // Apply gradient background to main container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDarkTheme) CustomDarkGradient else CustomLightGradient
                    )
            ) {
                Row(
                    Modifier.fillMaxSize()
                ) {
                    // Enhanced Sidebar
                    EnhancedSidebar(
                        currentUser = viewModel.username!!,
                        onlineUsers = viewModel.onlineUsers,
                        selectedUser = viewModel.selectedPrivateUser,
                        onSelectUser = { user -> viewModel.selectPrivateUser(user) },
                        onDisconnect = { viewModel.disconnect() },
                        onExit = { viewModel.exit() },
                        onThemeToggle = { isDarkTheme = !isDarkTheme },
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.width(260.dp)
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp),
                        thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Chat area
                    ChatArea(
                        viewModel = viewModel,
                        listState = listState
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedSidebar(
    currentUser: String,
    onlineUsers: List<String>,
    selectedUser: String?,
    onSelectUser: (String?) -> Unit,
    onDisconnect: () -> Unit,
    onExit: () -> Unit,
    onThemeToggle: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var showUserMenu by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var userStatus by remember { mutableStateOf("Онлайн") }
    var onlineUsersCounter by remember { mutableStateOf(onlineUsers.size) }
    val userColor = remember(currentUser) {
        Color.hsv(Random(currentUser.hashCode()).nextFloat() * 360f, 0.6f, 0.9f)
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f))
            .fillMaxHeight()
    ) {
        // User Profile Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { showUserMenu = true },
            shape = RoundedCornerShape(16.dp),
            //elevation = 4.dp,
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with gradient background
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    userColor,
                                    userColor.copy(alpha = 0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.take(2).uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Username
                Text(
                    text = currentUser,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showStatusMenu = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when (userStatus) {
                                    "Онлайн" -> Color(0xFF4CAF50)
                                    "Отошёл" -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userStatus,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // User menu dropdown
        DropdownMenu(
            expanded = showUserMenu,
            onDismissRequest = { showUserMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Мой профиль") },
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                onClick = { showUserMenu = false }
            )
            DropdownMenuItem(
                text = { Text("Настройки") },
                leadingIcon = { Icon(Icons.Default.Settings, null) },
                onClick = { showUserMenu = false }
            )
            DropdownMenuItem(
                text = { Text(if (isDarkTheme) "Светлая тема" else "Тёмная тема") },
                leadingIcon = { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) },
                onClick = {
                    onThemeToggle()
                    showUserMenu = false
                }
            )
            /*
            DropdownMenuItem(
                text = { Text("Отключиться") },
                leadingIcon = { Icon(Icons.Default.ExitToApp, null) },
                onClick = {
                    onDisconnect()
                    showUserMenu = false
                }
            )
            */
            DropdownMenuItem(
                text = { Text("Выйти из приложения") },
                leadingIcon = { Icon(Icons.Default.Logout, null) },
                onClick = {
                    onExit()
                    showUserMenu = false
                }
            )
        }

        // Status menu dropdown
        DropdownMenu(
            expanded = showStatusMenu,
            onDismissRequest = { showStatusMenu = false }
        ) {
            listOf("Онлайн", "Отошёл", "Не беспокоить").forEach { status ->
                DropdownMenuItem(
                    text = { Text(status) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (status) {
                                        "Онлайн" -> Color(0xFF4CAF50)
                                        "Отошёл" -> Color(0xFFFFC107)
                                        else -> Color(0xFFF44336)
                                    },
                                    CircleShape
                                )
                        )
                    },
                    onClick = {
                        userStatus = status
                        showStatusMenu = false
                    }
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant
        )

        // Chat header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Чаты",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text(onlineUsersCounter.toString())
                    }
                }
            ) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = "Чаты",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Public chat option
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectUser(null) },
            color = if (selectedUser == null)
                MaterialTheme.colorScheme.primaryContainer
            else
                Color.Transparent,
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    tint = if (selectedUser == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Публичный чат",
                        fontWeight = if (selectedUser == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedUser == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Общее обсуждение",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant
        )

        // Online users header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Пользователи онлайн",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = onlineUsersCounter.toString(),
                fontSize = 12.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }

        // User list
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(onlineUsers) { user ->
                if (user != currentUser) {
                    EnhancedUserItem(
                        username = user,
                        isSelected = selectedUser == user,
                        onClick = { onSelectUser(user) },
                        currentUser = currentUser
                    )
                }
            }
        }

        /**
        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = MaterialTheme.colorScheme.outlineVariant)

        // Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = { /* Future: Add emoji picker */ },
                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji")
                }
                IconButton(
                    onClick = { /* Future: Add file share */ },
                    colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Пригласить")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Карета 05-307 v1.0",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        */
    }
}

@Composable
fun EnhancedUserItem(
    username: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    currentUser: String
) {
    val userColor = remember(username) {
        Color.hsv(Random(username.hashCode()).nextFloat() * 360f, 0.5f, 0.8f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            Color.Transparent,
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle with initials
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(userColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(2).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        username,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    // Online indicator
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                }
                Text(
                    "Нажмите для чата",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status badge (could show unread count in future)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

@Composable
fun ChatArea(
    viewModel: GraphicsUI,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Column(
        Modifier
            // .weight(1f)
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        // Chat header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    var onlineUsersCounter by remember { mutableStateOf(viewModel.onlineUsers.size) }
                    Text(
                        text = if (viewModel.selectedPrivateUser != null)
                            "Приватный чат"
                        else
                            "Публичный чат",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (viewModel.selectedPrivateUser != null) {
                        Text(
                            text = "Собеседник: ${viewModel.selectedPrivateUser}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Участников: ${onlineUsersCounter.toString()}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Typing indicator (future feature)
                if (viewModel.selectedPrivateUser != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✏️ Печатает...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

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
                if (message.author.isEmpty()) {
                    NotificationBubble(message = message)
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

            // LaunchedEffect to sync when userText changes externally
            LaunchedEffect(viewModel.userText) {
                if (textFieldValue != viewModel.userText) {
                    textFieldValue = viewModel.userText
                }
            }

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
                placeholder = {
                    Text(if (viewModel.selectedPrivateUser != null)
                        "Введите приватное сообщение..."
                    else
                        "Введите сообщение... (/pm имя сообщение для приватного чата)")
                },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
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
                        if (viewModel.selectedPrivateUser != null)
                            MaterialTheme.colorScheme.secondary
                        else
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
            if (!isFromMe && message.author.isNotEmpty()) {
                Text(
                    text = message.author,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (message.messageType == InfoType.PRIVATE)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
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