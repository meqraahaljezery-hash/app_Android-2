package com.example.data.model

enum class MessageType {
    TEXT,
    PHOTO,
    SYSTEM_EVENT,
    QURAN_AYAH
}

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: UserRole,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val timestamp: String,
    val isMine: Boolean = false
)

data class Conversation(
    val id: String,
    val participantName: String,
    val participantRole: String,
    val participantCode: String,
    val participantBio: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val isOfficial: Boolean = false,
    val enrolledPrograms: List<String> = listOf("الإجازة برواية حفص عن عاصم")
)
