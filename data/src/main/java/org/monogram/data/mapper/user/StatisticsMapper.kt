package org.monogram.data.mapper.user

import org.drinkless.tdlib.TdApi
import org.monogram.domain.models.ChatRevenueStatisticsModel
import org.monogram.domain.models.ChatStatisticsModel
import org.monogram.domain.models.DateRangeModel
import org.monogram.domain.models.RevenueAmountModel
import org.monogram.domain.models.StatisticsGraphModel
import org.monogram.domain.models.StatisticsType
import org.monogram.domain.models.StatisticsValueModel
import org.monogram.domain.models.TopAdministratorModel
import org.monogram.domain.models.TopInviterModel
import org.monogram.domain.models.TopSenderModel

fun TdApi.ChatStatistics.toDomain(): ChatStatisticsModel = when (this) {
    is TdApi.ChatStatisticsSupergroup -> ChatStatisticsModel(
        type = StatisticsType.SUPERGROUP,
        period = DateRangeModel(period.startDate, period.endDate),
        memberCount = memberCount.toDomain(),
        messageCount = messageCount.toDomain(),
        viewerCount = viewerCount.toDomain(),
        senderCount = senderCount.toDomain(),
        topSenders = topSenders.map {
            TopSenderModel(it.userId, it.sentMessageCount, it.averageCharacterCount)
        },
        topAdministrators = topAdministrators.map {
            TopAdministratorModel(
                it.userId,
                it.deletedMessageCount,
                it.bannedUserCount,
                it.restrictedUserCount
            )
        },
        topInviters = topInviters.map {
            TopInviterModel(it.userId, it.addedMemberCount)
        },
        memberCountGraph = memberCountGraph.toDomain(),
        joinGraph = joinGraph.toDomain(),
        muteGraph = joinBySourceGraph.toDomain(),
        joinBySourceGraph = joinBySourceGraph.toDomain(),
        languageGraph = languageGraph.toDomain(),
        messageContentGraph = messageContentGraph.toDomain(),
        actionGraph = actionGraph.toDomain(),
        dayGraph = dayGraph.toDomain(),
        weekGraph = weekGraph.toDomain()
    )
    is TdApi.ChatStatisticsChannel -> ChatStatisticsModel(
        type = StatisticsType.CHANNEL,
        period = DateRangeModel(period.startDate, period.endDate),
        memberCount = memberCount.toDomain(),
        meanViewCount = meanMessageViewCount.toDomain(),
        meanShareCount = meanMessageShareCount.toDomain(),
        enabledNotificationsPercentage = enabledNotificationsPercentage,
        memberCountGraph = memberCountGraph.toDomain(),
        joinGraph = joinGraph.toDomain(),
        muteGraph = muteGraph.toDomain(),
        viewCountByHourGraph = viewCountByHourGraph.toDomain(),
        viewCountBySourceGraph = viewCountBySourceGraph.toDomain(),
        joinBySourceGraph = joinBySourceGraph.toDomain(),
        languageGraph = languageGraph.toDomain(),
        messageContentGraph = messageInteractionGraph.toDomain(),
        actionGraph = instantViewInteractionGraph.toDomain()
    )
    else -> ChatStatisticsModel(
        type = StatisticsType.SUPERGROUP,
        period = DateRangeModel(0, 0),
        memberCount = StatisticsValueModel(0.0, 0.0, 0.0)
    )
}

fun TdApi.ChatRevenueStatistics.toDomain(): ChatRevenueStatisticsModel {
    return ChatRevenueStatisticsModel(
        revenueByHourGraph = revenueByHourGraph.toDomain(),
        revenueGraph = revenueGraph.toDomain(),
        revenueAmount = RevenueAmountModel(
            revenueAmount.cryptocurrency,
            revenueAmount.balanceAmount,
            revenueAmount.availableAmount
        ),
        usdRate = usdRate
    )
}

fun TdApi.StatisticalGraph.toDomain(): StatisticsGraphModel = when (this) {
    is TdApi.StatisticalGraphData -> StatisticsGraphModel.Data(jsonData, zoomToken)
    is TdApi.StatisticalGraphAsync -> StatisticsGraphModel.Async(token)
    is TdApi.StatisticalGraphError -> StatisticsGraphModel.Error(errorMessage)
    else -> StatisticsGraphModel.Error("Unknown graph type")
}

private fun TdApi.StatisticalValue.toDomain(): StatisticsValueModel =
    StatisticsValueModel(value, previousValue, growthRatePercentage)