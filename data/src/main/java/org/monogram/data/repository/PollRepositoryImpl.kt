package org.monogram.data.repository

import org.monogram.domain.repository.PollRepository

class PollRepositoryImpl : PollRepository {
    private val pollToMessage = mutableMapOf<Long, Pair<Long, Long>>()

    override suspend fun mapPollIdToMessage(pollId: Long, chatId: Long, messageId: Long) {
        pollToMessage[pollId] = chatId to messageId
    }

    override suspend fun getMessageIdByPollId(pollId: Long): Pair<Long, Long>? {
        return pollToMessage[pollId]
    }
}