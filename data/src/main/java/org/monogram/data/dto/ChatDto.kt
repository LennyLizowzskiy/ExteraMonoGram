package org.monogram.data.dto

import org.drinkless.tdlib.TdApi

data class ChatDto(
    val id: Long,
    val title: String,
    val lastMessageId: Long,
    val unreadCount: Int
)

fun TdApi.Chat.toDto(): ChatDto =
    ChatDto(
        id = id,
        title = title,
        unreadCount = unreadCount,
        lastMessageId = lastMessage?.id ?: 0
    )