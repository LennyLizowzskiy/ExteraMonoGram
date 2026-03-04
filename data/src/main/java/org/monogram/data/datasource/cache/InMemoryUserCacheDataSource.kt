package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserCacheDataSource : UserCacheDataSource {
    private val userCache = ConcurrentHashMap<Long, TdApi.User>()
    private val userFullInfoCache = ConcurrentHashMap<Long, TdApi.UserFullInfo>()
    private val supergroupFullInfoCache = ConcurrentHashMap<Long, TdApi.SupergroupFullInfo>()
    private val basicGroupFullInfoCache = ConcurrentHashMap<Long, TdApi.BasicGroupFullInfo>()
    private val supergroupCache = ConcurrentHashMap<Long, TdApi.Supergroup>()
    private val chatCache = ConcurrentHashMap<Long, TdApi.Chat>()
    private val messageCache = ConcurrentHashMap<Pair<Long, Long>, TdApi.Message>()

    override fun getUser(userId: Long): TdApi.User? = userCache[userId]

    override fun putUser(user: TdApi.User) {
        userCache[user.id] = user
    }

    override fun getUserFullInfo(userId: Long): TdApi.UserFullInfo? = userFullInfoCache[userId]

    override fun putUserFullInfo(userId: Long, userFullInfo: TdApi.UserFullInfo) {
        userFullInfoCache[userId] = userFullInfo
    }

    override fun getSupergroupFullInfo(supergroupId: Long): TdApi.SupergroupFullInfo? = supergroupFullInfoCache[supergroupId]

    override fun putSupergroupFullInfo(supergroupId: Long, supergroupFullInfo: TdApi.SupergroupFullInfo) {
        supergroupFullInfoCache[supergroupId] = supergroupFullInfo
    }

    override fun getBasicGroupFullInfo(basicGroupId: Long): TdApi.BasicGroupFullInfo? = basicGroupFullInfoCache[basicGroupId]

    override fun putBasicGroupFullInfo(basicGroupId: Long, basicGroupFullInfo: TdApi.BasicGroupFullInfo) {
        basicGroupFullInfoCache[basicGroupId] = basicGroupFullInfo
    }

    override fun getSupergroup(supergroupId: Long): TdApi.Supergroup? = supergroupCache[supergroupId]

    override fun putSupergroup(supergroup: TdApi.Supergroup) {
        supergroupCache[supergroup.id] = supergroup
    }

    override fun getChat(chatId: Long): TdApi.Chat? = chatCache[chatId]

    override fun putChat(chat: TdApi.Chat) {
        chatCache[chat.id] = chat
    }

    override fun getMessage(chatId: Long, messageId: Long): TdApi.Message? = messageCache[chatId to messageId]

    override fun putMessage(message: TdApi.Message) {
        messageCache[message.chatId to message.id] = message
    }

    override fun removeMessage(chatId: Long, messageId: Long) {
        messageCache.remove(chatId to messageId)
    }

    override fun clearAll() {
        userCache.clear()
        userFullInfoCache.clear()
        supergroupFullInfoCache.clear()
        basicGroupFullInfoCache.clear()
        supergroupCache.clear()
        chatCache.clear()
        messageCache.clear()
    }
}