package org.monogram.data.gateway

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import org.drinkless.tdlib.TdApi

class UpdateDispatcherImpl(
    gateway: TelegramGateway
) : UpdateDispatcher {
    private val updates = gateway.updates

    private inline fun <reified T : TdApi.Update> flow(): Flow<T> =
        updates.filterIsInstance<T>()

    override val authorizationState = flow<TdApi.UpdateAuthorizationState>()

    override val newMessage = flow<TdApi.UpdateNewMessage>()
    override val messageEdited = flow<TdApi.UpdateMessageEdited>()
    override val messageContent = flow<TdApi.UpdateMessageContent>()
    override val messageSendSucceeded = flow<TdApi.UpdateMessageSendSucceeded>()
    override val messageSendFailed = flow<TdApi.UpdateMessageSendFailed>()
    override val messageDeleted = flow<TdApi.UpdateDeleteMessages>()
    override val messagePinned = flow<TdApi.UpdateChatLastMessage>()
    override val messageInteractionInfo = flow<TdApi.UpdateMessageInteractionInfo>()

    override val chatLastMessage = flow<TdApi.UpdateChatLastMessage>()
    override val chatPosition = flow<TdApi.UpdateChatPosition>()
    override val chatReadInbox = flow<TdApi.UpdateChatReadInbox>()
    override val chatReadOutbox = flow<TdApi.UpdateChatReadOutbox>()
    override val chatUnreadMentionCount = flow<TdApi.UpdateChatUnreadMentionCount>()
    override val chatNotificationSettings = flow<TdApi.UpdateChatNotificationSettings>()
    override val chatTitle = flow<TdApi.UpdateChatTitle>()
    override val chatPhoto = flow<TdApi.UpdateChatPhoto>()
    override val chatPermissions = flow<TdApi.UpdateChatPermissions>()
    override val chatDraftMessage = flow<TdApi.UpdateChatDraftMessage>()
    override val chatAction = flow<TdApi.UpdateChatAction>()
    override val chatOnlineMemberCount = flow<TdApi.UpdateChatOnlineMemberCount>()
    override val chatFolders = flow<TdApi.UpdateChatFolders>()

    override val userStatus = flow<TdApi.UpdateUserStatus>()
    override val user = flow<TdApi.UpdateUser>()
    override val userPrivacySettingRules = flow<TdApi.UpdateUserPrivacySettingRules>()

    override val file = flow<TdApi.UpdateFile>()

    override val connectionState = flow<TdApi.UpdateConnectionState>()
    override val installedStickerSets = flow<TdApi.UpdateInstalledStickerSets>()
    override val newChat = flow<TdApi.UpdateNewChat>()
    override val attachmentMenuBots = flow<TdApi.UpdateAttachmentMenuBots>()
    override val chatsListUpdates = flow<TdApi.Update>()
}