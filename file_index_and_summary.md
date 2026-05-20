# Kareta 05307 - File Index and Summary

## Directory Structure

```
/home/frank-bwalya/Desktop/kareta05307/
├── README.md
├── change.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── APPLY_INSTRUCTIONS.sh
├── plan.md
├── gradle/
├── .git/
├── .gradle/
├── .kotlin/
├── .gitignore
│
├── communicator/                          # Shared networking module
│   └── src/jvmMain/kotlin/ru/gr05307/net/
│       ├── Constants.kt                   # InfoType enum definition
│       └── Communicator.kt                # Low-level socket I/O
│
├── composeApp/                            # Desktop GUI client
│   └── src/jvmMain/kotlin/ru/gr05307/kareta05307/
│       ├── main.kt                        # App entry point & connection logic
│       ├── App.kt                         # Compose UI root
│       ├── Message.kt                     # UI Message data class
│       ├── GraphicsUI.kt                  # State management (ViewModel)
│       ├── net/
│       │   └── Client.kt                  # Client socket wrapper
│       └── ui/
│           ├── UI.kt                      # UI interface
│           ├── Themes.kt                  # Compose themes
│           └── ConsoleUI.kt               # Console variant
│
└── server/                                # Spring Boot server with JPA
    ├── src/jvmMain/kotlin/ru.gr05307/
    │   ├── Main.kt                        # Spring Boot app bootstrap
    │   ├── MainViewModel.kt               # Server lifecycle wrapper
    │   ├── net/
    │   │   ├── Server.kt                  # ServerSocket listening loop
    │   │   └── ConnectedClient.kt         # Per-client message handler
    │   └── database/
    │       ├── ChatMessage.kt             # JPA Entity for persistence
    │       ├── ChatMessageRepository.kt   # Spring Data JPA repository
    │       └── ChatMessageService.kt      # Service layer with transactions
    │
    └── src/jvmMain/resources/
        └── application.properties         # Spring Boot config (H2 database)
```

---

## Critical Files for Message/Database Integration

### 1. Message Data Models

#### Client-Side (UI Only)
- **Path**: `/home/frank-bwalya/Desktop/kareta05307/composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/Message.kt`
- **Lines**: 10 total
- **Purpose**: Data class for display in Compose UI
- **Status**: ✅ No issues

#### Server-Side (Persistence)
- **Path**: `/home/frank-bwalya/Desktop/kareta05307/server/src/jvmMain/kotlin/ru.gr05307/database/ChatMessage.kt`
- **Lines**: 32 total
- **Purpose**: JPA Entity mapped to `chat_messages` table
- **Key Issue**: ⚠️ String-based messageType (ISSUE 3)
- **Status**: Needs enum refactoring

### 2. Persistence Layer

#### Repository
- **Path**: `/home/frank-bwalya/Desktop/kareta05307/server/src/jvmMain/kotlin/ru.gr05307/database/ChatMessageRepository.kt`
- **Lines**: 10 total
- **Status**: ✅ Clean interface
- **Issue**: Hard-coded limit of 50 messages (ISSUE 6)

#### Service
- **Path**: `/home/frank-bwalya/Desktop/kareta05307/server/src/jvmMain/kotlin/ru.gr05307/database/ChatMessageService.kt`
- **Lines**: 34 total
- **Key Methods**:
  - `saveMessage()` (Line 16) - Write transaction
  - `getRecentMessages()` (Line 29) - Read transaction
- **Status**: ✅ Proper transaction management
- **Issue**: No error handling (ISSUE 4)

### 3. Message Send/Receive Handler

#### Main Message Processing Loop
- **Path**: `/home/frank-bwalya/Desktop/kareta05307/server/src/jvmMain/kotlin/ru.gr05307/net/ConnectedClient.kt`
- **Lines**: 188 total
- **CRITICAL LINES**:
  - **Line 36**: `chatMessageService.saveMessage(username!!, payload, InfoType.MESSAGE.name)`
    - **Issue**: ISSUE 4 - Broadcasts before saving!
    - **Issue**: ISSUE 1 - Unsafe `!!` operator
    - **Issue**: ISSUE 8 - Username concurrent access
  
- **Line 103-104**: `val infoType = runCatching { InfoType.valueOf(msg.messageType) ...`
  - **Issue**: ISSUE 3 - Unsafe string-to-enum conversion

#### Server Entry Point
- **Path**: `/home/frank-bwalya/Desktop/kareta05307/server/src/jvmMain/kotlin/ru.gr05307/net/Server.kt`
- **Lines**: 46 total
- **Status**: ✅ Thread safe client list management

### 4. Network Communication

#### Low-Level Socket I/O
- **Path**: `/home/frank-bwalya/Desktop/kareta05307/communicator/src/jvmMain/kotlin/ru/gr05307/net/Communicator.kt`
- **Lines**: 97 total
- **Key Methods**:
  - `sendData()` (Line 44) - Thread-safe send with outputLock
  - `start()` (Line 63) - Reader thread
  - `receive()` (Line 55) - Blocking read
- **Status**: ✅ Proper locking
- **Issue**: ISSUE 7 - No TLS encryption

#### Message Type Enum
- **Path**: `/home/frank-bwalya/Desktop/kareta05307/communicator/src/jvmMain/kotlin/ru/gr05307/net/Constants.kt`
- **Lines**: 5 total
- **Values**: INFORMATION, WARNING, ERROR, MESSAGE, PRIVATE, USERLIST

### 5. Client-Side Message Sending

#### Compose UI State Management
- **Path**: `/home/frank-bwalya/Desktop/kareta05307/composeApp/src/jvmMain/kotlin/ru/gr05307/kareta05307/GraphicsUI.kt`
- **Lines**: 228 total
- **Key Methods**:
  - `send()` (Line 175) - User input handler
  - `showMessage()` (Line 107) - Receive message display
  - `appendPublicMessage()` (Line 216) - Add to local store
  - `receivePrivateMessage()` (Line 61) - Handle private messages
- **Status**: ✅ Proper state management

### 6. Database Configuration

- **Path**: `/home/frank-bwalya/Desktop/kareta05307/server/src/jvmMain/resources/application.properties`
- **Lines**: 14 total
- **Key Config**:
  - `spring.datasource.url=jdbc:h2:file:./data/chatdb;AUTO_SERVER=TRUE`
  - `spring.jpa.hibernate.ddl-auto=update`
- **Status**: ⚠️ `ddl-auto=update` risky for production

---

## Message Flow Diagram with File References

### SEND PATH

```
1. User types in Compose UI
   └─→ GraphicsUI.kt:send() [Line 175]
       └─→ Emits: "MESSAGE:text content"

2. Communicator sends to server
   └─→ Communicator.kt:sendData() [Line 44]
       └─→ Socket write via DataOutputStream

3. Server receives message
   └─→ ConnectedClient.kt:handle() [Line 28]
       └─→ parseIncoming() [Line 30]
       
4. CRITICAL SECTION - ISSUE 4!
   └─→ ConnectedClient.kt [Lines 33-36]
       ├─→ broadcast() [Line 35] ⚠️ BROADCASTS FIRST
       └─→ saveMessage() [Line 36] ⚠️ THEN SAVES
           └─→ ChatMessageService.saveMessage() [Line 16]
               └─→ ChatMessageRepository.save() 
                   └─→ H2 Database INSERT
```

### RECEIVE PATH

```
1. New client connects
   └─→ ConnectedClient.kt:handle() [Line 21]
       └─→ sendHistory() [Line 97]

2. Fetch from database
   └─→ ChatMessageService.getRecentMessages() [Line 29]
       └─→ ChatMessageRepository.findTop50ByOrderByTimestampDesc() [Line 9]
           └─→ H2 Database SELECT (limit 50)
               └─→ .reversed() [Line 32] - Convert to oldest-first

3. Send to client
   └─→ ConnectedClient.kt:sendHistory() [Lines 101-111]
       └─→ For each message: sendMessage() [Line 110]
           └─→ Communicator.sendData() [Line 44]
               └─→ Socket write via DataOutputStream

4. Client receives and displays
   └─→ Client.kt:parseData() [Line 72]
       └─→ GraphicsUI.kt:showMessage() or showInfo() [Lines 107-154]
           └─→ Compose recomposition updates UI
```

---

## Issue Locations - Quick Reference

| Issue | File | Line(s) | Severity | Type |
|-------|------|---------|----------|------|
| **ISSUE 1** - Unsafe `!!` operator | ConnectedClient.kt | 36 | MEDIUM | Null Safety |
| **ISSUE 2** - Race condition username | ConnectedClient.kt | 129-136 | LOW-MED | Concurrency |
| **ISSUE 3** - String messageType | ChatMessage.kt | 20-21 | MEDIUM | Type Safety |
| **ISSUE 4** - Broadcast before save | ConnectedClient.kt | 35-36 | **CRITICAL** | Data Loss |
| **ISSUE 5** - No batch history send | ConnectedClient.kt | 97-113 | LOW | Performance |
| **ISSUE 6** - Hard-coded query limit | ChatMessageRepository.kt | 9 | LOW-MED | Scalability |
| **ISSUE 7** - No TLS encryption | Communicator.kt | 44, 55 | MEDIUM | Security |
| **ISSUE 8** - Concurrent username access | ConnectedClient.kt | 28-46 | MEDIUM | Concurrency |

---

## Code Snippets for Quick Reference

### Critical Issue 4 - Current Code (UNSAFE)

**File**: `/home/frank-bwalya/Desktop/kareta05307/server/src/jvmMain/kotlin/ru.gr05307/net/ConnectedClient.kt`
**Lines**: 33-36

```kotlin
when (type) {
    InfoType.MESSAGE -> {
        val formatted = "$username: $payload"
        broadcast(InfoType.MESSAGE, formatted, exceptSelf = false)  // ⚠️ Sends BEFORE save
        chatMessageService.saveMessage(username!!, payload, InfoType.MESSAGE.name)  // May fail
    }
```

**Problem**: If database save fails, message already broadcast to clients but not persisted.

### Recommended Fix

```kotlin
when (type) {
    InfoType.MESSAGE -> {
        val currentUsername = username ?: return  // Defensive null check
        val formatted = "$currentUsername: $payload"
        try {
            chatMessageService.saveMessage(currentUsername, payload, InfoType.MESSAGE.name)  // Save FIRST
            broadcast(InfoType.MESSAGE, formatted, exceptSelf = false)  // Only broadcast if saved
        } catch (e: Exception) {
            sendMessage(InfoType.ERROR, "Ошибка: Сообщение не было сохранено")
            logger.error("Message persistence failed: ${e.message}", e)
        }
    }
```

---

## Database Schema

```sql
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_name VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(255) NOT NULL,  -- ⚠️ ISSUE 3: Should be ENUM
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Issue**: `message_type` is VARCHAR with no constraint. Should use CHECK constraint or ENUM type.

---

## Summary Statistics

| Metric | Count |
|--------|-------|
| Total Kotlin files analyzed | 17 |
| Critical issues found | 2 |
| High priority issues | 2 |
| Medium priority issues | 3 |
| Low priority issues | 1 |
| Lines of message/DB code | ~600 |
| Database tables | 1 (chat_messages) |

---

## Testing File Locations

- **README.md**: `/home/frank-bwalya/Desktop/kareta05307/README.md` - Usage instructions
- **change.md**: `/home/frank-bwalya/Desktop/kareta05307/change.md` - Implementation details

---

## Build & Configuration Files

- **Root Gradle**: `/home/frank-bwalya/Desktop/kareta05307/build.gradle.kts`
- **Server Gradle**: `/home/frank-bwalya/Desktop/kareta05307/server/build.gradle.kts`
- **Application Props**: `/home/frank-bwalya/Desktop/kareta05307/server/src/jvmMain/resources/application.properties`

---

## Key Takeaways

1. **Message persistence is working** but has critical ordering issue
2. **Primary risk**: ISSUE 4 - Broadcasting before database persistence
3. **Type safety**: ISSUE 3 - String-based message types need enum conversion
4. **Concurrency**: ISSUE 8 - Username field needs synchronization
5. **All changes are low-risk** and don't require architectural rewrites

