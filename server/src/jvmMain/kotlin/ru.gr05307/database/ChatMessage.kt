package ru.gr05307.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "chat_messages")
open class ChatMessage(
    @Column(nullable = false)
    open var senderName: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    open var content: String,

    @Column(nullable = false)
    open var messageType: String,   // "MESSAGE", "INFORMATION", "WARNING", "ERROR"

    @Column(nullable = false)
    open var timestamp: LocalDateTime = LocalDateTime.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    // No-arg constructor required by JPA/Hibernate
    constructor() : this("", "", "MESSAGE")
}
