package org.monogram.data.datasource.remote

import org.drinkless.tdlib.TdApi

interface ChatsRemoteDataSource {
    suspend fun getChat(chatId: Long): TdApi.Chat?
    suspend fun searchChats(query: String, limit: Int): TdApi.Chats?
    suspend fun searchPublicChats(query: String): TdApi.Chats?
    suspend fun getChatNotificationSettingsExceptions(scope: TdApi.NotificationSettingsScope, compareSound: Boolean): TdApi.Chats?
    suspend fun getForumTopics(chatId: Long, query: String, offsetDate: Int, offsetMessageId: Long, offsetForumTopicId: Int, limit: Int): TdApi.ForumTopics?
}