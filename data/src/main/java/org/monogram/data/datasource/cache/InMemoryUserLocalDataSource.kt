package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserLocalDataSource : UserLocalDataSource {
    private val users = ConcurrentHashMap<Long, TdApi.User>()
    private val fullInfos = ConcurrentHashMap<Long, TdApi.UserFullInfo>()

    override fun getUser(userId: Long): TdApi.User? = users[userId]

    override fun putUser(user: TdApi.User) {
        users[user.id] = user
    }

    override fun getUserFullInfo(userId: Long): TdApi.UserFullInfo? = fullInfos[userId]

    override fun putUserFullInfo(userId: Long, info: TdApi.UserFullInfo) {
        fullInfos[userId] = info
    }

    override fun clearAll() {
        users.clear()
        fullInfos.clear()
    }
}