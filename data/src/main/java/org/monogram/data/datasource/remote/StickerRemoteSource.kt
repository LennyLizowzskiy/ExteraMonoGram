package org.monogram.data.datasource.remote

import org.monogram.domain.models.GifModel
import org.monogram.domain.models.StickerModel
import org.monogram.domain.models.StickerSetModel
import org.monogram.domain.models.StickerType

interface StickerRemoteSource {
    suspend fun getInstalledStickerSets(type: StickerType): List<StickerSetModel>
    suspend fun getArchivedStickerSets(type: StickerType): List<StickerSetModel>
    suspend fun getStickerSet(setId: Long): StickerSetModel?
    suspend fun getStickerSetByName(name: String): StickerSetModel?
    suspend fun getRecentStickers(): List<StickerModel>
    suspend fun toggleStickerSetInstalled(setId: Long, isInstalled: Boolean)
    suspend fun toggleStickerSetArchived(setId: Long, isArchived: Boolean)
    suspend fun reorderStickerSets(type: StickerType, setIds: List<Long>)
    suspend fun getEmojiCategories(): List<String>
    suspend fun getMessageAvailableReactions(chatId: Long, messageId: Long): List<String>
    suspend fun searchEmojis(query: String): List<String>
    suspend fun searchCustomEmojis(query: String): List<StickerModel>
    suspend fun searchStickers(query: String): List<StickerModel>
    suspend fun searchStickerSets(query: String): List<StickerSetModel>
    suspend fun getSavedGifs(): List<GifModel>
    suspend fun addSavedGif(path: String)
    suspend fun searchGifs(query: String): List<GifModel>
    suspend fun clearRecentStickers()
}