package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi

interface ChatLocalDataSource {
    fun getChat(chatId: Long): TdApi.Chat?
    fun putChat(chat: TdApi.Chat)
    fun getSupergroup(supergroupId: Long): TdApi.Supergroup?
    fun putSupergroup(supergroup: TdApi.Supergroup)
    fun getSupergroupFullInfo(supergroupId: Long): TdApi.SupergroupFullInfo?
    fun putSupergroupFullInfo(supergroupId: Long, info: TdApi.SupergroupFullInfo)
    fun getBasicGroupFullInfo(basicGroupId: Long): TdApi.BasicGroupFullInfo?
    fun putBasicGroupFullInfo(basicGroupId: Long, info: TdApi.BasicGroupFullInfo)
    fun clearAll()
}