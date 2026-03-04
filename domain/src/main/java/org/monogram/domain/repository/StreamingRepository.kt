package org.monogram.domain.repository

import kotlinx.coroutines.flow.Flow

interface StreamingRepository {
    fun getDownloadProgress(fileId: Int): Flow<Float>
}

interface PlayerDataSourceFactory {
    fun createPayload(fileId: Int): Any
}