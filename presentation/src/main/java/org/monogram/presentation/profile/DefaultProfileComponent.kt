package org.monogram.presentation.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import org.monogram.domain.models.*
import org.monogram.domain.repository.*
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import org.monogram.presentation.util.IDownloadUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DefaultProfileComponent(
    context: AppComponentContext,
    private val chatId: Long,
    private val chatsListRepository: ChatsListRepository = context.container.repositories.chatsListRepository,
    private val userRepository: UserRepository = context.container.repositories.userRepository,
    override val messageRepository: MessageRepository = context.container.repositories.messageRepository,
    override val videoPlayerPool: VideoPlayerPool = context.container.utils.videoPlayerPool,
    private val locationRepository: LocationRepository = context.container.repositories.locationRepository,
    private val botPreferences: BotPreferencesProvider = context.container.preferences.botPreferencesProvider,
    override val downloadUtils: IDownloadUtils = context.container.utils.downloadUtils(),
    private val onBackClicked: () -> Unit,
    private val onMessageClicked: (MessageModel) -> Unit = {},
    private val onMessageLongClicked: (MessageModel) -> Unit = {},
    private val onAvatarClicked: (String) -> Unit = {},
    private val onEditClicked: () -> Unit = {},
    private val onSendMessageClicked: (Long) -> Unit = {},
    private val onShowLogsClicked: (Long) -> Unit = {},
    private val onMemberClicked: (Long) -> Unit = {},
    private val onMemberLongClicked: (Long, Long) -> Unit = { _, _ -> }
) : ProfileComponent, AppComponentContext by context {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _state = MutableValue(ProfileComponent.State(chatId = chatId))
    override val state: Value<ProfileComponent.State> = _state

    private var lastLoadedMessageId: Long = 0L
    private val PAGE_SIZE = 50
    private var membersOffset = 0
    private var isCurrentlyLoadingMedia = false

    init {
        lifecycle.doOnDestroy {
            scope.cancel()
        }
        loadData()
        loadMediaNextPage(isFirstLoad = true)
        observeProfilePhotos()
        observeUserUpdates()
        observeCurrentUser()
    }

    private fun loadData() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val chat = chatsListRepository.getChatById(chatId)
                val user = if (chat != null && !chat.isGroup && !chat.isChannel) {
                    userRepository.getUser(chatId)
                } else null
                val fullInfo = userRepository.getChatFullInfo(chatId)
                val description = fullInfo?.description
                val link = fullInfo?.inviteLink
                    ?: chat?.username?.let { "https://t.me/$it" }
                    ?: user?.username?.let { "https://t.me/$it" }

                var botWebAppUrl: String? = null
                var botWebAppName: String? = null
                var isTOSAccepted = false

                if (user?.type == UserTypeEnum.BOT) {
                    val botInfo = userRepository.getBotInfo(chatId)
                    val menuButton = botInfo?.menuButton
                    if (menuButton is BotMenuButtonModel.WebApp) {
                        botWebAppUrl = menuButton.url
                        botWebAppName = menuButton.text
                    }
                    isTOSAccepted = botPreferences.getWebappPermission(user.id, "tos_accepted")
                }

                val linkedChatId = fullInfo?.linkedChatId?.takeIf { it != 0L }
                val linkedChat = linkedChatId?.let {
                    chatsListRepository.getChatById(it)
                }

                _state.value = _state.value.copy(
                    chat = chat,
                    user = user,
                    fullInfo = fullInfo,
                    about = description,
                    publicLink = link,
                    botWebAppUrl = botWebAppUrl,
                    botWebAppName = botWebAppName,
                    qrContent = link ?: "",
                    personalAvatarPath = user?.personalAvatarPath,
                    linkedChat = linkedChat,
                    isTOSAccepted = isTOSAccepted
                )
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun observeCurrentUser() {
        scope.launch {
            userRepository.currentUserFlow.collectLatest { user ->
                _state.value = _state.value.copy(currentUser = user)
            }
        }
    }

    override fun onLoadMoreMedia() {
        val isGroup = _state.value.chat?.let { it.isGroup || it.isChannel } ?: false
        if (isGroup && _state.value.selectedTabIndex == 1) {
            loadMembersNextPage()
        } else {
            loadMediaNextPage(isFirstLoad = false)
        }
    }

    override fun onDownloadMedia(message: MessageModel) {
        scope.launch {

            val highResId = if (message.content is MessageContent.Photo) {
                messageRepository.getHighResFileId(chatId, message.id)
            } else null
            val fileId = if (highResId != null && highResId != 0) {
                highResId
            } else {
                when (val content = message.content) {
                    is MessageContent.Photo -> content.fileId
                    is MessageContent.Video -> content.fileId
                    else -> 0
                }
            }

            if (fileId != 0) {
                messageRepository.downloadFile(fileId, priority = 32)
                var attempts = 0

                while (attempts < 60) {
                    delay(500)
                    val fileInfo = messageRepository.getFileInfo(fileId)
                    if (fileInfo?.local?.isDownloadingCompleted == true && fileInfo.local.path.isNotEmpty()) {
                        withContext(Dispatchers.Main) {

                            onFileDownloaded(fileId, fileInfo.local.path)


                            if (message.content is MessageContent.Photo) {
                                val currentImages = _state.value.fullScreenImages
                                if (currentImages != null && !_state.value.isViewingProfilePhotos) {
                                    val allPhotos = _state.value.mediaMessages.filter { it.content is MessageContent.Photo }
                                    val index = allPhotos.indexOfFirst { it.id == message.id }

                                    if (index != -1 && index < currentImages.size) {
                                        val newImages = currentImages.toMutableList()
                                        newImages[index] = fileInfo.local.path
                                        _state.value = _state.value.copy(fullScreenImages = newImages)
                                    }
                                }
                            }
                        }
                        break
                    }
                    attempts++
                }
            }
        }
    }

    fun onFileDownloaded(fileId: Int, newPath: String) {
        if (fileId == 0) return

        val currentState = _state.value

        val updatedMedia = currentState.mediaMessages.map { msg ->
            updateMessagePathIfNeeded(msg, fileId, newPath)
        }

        val updatedFiles = currentState.fileMessages.map { msg ->
            updateMessagePathIfNeeded(msg, fileId, newPath)
        }

        if (updatedMedia != currentState.mediaMessages || updatedFiles != currentState.fileMessages) {
            var nextState = currentState.copy(
                mediaMessages = updatedMedia,
                fileMessages = updatedFiles
            )

            if (currentState.fullScreenImages != null && !currentState.isViewingProfilePhotos) {
                val allPhotos = updatedMedia.filter { it.content is MessageContent.Photo }
                val paths = allPhotos.mapNotNull { (it.content as? MessageContent.Photo)?.path }

                nextState = nextState.copy(
                    fullScreenImages = paths
                )
            }

            _state.value = nextState
        }
    }

    private fun updateMessagePathIfNeeded(msg: MessageModel, targetFileId: Int, newPath: String): MessageModel {
        val content = msg.content
        val shouldUpdate = when (content) {
            is MessageContent.Photo -> content.fileId == targetFileId
            is MessageContent.Video -> content.fileId == targetFileId
            is MessageContent.Document -> content.fileId == targetFileId
            else -> false
        }

        return if (shouldUpdate) {
            val newContent = when (content) {
                is MessageContent.Photo -> content.copy(path = newPath)
                is MessageContent.Video -> content.copy(path = newPath)
                is MessageContent.Document -> content.copy(path = newPath)
                else -> content
            }
            msg.copy(content = newContent)
        } else {
            msg
        }
    }

    private fun loadMembersNextPage() {
        if (_state.value.isLoadingMembers || !_state.value.canLoadMoreMembers) return

        scope.launch {
            _state.value = _state.value.copy(isLoadingMembers = true)
            try {
                val limit = 20
                val newMembers = userRepository.getChatMembers(chatId, membersOffset, limit, ChatMembersFilter.Recent)

                if (newMembers.isEmpty()) {
                    _state.value = _state.value.copy(canLoadMoreMembers = false)
                } else {
                    membersOffset += newMembers.size
                    _state.value = _state.value.copy(
                        members = _state.value.members + newMembers,
                        canLoadMoreMembers = newMembers.size >= limit
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.value = _state.value.copy(isLoadingMembers = false)
            }
        }
    }

    private fun loadMediaNextPage(isFirstLoad: Boolean) {
        if (isCurrentlyLoadingMedia || (!isFirstLoad && !_state.value.canLoadMoreMedia)) return
        val state = _state.value
        val isGroup = state.chat?.let { it.isGroup || it.isChannel } ?: false
        val tabIndex = state.selectedTabIndex
        if (isGroup && tabIndex == 1) return

        val mediaTypeIndex = if (isGroup && tabIndex > 1) tabIndex - 1 else tabIndex
        val filter = when (mediaTypeIndex) {
            0 -> ProfileMediaFilter.MEDIA
            1 -> ProfileMediaFilter.FILES
            2 -> ProfileMediaFilter.AUDIO
            3 -> ProfileMediaFilter.VOICE
            4 -> ProfileMediaFilter.LINKS
            5 -> ProfileMediaFilter.GIFS
            else -> return
        }
        val lastId = if (isFirstLoad) 0L else getLastMessageIdForFilter(state, filter)

        scope.launch {
            isCurrentlyLoadingMedia = true
            _state.value = _state.value.copy(
                isLoadingMedia = isFirstLoad,
                isLoadingMoreMedia = !isFirstLoad
            )

            try {
                val messages = messageRepository.getProfileMedia(
                    chatId = chatId,
                    filter = filter,
                    fromMessageId = lastId,
                    limit = PAGE_SIZE
                )

                if (messages.isEmpty()) {
                    _state.value = _state.value.copy(canLoadMoreMedia = false)
                } else {
                    _state.value = appendMessagesToState(_state.value, filter, messages)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isCurrentlyLoadingMedia = false
                _state.value = _state.value.copy(
                    isLoadingMedia = false,
                    isLoadingMoreMedia = false
                )
            }
        }
    }

    private fun getLastMessageIdForFilter(state: ProfileComponent.State, filter: ProfileMediaFilter): Long {
        val list = when (filter) {
            ProfileMediaFilter.MEDIA -> state.mediaMessages
            ProfileMediaFilter.FILES -> state.fileMessages
            ProfileMediaFilter.AUDIO -> state.audioMessages
            ProfileMediaFilter.VOICE -> state.voiceMessages
            ProfileMediaFilter.LINKS -> state.linkMessages
            ProfileMediaFilter.GIFS -> state.gifMessages
        }
        return list.lastOrNull()?.id ?: 0L
    }

    private fun appendMessagesToState(
        currentState: ProfileComponent.State,
        filter: ProfileMediaFilter,
        newMessages: List<MessageModel>
    ): ProfileComponent.State {
        val canLoadMore = newMessages.size >= PAGE_SIZE

        val nextState = when (filter) {
            ProfileMediaFilter.MEDIA -> currentState.copy(
                mediaMessages = currentState.mediaMessages + newMessages,
                canLoadMoreMedia = canLoadMore
            )
            ProfileMediaFilter.FILES -> currentState.copy(
                fileMessages = currentState.fileMessages + newMessages,
                canLoadMoreMedia = canLoadMore
            )
            ProfileMediaFilter.AUDIO -> currentState.copy(
                audioMessages = currentState.audioMessages + newMessages,
                canLoadMoreMedia = canLoadMore
            )
            ProfileMediaFilter.VOICE -> currentState.copy(
                voiceMessages = currentState.voiceMessages + newMessages,
                canLoadMoreMedia = canLoadMore
            )
            ProfileMediaFilter.LINKS -> currentState.copy(
                linkMessages = currentState.linkMessages + newMessages,
                canLoadMoreMedia = canLoadMore
            )
            ProfileMediaFilter.GIFS -> currentState.copy(
                gifMessages = currentState.gifMessages + newMessages,
                canLoadMoreMedia = canLoadMore
            )
        }

        if (filter == ProfileMediaFilter.MEDIA && nextState.fullScreenImages != null && !nextState.isViewingProfilePhotos) {
            val allPhotos = nextState.mediaMessages.filter { it.content is MessageContent.Photo }
            val paths = allPhotos.mapNotNull { (it.content as? MessageContent.Photo)?.path }
            val captions = allPhotos.map { (it.content as? MessageContent.Photo)?.caption }
            return nextState.copy(
                fullScreenImages = paths,
                fullScreenCaptions = captions
            )
        }

        return nextState
    }

    override fun onTabSelected(index: Int) {
        if (_state.value.selectedTabIndex == index) return

        _state.value = _state.value.copy(
            selectedTabIndex = index,
            canLoadMoreMedia = true,
            isLoadingMedia = false
        )

        val isGroup = _state.value.chat?.let { it.isGroup || it.isChannel } ?: false
        if (isGroup && index == 1) {
            if (_state.value.members.isEmpty()) loadMembersNextPage()
        } else {
            val filterIndex = if (isGroup && index > 1) index - 1 else index
            val isEmpty = when (filterIndex) {
                0 -> _state.value.mediaMessages.isEmpty()
                1 -> _state.value.fileMessages.isEmpty()
                2 -> _state.value.audioMessages.isEmpty()
                3 -> _state.value.voiceMessages.isEmpty()
                4 -> _state.value.linkMessages.isEmpty()
                5 -> _state.value.gifMessages.isEmpty()
                else -> false
            }

            if (isEmpty) {
                loadMediaNextPage(isFirstLoad = true)
            }
        }
    }

    private fun isAudioFile(fileName: String): Boolean {
        return fileName.endsWith(".mp3", ignoreCase = true) ||
                fileName.endsWith(".m4a", ignoreCase = true) ||
                fileName.endsWith(".flac", ignoreCase = true) ||
                fileName.endsWith(".wav", ignoreCase = true) ||
                fileName.endsWith(".ogg", ignoreCase = true)
    }

    private fun observeUserUpdates() {
        scope.launch {
            userRepository.getUserFlow(chatId).collectLatest { user ->
                if (user != null) {
                    _state.value =
                        _state.value.copy(user = user, personalAvatarPath = user.personalAvatarPath)
                }
            }
        }
    }

    private fun observeProfilePhotos() {
        scope.launch {
            userRepository.getUserProfilePhotosFlow(chatId).collectLatest { photos ->
                if (photos.isNotEmpty()) {
                    _state.value = _state.value.copy(profilePhotos = photos)
                }
            }
        }
    }

    override fun onBack() {
        onBackClicked()
    }

    override fun onMessageClick(message: MessageModel) {
        when (val content = message.content) {
            is MessageContent.Photo -> {
                scope.launch {
                    val bigFileId = messageRepository.getHighResFileId(chatId, message.id)
                    if (bigFileId != null && bigFileId != 0) {
                        messageRepository.downloadFile(bigFileId, priority = 32)
                        var attempts = 0
                        while (attempts < 60) {
                            delay(500)
                            val fileInfo = messageRepository.getFileInfo(bigFileId)
                            if (fileInfo?.local?.isDownloadingCompleted == true && fileInfo.local.path.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    val currentImages = _state.value.fullScreenImages
                                    if (currentImages != null) {
                                        val allPhotos = _state.value.mediaMessages.filter { it.content is MessageContent.Photo }
                                        val index = allPhotos.indexOfFirst { it.id == message.id }

                                        if (index != -1 && index < currentImages.size) {
                                            val newImages = currentImages.toMutableList()
                                            newImages[index] = fileInfo.local.path
                                            _state.value = _state.value.copy(fullScreenImages = newImages)
                                        }
                                    }
                                }
                                break
                            }
                            attempts++
                        }
                    }
                }

                content.path?.let { clickedPath ->
                    val mediaList = _state.value.mediaMessages
                    val allPhotos = mediaList.filter { it.content is MessageContent.Photo }

                    val paths = allPhotos.mapNotNull { (it.content as? MessageContent.Photo)?.path }
                    val captions = allPhotos.map { (it.content as? MessageContent.Photo)?.caption }

                    val startIndex = paths.indexOf(clickedPath).takeIf { it != -1 } ?: 0

                    _state.value = _state.value.copy(
                        fullScreenImages = paths,
                        fullScreenCaptions = captions,
                        fullScreenStartIndex = startIndex,
                        isViewingProfilePhotos = false
                    )
                }
            }
            is MessageContent.Video -> {
                content.path?.let { path ->
                    _state.value = _state.value.copy(
                        fullScreenVideoPath = path,
                        fullScreenVideoCaption = content.caption
                    )
                }
            }
            is MessageContent.Gif -> {
                content.path?.let { path ->
                    _state.value = _state.value.copy(
                        fullScreenVideoPath = path,
                        fullScreenVideoCaption = content.caption
                    )
                }
            }
            is MessageContent.VideoNote -> {
                content.path?.let { path ->
                    _state.value = _state.value.copy(
                        fullScreenVideoPath = path,
                        fullScreenVideoCaption = null
                    )
                }
            }
            is MessageContent.Location -> {
                onLocationClick(content.latitude, content.longitude, "Location")
            }

            is MessageContent.Venue -> {
                onLocationClick(content.latitude, content.longitude, content.title)
            }
            else -> onMessageClicked(message)
        }
    }

    override fun onMessageLongClick(message: MessageModel) {
        onMessageLongClicked(message)
    }

    override fun onAvatarClick() {
        val photos = _state.value.profilePhotos
        if (photos.isNotEmpty()) {
            val firstPhoto = photos.first()
            if (firstPhoto.endsWith(".mp4", ignoreCase = true)) {
                _state.value = _state.value.copy(
                    fullScreenVideoPath = firstPhoto,
                    fullScreenVideoCaption = null
                )
            } else {
                _state.value = _state.value.copy(
                    fullScreenImages = photos.filter { !it.endsWith(".mp4", ignoreCase = true) },
                    fullScreenCaptions = photos.map { null },
                    fullScreenStartIndex = 0,
                    isViewingProfilePhotos = true
                )
            }
        } else {
            val avatarPath =
                _state.value.personalAvatarPath ?: _state.value.chat?.avatarPath
                ?: _state.value.user?.avatarPath
            avatarPath?.let { path ->
                _state.value = _state.value.copy(
                    fullScreenImages = listOf(path),
                    fullScreenCaptions = listOf(null),
                    fullScreenStartIndex = 0,
                    isViewingProfilePhotos = true
                )
            }
        }
    }

    override fun onDismissViewer() {
        _state.value = _state.value.copy(
            fullScreenImages = null,
            fullScreenCaptions = emptyList(),
            fullScreenVideoPath = null,
            fullScreenVideoCaption = null,
            isViewingProfilePhotos = false
        )
    }

    override fun onOpenMiniApp(url: String, name: String, chatId: Long) {
        _state.value = _state.value.copy(miniAppUrl = url, miniAppName = name, chatId = chatId)
    }

    override fun onDismissMiniApp() {
        _state.value = _state.value.copy(miniAppUrl = null, miniAppName = null)
    }

    override fun onToggleMute() {
        val chat = _state.value.chat ?: return
        val shouldMute = !chat.isMuted

        scope.launch {
            chatsListRepository.toggleMuteChats(setOf(chatId), shouldMute)
            updateChat(chatId)
        }
    }

    override fun onEdit() {
        onEditClicked()
    }

    override fun onShowQRCode() {
        _state.value = _state.value.copy(isQrVisible = true)
    }

    override fun onDismissQRCode() {
        _state.value = _state.value.copy(isQrVisible = false)
    }

    override fun onSendMessage() {
        onSendMessageClicked(chatId)
    }

    override fun onLeave() {
        scope.launch {
            chatsListRepository.leaveChat(chatId)
            updateChat(chatId)
        }
    }

    override fun onJoinChat() {
        scope.launch {
            messageRepository.joinChat(chatId)
            updateChat(chatId)
        }
    }

    override fun onReport(reason: String) {
        scope.launch(Dispatchers.IO) {
            chatsListRepository.reportChat(chatId, reason)
            withContext(Dispatchers.Main) {
                _state.value = _state.value.copy(isReportVisible = false)
            }
        }
    }

    override fun onDismissReport() {
        _state.value = _state.value.copy(isReportVisible = false)
    }

    override fun onShowReport() {
        _state.value = _state.value.copy(isReportVisible = true)
    }

    override fun onShowLogs() {
        onShowLogsClicked(chatId)
    }

    override fun onMemberClick(userId: Long) {
        scope.launch {
            val chat = chatsListRepository.getChatById(userId)
            if (chat != null && (chat.isGroup || chat.isChannel)) {
                onMemberClicked(userId)
            } else {
                onSendMessageClicked(userId)
            }
        }
    }

    override fun onMemberLongClick(userId: Long) {
        scope.launch {
            val member = userRepository.getChatMember(chatId, userId)
            if (member?.status is ChatMemberStatus.Administrator || member?.status is ChatMemberStatus.Creator) {
                onMemberLongClicked(chatId, userId)
            }
        }
    }

    override fun onUpdateChatTitle(title: String) {
        scope.launch {
            chatsListRepository.setChatTitle(chatId, title)
            updateChat(chatId)
        }
    }

    override fun onUpdateChatDescription(description: String) {
        scope.launch {
            chatsListRepository.setChatDescription(chatId, description)
            loadData()
        }
    }

    override fun onUpdateChatUsername(username: String) {
        scope.launch {
            chatsListRepository.setChatUsername(chatId, username)
            loadData()
        }
    }

    override fun onUpdateChatPermissions(permissions: ChatPermissionsModel) {
        scope.launch {
            chatsListRepository.setChatPermissions(chatId, permissions)
            updateChat(chatId)
        }
    }

    override fun onUpdateChatSlowModeDelay(delay: Int) {
        scope.launch {
            chatsListRepository.setChatSlowModeDelay(chatId, delay)
            loadData()
        }
    }

    override fun onUpdateMemberStatus(userId: Long, status: ChatMemberStatus) {
        scope.launch {
            userRepository.setChatMemberStatus(chatId, userId, status)
            membersOffset = 0
            _state.value = _state.value.copy(members = emptyList(), canLoadMoreMembers = true)
            loadMembersNextPage()
        }
    }

    override fun onShowStatistics() {
        scope.launch {
            val stats = userRepository.getChatStatistics(chatId, false)
            _state.value = _state.value.copy(statistics = stats, isStatisticsVisible = true)
        }
    }

    override fun onShowRevenueStatistics() {
        scope.launch {
            val stats = userRepository.getChatRevenueStatistics(chatId, false)
            _state.value =
                _state.value.copy(revenueStatistics = stats, isRevenueStatisticsVisible = true)
        }
    }

    override fun onDismissStatistics() {
        _state.value = _state.value.copy(
            isStatisticsVisible = false,
            isRevenueStatisticsVisible = false,
            statistics = null,
            revenueStatistics = null
        )
    }

    override fun onLoadStatisticsGraph(token: String) {
        scope.launch {
            val stats = _state.value.statistics ?: return@launch
            val x = stats.period.endDate.toLong()
            val graph = userRepository.loadStatisticsGraph(chatId, token, x)

            if (graph != null) {
                val updatedStats = updateStatisticsWithGraph(stats, token, graph)
                _state.value = _state.value.copy(statistics = updatedStats)
            }
        }
    }

    override fun onLinkedChatClick() {
        _state.value.linkedChat?.id?.let { onSendMessageClicked(it) }
    }

    override fun onShowPermissions() {
        val botId = _state.value.user?.id ?: return
        val permissions = mapOf(
            "Location" to botPreferences.getWebappPermission(botId, "location"),
            "Biometry" to botPreferences.getWebappPermission(botId, "biometry"),
            "Terms of Service" to botPreferences.getWebappPermission(botId, "tos_accepted")
        )
        _state.value = _state.value.copy(isPermissionsVisible = true, botPermissions = permissions)
    }

    override fun onDismissPermissions() {
        _state.value = _state.value.copy(isPermissionsVisible = false)
    }

    override fun onTogglePermission(permission: String) {
        val botId = _state.value.user?.id ?: return
        val key = when (permission) {
            "Location" -> "location"
            "Biometry" -> "biometry"
            "Terms of Service" -> "tos_accepted"
            else -> return
        }
        val current = botPreferences.getWebappPermission(botId, key)
        botPreferences.setWebappPermission(botId, key, !current)

        if (key == "tos_accepted") {
            _state.value = _state.value.copy(isTOSAccepted = !current)
        }

        onShowPermissions()
    }

    override fun onAcceptTOS() {
        val botId = _state.value.user?.id ?: return
        scope.launch {
            _state.value = _state.value.copy(isAcceptingTOS = true)
            delay(1000) // Animation delay
            botPreferences.setWebappPermission(botId, "tos_accepted", true)
            _state.value = _state.value.copy(
                isTOSVisible = false,
                isTOSAccepted = true,
                isAcceptingTOS = false
            )
        }
    }

    override fun onDismissTOS() {
        _state.value = _state.value.copy(
            isTOSVisible = false,
            pendingMiniAppUrl = null,
            pendingMiniAppName = null
        )
    }

    override fun onLocationClick(lat: Double, lon: Double, address: String) {
        scope.launch {
            var finalAddress = address
            if (address == "Location") {
                val reverse = locationRepository.reverseGeocode(lat, lon)
                if (reverse != null) {
                    finalAddress = reverse.address?.city ?: reverse.address?.toString() ?: "Location"
                }
            }
            _state.value = _state.value.copy(
                selectedLocation = ProfileComponent.LocationData(lat, lon, finalAddress)
            )
        }
    }

    override fun onDismissLocation() {
        _state.value = _state.value.copy(selectedLocation = null)
    }

    private fun updateStatisticsWithGraph(
        stats: ChatStatisticsModel,
        token: String,
        newGraph: StatisticsGraphModel
    ): ChatStatisticsModel {
        fun StatisticsGraphModel?.matchesToken(token: String): Boolean {
            return this is StatisticsGraphModel.Async && this.token == token
        }

        return stats.copy(
            memberCountGraph = if (stats.memberCountGraph.matchesToken(token)) newGraph else stats.memberCountGraph,
            joinGraph = if (stats.joinGraph.matchesToken(token)) newGraph else stats.joinGraph,
            muteGraph = if (stats.muteGraph.matchesToken(token)) newGraph else stats.muteGraph,
            viewCountByHourGraph = if (stats.viewCountByHourGraph.matchesToken(token)) newGraph else stats.viewCountByHourGraph,
            viewCountBySourceGraph = if (stats.viewCountBySourceGraph.matchesToken(token)) newGraph else stats.viewCountBySourceGraph,
            joinBySourceGraph = if (stats.joinBySourceGraph.matchesToken(token)) newGraph else stats.joinBySourceGraph,
            languageGraph = if (stats.languageGraph.matchesToken(token)) newGraph else stats.languageGraph,
            messageContentGraph = if (stats.messageContentGraph.matchesToken(token)) newGraph else stats.messageContentGraph,
            actionGraph = if (stats.actionGraph.matchesToken(token)) newGraph else stats.actionGraph,
            dayGraph = if (stats.dayGraph.matchesToken(token)) newGraph else stats.dayGraph,
            weekGraph = if (stats.weekGraph.matchesToken(token)) newGraph else stats.weekGraph,
            topHoursGraph = if (stats.topHoursGraph.matchesToken(token)) newGraph else stats.topHoursGraph
        )
    }

    private fun updateChat(chatId: Long) {
        scope.launch {
            val updatedChat = chatsListRepository.getChatById(chatId)
            withContext(Dispatchers.Main) {
                _state.value = _state.value.copy(chat = updatedChat)
            }
        }
    }
}
