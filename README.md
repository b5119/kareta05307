# Карета 05-307

A desktop chat application built with **Kotlin Multiplatform** and **Compose Multiplatform**. This project implements a client-server chat system with a modern Material Design 3 UI.

## Features

- **Real-time messaging** - Send and receive messages instantly
- **Multi-user support** - Multiple clients can connect simultaneously
- **Unique usernames** - Server enforces username uniqueness and validation
- **Visual message bubbles** - Own messages appear on the right, others on the left
- **System notifications** - Join/leave events displayed as centered notifications
- **Connection error handling** - Clear feedback when server is unavailable
- **Auto-scroll** - Chat automatically scrolls to show latest messages
- **Keyboard shortcuts** - Send messages with Enter key
- **Material Design 3** - Modern, responsive UI with themed colors

## Project Structure

The project follows a multi-module architecture:

```
kareta05307/
├── composeApp/          # Desktop GUI client application
│   └── src/jvmMain/kotlin/ru/gr05307/kareta05307/
│       ├── App.kt              # Main Compose UI with dialogs
│       ├── main.kt             # Application entry point
│       ├── GraphicsUI.kt       # ViewModel managing UI state
│       ├── Message.kt          # Data class for chat messages
│       ├── net/
│       │   └── Client.kt       # Client network handler
│       └── ui/
│           ├── UI.kt           # UI interface
│           └── ConsoleUI.kt    # Console-based UI (alternative)
├── server/              # Server application
│   └── src/jvmMain/kotlin/ru/gr05307/
│       ├── Main.kt             # Server entry point
│       ├── MainViewModel.kt    # Server lifecycle management
│       └── net/
│           ├── Server.kt          # TCP socket server
│           └── ConnectedClient.kt # Per-client connection handler
└── communicator/        # Shared networking module
    └── src/jvmMain/kotlin/ru/gr05307/net/
        ├── Communicator.kt   # Low-level socket communication
        └── Constants.kt      # InfoType enum for message types
```

## Architecture

### Client Architecture

```
┌─────────────────────────────────────────┐
│              Compose UI (App.kt)        │
│  ┌───────────────────────────────────┐  │
│  │      AlertDialog (Errors/Input)   │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │     LazyColumn (Message List)     │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │      TextField + Send Button      │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│           GraphicsUI (ViewModel)        │
│     - Manages UI state                  │
│     - Handles user input validation     │
│     - Messages state (mutableStateList) │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│              Client (net)               │
│     - Socket connection to server       │
│     - Message parsing and routing       │
│     - Disconnect detection              │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│           Communicator                  │
│     - Low-level socket I/O              │
│     - DataInputStream/DataOutputStream  │
└─────────────────────────────────────────┘
```

### Server Architecture

```
┌─────────────────────────────────────────┐
│              Server                     │
│     - ServerSocket on port 5307         │
│     - Accepts incoming connections      │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│         ConnectedClient (per client)    │
│     - Username validation               │
│     - Broadcasts messages to all users  │
│     - Handles disconnect cleanup        │
└─────────────────────────────────────────┘
```

### Communication Protocol

Messages are exchanged as UTF-8 strings with a type prefix:

```
<message_type>:<payload>
```

**Message Types:**
- `INFORMATION` - General info messages (welcome, user joined/left)
- `WARNING` - Validation errors (duplicate username, invalid input)
- `ERROR` - Critical errors
- `MESSAGE` - Regular chat messages (`username: message text`)

## Build & Run

### Prerequisites

- JDK 17 or later
- Gradle (wrapper included)

### Running the Server

```bash
./gradlew :server:run
```

Server starts on **port 5307** by default.

### Running the Client

```bash
./gradlew :composeApp:run
```

On Windows:
```bash
.\gradlew.bat :composeApp:run
```

### Building Distribution

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

Creates native packages (`.dmg` for macOS, `.msi` for Windows, `.deb` for Linux).

## Usage

1. **Start the server** first using the server run command above
2. **Launch the client** application
3. **Enter a username** when prompted:
   - Must be unique (server will reject duplicates)
   - Cannot contain spaces
   - Cannot be empty
4. **Start chatting**:
   - Type messages in the text field
   - Press **Enter** to send
   - Press **Shift+Enter** for new lines
   - Your messages appear on the **right** (blue bubbles)
   - Others' messages appear on the **left** (gray bubbles)

## Error Handling

The application handles various error scenarios:

| Scenario | Behavior |
|----------|----------|
| Server offline on launch | Shows "Ошибка подключения" dialog, requires exit |
| Server disconnects mid-session | Shows "Соединение разорвано" dialog, requires exit |
| Invalid username | Shows inline warning, prompts again |
| Duplicate username | Shows warning, prompts for different name |
| Connection lost during send | Detected and handled gracefully |

## Technical Details

### Dependencies

- **Kotlin** 2.3.0
- **Compose Multiplatform** 1.10.0
- **Material3** 1.10.0-alpha05
- **AndroidX Lifecycle** 2.9.6 (ViewModel support)
- **Kotlinx Coroutines** 1.10.2

### Module Dependencies

```
composeApp ──> communicator
server     ──> communicator
```

The `communicator` module provides shared networking code used by both client and server.

### State Management

The client uses Compose's `mutableStateOf` and `mutableStateListOf` for reactive UI updates:

- `GraphicsUI` extends `ViewModel` for lifecycle-aware state
- `messages` - observable list of chat messages
- `connectionError` / `isDisconnected` - error state flags
- `userText` - input field state

### Threading

- **Server**: Each client runs in a dedicated thread (`kotlin.concurrent.thread`)
- **Client**: Network I/O runs on background threads; UI updates on main thread via Compose's state system

## Development

### Project Files

- `plan.md` - Development roadmap
- `change.md` - Detailed changelog of connection error handling implementation

### Testing Scenarios

1. **Basic Chat**: Start server, connect multiple clients, verify message broadcasting
2. **Username Validation**: Test empty names, names with spaces, duplicate names
3. **Disconnect Handling**: Kill server while clients are connected
4. **Offline Launch**: Launch client without server running
5. **Reconnection**: Restart client after server comes back online

---

Built with Kotlin Multiplatform and Compose Multiplatform
