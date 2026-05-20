plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
}

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Runs the server application."
    dependsOn("jvmJar")
    classpath(
        tasks.named("jvmJar"),
        configurations.named("jvmRuntimeClasspath"),
    )
    mainClass.set("ru.gr05307.MainKt")
}

val kotlinVersion = libs.versions.kotlin.get()

configurations.configureEach {
    if (name.startsWith("kotlin")) {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(kotlinVersion)
                because("Spring dependency management must not downgrade the Kotlin compiler toolchain")
            }
        }
    }
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":communicator"))
        }
        jvmMain.dependencies {
            implementation(project(":communicator"))
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.spring.boot.starter.data.jpa)
            runtimeOnly(libs.h2)
        }
    }
}

// Enable Spring Boot's fat jar task
springBoot {
    mainClass.set("ru.gr05307.MainKt")
}
