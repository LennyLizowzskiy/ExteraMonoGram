package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi

interface ChatsCacheDataSource {
    fun getChat(chatId: Long): TdApi.Chat?
    fun putChat(chat: TdApi.Chat)
    fun getSupergroup(supergroupId: Long): TdApi.Supergroup?
    fun putSupergroup(supergroup: TdApi.Supergroup)
    fun getBasicGroup(basicGroupId: Long): TdApi.BasicGroup?
    fun putBasicGroup(basicGroup: TdApi.BasicGroup)
    fun getSupergroupFullInfo(supergroupId: Long): TdApi.SupergroupFullInfo?
    fun putSupergroupFullInfo(supergroupId: Long, supergroupFullInfo: TdApi.SupergroupFullInfo)
    fun getBasicGroupFullInfo(basicGroupId: Long): TdApi.BasicGroupFullInfo?
    fun putBasicGroupFullInfo(basicGroupId: Long, basicGroupFullInfo: TdApi.BasicGroupFullInfo)
    fun getChatPermissions(chatId: Long): TdApi.ChatPermissions?
    fun putChatPermissions(chatId: Long, permissions: TdApi.ChatPermissions)
    fun getMyChatMember(chatId: Long): TdApi.ChatMember?
    fun putMyChatMember(chatId: Long, chatMember: TdApi.ChatMember)
    fun getOnlineMemberCount(chatId: Long): Int?
    fun putOnlineMemberCount(chatId: Long, count: Int)
    fun getSecretChat(secretChatId: Long): TdApi.SecretChat?
    fun putSecretChat(secretChat: TdApi.SecretChat)
    fun clearAll()
}