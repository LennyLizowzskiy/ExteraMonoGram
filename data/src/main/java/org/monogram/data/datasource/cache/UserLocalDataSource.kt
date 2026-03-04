package org.monogram.data.datasource.cache

import org.drinkless.tdlib.TdApi

interface UserLocalDataSource {
    fun getUser(userId: Long): TdApi.User?
    fun putUser(user: TdApi.User)
    fun getUserFullInfo(userId: Long): TdApi.UserFullInfo?
    fun putUserFullInfo(userId: Long, info: TdApi.UserFullInfo)
    fun clearAll()
}