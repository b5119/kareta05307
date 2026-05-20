package ru.gr05307.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    // Returns the most recent N messages, newest first
    fun findTop50ByOrderByTimestampDesc(): List<ChatMessage>
}
