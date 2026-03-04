package org.monogram.data.datasource.remote

import org.drinkless.tdlib.TdApi
import org.monogram.data.gateway.TelegramGateway
import org.monogram.data.mapper.toUpdateInfo
import org.monogram.domain.models.UpdateInfo

class TdUpdateRemoteDataSource(
    private val gateway: TelegramGateway,
    private val channelId: Long = -1003566234286L
) : UpdateRemoteDateSource {

    override suspend fun fetchLatestUpdate(): UpdateInfo? {
        return runCatching {
            val messages = gateway.execute(TdApi.GetChatHistory(channelId, 0, 0, 1, false))
            val doc = messages.messages
                .firstOrNull()
                ?.content as? TdApi.MessageDocument
            doc?.toUpdateInfo()
        }.getOrNull()
    }

    override suspend fun getTdLibVersion(): String {
        return runCatching {
            (gateway.execute(TdApi.GetOption("version")) as? TdApi.OptionValueString)?.value
        }.getOrNull() ?: "Unknown"
    }

    override suspend fun getTdLibCommitHash(): String {
        return runCatching {
            (gateway.execute(TdApi.GetOption("commit_hash")) as? TdApi.OptionValueString)?.value
        }.getOrNull() ?: ""
    }
}
