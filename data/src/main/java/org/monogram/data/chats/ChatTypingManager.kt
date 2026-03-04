package org.monogram.data.chats

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import java.util.concurrent.ConcurrentHashMap

class ChatTypingManager(
    private val scope: CoroutineScope,
    private val usersCache: Map<Long, TdApi.User>,
    private val allChats: Map<Long, TdApi.Chat>,
    private val onUpdate: () -> Unit,
    private val onUserNeeded: (Long) -> Unit
) {
    private val typingStates = ConcurrentHashMap<Long, ConcurrentHashMap<Long, String>>()
    private val typingJobs = ConcurrentHashMap<Long, ConcurrentHashMap<Long, Job>>()

    fun handleChatAction(update: TdApi.UpdateChatAction) {
        val chatId = update.chatId
        val action = update.action
        val senderId = update.senderId
        if (senderId !is TdApi.MessageSenderUser) return
        val userId = senderId.userId

        if (action is TdApi.ChatActionCancel) {
            removeTypingUser(chatId, userId)
            onUpdate()
            return
        }

        val actionString = when (action) {
            is TdApi.ChatActionTyping -> "печатает"
            is TdApi.ChatActionRecordingVideo -> "записывает видео"
            is TdApi.ChatActionRecordingVoiceNote -> "записывает голосовое"
            is TdApi.ChatActionUploadingPhoto -> "отправляет фото"
            is TdApi.ChatActionUploadingVideo -> "отправляет видео"
            is TdApi.ChatActionUploadingDocument -> "отправляет файл"
            is TdApi.ChatActionChoosingSticker -> "выбирает стикер"
            is TdApi.ChatActionStartPlayingGame -> "играет"
            else -> null
        }

        if (actionString == null) return

        if (usersCache[userId] == null) {
            onUserNeeded(userId)
        }

        val chatTyping = typingStates.getOrPut(chatId) { ConcurrentHashMap() }
        chatTyping[userId] = actionString
        val chatJobs = typingJobs.getOrPut(chatId) { ConcurrentHashMap() }
        chatJobs[userId]?.cancel()
        chatJobs[userId] = scope.launch {
            delay(6000)
            removeTypingUser(chatId, userId)
            onUpdate()
        }
        onUpdate()
    }

    fun removeTypingUser(chatId: Long, userId: Long) {
        typingStates[chatId]?.remove(userId)
        typingJobs[chatId]?.remove(userId)?.cancel()
    }

    fun formatTypingAction(chatId: Long): String? {
        val users = typingStates[chatId] ?: return null
        val activeUserIds = users.keys.toList()
        if (activeUserIds.isEmpty()) return null
        val chat = allChats[chatId] ?: return null
        if (chat.type is TdApi.ChatTypePrivate) {
            return "${users[activeUserIds[0]]}"
        }
        return when (val count = activeUserIds.size) {
            1 -> {
                val user = usersCache[activeUserIds[0]]
                if (user != null) "${user.firstName} ${users[activeUserIds[0]]}"
                else "Кто-то ${users[activeUserIds[0]]}"
            }

            2 -> {
                val user1 = usersCache[activeUserIds[0]]
                val user2 = usersCache[activeUserIds[1]]
                if (user1 != null && user2 != null) "${user1.firstName} и ${user2.firstName} печатают"
                else "2 человека печатают"
            }

            else -> {
                val user1 = usersCache[activeUserIds[0]]
                if (user1 != null) "${user1.firstName} и еще ${count - 1} печатают"
                else "$count человека печатают"
            }
        }
    }

    fun clearTypingStatus(chatId: Long) {
        typingStates.remove(chatId)
        typingJobs.remove(chatId)?.values?.forEach { it.cancel() }
    }
}
