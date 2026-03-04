package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi
import java.util.concurrent.ConcurrentHashMap

class InMemorySettingsCacheDataSource : SettingsCacheDataSource {
    private val cache = ConcurrentHashMap<String, Any>()

    private inline fun <reified T> get(key: String): T? = cache[key] as? T

    private fun put(key: String, value: Any) {
        cache[key] = value
    }

    override fun getChat(chatId: Long): TdApi.Chat? = get("chat_$chatId")

    override fun putChat(chat: TdApi.Chat) {
        put("chat_${chat.id}", chat)
    }

    override fun getAttachMenuBots(): List<TdApi.AttachmentMenuBot>? = get("attachMenuBots")

    override fun putAttachMenuBots(bots: Array<TdApi.AttachmentMenuBot>) {
        put("attachMenuBots", bots.toList())
    }

    override fun clearAll() {
        cache.clear()
    }
}