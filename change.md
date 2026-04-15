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

## Summary

This implementation transforms a silent failure into explicit user feedback. The changes are minimal (3 files, ~30 lines added/modified) but significantly improve user experience by:

1. **Eliminating confusion** - Users know immediately if server is unreachable
2. **Providing clear instructions** - Error message tells users what to check
3. **Preventing broken state** - Modal dialog blocks access to non-functional UI
4. **Maintaining simplicity** - No retry complexity, just exit and restart

The implementation follows existing code patterns (state management, Compose UI, Russian language) and integrates cleanly with the current architecture.
