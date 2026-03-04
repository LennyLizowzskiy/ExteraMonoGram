package org.monogram.domain.models

data class WallpaperModel(
    val id: Long,
    val slug: String,
    val title: String,
    val pattern: Boolean,
    val documentId: Long,
    val thumbnail: ThumbnailModel?,
    val settings: WallpaperSettings?,
    val isDownloaded: Boolean,
    val localPath: String?,
    val isDefault: Boolean = false
)

data class ThumbnailModel(
    val fileId: Int,
    val width: Int,
    val height: Int,
    val localPath: String?
)

data class WallpaperSettings(
    val backgroundColor: Int?,
    val secondBackgroundColor: Int?,
    val thirdBackgroundColor: Int?,
    val fourthBackgroundColor: Int?,
    val intensity: Int?,
    val rotation: Int?,
    val isInverted: Boolean? = null
)
