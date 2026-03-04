package org.monogram.data.mapper

import org.drinkless.tdlib.TdApi
import org.monogram.data.di.TdLibClient
import org.monogram.domain.models.ChatModel

fun TdApi.Chat.toDomain(tdLibClient: TdLibClient): ChatModel {
    val smallPhoto = this.photo?.small
    var finalPath: String? = null
    if (smallPhoto != null) {
        finalPath = smallPhoto.local.path.ifEmpty { null }
        if (finalPath == null) {
            tdLibClient.send(TdApi.DownloadFile(smallPhoto.id, 1, 0, 0, true)) {}
        }
    }

    val isChannel = this.type is TdApi.ChatTypeSupergroup && (this.type as TdApi.ChatTypeSupergroup).isChannel

    return ChatModel(
        id = this.id,
        title = this.title,
        unreadCount = this.unreadCount,
        avatarPath = finalPath,
        isMuted = this.notificationSettings.muteFor > 0,
        isChannel = isChannel,
        isGroup = this.type is TdApi.ChatTypeBasicGroup || (this.type is TdApi.ChatTypeSupergroup && !isChannel),
        isMember = true,
        lastMessageText = ""
    )
}
