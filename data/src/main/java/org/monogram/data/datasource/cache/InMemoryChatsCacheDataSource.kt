package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi
import java.util.concurrent.ConcurrentHashMap

class InMemoryChatsCacheDataSource : ChatsCacheDataSource {
    private val cache = ConcurrentHashMap<String, Any>()

    private inline fun <reified T> get(key: String): T? = cache[key] as? T

    private fun put(key: String, value: Any) {
        cache[key] = value
    }

    override fun getChat(chatId: Long): TdApi.Chat? = get("chat_$chatId")

    override fun putChat(chat: TdApi.Chat) = put("chat_${chat.id}", chat)

    override fun getSupergroup(supergroupId: Long): TdApi.Supergroup? = get("supergroup_$supergroupId")

    override fun putSupergroup(supergroup: TdApi.Supergroup) = put("supergroup_${supergroup.id}", supergroup)

    override fun getBasicGroup(basicGroupId: Long): TdApi.BasicGroup? = get("basicGroup_$basicGroupId")

    override fun putBasicGroup(basicGroup: TdApi.BasicGroup) = put("basicGroup_${basicGroup.id}", basicGroup)

    override fun getSupergroupFullInfo(supergroupId: Long): TdApi.SupergroupFullInfo? = get("supergroupFullInfo_$supergroupId")

    override fun putSupergroupFullInfo(supergroupId: Long, supergroupFullInfo: TdApi.SupergroupFullInfo) = put("supergroupFullInfo_$supergroupId", supergroupFullInfo)

    override fun getBasicGroupFullInfo(basicGroupId: Long): TdApi.BasicGroupFullInfo? = get("basicGroupFullInfo_$basicGroupId")

    override fun putBasicGroupFullInfo(basicGroupId: Long, basicGroupFullInfo: TdApi.BasicGroupFullInfo) = put("basicGroupFullInfo_$basicGroupId", basicGroupFullInfo)

    override fun getChatPermissions(chatId: Long): TdApi.ChatPermissions? = get("chatPermissions_$chatId")

    override fun putChatPermissions(chatId: Long, permissions: TdApi.ChatPermissions) = put("chatPermissions_$chatId", permissions)

    override fun getMyChatMember(chatId: Long): TdApi.ChatMember? = get("myChatMember_$chatId")

    override fun putMyChatMember(chatId: Long, chatMember: TdApi.ChatMember) = put("myChatMember_$chatId", chatMember)

    override fun getOnlineMemberCount(chatId: Long): Int? = get("onlineMemberCount_$chatId")

    override fun putOnlineMemberCount(chatId: Long, count: Int) = put("onlineMemberCount_$chatId", count)

    override fun getSecretChat(secretChatId: Long): TdApi.SecretChat? = get("secretChat_$secretChatId")

    override fun putSecretChat(secretChat: TdApi.SecretChat) = put("secretChat_${secretChat.id.toLong()}", secretChat)

    override fun clearAll() {
        cache.clear()
    }
}