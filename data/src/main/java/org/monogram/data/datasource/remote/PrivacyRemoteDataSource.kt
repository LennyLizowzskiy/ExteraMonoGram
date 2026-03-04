package org.monogram.data.datasource.remote

import org.drinkless.tdlib.TdApi

interface PrivacyRemoteDataSource {
    suspend fun getPrivacyRules(setting: TdApi.UserPrivacySetting): List<TdApi.UserPrivacySettingRule>
    suspend fun setPrivacyRules(setting: TdApi.UserPrivacySetting, rules: TdApi.UserPrivacySettingRules)
    suspend fun getBlockedUsers(): List<Long>
    suspend fun blockUser(userId: Long)
    suspend fun unblockUser(userId: Long)
    suspend fun deleteAccount(reason: String, password: String)
    suspend fun getAccountTtl(): Int
    suspend fun setAccountTtl(days: Int)
    suspend fun getPasswordState(): Boolean
    suspend fun getOption(name: String): Boolean
    suspend fun setOption(name: String, value: Boolean)
}
