package org.monogram.domain.repository

import org.monogram.domain.models.PrivacyRule
import kotlinx.coroutines.flow.Flow

interface PrivacyRepository {
    fun getPrivacyRules(key: PrivacyKey): Flow<List<PrivacyRule>>
    suspend fun setPrivacyRule(key: PrivacyKey, rules: List<PrivacyRule>)

    suspend fun getBlockedUsers(): List<Long>
    suspend fun blockUser(userId: Long)
    suspend fun unblockUser(userId: Long)

    suspend fun deleteAccount(reason: String, password: String)

    suspend fun getAccountTtl(): Int
    suspend fun setAccountTtl(days: Int)

    suspend fun getPasswordState(): Boolean

    suspend fun canShowSensitiveContent(): Boolean
    suspend fun isShowSensitiveContentEnabled(): Boolean
    suspend fun setShowSensitiveContent(enabled: Boolean)
}

enum class PrivacyKey {
    PHONE_NUMBER,
    PHONE_NUMBER_SEARCH,
    LAST_SEEN,
    PROFILE_PHOTO,
    BIO,
    FORWARDED_MESSAGES,
    CALLS,
    GROUPS_AND_CHANNELS
}