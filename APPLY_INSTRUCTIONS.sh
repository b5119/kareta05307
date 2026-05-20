# How to Apply the DB Integration — kareta05307
# Run all commands from: ~/Desktop/kareta05307

# ════════════════════════════════════════════════════════
# STEP 1 — Create the database package directory
# ════════════════════════════════════════════════════════
mkdir -p server/src/jvmMain/kotlin/ru.gr05307/database
mkdir -p server/src/jvmMain/resources

# ════════════════════════════════════════════════════════
# STEP 2 — Copy the 3 new DB files
# ════════════════════════════════════════════════════════
# ChatMessage.kt  → server/src/jvmMain/kotlin/ru.gr05307/database/
# ChatMessageRepository.kt → same folder
# ChatMessageService.kt    → same folder

# ════════════════════════════════════════════════════════
# STEP 3 — Replace 4 existing server files
# ════════════════════════════════════════════════════════
# Main.kt         → server/src/jvmMain/kotlin/ru.gr05307/
# MainViewModel.kt → server/src/jvmMain/kotlin/ru.gr05307/
# Server.kt       → server/src/jvmMain/kotlin/ru.gr05307/net/
# ConnectedClient.kt → server/src/jvmMain/kotlin/ru.gr05307/net/

# ════════════════════════════════════════════════════════
# STEP 4 — Add Spring config file
# ════════════════════════════════════════════════════════
# application.properties → server/src/jvmMain/resources/

# ════════════════════════════════════════════════════════
# STEP 5 — Replace server/build.gradle.kts
# ════════════════════════════════════════════════════════

# ════════════════════════════════════════════════════════
# STEP 6 — Update gradle/libs.versions.toml
# ════════════════════════════════════════════════════════
# Open gradle/libs.versions.toml and add these entries:

# In [versions]:
#   springBoot = "3.3.5"
#   springDependencyManagement = "1.1.6"
#   h2 = "2.3.232"

# In [libraries]:
#   spring-boot-starter-data-jpa = { module = "org.springframework.boot:spring-boot-starter-data-jpa", version.ref = "springBoot" }
#   h2 = { module = "com.h2database:h2", version.ref = "h2" }

# In [plugins]:
#   kotlin-spring = { id = "org.jetbrains.kotlin.plugin.spring", version.ref = "kotlin" }
#   kotlin-jpa   = { id = "org.jetbrains.kotlin.plugin.jpa",    version.ref = "kotlin" }
#   spring-boot  = { id = "org.springframework.boot",            version.ref = "springBoot" }
#   dependency-management = { id = "io.spring.dependency-management", version.ref = "springDependencyManagement" }

# ════════════════════════════════════════════════════════
# STEP 7 — Update root build.gradle.kts
# ════════════════════════════════════════════════════════
# In the root build.gradle.kts plugins{} block add (apply false):
#   alias(libs.plugins.kotlin.spring) apply false
#   alias(libs.plugins.kotlin.jpa) apply false
#   alias(libs.plugins.spring.boot) apply false
#   alias(libs.plugins.dependency.management) apply false

# ════════════════════════════════════════════════════════
# STEP 8 — Test build
# ════════════════════════════════════════════════════════
./gradlew :server:build

# ════════════════════════════════════════════════════════
# STEP 9 — Run server then client
# ════════════════════════════════════════════════════════
./gradlew :server:run       # starts Spring + TCP server
# in a second terminal:
./gradlew :composeApp:run   # start the chat client
