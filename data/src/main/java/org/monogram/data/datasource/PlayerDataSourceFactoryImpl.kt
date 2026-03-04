package org.monogram.data.datasource

import org.monogram.data.gateway.TelegramGateway
import org.monogram.domain.repository.PlayerDataSourceFactory

class PlayerDataSourceFactoryImpl(
    private val gateway: TelegramGateway
) : PlayerDataSourceFactory {

    override fun createPayload(fileId: Int): Any {
        return TelegramStreamingDataSource.Factory(gateway, fileId)
    }
}