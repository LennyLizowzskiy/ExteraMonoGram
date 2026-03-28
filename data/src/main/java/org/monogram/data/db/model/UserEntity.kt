package org.monogram.data.db.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"]),
        Index(value = ["createdAt"])
    ]
)
data class UserEntity(
    @PrimaryKey val id: Long,
    val firstName: String,
    val lastName: String?,
    val phoneNumber: String?,
    val avatarPath: String?,
    val personalAvatarPath: String? = null,
    val isPremium: Boolean,
    val isVerified: Boolean,
    val isSupport: Boolean = false,
    val isContact: Boolean = false,
    val isMutualContact: Boolean = false,
    val isCloseFriend: Boolean = false,
    val haveAccess: Boolean = true,
    val username: String?,
    val usernamesData: String? = null,
    val statusType: String = "OFFLINE",
    val accentColorId: Int = 0,
    val profileAccentColorId: Int = -1,
    val statusEmojiId: Long = 0L,
    val languageCode: String? = null,
    val lastSeen: Long,
    val createdAt: Long = System.currentTimeMillis()
)