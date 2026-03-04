package org.monogram.data.chats

import org.drinkless.tdlib.TdApi
import org.monogram.data.datasource.cache.ChatsCacheDataSource
import org.monogram.data.datasource.cache.UserCacheDataSource
import java.util.concurrent.ConcurrentHashMap

class ChatCache : ChatsCacheDataSource, UserCacheDataSource {
    // Chats and their positions in lists
    val allChats = ConcurrentHashMap<Long, TdApi.Chat>()
    val activeListPositions = ConcurrentHashMap<Long, TdApi.ChatPosition>()
    val onlineMemberCount = ConcurrentHashMap<Long, Int>()

    // Messages: ChatId -> (MessageId -> Message)
    private val messages = ConcurrentHashMap<Long, ConcurrentHashMap<Long, TdApi.Message>>()

    // Users and their full info
    val usersCache = ConcurrentHashMap<Long, TdApi.User>()
    val userFullInfoCache = ConcurrentHashMap<Long, TdApi.UserFullInfo>()

    // Groups and Supergroups
    val basicGroups = ConcurrentHashMap<Long, TdApi.BasicGroup>()
    val basicGroupFullInfoCache = ConcurrentHashMap<Long, TdApi.BasicGroupFullInfo>()
    val supergroups = ConcurrentHashMap<Long, TdApi.Supergroup>()
    val supergroupFullInfoCache = ConcurrentHashMap<Long, TdApi.SupergroupFullInfo>()
    val secretChats = ConcurrentHashMap<Int, TdApi.SecretChat>()

    // Files
    val fileCache = ConcurrentHashMap<Int, TdApi.File>()

    // Permissions and Member Status
    val chatPermissionsCache = ConcurrentHashMap<Long, TdApi.ChatPermissions>()
    val myChatMemberCache = ConcurrentHashMap<Long, TdApi.ChatMember>()

    // Pending requests tracking
    val pendingChats = ConcurrentHashMap.newKeySet<Long>()
    val pendingUsers = ConcurrentHashMap.newKeySet<Long>()
    val pendingUserFullInfo = ConcurrentHashMap.newKeySet<Long>()
    val pendingBasicGroups = ConcurrentHashMap.newKeySet<Long>()
    val pendingBasicGroupFullInfo = ConcurrentHashMap.newKeySet<Long>()
    val pendingSupergroups = ConcurrentHashMap.newKeySet<Long>()
    val pendingSupergroupFullInfo = ConcurrentHashMap.newKeySet<Long>()
    val pendingSecretChats = ConcurrentHashMap.newKeySet<Int>()
    val pendingChatPermissions = ConcurrentHashMap.newKeySet<Long>()
    val pendingMyChatMember = ConcurrentHashMap.newKeySet<Long>()

    override fun getChat(chatId: Long): TdApi.Chat? = allChats[chatId]
    override fun putChat(chat: TdApi.Chat) {
        allChats[chat.id] = chat
    }

    override fun getUser(userId: Long): TdApi.User? = usersCache[userId]
    override fun putUser(user: TdApi.User) {
        usersCache[user.id] = user
    }

    override fun getUserFullInfo(userId: Long): TdApi.UserFullInfo? = userFullInfoCache[userId]
    override fun putUserFullInfo(userId: Long, userFullInfo: TdApi.UserFullInfo) {
        userFullInfoCache[userId] = userFullInfo
    }

    override fun getSupergroup(supergroupId: Long): TdApi.Supergroup? = supergroups[supergroupId]
    override fun putSupergroup(supergroup: TdApi.Supergroup) {
        supergroups[supergroup.id] = supergroup
    }

    override fun getBasicGroup(basicGroupId: Long): TdApi.BasicGroup? = basicGroups[basicGroupId]
    override fun putBasicGroup(basicGroup: TdApi.BasicGroup) {
        basicGroups[basicGroup.id] = basicGroup
    }

    override fun getSupergroupFullInfo(supergroupId: Long): TdApi.SupergroupFullInfo? =
        supergroupFullInfoCache[supergroupId]

    override fun putSupergroupFullInfo(supergroupId: Long, supergroupFullInfo: TdApi.SupergroupFullInfo) {
        supergroupFullInfoCache[supergroupId] = supergroupFullInfo
    }

    override fun getBasicGroupFullInfo(basicGroupId: Long): TdApi.BasicGroupFullInfo? =
        basicGroupFullInfoCache[basicGroupId]

    override fun putBasicGroupFullInfo(basicGroupId: Long, basicGroupFullInfo: TdApi.BasicGroupFullInfo) {
        basicGroupFullInfoCache[basicGroupId] = basicGroupFullInfo
    }

    override fun getChatPermissions(chatId: Long): TdApi.ChatPermissions? = chatPermissionsCache[chatId]
    override fun putChatPermissions(chatId: Long, permissions: TdApi.ChatPermissions) {
        chatPermissionsCache[chatId] = permissions
    }

    override fun getMyChatMember(chatId: Long): TdApi.ChatMember? = myChatMemberCache[chatId]
    override fun putMyChatMember(chatId: Long, chatMember: TdApi.ChatMember) {
        myChatMemberCache[chatId] = chatMember
    }

    override fun getOnlineMemberCount(chatId: Long): Int? = onlineMemberCount[chatId]
    override fun putOnlineMemberCount(chatId: Long, count: Int) {
        onlineMemberCount[chatId] = count
    }

    override fun getSecretChat(secretChatId: Long): TdApi.SecretChat? = secretChats[secretChatId.toInt()]
    override fun putSecretChat(secretChat: TdApi.SecretChat) {
        secretChats[secretChat.id] = secretChat
    }

    override fun getMessage(chatId: Long, messageId: Long): TdApi.Message? = messages[chatId]?.get(messageId)
    override fun putMessage(message: TdApi.Message) {
        messages.getOrPut(message.chatId) { ConcurrentHashMap() }[message.id] = message
    }

    override fun removeMessage(chatId: Long, messageId: Long) {
        messages[chatId]?.remove(messageId)
    }

    fun updateChat(chatId: Long, action: (TdApi.Chat) -> Unit) {
        allChats[chatId]?.let { synchronized(it) { action(it) } }
    }

    fun updateUser(userId: Long, action: (TdApi.User) -> Unit) {
        usersCache[userId]?.let { synchronized(it) { action(it) } }
    }

    fun removeMessages(chatId: Long, messageIds: List<Long>) {
        val chatMessages = messages[chatId] ?: return
        messageIds.forEach { chatMessages.remove(it) }
    }

    fun clearMessages(chatId: Long) {
        messages.remove(chatId)
    }

    fun updateMessageId(chatId: Long, oldId: Long, newId: Long) {
        val chatMessages = messages[chatId] ?: return
        val message = chatMessages.remove(oldId) ?: return
        message.id = newId
        chatMessages[newId] = message
    }

    override fun clearAll() {
        allChats.clear()
        activeListPositions.clear()
        onlineMemberCount.clear()
        messages.clear()
        usersCache.clear()
        userFullInfoCache.clear()
        basicGroups.clear()
        basicGroupFullInfoCache.clear()
        supergroups.clear()
        supergroupFullInfoCache.clear()
        secretChats.clear()
        fileCache.clear()
        chatPermissionsCache.clear()
        myChatMemberCache.clear()

        pendingChats.clear()
        pendingUsers.clear()
        pendingUserFullInfo.clear()
        pendingBasicGroups.clear()
        pendingBasicGroupFullInfo.clear()
        pendingSupergroups.clear()
        pendingSupergroupFullInfo.clear()
        pendingSecretChats.clear()
        pendingChatPermissions.clear()
        pendingMyChatMember.clear()
    }
}