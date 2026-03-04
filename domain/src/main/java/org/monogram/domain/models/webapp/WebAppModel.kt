package org.monogram.domain.models.webapp

data class WebAppInfoModel(
    val launchId: Long,
    val url: String
)

data class ThemeParams(
    val colorScheme: String? = null,
    val backgroundColor: String?,
    val secondaryBackgroundColor: String?,
    val headerBackgroundColor: String?,
    val bottomBarBackgroundColor: String?,
    val sectionBackgroundColor: String?,
    val sectionSeparatorColor: String?,
    val textColor: String?,
    val accentTextColor: String?,
    val sectionHeaderTextColor: String?,
    val subtitleTextColor: String?,
    val destructiveTextColor: String?,
    val hintColor: String?,
    val linkColor: String?,
    val buttonColor: String?,
    val buttonTextColor: String?,
)

data class InvoiceModel(
    val title: String,
    val description: String,
    val currency: String,
    val totalAmount: Long,
    val photoUrl: String? = null,
    val isTest: Boolean = false,
    val slug: String? = null
)

data class WebAppPopupButton(
    val id: String,
    val type: String?,
    val text: String,
    val isDestructive: Boolean = false
)

data class WebAppPopupState(
    val title: String?,
    val message: String,
    val buttons: List<WebAppPopupButton>,
    val callbackId: String
)
