package ru.gr05307.database

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository
) {
    /**
     * Persist a new chat message to the database.
     */
    @Transactional
    fun saveMessage(senderName: String, content: String, messageType: String): ChatMessage {
        val msg = ChatMessage(
            senderName = senderName,
            content = content,
            messageType = messageType,
            timestamp = LocalDateTime.now(),
        )
        return chatMessageRepository.save(msg)
    }

    /**
     * Returns up to 50 recent messages, oldest first (reversed for display).
     */
    fun getRecentMessages(): List<ChatMessage> {
        return chatMessageRepository
            .findTop50ByOrderByTimestampDesc()
            .reversed()   // flip so oldest is first — correct chat order
    }
}
