package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi
import java.util.concurrent.ConcurrentHashMap

class InMemoryChatLocalDataSource : ChatLocalDataSource {

    private val chats = ConcurrentHashMap<Long, TdApi.Chat>()
    private val supergroups = ConcurrentHashMap<Long, TdApi.Supergroup>()
    private val supergroupFullInfos = ConcurrentHashMap<Long, TdApi.SupergroupFullInfo>()
    private val basicGroupFullInfos = ConcurrentHashMap<Long, TdApi.BasicGroupFullInfo>()

    override fun getChat(chatId: Long): TdApi.Chat? = chats[chatId]

    override fun putChat(chat: TdApi.Chat) {
        chats[chat.id] = chat
    }

    override fun getSupergroup(supergroupId: Long): TdApi.Supergroup? = supergroups[supergroupId]

    override fun putSupergroup(supergroup: TdApi.Supergroup) {
        supergroups[supergroup.id] = supergroup
    }

    override fun getSupergroupFullInfo(supergroupId: Long): TdApi.SupergroupFullInfo? =
        supergroupFullInfos[supergroupId]

    override fun putSupergroupFullInfo(supergroupId: Long, info: TdApi.SupergroupFullInfo) {
        supergroupFullInfos[supergroupId] = info
    }

    override fun getBasicGroupFullInfo(basicGroupId: Long): TdApi.BasicGroupFullInfo? =
        basicGroupFullInfos[basicGroupId]

    override fun putBasicGroupFullInfo(basicGroupId: Long, info: TdApi.BasicGroupFullInfo) {
        basicGroupFullInfos[basicGroupId] = info
    }

    override fun clearAll() {
        chats.clear()
        supergroups.clear()
        supergroupFullInfos.clear()
        basicGroupFullInfos.clear()
    }
}