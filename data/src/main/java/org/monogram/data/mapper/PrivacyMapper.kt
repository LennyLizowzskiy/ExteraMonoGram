package org.monogram.data.mapper

import org.drinkless.tdlib.TdApi
import org.monogram.domain.models.PrivacyRule
import org.monogram.domain.repository.PrivacyKey

fun PrivacyKey.toApi(): TdApi.UserPrivacySetting = when (this) {
    PrivacyKey.PHONE_NUMBER -> TdApi.UserPrivacySettingShowPhoneNumber()
    PrivacyKey.PHONE_NUMBER_SEARCH -> TdApi.UserPrivacySettingAllowFindingByPhoneNumber()
    PrivacyKey.LAST_SEEN -> TdApi.UserPrivacySettingShowStatus()
    PrivacyKey.PROFILE_PHOTO -> TdApi.UserPrivacySettingShowProfilePhoto()
    PrivacyKey.BIO -> TdApi.UserPrivacySettingShowBio()
    PrivacyKey.FORWARDED_MESSAGES -> TdApi.UserPrivacySettingShowLinkInForwardedMessages()
    PrivacyKey.CALLS -> TdApi.UserPrivacySettingAllowCalls()
    PrivacyKey.GROUPS_AND_CHANNELS -> TdApi.UserPrivacySettingAllowChatInvites()
}

fun TdApi.UserPrivacySettingRule.toDomain(): PrivacyRule = when (this) {
    is TdApi.UserPrivacySettingRuleAllowAll -> PrivacyRule.AllowAll
    is TdApi.UserPrivacySettingRuleAllowContacts -> PrivacyRule.AllowContacts
    is TdApi.UserPrivacySettingRuleAllowUsers -> PrivacyRule.AllowUsers(userIds.toList())
    is TdApi.UserPrivacySettingRuleAllowChatMembers -> PrivacyRule.AllowChatMembers(chatIds.toList())
    is TdApi.UserPrivacySettingRuleRestrictAll -> PrivacyRule.AllowNone
    is TdApi.UserPrivacySettingRuleRestrictContacts -> PrivacyRule.DisallowContacts
    is TdApi.UserPrivacySettingRuleRestrictUsers -> PrivacyRule.DisallowUsers(userIds.toList())
    is TdApi.UserPrivacySettingRuleRestrictChatMembers -> PrivacyRule.DisallowChatMembers(chatIds.toList())
    else -> PrivacyRule.AllowNone
}

fun PrivacyRule.toApi(): TdApi.UserPrivacySettingRule = when (this) {
    is PrivacyRule.AllowAll -> TdApi.UserPrivacySettingRuleAllowAll()
    is PrivacyRule.AllowContacts -> TdApi.UserPrivacySettingRuleAllowContacts()
    is PrivacyRule.AllowNone -> TdApi.UserPrivacySettingRuleRestrictAll()
    is PrivacyRule.AllowUsers -> TdApi.UserPrivacySettingRuleAllowUsers(userIds.toLongArray())
    is PrivacyRule.AllowChatMembers -> TdApi.UserPrivacySettingRuleAllowChatMembers(chatIds.toLongArray())
    is PrivacyRule.DisallowContacts -> TdApi.UserPrivacySettingRuleRestrictContacts()
    is PrivacyRule.DisallowUsers -> TdApi.UserPrivacySettingRuleRestrictUsers(userIds.toLongArray())
    is PrivacyRule.DisallowChatMembers -> TdApi.UserPrivacySettingRuleRestrictChatMembers(chatIds.toLongArray())
}