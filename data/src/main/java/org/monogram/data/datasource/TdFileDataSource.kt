package org.monogram.data.datasource

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.tdlib.TdApi
import org.monogram.data.di.TdLibClient
import org.monogram.data.gateway.TelegramGateway
import kotlin.coroutines.resume

class TdFileDataSource(private val gateway: TelegramGateway) : FileDataSource {
    override suspend fun downloadFile(fileId: Int, priority: Int, offset: Long, limit: Long, synchronous: Boolean): TdApi.File?  {
        val result = gateway.execute(TdApi.DownloadFile(fileId, priority, offset, limit, synchronous))
        return if (result is TdApi.File) result else null
    }

    override suspend fun cancelDownload(fileId: Int): TdApi.Ok? {
        val result = gateway.execute(TdApi.CancelDownloadFile(fileId, true))
        return if (result is TdApi.Ok) result else null
    }

    override suspend fun getFile(fileId: Int): TdApi.File? {
        val result = gateway.execute(TdApi.GetFile(fileId))
        return if (result is TdApi.File) result else null
    }

    override suspend fun getFileDownloadedPrefixSize(fileId: Int, offset: Long): TdApi.Count? {
        val result = gateway.execute(TdApi.GetFileDownloadedPrefixSize(fileId, offset))
        return result as? TdApi.Count
    }

    override suspend fun readFilePart(fileId: Int, offset: Long, count: Long): TdApi.ReadFilePart? {
        val result = gateway.execute(TdApi.ReadFilePart(fileId, offset, count))
        return result as? TdApi.ReadFilePart
    }

    override fun waitForUpload(fileId: Int): CompletableDeferred<Unit> {
        return CompletableDeferred()
    }
}