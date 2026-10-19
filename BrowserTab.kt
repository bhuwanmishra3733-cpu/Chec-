package com.vampire.browser.browser

import android.webkit.WebView
import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "New Tab",
    var url: String = "https://www.google.com",
    val isPrivate: Boolean = false,
    var isDesktopMode: Boolean = false,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var isLoading: Boolean = false,
    var progress: Int = 0,
    var isSecure: Boolean = false,
    var lastError: String? = null,
    var webView: WebView? = null
)