package org.monogram.data.mapper

import org.drinkless.tdlib.TdApi
import org.monogram.domain.models.ChatStorageUsageModel
import org.monogram.domain.models.FileTypeStorageUsageModel
import org.monogram.domain.models.StorageUsageModel

fun TdApi.StorageStatistics.toDomain(chatStats: List<ChatStorageUsageModel>): StorageUsageModel {
    return StorageUsageModel(
        totalSize = this.size,
        fileCount = this.count,
        chatStats = chatStats
    )
}

fun TdApi.StorageStatisticsByChat.toDomain(chatTitle: String): ChatStorageUsageModel {
    return ChatStorageUsageModel(
        chatId = this.chatId,
        chatTitle = chatTitle,
        size = this.size,
        fileCount = this.count,
        byFileType = this.byFileType.map { it.toDomain() }
    )
}

fun TdApi.StorageStatisticsByFileType.toDomain(): FileTypeStorageUsageModel {
    return FileTypeStorageUsageModel(
        fileType = this.fileType.toDomain(),
        size = this.size,
        fileCount = this.count
    )
}

fun TdApi.FileType.toDomain(): String {
    return when (this) {
        is TdApi.FileTypePhoto -> "Photos"
        is TdApi.FileTypeVideo -> "Videos"
        is TdApi.FileTypeDocument -> "Documents"
        is TdApi.FileTypeSticker -> "Stickers"
        is TdApi.FileTypeAudio -> "Music"
        is TdApi.FileTypeVoiceNote -> "Voice Messages"
        is TdApi.FileTypeVideoNote -> "Video Messages"
        else -> "Other Files"
    }
}
