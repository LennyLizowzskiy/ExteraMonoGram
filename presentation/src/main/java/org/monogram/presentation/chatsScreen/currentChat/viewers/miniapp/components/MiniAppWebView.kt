package org.monogram.presentation.chatsScreen.currentChat.viewers.miniapp.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.monogram.domain.models.webapp.ThemeParams
import org.monogram.presentation.chatsScreen.currentChat.viewers.miniapp.TelegramWebAppHost
import org.monogram.presentation.chatsScreen.currentChat.viewers.miniapp.TelegramWebviewProxy

@Composable
fun MiniAppWebView(
    url: String,
    themeParams: ThemeParams,
    host: TelegramWebAppHost,
    onWebViewCreated: (WebView, TelegramWebviewProxy) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    showMenu: Boolean,
    onHideMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.then(Modifier.clickable(enabled = showMenu) { onHideMenu() })) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    @SuppressLint("SetJavaScriptEnabled")
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString =
                            "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}; ${android.os.Build.MODEL}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Mobile Safari/537.36 Telegram-Android/12.3.1 (Android; ${android.os.Build.MODEL}; SDK ${android.os.Build.VERSION.SDK_INT}; AVERAGE)"
                    }

                    val bridge = TelegramWebviewProxy(
                        context = ctx,
                        webView = this,
                        themeParams = themeParams,
                        host = host
                    )

                    onWebViewCreated(this, bridge)
                    addJavascriptInterface(bridge, "TelegramWebviewProxy")

                    webViewClient = object : android.webkit.WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            onLoadingChanged(true)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            onLoadingChanged(false)
                        }

                        @SuppressLint("WebViewClientOnReceivedSslError")
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: android.webkit.SslErrorHandler?,
                            error: android.net.http.SslError?
                        ) {
                            handler?.proceed()
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?
                        ): Boolean {
                            val uri = request?.url ?: return false
                            if (uri.scheme == "tg" || !listOf("http", "https").contains(uri.scheme)) {
                                runCatching {
                                    ctx.startActivity(
                                        android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            uri
                                        )
                                    )
                                }
                                return true
                            }
                            return false
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            onProgressChanged(newProgress)
                        }
                    }

                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    if (url.isNotEmpty()) {
                        loadUrl(url)
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (url.isNotEmpty() && view.url != url) {
                    view.loadUrl(url)
                }
            }
        )
    }
}
