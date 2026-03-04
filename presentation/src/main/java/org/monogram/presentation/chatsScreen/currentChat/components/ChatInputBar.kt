package org.monogram.presentation.chatsScreen.currentChat.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.monogram.domain.models.*
import org.monogram.domain.repository.InlineBotResultsModel
import org.monogram.domain.repository.StickerRepository
import org.monogram.presentation.chatsScreen.currentChat.components.chats.BotCommandSuggestions
import org.monogram.presentation.chatsScreen.currentChat.components.chats.getEmojiFontFamily
import org.monogram.presentation.chatsScreen.currentChat.components.inputbar.*
import org.monogram.presentation.stickers.ui.menu.StickerEmojiMenu
import org.monogram.presentation.util.AppPreferences

private const val CUSTOM_EMOJI_TAG = "custom_emoji"
private const val MENTION_TAG = "mention"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatInputBar(
    onSend: (String, List<MessageEntity>) -> Unit,
    onStickerClick: (String) -> Unit = {},
    onGifClick: (GifModel) -> Unit = {},
    onAttachClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onSendVoice: (String, Int, ByteArray) -> Unit = { _, _, _ -> },
    replyMessage: MessageModel? = null,
    onCancelReply: () -> Unit = {},
    editingMessage: MessageModel? = null,
    onCancelEdit: () -> Unit = {},
    onSaveEdit: (String, List<MessageEntity>) -> Unit = { _, _ -> },
    draftText: String = "",
    onDraftChange: (String) -> Unit = {},
    onTyping: () -> Unit = {},
    pendingMediaPaths: List<String> = emptyList(),
    onCancelMedia: () -> Unit = {},
    onSendMedia: (List<String>, String) -> Unit = { _, _ -> },
    onMediaOrderChange: (List<String>) -> Unit = {},
    onMediaClick: (String) -> Unit = {},
    isClosed: Boolean = false,
    permissions: ChatPermissionsModel = ChatPermissionsModel(),
    isAdmin: Boolean = false,
    isChannel: Boolean = false,
    isBot: Boolean = false,
    botCommands: List<BotCommandModel> = emptyList(),
    botMenuButton: BotMenuButtonModel = BotMenuButtonModel.Default,
    onShowBotCommands: () -> Unit = {},
    replyMarkup: ReplyMarkupModel? = null,
    onReplyMarkupButtonClick: (KeyboardButtonModel) -> Unit = {},
    onOpenMiniApp: (String, String) -> Unit = { _, _ -> },
    mentionSuggestions: List<UserModel> = emptyList(),
    onMentionQueryChange: (String?) -> Unit = {},
    inlineBotResults: InlineBotResultsModel? = null,
    isInlineBotLoading: Boolean = false,
    onInlineQueryChange: (String, String) -> Unit = { _, _ -> },
    onLoadMoreInlineResults: (String) -> Unit = {},
    onSendInlineResult: (String) -> Unit = {},
    appPreferences: AppPreferences,
    videoPlayerPool: VideoPlayerPool,
    stickerRepository: StickerRepository
) {
    if (isClosed) {
        ClosedTopicBar()
        return
    }

    val emojiStyle by appPreferences.emojiStyle.collectAsState()
    val emojiFontFamily = getEmojiFontFamily(emojiStyle)

    val canWriteText = if (isChannel) true else (isAdmin || permissions.canSendBasicMessages)
    val canSendMedia = if (isChannel) true else (isAdmin || (permissions.canSendPhotos || permissions.canSendVideos || permissions.canSendDocuments))
    val canSendStickers = if (isChannel) true else (isAdmin || permissions.canSendOtherMessages)
    val canSendVoice = if (isChannel) true else (isAdmin || permissions.canSendVoiceNotes)

    var textValue by remember { mutableStateOf(TextFieldValue(draftText)) }
    var isStickerMenuVisible by remember { mutableStateOf(false) }
    var isVideoMessageMode by remember { mutableStateOf(false) }
    var isGifSearchFocused by remember { mutableStateOf(false) }

    val knownCustomEmojis = remember { mutableStateMapOf<Long, StickerModel>() }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0

    var lastEditingMessageId by remember { mutableStateOf<Long?>(null) }

    val voiceRecorder = rememberVoiceRecorder(onRecordingFinished = onSendVoice)
    voiceRecorder.UpdateLoop()

    val filteredCommands = remember(textValue.text, botCommands) {
        if (textValue.text.startsWith("/")) {
            val query = textValue.text.substring(1).lowercase()
            botCommands.filter { it.command.lowercase().startsWith(query) }
        } else {
            emptyList()
        }
    }

    LaunchedEffect(textValue.text, textValue.selection) {
        val text = textValue.text
        val selection = textValue.selection
        if (selection.collapsed && selection.start > 0) {
            val lastAt = text.lastIndexOf('@', selection.start - 1)
            if (lastAt != -1) {
                val isStartOfWord = lastAt == 0 || text[lastAt - 1].isWhitespace()
                if (isStartOfWord) {
                    val query = text.substring(lastAt + 1, selection.start)
                    if (!query.contains(' ')) {
                        onMentionQueryChange(query)
                    } else {
                        onMentionQueryChange(null)
                    }
                } else {
                    onMentionQueryChange(null)
                }
            } else {
                onMentionQueryChange(null)
            }
        } else {
            onMentionQueryChange(null)
        }
    }

    LaunchedEffect(textValue.text) {
        val text = textValue.text
        if (text.startsWith("@") && text.contains(" ")) {
            val parts = text.split(" ", limit = 2)
            val botUsername = parts[0].substring(1)
            val query = parts[1]
            if (botUsername.isNotEmpty()) {
                onInlineQueryChange(botUsername, query)
            }
        }
    }

    LaunchedEffect(draftText) {
        if (textValue.text.isEmpty() && draftText.isNotEmpty()) {
            textValue = TextFieldValue(draftText, TextRange(draftText.length))
        }
    }

    LaunchedEffect(textValue.text) {
        if (editingMessage == null && textValue.text != draftText) {
            onDraftChange(textValue.text)
        }
        if (textValue.text.isNotEmpty()) {
            onTyping()
        }
    }

    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            if (editingMessage.id != lastEditingMessageId) {
                val content = editingMessage.content
                if (content is MessageContent.Text) {
                    knownCustomEmojis.clear()
                    content.entities.forEach { entity ->
                        when (val type = entity.type) {
                            is MessageEntityType.CustomEmoji -> {
                                if (type.path != null) {
                                    knownCustomEmojis[type.emojiId] = StickerModel(
                                        id = type.emojiId,
                                        width = 0,
                                        height = 0,
                                        emoji = "",
                                        path = type.path,
                                        format = StickerFormat.UNKNOWN
                                    )
                                }
                            }

                            else -> {}
                        }
                    }

                    val annotatedText = buildAnnotatedString {
                        append(content.text)
                        content.entities.forEach { entity ->
                            when (val type = entity.type) {
                                is MessageEntityType.CustomEmoji -> {
                                    addStringAnnotation(
                                        CUSTOM_EMOJI_TAG,
                                        type.emojiId.toString(),
                                        entity.offset,
                                        entity.offset + entity.length
                                    )
                                }

                                is MessageEntityType.TextMention -> {
                                    addStringAnnotation(
                                        MENTION_TAG,
                                        type.userId.toString(),
                                        entity.offset,
                                        entity.offset + entity.length
                                    )
                                }

                                else -> {}
                            }
                        }
                    }

                    textValue = TextFieldValue(annotatedText, TextRange(content.text.length))
                    focusRequester.requestFocus()
                }
                lastEditingMessageId = editingMessage.id
            }
        } else {
            if (lastEditingMessageId != null) {
                textValue = TextFieldValue(draftText, TextRange(draftText.length))
                lastEditingMessageId = null
                knownCustomEmojis.clear()
            }
        }
    }

    BackHandler(enabled = isStickerMenuVisible || pendingMediaPaths.isNotEmpty()) {
        if (isGifSearchFocused) {
            focusManager.clearFocus()
        } else if (isStickerMenuVisible) {
            isStickerMenuVisible = false
        } else if (pendingMediaPaths.isNotEmpty()) {
            onCancelMedia()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            InputPreviewSection(
                editingMessage = editingMessage,
                replyMessage = replyMessage,
                pendingMediaPaths = pendingMediaPaths,
                onCancelEdit = onCancelEdit,
                onCancelReply = onCancelReply,
                onCancelMedia = onCancelMedia,
                onMediaOrderChange = onMediaOrderChange,
                onMediaClick = onMediaClick
            )

            AnimatedVisibility(
                visible = mentionSuggestions.isNotEmpty(),
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                MentionSuggestions(
                    suggestions = mentionSuggestions,
                    onMentionClick = { user ->
                        val text = textValue.text
                        val selection = textValue.selection
                        val lastAt = text.lastIndexOf('@', selection.start - 1)
                        if (lastAt != -1) {
                            val mentionText = user.username ?: user.firstName
                            val newText = text.replaceRange(lastAt + 1, selection.start, "$mentionText ")

                            val annotatedBuilder = AnnotatedString.Builder()
                            annotatedBuilder.append(newText)

                            textValue.annotatedString.getStringAnnotations(0, text.length).forEach { annotation ->
                                if (annotation.start < lastAt) {
                                    annotatedBuilder.addStringAnnotation(
                                        annotation.tag,
                                        annotation.item,
                                        annotation.start,
                                        annotation.end
                                    )
                                } else if (annotation.start >= selection.start) {
                                    val offset = (mentionText.length + 1) - (selection.start - (lastAt + 1))
                                    annotatedBuilder.addStringAnnotation(
                                        annotation.tag,
                                        annotation.item,
                                        annotation.start + offset,
                                        annotation.end + offset
                                    )
                                }
                            }

                            if (user.username == null) {
                                annotatedBuilder.addStringAnnotation(
                                    MENTION_TAG,
                                    user.id.toString(),
                                    lastAt,
                                    lastAt + mentionText.length + 1
                                )
                            }

                            textValue = TextFieldValue(
                                annotatedString = annotatedBuilder.toAnnotatedString(),
                                selection = TextRange(lastAt + mentionText.length + 2)
                            )
                        }
                        onMentionQueryChange(null)
                    },
                    videoPlayerPool = videoPlayerPool
                )
            }

            AnimatedVisibility(
                visible = filteredCommands.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                BotCommandSuggestions(
                    commands = filteredCommands,
                    onCommandClick = { command ->
                        onSend("/$command", emptyList())
                        textValue = TextFieldValue("")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = (inlineBotResults != null && (inlineBotResults.results.isNotEmpty() || inlineBotResults.switchPmText != null)) || isInlineBotLoading,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                InlineBotResults(
                    inlineBotResults = inlineBotResults,
                    isLoading = isInlineBotLoading,
                    onResultClick = { resultId ->
                        onSendInlineResult(resultId)
                        textValue = TextFieldValue("")
                    },
                    onSwitchPmClick = { text ->
                        onOpenMiniApp(
                            text,
                            "switch_pm"
                        )
                    },
                    onLoadMore = { offset ->
                        onLoadMoreInlineResults(offset)
                    }
                )
            }

            AnimatedVisibility(
                visible = !isGifSearchFocused,
                enter = expandVertically(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AnimatedVisibility(
                        visible = !voiceRecorder.isRecording,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        InputBarLeadingIcons(
                            editingMessage = editingMessage,
                            pendingMediaPaths = pendingMediaPaths,
                            canSendMedia = canSendMedia,
                            onAttachClick = onAttachClick
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = if (voiceRecorder.isRecording) 0.dp else 4.dp)
                    ) {
                        AnimatedContent(
                            targetState = voiceRecorder.isRecording,
                            transitionSpec = {
                                (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                            },
                            label = "InputContent"
                        ) { isRecording ->
                            if (isRecording) {
                                RecordingUI(
                                    voiceRecorderState = voiceRecorder,
                                    onStop = { voiceRecorder.stopRecording(cancel = false) },
                                    onCancel = { voiceRecorder.stopRecording(cancel = true) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                InputTextFieldContainer(
                                    textValue = textValue,
                                    onValueChange = { textValue = it },
                                    isBot = isBot,
                                    botMenuButton = botMenuButton,
                                    botCommands = botCommands,
                                    canSendStickers = canSendStickers,
                                    canWriteText = canWriteText,
                                    isStickerMenuVisible = isStickerMenuVisible,
                                    onStickerMenuToggle = {
                                        isStickerMenuVisible = !isStickerMenuVisible
                                        if (isStickerMenuVisible) focusManager.clearFocus()
                                    },
                                    onShowBotCommands = onShowBotCommands,
                                    onOpenMiniApp = onOpenMiniApp,
                                    knownCustomEmojis = knownCustomEmojis,
                                    emojiFontFamily = emojiFontFamily,
                                    focusRequester = focusRequester,
                                    pendingMediaPaths = pendingMediaPaths,
                                    onFocus = { isStickerMenuVisible = false },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (!voiceRecorder.isLocked) {
                        Spacer(modifier = Modifier.width(8.dp))

                        InputBarSendButton(
                            textValue = textValue,
                            editingMessage = editingMessage,
                            pendingMediaPaths = pendingMediaPaths,
                            canWriteText = canWriteText,
                            canSendVoice = canSendVoice,
                            canSendMedia = canSendMedia,
                            isVideoMessageMode = isVideoMessageMode,
                            knownCustomEmojis = knownCustomEmojis,
                            onSend = onSend,
                            onSaveEdit = onSaveEdit,
                            onSendMedia = onSendMedia,
                            onCameraClick = onCameraClick,
                            onVideoModeToggle = { isVideoMessageMode = !isVideoMessageMode },
                            onTextValueChange = { textValue = it },
                            onKnownEmojisClear = { knownCustomEmojis.clear() },
                            onVoiceStart = { voiceRecorder.startRecording() },
                            onVoiceStop = { cancel -> voiceRecorder.stopRecording(cancel) },
                            onVoiceLock = { voiceRecorder.lockRecording() }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = replyMarkup is ReplyMarkupModel.ShowKeyboard && textValue.text.isEmpty() && !isStickerMenuVisible && !isKeyboardVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                KeyboardMarkupView(
                    markup = replyMarkup as ReplyMarkupModel.ShowKeyboard,
                    onButtonClick = onReplyMarkupButtonClick,
                    onOpenMiniApp = onOpenMiniApp
                )
            }

            AnimatedVisibility(
                visible = isStickerMenuVisible,
                enter = expandVertically(
                    animationSpec = tween(200),
                    expandFrom = Alignment.Top
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = tween(200),
                    shrinkTowards = Alignment.Top
                ) + fadeOut()
            ) {
                StickerEmojiMenu(
                    onStickerSelected = { sticker ->
                        onStickerClick(sticker)
                    },
                    onEmojiSelected = { emoji, sticker ->
                        val currentText = textValue.annotatedString
                        val selection = textValue.selection

                        val emojiAnnotated = if (sticker != null) {
                            knownCustomEmojis[sticker.id] = sticker
                            buildAnnotatedString {
                                append(emoji)
                                addStringAnnotation(CUSTOM_EMOJI_TAG, sticker.id.toString(), 0, emoji.length)
                            }
                        } else {
                            AnnotatedString(emoji)
                        }

                        val newText = buildAnnotatedString {
                            append(currentText.subSequence(0, selection.start))
                            append(emojiAnnotated)
                            append(currentText.subSequence(selection.end, currentText.length))
                        }

                        textValue = textValue.copy(
                            annotatedString = newText,
                            selection = TextRange(selection.start + emojiAnnotated.length)
                        )
                    },
                    onGifSelected = { gif ->
                        onGifClick(gif)
                    },
                    onSearchFocused = { focused ->
                        isGifSearchFocused = focused
                    },
                    videoPlayerPool = videoPlayerPool,
                    stickerRepository = stickerRepository
                )
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun ClosedTopicBar() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "This topic is closed",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InputBarLeadingIcons(
    editingMessage: MessageModel?,
    pendingMediaPaths: List<String>,
    canSendMedia: Boolean,
    onAttachClick: () -> Unit
) {
    if (editingMessage == null && pendingMediaPaths.isEmpty() && canSendMedia) {
        IconButton(
            onClick = onAttachClick
        ) {
            Icon(
                imageVector = Icons.Outlined.AddCircleOutline,
                contentDescription = "Attach",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else if (!canSendMedia) {
        Spacer(modifier = Modifier.width(12.dp))
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
