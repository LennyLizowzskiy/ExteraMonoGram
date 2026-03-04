package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi

interface SettingsCacheDataSource {
    fun getChat(chatId: Long): TdApi.Chat?
    fun putChat(chat: TdApi.Chat)
    fun getAttachMenuBots(): List<TdApi.AttachmentMenuBot>?
    fun putAttachMenuBots(bots: Array<TdApi.AttachmentMenuBot>)
    fun clearAll()
}