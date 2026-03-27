package org.monogram.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_full_info")
data class UserFullInfoEntity(
    @PrimaryKey val userId: Long,
    val bio: String?,
    val commonGroupsCount: Int,
    val giftCount: Int = 0,
    val botInfoDescription: String? = null,
    val personalChatId: Long = 0L,
    val birthdateDay: Int = 0,
    val birthdateMonth: Int = 0,
    val birthdateYear: Int = 0,
    val businessLocationAddress: String? = null,
    val businessLocationLatitude: Double = 0.0,
    val businessLocationLongitude: Double = 0.0,
    val businessOpeningHoursTimeZone: String? = null,
    val businessNextOpenIn: Int = 0,
    val businessNextCloseIn: Int = 0,
    val businessStartPageTitle: String? = null,
    val businessStartPageMessage: String? = null,
    val personalPhotoPath: String? = null,
    val isBlocked: Boolean,
    val canBeCalled: Boolean,
    val supportsVideoCalls: Boolean,
    val hasPrivateCalls: Boolean,
    val hasPrivateForwards: Boolean,
    val hasRestrictedVoiceAndVideoNoteMessages: Boolean = false,
    val hasPostedToProfileStories: Boolean = false,
    val setChatBackground: Boolean = false,
    val canGetRevenueStatistics: Boolean = false,
    val incomingPaidMessageStarCount: Long = 0L,
    val outgoingPaidMessageStarCount: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)