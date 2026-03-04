package org.monogram.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class FileModel(
    val id: Int,
    val size: Long,
    val expectedSize: Long,
    val local: FileLocalModel,
    val remote: FileRemoteModel? = null
)

@Serializable
data class FileLocalModel(
    val path: String,
    val isDownloadingActive: Boolean,
    val canBeDownloaded: Boolean,
    val isDownloadingCompleted: Boolean,
    val canBeDeleted: Boolean = false,
    val downloadOffset: Long = 0,
    val downloadedPrefixSize: Long = 0,
    val downloadedSize: Long = 0
)

@Serializable
data class FileRemoteModel(
    val id: String,
    val uniqueId: String,
    val isUploadingActive: Boolean,
    val isUploadingCompleted: Boolean,
    val uploadedSize: Long
)
