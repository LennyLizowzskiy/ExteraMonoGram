package org.monogram.domain.models

data class TopicModel(
    val id: Int,
    val name: String,
    val iconCustomEmojiId: Long = 0L,
    val iconCustomEmojiPath: String? = null,
    val iconColor: Int = 0,
    val isClosed: Boolean = false,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessageText: String = "",
    val lastMessageEntities: List<MessageEntity> = emptyList(),
    val lastMessageTime: String = "",
    val order: Long = 0L,
    val lastMessageSenderName: String? = null,
    val lastMessageSenderAvatar: String? = null
)