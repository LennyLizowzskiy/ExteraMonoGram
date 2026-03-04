package org.monogram.presentation.chatsScreen.currentChat.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.withLock
import org.monogram.domain.models.MessageContent
import org.monogram.domain.models.MessageModel
import org.monogram.domain.models.MessageSendingState
import org.monogram.domain.repository.ReadUpdate
import org.monogram.presentation.chatsScreen.currentChat.DefaultChatComponent

private const val PAGE_SIZE = 50

internal suspend fun DefaultChatComponent.updateMessages(newMessages: List<MessageModel>, replace: Boolean = false) {
    messageMutex.withLock {
        val adBlockEnabled = appPreferences.isAdBlockEnabled.value
        val keywords = appPreferences.adBlockKeywords.value
        val whitelistedChannels = appPreferences.adBlockWhitelistedChannels.value
        val isChannel = _state.value.isChannel
        val isWhitelisted = whitelistedChannels.contains(chatId)

        val filteredNewMessages = if (adBlockEnabled && isChannel && !isWhitelisted) {
            withContext(Dispatchers.Default) {
                newMessages.filterNot { message ->
                    val text = when (val content = message.content) {
                        is MessageContent.Text -> content.text
                        is MessageContent.Photo -> content.caption
                        is MessageContent.Video -> content.caption
                        is MessageContent.Document -> content.caption
                        is MessageContent.Gif -> content.caption
                        else -> ""
                    }
                    keywords.any { text.contains(it, ignoreCase = true) }
                }
            }
        } else {
            newMessages
        }

        withContext(Dispatchers.Main) {
            _state.value = _state.value.let { currentState ->
                val currentList = if (replace) {
                    currentState.messages.filter { it.sendingState is MessageSendingState.Pending }
                } else {
                    currentState.messages
                }

                val isComments = currentState.rootMessage != null
                val mergedMessages = (currentList + filteredNewMessages)
                    .distinctBy { it.id }
                    .let {
                        if (isComments) it.sortedBy { it.id }
                        else it.sortedByDescending { it.id }
                    }

                currentState.copy(messages = mergedMessages)
            }
        }
    }
}

internal fun DefaultChatComponent.loadMessages(force: Boolean = false) {
    val state = _state.value
    if (state.isLoading) return
    if (!force && state.messages.size >= PAGE_SIZE && state.currentTopicId == null) return

    cancelAllLoadingJobs()
    messageLoadingJob = scope.launch {
        _state.value = _state.value.copy(
            isLoading = true,
            isOldestLoaded = false,
            isLatestLoaded = false
        )

        try {
            val threadId = _state.value.currentTopicId
            val isComments = _state.value.rootMessage != null
            val savedScrollPosition = if (threadId == null) cacheProvider.getChatScrollPosition(chatId) else 0L

            if (isComments && threadId != null) {
                loadComments(threadId)
            } else if (savedScrollPosition != 0L) {
                loadAroundMessage(savedScrollPosition, threadId)
            } else {
                val chat = chatsListRepository.getChatById(chatId)
                val firstUnreadId = chat?.lastReadInboxMessageId?.let { lastRead ->
                    if (chat.unreadCount > 0) {
                        repositoryMessage.getMessagesNewer(chatId, lastRead, 1, threadId).firstOrNull()?.id
                    } else null
                }

                if (firstUnreadId != null) {
                    loadAroundMessage(firstUnreadId, threadId)
                } else {
                    loadBottomMessages(threadId)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (coroutineContext[Job] === messageLoadingJob) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

private suspend fun DefaultChatComponent.loadComments(threadId: Long) {
    val messages = repositoryMessage.getMessagesNewer(chatId, threadId, PAGE_SIZE, threadId)
    val reachedEnd = messages.size < PAGE_SIZE
    _state.value = _state.value.copy(
        isAtBottom = reachedEnd,
        isLatestLoaded = reachedEnd,
        isOldestLoaded = true,
        scrollToMessageId = null
    )
    updateMessages(messages, replace = true)
    if (!reachedEnd) {
        delay(200)
        loadNewerMessages()
    }
}

private suspend fun DefaultChatComponent.loadBottomMessages(threadId: Long?) {
    val messages = repositoryMessage.getMessagesOlder(chatId, 0, PAGE_SIZE, threadId)
    val isOldestLoaded = messages.size < PAGE_SIZE
    _state.value = _state.value.copy(
        isAtBottom = true,
        isLatestLoaded = true,
        isOldestLoaded = isOldestLoaded,
        scrollToMessageId = null
    )
    updateMessages(messages, replace = true)
    if (!isOldestLoaded) {
        delay(200)
        loadMoreMessages()
    }
}

private suspend fun DefaultChatComponent.loadAroundMessage(messageId: Long, threadId: Long?) {
    val messages = repositoryMessage.getMessagesAround(chatId, messageId, PAGE_SIZE, threadId)
    if (messages.isNotEmpty()) {
        _state.value = _state.value.copy(
            isAtBottom = false,
            isLatestLoaded = false,
            isOldestLoaded = false,
            scrollToMessageId = messageId,
            highlightedMessageId = messageId
        )
        updateMessages(messages, replace = true)
        delay(200)
        loadMoreMessages()
        loadNewerMessages()
    } else {
        loadBottomMessages(threadId)
    }
}

internal fun DefaultChatComponent.loadMoreMessages() {
    val state = _state.value
    val forceLoad = state.isOldestLoaded && state.messages.size < 10
    if (state.isLoadingOlder || (state.isOldestLoaded && !forceLoad)) return

    loadMoreJob?.cancel()
    val job = scope.launch {
        _state.value = _state.value.copy(isLoadingOlder = true)
        try {
            val currentMessages = _state.value.messages
            val isComments = _state.value.rootMessage != null
            val threadId = _state.value.currentTopicId

            val anchorId = if (isComments) {
                currentMessages.firstOrNull { it.id > 0 }?.id ?: 0L
            } else {
                currentMessages.lastOrNull { it.id > 0 }?.id ?: 0L
            }

            val olderMessages = repositoryMessage.getMessagesOlder(chatId, anchorId, PAGE_SIZE, threadId)

            val isOldestLoaded =
                olderMessages.size < PAGE_SIZE || (anchorId != 0L && olderMessages.all { msg -> currentMessages.any { it.id == msg.id } })

            if (olderMessages.isNotEmpty()) {
                updateMessages(olderMessages)
            }

            if (coroutineContext[Job] === loadMoreJob) {
                _state.value = _state.value.copy(isOldestLoaded = isOldestLoaded)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (coroutineContext[Job] === loadMoreJob) {
                _state.value = _state.value.copy(isLoadingOlder = false)
            }
        }
    }
    loadMoreJob = job
}

internal fun DefaultChatComponent.loadNewerMessages() {
    val state = _state.value
    if (state.isLoadingNewer || state.isLatestLoaded) return

    loadNewerJob?.cancel()
    val job = scope.launch {
        _state.value = _state.value.copy(isLoadingNewer = true)
        try {
            val currentMessages = _state.value.messages
            val isComments = _state.value.rootMessage != null
            val threadId = _state.value.currentTopicId

            val anchorId = if (isComments) {
                currentMessages.lastOrNull { it.id > 0 }?.id ?: return@launch
            } else {
                currentMessages.firstOrNull { it.id > 0 }?.id ?: return@launch
            }

            val newerMessages = repositoryMessage.getMessagesNewer(chatId, anchorId, PAGE_SIZE, threadId)
            val isLatestLoaded =
                newerMessages.size < PAGE_SIZE || (newerMessages.isNotEmpty() && newerMessages.all { msg -> currentMessages.any { it.id == msg.id } })

            if (newerMessages.isNotEmpty()) {
                updateMessages(newerMessages)
            }

            if (coroutineContext[Job] === loadNewerJob) {
                _state.value = _state.value.copy(isLatestLoaded = isLatestLoaded)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (coroutineContext[Job] === loadNewerJob) {
                _state.value = _state.value.copy(isLoadingNewer = false)
            }
        }
    }
    loadNewerJob = job
}

internal fun DefaultChatComponent.scrollToMessageInternal(messageId: Long) {
    cancelAllLoadingJobs()
    messageLoadingJob = scope.launch {
        _state.value = _state.value.copy(
            isLoading = true,
            isOldestLoaded = false,
            isLatestLoaded = false
        )
        try {
            loadAroundMessage(messageId, _state.value.currentTopicId)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (coroutineContext[Job] === messageLoadingJob) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

internal fun DefaultChatComponent.scrollToBottomInternal() {
    if (_state.value.isLoading) return
    cancelAllLoadingJobs()
    messageLoadingJob = scope.launch {
        _state.value = _state.value.copy(
            isLoading = true,
            isOldestLoaded = false,
            isLatestLoaded = false
        )
        try {
            loadBottomMessages(_state.value.currentTopicId)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (coroutineContext[Job] === messageLoadingJob) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

internal fun DefaultChatComponent.cancelAllLoadingJobs() {
    messageLoadingJob?.cancel()
    loadMoreJob?.cancel()
    loadNewerJob?.cancel()
}

internal fun DefaultChatComponent.setupMessageCollectors() {
    repositoryMessage.newMessageFlow
        .onEach { message ->
            if (message.chatId == chatId) {
                val currentMessages = _state.value.messages
                val exists = currentMessages.any { it.id == message.id }

                if (exists) {
                    val updated = currentMessages.map { if (it.id == message.id) message else it }
                    _state.value = _state.value.copy(messages = updated)
                } else {
                    val state = _state.value
                    val isCorrectThread =
                        state.currentTopicId == null || message.threadId?.toLong() == state.currentTopicId

                    if (isCorrectThread && (state.isAtBottom || state.isLatestLoaded || message.isOutgoing)) {
                        updateMessages(listOf(message))
                        if (state.isAtBottom || state.isLatestLoaded) {
                            _state.value = _state.value.copy(isLatestLoaded = true)
                        }
                    }
                }
            }
        }
        .launchIn(scope)

    repositoryMessage.messageIdUpdateFlow
        .onEach { (cId, oldId, newMessage) ->
            if (cId == chatId) {
                val currentMessages = _state.value.messages.toMutableList()
                val index = currentMessages.indexOfFirst { it.id == oldId }

                if (index != -1) {
                    currentMessages[index] = newMessage
                    _state.value = _state.value.copy(messages = currentMessages)
                } else if (_state.value.isAtBottom || _state.value.isLatestLoaded || newMessage.isOutgoing) {
                    updateMessages(listOf(newMessage))
                    if (_state.value.isAtBottom || _state.value.isLatestLoaded) {
                        _state.value = _state.value.copy(isLatestLoaded = true)
                    }
                }
            }
        }
        .launchIn(scope)

    repositoryMessage.messageUploadProgressFlow
        .onEach { (messageId, progress) ->
            updateMessageContent(messageId) { message ->
                val isUploading = progress < 1f && message.sendingState is MessageSendingState.Pending
                val newSendingState = if (progress >= 1f) null else message.sendingState

                val newContent = when (val content = message.content) {
                    is MessageContent.Photo -> content.copy(isUploading = isUploading, uploadProgress = progress)
                    is MessageContent.Video -> content.copy(isUploading = isUploading, uploadProgress = progress)
                    is MessageContent.VideoNote -> content.copy(isUploading = isUploading, uploadProgress = progress)
                    is MessageContent.Document -> content.copy(isUploading = isUploading, uploadProgress = progress)
                    is MessageContent.Gif -> content.copy(isUploading = isUploading, uploadProgress = progress)
                    is MessageContent.Voice -> content.copy(isUploading = isUploading, uploadProgress = progress)
                    else -> content
                }
                message.copy(content = newContent, sendingState = newSendingState)
            }
        }
        .launchIn(scope)

    repositoryMessage.messageDownloadProgressFlow
        .onEach { (messageId, progress) ->
            updateMessageContent(messageId) { message ->
                val isDownloading = progress < 1f
                val newContent = when (val content = message.content) {
                    is MessageContent.Photo -> content.copy(
                        isDownloading = isDownloading,
                        downloadProgress = progress,
                        downloadError = false
                    )

                    is MessageContent.Video -> content.copy(
                        isDownloading = isDownloading,
                        downloadProgress = progress,
                        downloadError = false
                    )

                    is MessageContent.VideoNote -> content.copy(
                        isDownloading = isDownloading,
                        downloadProgress = progress,
                        downloadError = false
                    )

                    is MessageContent.Document -> content.copy(
                        isDownloading = isDownloading,
                        downloadProgress = progress,
                        downloadError = false
                    )

                    is MessageContent.Gif -> content.copy(
                        isDownloading = isDownloading,
                        downloadProgress = progress,
                        downloadError = false
                    )

                    is MessageContent.Voice -> content.copy(
                        isDownloading = isDownloading,
                        downloadProgress = progress,
                        downloadError = false
                    )

                    is MessageContent.Sticker -> content.copy(
                        isDownloading = isDownloading,
                        downloadProgress = progress,
                        downloadError = false
                    )

                    else -> content
                }
                message.copy(content = newContent)
            }
        }
        .launchIn(scope)

    repositoryMessage.messageDownloadCompletedFlow
        .onEach { (messageId, path) ->
            var fileIdToRetry: Int? = null

            updateMessageContent(messageId) { message ->
                val isError = path.isEmpty()
                val finalPath = path.ifEmpty { null }

                val newContent = when (val content = message.content) {
                    is MessageContent.Photo -> {
                        if (isError) fileIdToRetry = content.fileId
                        content.copy(path = finalPath, isDownloading = false, downloadError = isError)
                    }

                    is MessageContent.Video -> {
                        if (isError) fileIdToRetry = content.fileId
                        content.copy(path = finalPath, isDownloading = false, downloadError = isError)
                    }

                    is MessageContent.VideoNote -> {
                        if (isError) fileIdToRetry = content.fileId
                        content.copy(path = finalPath, isDownloading = false, downloadError = isError)
                    }

                    is MessageContent.Document -> {
                        if (isError) fileIdToRetry = content.fileId
                        content.copy(path = finalPath, isDownloading = false, downloadError = isError)
                    }

                    is MessageContent.Gif -> {
                        if (isError) fileIdToRetry = content.fileId
                        content.copy(path = finalPath, isDownloading = false, downloadError = isError)
                    }

                    is MessageContent.Voice -> {
                        if (isError) fileIdToRetry = content.fileId
                        content.copy(path = finalPath, isDownloading = false, downloadError = isError)
                    }

                    is MessageContent.Sticker -> {
                        if (isError) fileIdToRetry = content.fileId
                        content.copy(path = finalPath, isDownloading = false, downloadError = isError)
                    }

                    else -> content
                }
                message.copy(content = newContent)
            }

            fileIdToRetry?.let { if (it != 0) onDownloadFile(it) }
        }
        .launchIn(scope)

    repositoryMessage.messageDeletedFlow
        .onEach { (cId, messageIds) ->
            if (cId == chatId) {
                val currentMessages = _state.value.messages.toMutableList()
                val removed = currentMessages.removeAll { messageIds.contains(it.id) }
                if (removed) {
                    _state.value = _state.value.copy(messages = currentMessages)
                }
            }
        }
        .launchIn(scope)

    repositoryMessage.messageEditedFlow
        .onEach { message ->
            if (message.chatId == chatId) {
                updateMessageContent(message.id) { message }
            }
        }
        .launchIn(scope)

    repositoryMessage.mediaUpdateFlow
        .onEach {
            loadChatInfo()
        }
        .launchIn(scope)

    repositoryMessage.messageReadFlow
        .onEach { readUpdate ->
            if (readUpdate.chatId == chatId) {
                val currentMessages = _state.value.messages
                var hasChanges = false
                val updatedMessages = currentMessages.map { message ->
                    if (readUpdate is ReadUpdate.Outbox && message.isOutgoing && !message.isRead && message.id <= readUpdate.messageId) {
                        hasChanges = true
                        message.copy(isRead = true)
                    } else {
                        message
                    }
                }
                if (hasChanges) {
                    _state.value = _state.value.copy(messages = updatedMessages)
                }
            }
        }
        .launchIn(scope)
}

private inline fun DefaultChatComponent.updateMessageContent(
    messageId: Long,
    transform: (MessageModel) -> MessageModel
) {
    val currentMessages = _state.value.messages.toMutableList()
    val index = currentMessages.indexOfFirst { it.id == messageId }
    if (index != -1) {
        currentMessages[index] = transform(currentMessages[index])
        _state.value = _state.value.copy(messages = currentMessages)
    }
}

internal fun DefaultChatComponent.loadDraft() {
    scope.launch {
        val threadId = _state.value.currentTopicId
        val draft = repositoryMessage.getChatDraft(chatId, threadId)
        if (!draft.isNullOrEmpty()) {
            _state.value = _state.value.copy(draftText = draft)
        }
    }
}
