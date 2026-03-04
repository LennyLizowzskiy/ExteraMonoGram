package org.monogram.presentation.chatsScreen.currentChat.impl

import android.util.Log
import org.monogram.presentation.chatsScreen.currentChat.DefaultChatComponent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal fun DefaultChatComponent.loadPinnedMessage() {
    scope.launch {
        val threadId = _state.value.currentTopicId
        try {
            val pinned = repositoryMessage.getPinnedMessage(chatId, threadId)
            val count = repositoryMessage.getPinnedMessageCount(chatId, threadId)
            Log.d(
                "DefaultChatComponent",
                "loadPinnedMessage: chatId=$chatId, threadId=$threadId, pinnedId=${pinned?.id}, count=$count"
            )
            _state.value = _state.value.copy(
                pinnedMessage = pinned,
                pinnedMessageCount = count,
                pinnedMessageIndex = 0
            )
        } catch (e: Exception) {
            Log.e("DefaultChatComponent", "Error loading pinned message", e)
        }
    }
}

internal fun DefaultChatComponent.setupPinnedMessageCollector() {
    repositoryMessage.pinnedMessageFlow
        .onEach { cId ->
            if (cId == chatId) {
                Log.d("DefaultChatComponent", "pinnedMessageFlow triggered for chatId=$chatId")
                loadPinnedMessage()
            }
        }
        .launchIn(scope)
}

internal fun DefaultChatComponent.handlePinnedMessageClick(message: org.monogram.domain.models.MessageModel?) {
    if (message != null) {
        jumpToMessage(message)
        return
    }

    val currentState = _state.value
    if (currentState.pinnedMessageCount <= 1) {
        currentState.pinnedMessage?.let { jumpToMessage(it) }
        return
    }

    scope.launch {
        val threadId = currentState.currentTopicId
        val nextIndex = (currentState.pinnedMessageIndex + 1) % currentState.pinnedMessageCount
        val pinnedMessages = repositoryMessage.getAllPinnedMessages(chatId, threadId)
        if (pinnedMessages.isNotEmpty()) {
            val nextPinned = pinnedMessages.getOrNull(nextIndex) ?: pinnedMessages.first()
            _state.value = currentState.copy(
                pinnedMessage = nextPinned,
                pinnedMessageIndex = nextIndex
            )
        }
    }
}

private fun DefaultChatComponent.jumpToMessage(message: org.monogram.domain.models.MessageModel) {
    if (_state.value.isLoading) return

    scope.launch {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val threadId = _state.value.currentTopicId
            val messages = repositoryMessage.getMessagesAround(chatId, message.id, 50, threadId)
            if (messages.isNotEmpty()) {
                _state.value = _state.value.copy(
                    scrollToMessageId = message.id,
                    highlightedMessageId = message.id,
                    isAtBottom = false,
                    isLatestLoaded = false,
                    isOldestLoaded = false
                )
                updateMessages(messages, replace = true)
            }
        } catch (e: Exception) {
            Log.e("DefaultChatComponent", "Error jumping to message", e)
        } finally {
            _state.value = _state.value.copy(isLoading = false)
        }
    }
}
