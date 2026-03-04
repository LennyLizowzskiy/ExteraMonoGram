package org.monogram.domain.models

data class ChatEventModel(
    val id: Long,
    val date: Int,
    val memberId: MessageSenderModel,
    val action: ChatEventActionModel
)

sealed interface ChatEventActionModel {
    data class MessageEdited(val oldMessage: MessageModel, val newMessage: MessageModel) : ChatEventActionModel
    data class MessageDeleted(val message: MessageModel) : ChatEventActionModel
    data class MessagePinned(val message: MessageModel) : ChatEventActionModel
    data class MessageUnpinned(val message: MessageModel) : ChatEventActionModel
    data class MemberJoined(val userId: Long) : ChatEventActionModel
    data class MemberLeft(val userId: Long) : ChatEventActionModel
    data class MemberInvited(val userId: Long, val status: String) : ChatEventActionModel
    data class MemberPromoted(val userId: Long, val oldStatus: String, val newStatus: String) : ChatEventActionModel
    data class MemberRestricted(
        val userId: Long,
        val oldStatus: String,
        val newStatus: String,
        val untilDate: Int = 0,
        val oldPermissions: ChatPermissionsModel? = null,
        val newPermissions: ChatPermissionsModel? = null
    ) : ChatEventActionModel

    data class TitleChanged(val oldTitle: String, val newTitle: String) : ChatEventActionModel
    data class DescriptionChanged(val oldDescription: String, val newDescription: String) : ChatEventActionModel
    data class UsernameChanged(val oldUsername: String, val newUsername: String) : ChatEventActionModel
    data class PhotoChanged(val oldPhotoPath: String?, val newPhotoPath: String?) : ChatEventActionModel
    data class InviteLinkEdited(val oldLink: String, val newLink: String) : ChatEventActionModel
    data class InviteLinkRevoked(val link: String) : ChatEventActionModel
    data class InviteLinkDeleted(val link: String) : ChatEventActionModel
    data class VideoChatCreated(val groupCallId: Int) : ChatEventActionModel
    data class VideoChatEnded(val groupCallId: Int) : ChatEventActionModel
    data class Unknown(val type: String) : ChatEventActionModel
}

data class ChatEventLogFiltersModel(
    val messageEdits: Boolean = true,
    val messageDeletions: Boolean = true,
    val messagePins: Boolean = true,
    val memberJoins: Boolean = true,
    val memberLeaves: Boolean = true,
    val memberInvites: Boolean = true,
    val memberPromotions: Boolean = true,
    val memberRestrictions: Boolean = true,
    val infoChanges: Boolean = true,
    val settingChanges: Boolean = true,
    val inviteLinkChanges: Boolean = true,
    val videoChatChanges: Boolean = true,
    val forumChanges: Boolean = true,
    val subscriptionExtensions: Boolean = true,
    val userIds: List<Long> = emptyList()
)

sealed interface MessageSenderModel {
    data class User(val userId: Long) : MessageSenderModel
    data class Chat(val chatId: Long) : MessageSenderModel
}
