package org.monogram.domain.models

data class UpdateInfo(
    val version: String,
    val versionCode: Int,
    val description: String,
    val changelog: List<RichText>,
    val fileId: Int,
    val fileName: String,
    val fileSize: Long
)

data class RichText(
    val text: String,
    val entities: List<MessageEntity> = emptyList()
)

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    data class UpdateAvailable(val info: UpdateInfo) : UpdateState
    object UpToDate : UpdateState
    data class Downloading(val progress: Float, val totalSize: Long) : UpdateState
    data class ReadyToInstall(val filePath: String) : UpdateState
    data class Error(val message: String) : UpdateState
}
