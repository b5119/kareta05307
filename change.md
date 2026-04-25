# Changelog: Connection Error Handling Implementation

## Overview
This document provides an extensive breakdown of the connection error handling implementation that replaces the previously silent exception handling in the client application. The changes ensure users receive clear feedback when the server is unavailable and cannot proceed until the application is restarted with the server running.

---

## Problem Statement

### Original Issue
The original implementation in `composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/main.kt` contained an empty exception handler that silently swallowed connection failures:

```kotlin
// BEFORE (main.kt:29-37)
try {
    client = Client()
    main = Main(client, ui)
} catch (_: Exception) {
    // Empty catch block - no user feedback!
}
```

### Impact
- **Poor User Experience**: Users saw no indication when the server was unreachable
- **Silent Failure**: Application appeared to launch but had no functional connection
- **No Recovery Path**: No mechanism to inform users of the actual problem
- **Hidden Errors**: Connection exceptions (e.g., `ConnectException`, `SocketTimeoutException`) were discarded without logging

---

## Solution Architecture

The solution introduces a state-driven error handling mechanism across three components:

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    main.kt      │────▶│  GraphicsUI.kt  │◀────│    App.kt       │
│  (Connection    │     │  (Error State   │     │  (Error Dialog  │
│   Logic)        │     │   Management)   │     │   Display)      │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

---

## Detailed Implementation

### 1. Error State Management (GraphicsUI.kt)

#### New State Properties

**Location**: `composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/GraphicsUI.kt`

```kotlin
// Lines 22-28
var connectionError by mutableStateOf<String?>(null)
    private set
```

- **Purpose**: Stores the error message displayed to users
- **Type**: Nullable `String` (null = no error, non-null = error present)
- **Visibility**: Public read, private write (encapsulated state management)
- **Compose Integration**: Uses `mutableStateOf` for automatic UI recomposition

#### State Mutator Methods

**setConnectionError(error: String)**
```kotlin
// Lines 30-32
fun setConnectionError(error: String) {
    connectionError = error
}
```

- **Purpose**: Sets the error message when connection fails
- **Called from**: `main.kt` in catch block
- **Parameters**:
  - `error`: Human-readable error description in Russian

**clearConnectionError()**
```kotlin
// Lines 34-36
fun clearConnectionError() {
    connectionError = null
}
```

- **Purpose**: Resets error state on successful connection
- **Called from**: `main.kt` after successful client creation

---

### 2. Connection Logic with Error Handling (main.kt)

#### Refactored Connection Flow

**Location**: `composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/main.kt`

```kotlin
// Lines 29-48
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
            ui.setConnectionError(
                "Не удалось подключиться к серверу.\n\nПроверьте, что сервер запущен, и попробуйте снова."
            )
        }
    }

    connect()
    // ... window initialization
}
```

#### Key Changes

| Aspect | Before | After |
|--------|--------|-------|
| **Variable mutability** | `val client` (immutable) | `var client` (nullable, mutable) |
| **Error handling** | Empty catch block | Proper exception handling with UI feedback |
| **Connection attempt** | Single try-catch | Extracted to `connect()` function |
| **State management** | None | Error state set on failure, cleared on success |
| **User feedback** | Silent failure | Explicit error message in Russian |

#### Exception Handling Details

The catch block handles multiple exception types:
- `java.net.ConnectException`: Server not running or refused connection
- `java.net.UnknownHostException`: Invalid host/address
- `java.net.SocketTimeoutException`: Connection timeout
- `java.io.IOException`: General I/O errors

All exceptions result in:
1. Client object nulled (prevents partial initialization)
2. Main object nulled
3. Error state populated with user-friendly message

---

### 3. Error Dialog UI (App.kt)

#### Modal Error Dialog

**Location**: `composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/App.kt`

```kotlin
// Lines 60-72
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
```

#### Dialog Characteristics

| Property | Value | Purpose |
|----------|-------|---------|
| `onDismissRequest` | Empty lambda | Prevents dismissal via back button or click outside (modal) |
| `title` | "Ошибка подключения" | Clear indication of connection problem |
| `text` | Two-line error message from state | Instructs user to check server status |
| `confirmButton` | Exit button | Only option is to exit application |

#### Integration with Compose Lifecycle

The dialog is rendered before the main UI content, ensuring it appears on top:

```kotlin
MaterialTheme {
    // Error dialog first (renders on top if condition met)
    if (viewModel.connectionError != null) { ... }

    // Username dialog (existing)
    if (viewModel.showDialog) { ... }

    // Main chat UI
    Column { ... }
}
```

---

## User Flow

### Successful Connection Path
```
User launches app
    ↓
main() calls connect()
    ↓
Client() instantiation succeeds
    ↓
Main() created with valid client
    ↓
clearConnectionError() called (no-op, already null)
    ↓
Window opens with working chat UI
```

### Failed Connection Path
```
User launches app
    ↓
main() calls connect()
    ↓
Client() throws ConnectException (server down)
    ↓
Catch block executes
    ↓
client = null, main = null
    ↓
setConnectionError() called with Russian message
    ↓
Window opens
    ↓
Compose observes connectionError != null
    ↓
AlertDialog renders blocking UI
    ↓
User can only click "Выйти" to exit
    ↓
Application terminates
```

---

## Design Decisions

### 1. No Retry Button

**Rationale**: The retry functionality was intentionally removed based on requirements. Instead of allowing users to spam retry while the server is down, they must exit and restart the application once the server is confirmed running.

**Benefits**:
- Prevents rapid reconnection attempts that could flood logs
- Forces users to verify server status before restarting
- Simpler UX - one clear action (exit)
- Avoids potential race conditions with rapid connect/disconnect cycles

### 2. Modal Dialog

**Rationale**: Users cannot dismiss the error without taking action.

**Benefits**:
- Cannot accidentally proceed with non-functional UI
- Forces acknowledgment of the error state
- Prevents confusion from seeing chat UI that doesn't work

### 3. Russian Language

**Rationale**: All UI text is in Russian to match the existing application language.

**Consistency**:
- "Введите своё имя" (Enter username) - existing
- "Добро пожаловать" (Welcome) - existing
- "Ошибка подключения" (Connection error) - new, follows pattern

### 4. Nullable Client/Main

**Rationale**: Uses nullable types instead of lateinit or throwing.

**Safety**:
- `Client?` and `Main?` clearly indicate possible absence
- Null safety enforced by Kotlin compiler
- Prevents usage of partially initialized objects

---

## Files Modified

| File | Lines Changed | Description |
|------|---------------|-------------|
| `main.kt` | 29-48 | Refactored connection logic with error handling |
| `GraphicsUI.kt` | 22-36 | Added error state and management methods |
| `App.kt` | 60-72 | Added modal error dialog composable |

---

## Testing Scenarios

### Scenario 1: Server Not Running
**Steps**:
1. Ensure no server process on port 5307
2. Launch client application

**Expected**:
- Error dialog appears with "Ошибка подключения" title
- Message explains to check server status
- Only "Выйти" button available
- Clicking exit closes application

### Scenario 2: Server Starts After Client
**Steps**:
1. Launch client without server (see error dialog)
2. Start server
3. Must restart client to connect

**Expected**:
- Client shows error and exits
- Fresh client launch succeeds
- Chat UI functional

### Scenario 3: Successful Connection
**Steps**:
1. Start server
2. Launch client

**Expected**:
- No error dialog
- Username prompt appears
- Normal chat functionality

---

## Future Considerations

### Potential Enhancements

1. **Retry with Backoff**: Could add limited retries with exponential backoff if auto-recovery is desired

2. **Server Discovery**: Implement server discovery or configurable server address

3. **Error Details**: Show technical error details in expandable section for debugging

4. **Auto-reconnect**: Attempt reconnection periodically if connection drops during session

5. **Logging**: Add structured logging of connection attempts and failures

---

---

## Part 2: Mid-Session Server Disconnect Handling

### Overview
Extended the error handling to detect and respond to server disconnections that occur while the client is actively connected and chatting. Previously, the read loop would throw exceptions silently, leaving the user unaware the connection was dead.

---

### Problem Statement

#### Original Issue
When the server crashed or network was interrupted during an active session:
- `Communicator.readUTF()` threw `SocketException` or `EOFException`
- Exception propagated silently (caught nowhere)
- Client thread died silently
- User could still type messages that went nowhere
- No indication the connection was broken

---

### Solution Architecture

Event-driven disconnect notification chain:

```
Socket Exception
    ↓
Communicator catch block
    ↓
disconnectListeners notify
    ↓
Client.onDisconnect()
    ↓
GraphicsUI.setDisconnected()
    ↓
Compose AlertDialog renders
```

---

## Detailed Implementation

### 1. Communicator.kt — Disconnect Detection

**Location**: `communicator/src/jvmMain/kotlin/ru/gr05307/net/Communicator.kt`

#### New Listener Infrastructure

```kotlin
// Lines 16, 25-27
private val disconnectListeners = mutableListOf<() -> Unit>()

fun addDisconnectListener(listener: () -> Unit) {
    disconnectListeners.add(listener)
}

fun removeDisconnectListener(listener: () -> Unit) {
    disconnectListeners.remove(listener)
}
```

#### Wrapped Read Loop with Error Handling

```kotlin
// Lines 39-58
fun start(){
    thread {
        isActive = true
        try {
            DataInputStream(socket.getInputStream()).let { dis ->
                while (isActive) {
                    val userData = dis.readUTF()
                    dataListeners.forEach { it(userData) }
                }
            }
        } catch (_: Exception) {
            // Socket closed or connection lost
        } finally {
            isActive = false
            disconnectListeners.forEach { it() }
            try { socket.close() } catch (_: Exception) {}
        }
    }
}
```

#### Defensive Send Handling

```kotlin
// Lines 33-41
fun sendData(data: String) {
    try {
        DataOutputStream(socket.getOutputStream()).let { dos ->
            dos.writeUTF(data)
            dos.flush()
        }
    } catch (_: Exception) {
        // Connection likely broken, will be detected in read loop
    }
}
```

**Key Behaviors**:
- Read loop wrapped in try-catch-finally
- Any exception triggers disconnect notification
- Socket forcibly closed in finally block
- Send operations fail silently (errors detected on read side)

---

### 2. Client.kt — Event Propagation

**Location**: `composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/net/Client.kt`

#### Disconnect Listener Chain

```kotlin
// Lines 13, 37-47
private val disconnectListeners: MutableList<() -> Unit> = mutableListOf()

init {
    communicator = Communicator(Socket(host, port))
    communicator.addDataListener { parseData(it) }
    communicator.addDisconnectListener { onDisconnect() }  // NEW
}

fun addDisconnectListener(listener: () -> Unit) {
    disconnectListeners.add(listener)
}

fun removeDisconnectListener(listener: () -> Unit) {
    disconnectListeners.remove(listener)
}

private fun onDisconnect() {
    disconnectListeners.forEach { it() }
}
```

**Purpose**: Decouples low-level socket events from UI handling

---

### 3. GraphicsUI.kt — Disconnect State

**Location**: `composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/GraphicsUI.kt`

#### New State Property

```kotlin
// Lines 30-31
var isDisconnected by mutableStateOf(false)
    private set
```

#### State Mutator

```kotlin
// Lines 37-39
fun setDisconnected() {
    isDisconnected = true
}
```

**Separation of Concerns**:
- `connectionError` — initial connection failure
- `isDisconnected` — connection lost mid-session

---

### 4. App.kt — Disconnect Dialog

**Location**: `composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/App.kt`

```kotlin
// Lines 73-85
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
```

**Dialog Characteristics**:
| Property | Value |
|----------|-------|
| Title | "Соединение разорвано" (Connection broken) |
| Message | Explains server became unavailable |
| Action | Single "Выйти" button |
| Modal | Cannot be dismissed without action |

---

### 5. main.kt — Wiring

**Location**: `composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/main.kt`

#### Updated Main Class

```kotlin
// Lines 9-12
class Main(
    val client: Client,
    val ui: GraphicsUI,  // Changed from UI to GraphicsUI
) {
    fun start() {
        // ... existing listeners ...
        client.addDisconnectListener {
            ui.setDisconnected()
        }
        // ...
    }
}
```

---

## User Flow: Mid-Session Disconnect

```
User actively chatting
    ↓
Server process killed / network drops
    ↓
Communicator.readUTF() throws EOFException
    ↓
Catch block executes
    ↓
isActive = false
    ↓
disconnectListeners notified
    ↓
Client propagates to UI listener
    ↓
GraphicsUI.isDisconnected = true
    ↓
Compose observes state change
    ↓
Modal dialog appears over chat UI
    ↓
User sees "Соединение разорвано"
    ↓
Only option: click "Выйти"
    ↓
Application terminates
```

---

## Error State Differentiation

| Scenario | State Flag | Dialog | User Action |
|----------|-----------|--------|-------------|
| **Initial connection fails** | `connectionError != null` | "Ошибка подключения" | Exit only |
| **Connection lost mid-session** | `isDisconnected == true` | "Соединение разорвано" | Exit only |

Both states are mutually exclusive (only one can occur per session) and both force application exit.

---

## Updated Files Modified

| File | Lines | Changes |
|------|-------|---------|
| `Communicator.kt` | 16, 25-27, 33-41, 39-58 | Added disconnect listeners, wrapped read loop, defensive send |
| `Client.kt` | 13, 37-47 | Propagates disconnect events to UI |
| `GraphicsUI.kt` | 30-31, 37-39 | Added `isDisconnected` state |
| `App.kt` | 73-85 | Added disconnect dialog |
| `main.kt` | 12, 18-20 | Wired disconnect listener |

---

## Testing Scenarios

### Scenario 4: Server Dies While Chatting
**Steps**:
1. Start server
2. Connect client, enter username
3. Send a few messages
4. Kill server process
5. Observe client

**Expected**:
- Client shows "Соединение разорвано" dialog
- Previous chat messages remain visible (behind dialog)
- Cannot send new messages
- Must exit and restart

### Scenario 5: Network Interruption
**Steps**:
1. Start server, connect client
2. Disable network adapter / unplug cable
3. Wait a few seconds
4. Re-enable network

**Expected**:
- Dialog appears after socket timeout
- Same behavior as server death

---

## Summary

This implementation transforms a silent failure into explicit user feedback. The changes are minimal (3 files, ~30 lines added/modified) but significantly improve user experience by:

1. **Eliminating confusion** - Users know immediately if server is unreachable
2. **Providing clear instructions** - Error message tells users what to check
3. **Preventing broken state** - Modal dialog blocks access to non-functional UI
4. **Maintaining simplicity** - No retry complexity, just exit and restart
5. **Handling mid-session failures** - Detects disconnects during active chatting

The implementation follows existing code patterns (state management, Compose UI, Russian language) and integrates cleanly with the current architecture.
