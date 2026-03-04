package org.monogram.data.mapper

import org.drinkless.tdlib.TdApi
import org.monogram.data.datasource.remote.TdMessageRemoteDataSource
import org.monogram.data.infra.FileDownloadQueue
import org.monogram.domain.models.FileModel
import org.monogram.domain.models.FileLocalModel
import org.monogram.domain.models.FileRemoteModel

fun TdApi.File.toDomain(): FileModel {
    return FileModel(
        id = this.id,
        size = this.size,
        expectedSize = this.expectedSize,
        local = this.local.toDomain(),
        remote = this.remote.toDomain()
    )
}

fun TdApi.LocalFile.toDomain(): FileLocalModel {
    return FileLocalModel(
        path = this.path,
        canBeDownloaded = this.canBeDownloaded,
        canBeDeleted = this.canBeDeleted,
        isDownloadingActive = this.isDownloadingActive,
        isDownloadingCompleted = this.isDownloadingCompleted,
        downloadOffset = this.downloadOffset,
        downloadedPrefixSize = this.downloadedPrefixSize,
        downloadedSize = this.downloadedSize
    )
}

fun TdApi.RemoteFile.toDomain(): FileRemoteModel {
    return FileRemoteModel(
        id = this.id,
        uniqueId = this.uniqueId,
        isUploadingActive = this.isUploadingActive,
        isUploadingCompleted = this.isUploadingCompleted,
        uploadedSize = this.uploadedSize
    )
}

fun TdMessageRemoteDataSource.DownloadType.toDomain() : FileDownloadQueue.DownloadType {
    return when (this) {
        TdMessageRemoteDataSource.DownloadType.DEFAULT -> FileDownloadQueue.DownloadType.DEFAULT
        TdMessageRemoteDataSource.DownloadType.VIDEO -> FileDownloadQueue.DownloadType.VIDEO
        TdMessageRemoteDataSource.DownloadType.GIF -> FileDownloadQueue.DownloadType.GIF
        TdMessageRemoteDataSource.DownloadType.STICKER -> FileDownloadQueue.DownloadType.STICKER
        TdMessageRemoteDataSource.DownloadType.VIDEO_NOTE -> FileDownloadQueue.DownloadType.VIDEO_NOTE
    }
}