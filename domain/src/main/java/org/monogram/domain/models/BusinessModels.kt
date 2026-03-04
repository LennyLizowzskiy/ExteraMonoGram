package org.monogram.domain.models

data class BusinessInfoModel(
    val location: BusinessLocationModel? = null,
    val openingHours: BusinessOpeningHoursModel? = null,
    val startPage: BusinessStartPageModel? = null,
    val nextOpenIn: Int = 0,
    val nextCloseIn: Int = 0
)

data class BusinessLocationModel(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class BusinessOpeningHoursModel(
    val timeZoneId: String,
    val intervals: List<BusinessOpeningHoursIntervalModel>
)

data class BusinessOpeningHoursIntervalModel(
    val startMinute: Int,
    val endMinute: Int
)

data class BusinessStartPageModel(
    val title: String,
    val message: String,
    val stickerPath: String? = null
)