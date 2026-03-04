package org.monogram.presentation.chatsScreen.currentChat.impl

import org.monogram.domain.models.WallpaperModel
import org.monogram.presentation.chatsScreen.currentChat.DefaultChatComponent


import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal fun DefaultChatComponent.observePreferences(availableWallpapers: List<WallpaperModel>) {
    appPreferences.fontSize
        .onEach { size ->
            _state.value = _state.value.copy(fontSize = size)
        }
        .launchIn(scope)

    appPreferences.bubbleRadius
        .onEach { radius ->
            _state.value = _state.value.copy(bubbleRadius = radius)
        }
        .launchIn(scope)

    val firstThree = combine(
        appPreferences.wallpaper,
        appPreferences.isWallpaperBlurred,
        appPreferences.wallpaperBlurIntensity
    ) { wallpaper, blurred, intensity ->
        Triple(wallpaper, blurred, intensity)
    }

    val lastThree = combine(
        appPreferences.isWallpaperMoving,
        appPreferences.wallpaperDimming,
        appPreferences.isWallpaperGrayscale
    ) { moving, dimming, grayscale ->
        Triple(moving, dimming, grayscale)
    }

    combine(firstThree, lastThree) { first, last ->
        val (wallpaper, blurred, intensity) = first
        val (moving, dimming, grayscale) = last
        val model = if (wallpaper != null) {
            availableWallpapers.find { it.slug == wallpaper || it.localPath == wallpaper }
        } else {
            null
        }

        _state.value.copy(
            wallpaper = wallpaper,
            wallpaperModel = model,
            isWallpaperBlurred = blurred,
            wallpaperBlurIntensity = intensity,
            isWallpaperMoving = moving,
            wallpaperDimming = dimming,
            isWallpaperGrayscale = grayscale
        )
    }.onEach { newState ->
        _state.value = newState
    }.launchIn(scope)

    appPreferences.isPlayerGesturesEnabled
        .onEach { enabled ->
            _state.value = _state.value.copy(isPlayerGesturesEnabled = enabled)
        }
        .launchIn(scope)

    appPreferences.isPlayerDoubleTapSeekEnabled
        .onEach { enabled ->
            _state.value = _state.value.copy(isPlayerDoubleTapSeekEnabled = enabled)
        }
        .launchIn(scope)

    appPreferences.playerSeekDuration
        .onEach { duration ->
            _state.value = _state.value.copy(playerSeekDuration = duration)
        }
        .launchIn(scope)

    appPreferences.isPlayerZoomEnabled
        .onEach { enabled ->
            _state.value = _state.value.copy(isPlayerZoomEnabled = enabled)
        }
        .launchIn(scope)

    appPreferences.autoDownloadMobile.onEach {
        _state.value = _state.value.copy(autoDownloadMobile = it)
    }.launchIn(scope)

    appPreferences.autoDownloadWifi.onEach {
        _state.value = _state.value.copy(autoDownloadWifi = it)
    }.launchIn(scope)

    appPreferences.autoDownloadRoaming.onEach {
        _state.value = _state.value.copy(autoDownloadRoaming = it)
    }.launchIn(scope)

    appPreferences.autoDownloadFiles.onEach {
        _state.value = _state.value.copy(autoDownloadFiles = it)
    }.launchIn(scope)

    appPreferences.autoplayGifs.onEach {
        _state.value = _state.value.copy(autoplayGifs = it)
    }.launchIn(scope)

    appPreferences.autoplayVideos.onEach {
        _state.value = _state.value.copy(autoplayVideos = it)
    }.launchIn(scope)

    appPreferences.showLinkPreviews.onEach {
        _state.value = _state.value.copy(showLinkPreviews = it)
    }.launchIn(scope)

    combine(appPreferences.isChatAnimationsEnabled, appPreferences.isPowerSavingMode) { enabled, powerSaving ->
        if (powerSaving) false else enabled
    }.onEach {
        _state.value = _state.value.copy(isChatAnimationsEnabled = it)
    }.launchIn(scope)

    combine(appPreferences.isAdBlockEnabled, appPreferences.adBlockKeywords) { enabled, keywords ->
        enabled to keywords
    }.onEach {
        if (_state.value.isChannel) {
            loadMessages()
        }
    }.launchIn(scope)
}

internal fun DefaultChatComponent.loadWallpapers(onLoaded: (List<WallpaperModel>) -> Unit) {
    settingsRepository.getWallpapers()
        .onEach { wallpapers ->
            onLoaded(wallpapers)
            val currentWallpaper = appPreferences.wallpaper.value
            val model = if (currentWallpaper != null) {
                wallpapers.find { it.slug == currentWallpaper || it.localPath == currentWallpaper }
            } else {
                null
            }
            _state.value = _state.value.copy(wallpaperModel = model)
        }
        .launchIn(scope)
}
