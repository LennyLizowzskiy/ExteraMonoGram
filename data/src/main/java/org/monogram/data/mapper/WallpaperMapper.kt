package org.monogram.data.mapper

import org.drinkless.tdlib.TdApi
import org.monogram.domain.models.ThumbnailModel
import org.monogram.domain.models.WallpaperModel
import org.monogram.domain.models.WallpaperSettings

fun mapBackgrounds(backgrounds: Array<TdApi.Background>): List<WallpaperModel> {
    val defaultWallpapers = listOf(
        WallpaperModel(
            id = -1,
            slug = "default_blue",
            title = "Default Blue",
            pattern = false,
            documentId = 0,
            thumbnail = null,
            settings = WallpaperSettings(
                backgroundColor = 0x1E3557,
                secondBackgroundColor = 0x2D4A77,
                thirdBackgroundColor = null,
                fourthBackgroundColor = null,
                intensity = null,
                rotation = 45,
                isInverted = null
            ),
            isDownloaded = true,
            localPath = null,
            isDefault = true
        )
    )
    return defaultWallpapers + backgrounds.map { it.toDomain() }
}

fun TdApi.Background.toDomain(): WallpaperModel {
    val doc = this.document
    val file = doc?.document
    return WallpaperModel(
        id = this.id,
        slug = this.name,
        title = this.name,
        pattern = this.type is TdApi.BackgroundTypePattern,
        documentId = doc?.document?.id?.toLong() ?: 0L,
        thumbnail = doc?.thumbnail?.toDomain(),
        settings = this.type.toWallpaperSettings(),
        isDownloaded = file?.local?.isDownloadingCompleted == true,
        localPath = file?.local?.path?.ifEmpty { null },
        isDefault = this.isDefault
    )
}

fun TdApi.Thumbnail.toDomain(): ThumbnailModel = ThumbnailModel(
    fileId = this.file.id,
    width = this.width,
    height = this.height,
    localPath = this.file.local.path
)

fun TdApi.BackgroundType.toWallpaperSettings(): WallpaperSettings? = when (this) {
    is TdApi.BackgroundTypePattern -> fill.toWallpaperSettings()
        ?.copy(intensity = intensity, isInverted = isInverted)
    is TdApi.BackgroundTypeFill -> fill.toWallpaperSettings()
    else -> null
}

fun TdApi.BackgroundFill.toWallpaperSettings(): WallpaperSettings? = when (this) {
    is TdApi.BackgroundFillSolid -> WallpaperSettings(
        backgroundColor = color,
        secondBackgroundColor = null,
        thirdBackgroundColor = null,
        fourthBackgroundColor = null,
        intensity = null,
        rotation = null,
        isInverted = null
    )
    is TdApi.BackgroundFillGradient -> WallpaperSettings(
        backgroundColor = topColor,
        secondBackgroundColor = bottomColor,
        thirdBackgroundColor = null,
        fourthBackgroundColor = null,
        intensity = null,
        rotation = rotationAngle,
        isInverted = null
    )
    is TdApi.BackgroundFillFreeformGradient -> WallpaperSettings(
        backgroundColor = colors.getOrNull(0),
        secondBackgroundColor = colors.getOrNull(1),
        thirdBackgroundColor = colors.getOrNull(2),
        fourthBackgroundColor = colors.getOrNull(3),
        intensity = null,
        rotation = null,
        isInverted = null
    )
    else -> null
}