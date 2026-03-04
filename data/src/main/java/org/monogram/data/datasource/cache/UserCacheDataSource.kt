package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi

interface UserCacheDataSource {
    fun getUser(userId: Long): TdApi.User?
    fun putUser(user: TdApi.User)

    fun getUserFullInfo(userId: Long): TdApi.UserFullInfo?
    fun putUserFullInfo(userId: Long, userFullInfo: TdApi.UserFullInfo)

    fun getSupergroupFullInfo(supergroupId: Long): TdApi.SupergroupFullInfo?
    fun putSupergroupFullInfo(supergroupId: Long, supergroupFullInfo: TdApi.SupergroupFullInfo)

    fun getBasicGroupFullInfo(basicGroupId: Long): TdApi.BasicGroupFullInfo?
    fun putBasicGroupFullInfo(basicGroupId: Long, basicGroupFullInfo: TdApi.BasicGroupFullInfo)

    fun getSupergroup(supergroupId: Long): TdApi.Supergroup?
    fun putSupergroup(supergroup: TdApi.Supergroup)

    fun getChat(chatId: Long): TdApi.Chat?
    fun putChat(chat: TdApi.Chat)

    fun getMessage(chatId: Long, messageId: Long): TdApi.Message?
    fun putMessage(message: TdApi.Message)
    fun removeMessage(chatId: Long, messageId: Long)

    fun clearAll()
}
