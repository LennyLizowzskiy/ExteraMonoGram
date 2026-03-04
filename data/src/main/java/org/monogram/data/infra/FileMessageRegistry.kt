package org.monogram.data.infra

import java.util.concurrent.ConcurrentHashMap

class FileMessageRegistry {
    val fileIdToMessageMap = ConcurrentHashMap<Int, MutableSet<Pair<Long, Long>>>()
    private val messageToFileMap = ConcurrentHashMap<Pair<Long, Long>, MutableSet<Int>>()
    val standaloneFileIds = ConcurrentHashMap.newKeySet<Int>()

    fun register(fileId: Int, chatId: Long, messageId: Long) {
        val key = chatId to messageId
        fileIdToMessageMap.computeIfAbsent(fileId) { ConcurrentHashMap.newKeySet() }.add(key)
        messageToFileMap.computeIfAbsent(key) { ConcurrentHashMap.newKeySet() }.add(fileId)
    }

    fun removeMessages(chatId: Long, messageIds: List<Long>) {
        messageIds.forEach { messageId ->
            val key = chatId to messageId
            messageToFileMap.remove(key)?.forEach { fileId ->
                fileIdToMessageMap[fileId]?.let { set ->
                    set.remove(key)
                    if (set.isEmpty()) fileIdToMessageMap.remove(fileId)
                }
            }
        }
    }

    fun updateMessageId(chatId: Long, oldId: Long, newId: Long) {
        val oldKey = chatId to oldId
        val newKey = chatId to newId
        val fileIds = messageToFileMap.remove(oldKey) ?: return
        messageToFileMap.computeIfAbsent(newKey) { ConcurrentHashMap.newKeySet() }.addAll(fileIds)
        fileIds.forEach { fileId ->
            fileIdToMessageMap[fileId]?.let { set ->
                set.remove(oldKey)
                set.add(newKey)
            }
        }
    }

    fun getMessages(fileId: Int): Set<Pair<Long, Long>> =
        fileIdToMessageMap[fileId] ?: emptySet()

    fun getFileIdsForMessage(chatId: Long, messageId: Long): Set<Int> =
        messageToFileMap[chatId to messageId] ?: emptySet()
}
