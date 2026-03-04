package org.monogram.domain.models

data class PremiumStateModel(
    val state: String,
    val animations: List<PremiumFeatureType> = emptyList(),
    val paymentOptions: List<PremiumPaymentOptionModel> = emptyList()
)

enum class PremiumFeatureType {
    DOUBLE_LIMITS,
    VOICE_TO_TEXT,
    FASTER_DOWNLOAD,
    TRANSLATION,
    ANIMATED_EMOJI,
    ADVANCED_CHAT_MANAGEMENT,
    NO_ADS,
    INFINITE_REACTIONS,
    BADGE,
    PROFILE_BADGE,
    APP_ICONS,
    UNKNOWN
}

enum class PremiumSource {
    SETTINGS,
    LIMIT_EXCEEDED,
    VIDEO_STATUS,
    STORY_STATUS,
    LINK
}

enum class PremiumLimitType {
    SUPERGROUP_COUNT,
    CHAT_FOLDER_COUNT,
    PINNED_CHAT_COUNT,
    CREATED_PUBLIC_CHAT_COUNT,
    CHAT_FOLDER_INVITE_LINK_COUNT,
    SHAREABLE_CHAT_FOLDER_COUNT,
    ACTIVE_STORY_COUNT,
    WEEKLY_SENT_MESSAGE_COUNT,
    MONTHLY_SENT_MESSAGE_COUNT
}

data class PremiumPaymentOptionModel(
    val currency: String,
    val amount: Long,
    val monthCount: Int,
    val storeProductId: String,
    val paymentLink: String?
)
