package org.monogram.presentation.chatsScreen.currentChat.impl

import org.monogram.presentation.chatsScreen.currentChat.DefaultChatComponent
import kotlinx.coroutines.launch

internal fun DefaultChatComponent.handleToggleMessageSelection(messageId: Long) {
    val current = _state.value.selectedMessageIds
    if (current.contains(messageId)) {
        _state.value = _state.value.copy(selectedMessageIds = current - messageId)
    } else {
        if (current.size < 100) {
            _state.value = _state.value.copy(selectedMessageIds = current + messageId)
        }
    }
}

internal fun DefaultChatComponent.handleClearSelection() {
    _state.value = _state.value.copy(selectedMessageIds = emptySet())
}

internal fun DefaultChatComponent.handleDeleteSelectedMessages(revoke: Boolean = false) {
    val ids = _state.value.selectedMessageIds.toList().sorted()
    if (ids.isNotEmpty()) {
        scope.launch {
            repositoryMessage.deleteMessage(chatId, ids, revoke)
            onClearSelection()
        }
    }
}
