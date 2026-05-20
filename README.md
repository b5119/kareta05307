# Карета 05-307 — Chat Application with Spring Data JPA

## Overview
Карета 05-307 is a Kotlin Multiplatform desktop chat application built around a classic client-server architecture. The desktop client is implemented with Compose Multiplatform, communication is performed over raw TCP sockets, and chat history is now persisted to an embedded H2 database through Spring Boot and Spring Data JPA on the server side. This persistence integration was brought into the project from `kotlindbspring05-307` and adapted to the existing socket-based chat flow.

## Architecture
The project is split into three modules:

- `composeApp` — Compose Multiplatform desktop GUI client
- `server` — Kotlin + Spring Boot TCP server with JPA persistence
- `communicator` — shared networking module (`Communicator.kt`, `Constants.kt` / `InfoType` enum)

Plain-text architecture diagram:

```text
┌──────────────────────────────────────────────────────────────┐
│                        composeApp                           │
│  Compose Desktop UI (App.kt, GraphicsUI.kt, Client.kt)     │
└──────────────────────────────┬───────────────────────────────┘
                               │
                               │ TCP sockets
                               │ InfoType:payload
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                        communicator                         │
│ Shared socket I/O via DataInputStream/DataOutputStream      │
│ Communicator.kt + InfoType enum                             │
└──────────────────────────────┬───────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                           server                            │
│ Spring Boot context + custom TCP server + Spring Data JPA   │
│ Main.kt -> Server.kt -> ConnectedClient.kt                  │
└──────────────────────────────┬───────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                         H2 Database                         │
│ server/data/chatdb.mv.db                                    │
│ chat_messages table                                         │
└──────────────────────────────────────────────────────────────┘
```

## Tech Stack
| Category | Technology |
|---|---|
| Language | Kotlin 2.3.0 |
| UI framework | Compose Multiplatform 1.10.0 |
| Networking | Java TCP sockets with `ServerSocket` / `Socket` |
| Database | H2 file-based embedded database |
| ORM | Spring Data JPA + Hibernate |
| Dependency injection | Spring Boot application context |
| Build tool | Gradle 8.14.3 via Gradle Wrapper |
| Java version | JDK 17+ |

## Prerequisites
You need:

- JDK 17 or newer
- Gradle Wrapper support from the repository
- Git

How to verify:

```bash
java -version
./gradlew --version
git --version
```

## How to Run

### Step 1 — Clone
```bash
git clone https://github.com/b5119/kareta05307.git
cd kareta05307
chmod +x gradlew
```

### Step 2 — Start the Server (Terminal 1)
```bash
./gradlew :server:run --no-configuration-cache --no-daemon
```

Expected startup flow in the logs:

- Spring Boot banner appears
- Spring initializes the application context without an HTTP server
- HikariPool opens a connection to the H2 database
- The server prints `Database ready. Starting chat server on port 5307...`
- The server prints `Server listening on port 5307`

Keep this terminal open while testing the application.

### Step 3 — Start the Client (Terminal 2)
```bash
./gradlew :composeApp:run --no-configuration-cache
```

What happens:

- A Compose Desktop window opens
- The user is prompted to enter a username
- After successful validation, the user joins the chat and can send messages

### Step 4 — Test Persistence
Send several messages, close the client, and start it again. On reconnect, the latest chat history is replayed automatically from the database. This confirms that messages are persisted and restored correctly across client restarts.

## Database
- H2 file-based database stored at `server/data/chatdb.mv.db`
- Created automatically on first server start
- Table: `chat_messages` with columns: `id`, `sender_name`, `content`, `message_type`, `timestamp`
- `spring.jpa.hibernate.ddl-auto=update` means schema is created or updated automatically
- To switch to PostgreSQL: change `spring.datasource.*` in `application.properties` and swap `h2` `runtimeOnly` for `postgresql` in `server/build.gradle.kts`

## Message Protocol
The application uses a simple wire protocol over TCP:

- Payload format: `InfoType:payload`
- Serialization: `DataOutputStream.writeUTF(...)`
- Deserialization: `DataInputStream.readUTF(...)`

`InfoType` values:

- `INFORMATION` — system information, prompts, welcome messages, join/leave notices, history markers
- `WARNING` — validation failures such as duplicate usernames or invalid input
- `ERROR` — critical error messages
- `MESSAGE` — regular public chat messages
- `PRIVATE` — private direct-message payloads
- `USERLIST` — online user list updates

## Features
- [x] Multi-user public chat
- [x] Private messages (/pm username message)
- [x] User list broadcast on join/leave
- [x] Message persistence to H2 database
- [x] Chat history replay (last 50 messages) on new client connect
- [x] Username validation (unique, no spaces, non-empty)
- [x] Join/leave notifications persisted to DB

## Project Structure
```text
kareta05307/
├── README.md
│   Project overview and usage documentation.
├── settings.gradle.kts
│   Includes the three modules and configures plugin repositories.
├── gradle/
│   └── libs.versions.toml
│       Centralized dependency and plugin version catalog.
├── communicator/
│   Shared networking code used by client and server.
│   └── src/jvmMain/kotlin/ru/gr05307/net/
│       ├── Communicator.kt
│       │   Socket helper built on DataInputStream/DataOutputStream.
│       └── Constants.kt
│           Defines the InfoType protocol enum.
├── server/
│   TCP chat server backed by Spring Boot and Spring Data JPA.
│   ├── build.gradle.kts
│   │   Server module build, Spring plugins, runtime task, H2 dependency.
│   └── src/jvmMain/
│       ├── kotlin/ru.gr05307/
│       │   ├── Main.kt
│       │   │   Starts Spring with WebApplicationType.NONE, then starts the TCP server.
│       │   ├── MainViewModel.kt
│       │   │   Thin wrapper that starts the blocking server accept loop.
│       │   ├── database/
│       │   │   ├── ChatMessage.kt
│       │   │   │   JPA entity for persisted chat records.
│       │   │   ├── ChatMessageRepository.kt
│       │   │   │   Spring Data repository for retrieving recent messages.
│       │   │   └── ChatMessageService.kt
│       │   │       Service layer for storing and replaying chat history.
│       │   └── net/
│       │       ├── Server.kt
│       │       │   Listens on port 5307 and spawns a thread per client.
│       │       └── ConnectedClient.kt
│       │           Handles username negotiation, history replay, broadcast, and persistence.
│       └── resources/
│           └── application.properties
│               H2 and JPA configuration for the Spring server.
├── composeApp/
│   Compose Multiplatform desktop client.
│   └── src/jvmMain/kotlin/ru/gr05307/kareta05307/
│       ├── App.kt
│       │   Main Compose UI, dialogs, sidebar, chat area, theming.
│       ├── GraphicsUI.kt
│       │   ViewModel-like UI state holder for public chat, private chat, and presence.
│       ├── Message.kt
│       │   Message model used by the desktop UI.
│       ├── main.kt
│       │   Desktop app entrypoint.
│       ├── net/
│       │   └── Client.kt
│       │       Client socket layer and protocol parsing.
│       └── ui/
│           UI abstraction and supporting UI classes.
└── server/data/
    Runtime-generated H2 database files after first server launch.
```

## Build Commands Reference
| Command | Purpose |
|---|---|
| `./gradlew :server:build --no-configuration-cache --no-daemon` | Builds the server module and runs compile checks. |
| `./gradlew :server:run --no-configuration-cache --no-daemon` | Starts the Spring-backed TCP server. |
| `./gradlew :composeApp:build --no-configuration-cache --no-daemon` | Builds the Compose Desktop client. |
| `./gradlew :composeApp:run --no-configuration-cache` | Launches the desktop client UI. |
| `./gradlew :communicator:build --no-configuration-cache --no-daemon` | Builds the shared networking module. |

Flag reference:

- `--no-configuration-cache` avoids stale configuration-cache behavior during iterative development
- `--no-daemon` is especially useful for long-running server processes so Gradle does not manage them through a reusable daemon

## Spring Integration Notes
Spring was integrated without introducing an HTTP server. In `Main.kt`, the application starts with `SpringApplicationBuilder(...).web(WebApplicationType.NONE).run()`, which creates the Spring context only for dependency injection, JPA, Hibernate, and datasource management. After the context is ready, the code obtains `ChatMessageService` from Spring and starts the blocking TCP socket server.

The `kotlin.spring` and `kotlin.jpa` plugins are important here:

- `kotlin.spring` supports Spring-friendly open classes and proxying behavior
- `kotlin.jpa` supports JPA entity conventions such as proxy compatibility and no-arg constructor expectations

This allows the project to keep its original custom TCP chat server while using Spring Data JPA for persistence.

## Known Issues / Limitations
- Server must be started before client
- H2 dialect warning in logs is harmless (remove `spring.jpa.properties.hibernate.dialect` from `application.properties` to silence it)
- `./gradlew :server:run` uses Gradle daemon which may time out on long runs; use `--no-daemon` for production-like testing

## Contributing
Contributions should follow a normal fork-and-PR workflow against the upstream repository `ProfessorMB21/kareta05307`.

Suggested flow:

1. Fork the repository
2. Create a feature branch from `master`
3. Make and test your changes locally
4. Commit with clear messages
5. Push your branch to your fork
6. Open a pull request referencing the upstream `ProfessorMB21/kareta05307` project

For larger changes, prefer keeping module boundaries intact:

- UI work in `composeApp`
- persistence and server logic in `server`
- shared protocol/networking updates in `communicator`
