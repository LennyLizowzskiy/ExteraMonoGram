package org.monogram.data.service

import kotlinx.coroutines.CoroutineScope
import org.drinkless.tdlib.TdApi

interface TdNotificationManager {
    fun observeUpdates(scope: CoroutineScope)
    fun isChatMuted(chat: TdApi.Chat): Boolean
    fun clearHistory(chatId: Long)
    fun getChat(chatId: Long, callback: (TdApi.Chat) -> Unit)
}