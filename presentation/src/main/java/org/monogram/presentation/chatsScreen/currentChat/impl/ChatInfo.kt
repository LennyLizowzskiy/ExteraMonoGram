package org.monogram.presentation.chatsScreen.currentChat.impl

import org.monogram.domain.models.ChatModel
import org.monogram.domain.models.ChatType
import org.monogram.domain.models.UserStatusType
import org.monogram.domain.models.UserTypeEnum
import org.monogram.presentation.chatsScreen.currentChat.DefaultChatComponent
import org.monogram.presentation.util.getUserStatusText
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal fun DefaultChatComponent.loadChatInfo() {
    scope.launch {
        val chat = chatsListRepository.getChatById(chatId)
        if (chat != null) {
            updateChatState(chat)
            if (chat.viewAsTopics && state.value.topics.isEmpty()) {
                loadTopics()
            }

            val isBot = chat.type == ChatType.PRIVATE && chat.isBot
            if (isBot) {
                val botInfo = userRepository.getBotInfo(chatId)
                if (botInfo != null) {
                    _state.value = _state.value.copy(
                        botCommands = botInfo.commands,
                        botMenuButton = botInfo.menuButton,
                        isBot = true
                    )
                }
            }
        }
    }

    chatsListRepository.chatListFlow
        .map { chats -> chats.find { it.id == chatId } }
        .filterNotNull()
        .distinctUntilChanged { old, new ->
            old.viewAsTopics == new.viewAsTopics &&
                    old.title == new.title &&
                    old.avatarPath == new.avatarPath &&
                    old.personalAvatarPath == new.personalAvatarPath &&
                    old.isVerified == new.isVerified &&
                    old.emojiStatusPath == new.emojiStatusPath &&
                    old.isMuted == new.isMuted &&
                    old.permissions == new.permissions &&
                    old.unreadCount == new.unreadCount &&
                    old.unreadMentionCount == new.unreadMentionCount &&
                    old.unreadReactionCount == new.unreadReactionCount &&
                    old.isMember == new.isMember
        }
        .onEach { chat ->
            val wasTopics = state.value.viewAsTopics
            updateChatState(chat)
            if (chat.viewAsTopics) {
                if (state.value.topics.isEmpty()) {
                    loadTopics()
                }
            } else if (wasTopics) {
                loadMessages()
            }
        }
        .launchIn(scope)

    chatsListRepository.forumTopicsFlow
        .filter { it.first == chatId }
        .onEach { (_, topics) ->
            _state.value = _state.value.copy(topics = topics)
        }
        .launchIn(scope)
}

internal fun DefaultChatComponent.loadTopics() {
    if (state.value.isLoadingTopics) return
    scope.launch {
        _state.value = _state.value.copy(isLoadingTopics = true)
        try {
            val topics = chatsListRepository.getForumTopics(chatId)
            _state.value = _state.value.copy(topics = topics)
        } finally {
            _state.value = _state.value.copy(isLoadingTopics = false)
        }
    }
}

internal fun DefaultChatComponent.observeUserUpdates() {
    if (_state.value.isGroup || _state.value.isChannel) return
    scope.launch {
        userRepository.getUserFlow(chatId).collectLatest { user ->
            if (user != null) {
                val isBot = user.type == UserTypeEnum.BOT
                _state.value = _state.value.copy(
                    isOnline = !isBot && user.userStatus == UserStatusType.ONLINE,
                    isVerified = user.isVerified,
                    userStatus = getUserStatusText(user),
                    chatPersonalAvatar = user.personalAvatarPath
                )
            }
        }
    }
}

internal fun DefaultChatComponent.updateChatState(chat: ChatModel) {
    val isDetailedInfoMissing = (chat.isGroup || chat.isChannel) && chat.memberCount == 0
    val canWrite = if (chat.isAdmin) true else chat.permissions.canSendBasicMessages

    _state.value = _state.value.copy(
        chatTitle = chat.title,
        chatAvatar = chat.avatarPath,
        chatPersonalAvatar = chat.personalAvatarPath,
        chatEmojiStatus = chat.emojiStatusPath,
        isGroup = chat.isGroup,
        isChannel = chat.isChannel,
        isVerified = if (chat.isGroup || chat.isChannel) chat.isVerified else (chat.isVerified || _state.value.isVerified),
        canWrite = canWrite,
        isAdmin = chat.isAdmin,
        memberCount = if (!isDetailedInfoMissing) chat.memberCount else _state.value.memberCount,
        onlineCount = if (!isDetailedInfoMissing) chat.onlineCount else _state.value.onlineCount,
        unreadCount = chat.unreadCount,
        unreadMentionCount = chat.unreadMentionCount,
        unreadReactionCount = chat.unreadReactionCount,
        userStatus = chat.userStatus,
        typingAction = chat.typingAction,
        viewAsTopics = chat.viewAsTopics,
        isMuted = chat.isMuted,
        permissions = chat.permissions,
        isMember = chat.isMember
    )
}