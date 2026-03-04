package org.monogram.data.mapper

import org.drinkless.tdlib.TdApi
import org.monogram.domain.models.NetworkUsageCategory
import org.monogram.domain.models.NetworkUsageModel
import org.monogram.domain.models.NetworkTypeUsage

fun TdApi.NetworkStatistics.toDomain(): NetworkUsageModel {
    val mobileDetails = mutableMapOf<String, Pair<Long, Long>>()
    val wifiDetails = mutableMapOf<String, Pair<Long, Long>>()
    val roamingDetails = mutableMapOf<String, Pair<Long, Long>>()
    var mobileSent = 0L
    var mobileReceived = 0L
    var wifiSent = 0L
    var wifiReceived = 0L
    var roamingSent = 0L
    var roamingReceived = 0L

    this.entries.forEach { entry ->
        val categoryName = if (entry is TdApi.NetworkStatisticsEntryFile) entry.fileType.toDomain() else "Calls"
        val sent = if (entry is TdApi.NetworkStatisticsEntryFile) entry.sentBytes else if (entry is TdApi.NetworkStatisticsEntryCall) entry.sentBytes else 0L
        val received = if (entry is TdApi.NetworkStatisticsEntryFile) entry.receivedBytes else if (entry is TdApi.NetworkStatisticsEntryCall) entry.receivedBytes else 0L
        when (val networkType = if (entry is TdApi.NetworkStatisticsEntryFile) entry.networkType else if (entry is TdApi.NetworkStatisticsEntryCall) entry.networkType else null) {
            is TdApi.NetworkTypeMobile -> {
                mobileSent += sent
                mobileReceived += received
                mobileDetails[categoryName] = (mobileDetails[categoryName]?.first ?: 0) + sent to (mobileDetails[categoryName]?.second ?: 0) + received
            }
            is TdApi.NetworkTypeWiFi -> {
                wifiSent += sent
                wifiReceived += received
                wifiDetails[categoryName] = (wifiDetails[categoryName]?.first ?: 0) + sent to (wifiDetails[categoryName]?.second ?: 0) + received
            }
            is TdApi.NetworkTypeMobileRoaming -> {
                roamingSent += sent
                roamingReceived += received
                roamingDetails[categoryName] = (roamingDetails[categoryName]?.first ?: 0) + sent to (roamingDetails[categoryName]?.second ?: 0) + received
            }
            else -> {}
        }
    }

    return NetworkUsageModel(
        mobile = NetworkTypeUsage(mobileSent, mobileReceived, mobileDetails.map { (k, v) -> NetworkUsageCategory(k, v.first, v.second) }),
        wifi = NetworkTypeUsage(wifiSent, wifiReceived, wifiDetails.map { (k, v) -> NetworkUsageCategory(k, v.first, v.second) }),
        roaming = NetworkTypeUsage(roamingSent, roamingReceived, roamingDetails.map { (k, v) -> NetworkUsageCategory(k, v.first, v.second) })
    )
}
