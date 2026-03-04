package org.monogram.data.mapper

import androidx.core.graphics.toColorInt
import org.drinkless.tdlib.TdApi
import org.monogram.domain.models.webapp.ThemeParams

fun ThemeParams.toApi(): TdApi.ThemeParameters {
    return TdApi.ThemeParameters(
        fromHex(backgroundColor),
        fromHex(secondaryBackgroundColor),
        fromHex(headerBackgroundColor),
        fromHex(bottomBarBackgroundColor),
        fromHex(sectionBackgroundColor),
        fromHex(sectionSeparatorColor),
        fromHex(textColor),
        fromHex(accentTextColor),
        fromHex(sectionHeaderTextColor),
        fromHex(subtitleTextColor),
        fromHex(destructiveTextColor),
        fromHex(hintColor),
        fromHex(linkColor),
        fromHex(buttonColor),
        fromHex(buttonTextColor)
    )
}

private fun fromHex(hex: String?): Int {
    if (hex.isNullOrBlank()) return 0
    return hex.toColorInt()
}