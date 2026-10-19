package com.vampire.browser.browser

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import com.vampire.browser.data.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TabManager(
    private val context: Context,
    private val dbHelper: DatabaseHelper,
    private val scope: CoroutineScope
) {
    private val _tabs = MutableStateFlow<List<BrowserTab>>(emptyList())
    val tabs: StateFlow<List<BrowserTab>> = _tabs

    private val _activeTabId = MutableStateFlow<String?>(null)
    val activeTabId: StateFlow<String?> = _activeTabId

    fun activeTab(): BrowserTab? = _tabs.value.find { it.id == _activeTabId.value }

    fun createTab(url: String, isPrivate: Boolean): BrowserTab {
        val tab = BrowserTab(url = url, isPrivate = isPrivate)
        val wv = initWebView(tab)
        tab.webView = wv

        val updated = _tabs.value.toMutableList().apply { add(tab) }
        _tabs.value = updated
        _activeTabId.value = tab.id

        wv.loadUrl(url)
        return tab
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
    }

    fun closeTab(tabId: String) {
        val list = _tabs.value.toMutableList()
        val index = list.indexOfFirst { it.id == tabId }
        if (index != -1) {
            val removed = list.removeAt(index)
            destroyWebView(removed)
            _tabs.value = list
            if (_activeTabId.value == tabId) {
                _activeTabId.value = if (list.isNotEmpty()) {
                    val nextIdx = if (index >= list.size) list.size - 1 else index
                    list[nextIdx].id
                } else null
            }
        }
    }

    fun closeAllTabs() {
        _tabs.value.forEach { destroyWebView(it) }
        _tabs.value = emptyList()
        _activeTabId.value = null
        CookieManager.getInstance().removeSessionCookies(null)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(tab: BrowserTab): WebView {
        val webView = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = !tab.isPrivate
            allowFileAccess = false
            allowContentAccess = false
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                tab.progress = newProgress
                tab.isLoading = newProgress in 1..99
                _tabs.value = _tabs.value.toList()
            }
            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (!title.isNullOrBlank()) {
                    tab.title = title
                    _tabs.value = _tabs.value.toList()
                }
            }
        }

        webView.webViewClient = VampireWebClient(
            tab = tab,
            onStarted = { _tabs.value = _tabs.value.toList() },
            onFinished = { title, url ->
                _tabs.value = _tabs.value.toList()
                if (!tab.isPrivate) {
                    scope.launch { dbHelper.addHistory(title, url) }
                }
            },
            onError = { _tabs.value = _tabs.value.toList() }
        )

        return webView
    }

    fun toggleDesktopMode(tab: BrowserTab) {
        tab.isDesktopMode = !tab.isDesktopMode
        tab.webView?.let { wv ->
            val desktopUA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/124.0.0.0 Safari/537.36"
            wv.settings.userAgentString = if (tab.isDesktopMode) desktopUA else WebSettings.getDefaultUserAgent(context)
            wv.reload()
        }
        _tabs.value = _tabs.value.toList()
    }

    private fun destroyWebView(tab: BrowserTab) {
        tab.webView?.let {
            it.stopLoading()
            if (tab.isPrivate) {
                it.clearCache(true)
                it.clearFormData()
                it.clearHistory()
            }
            it.destroy()
        }
        tab.webView = null
    }
}