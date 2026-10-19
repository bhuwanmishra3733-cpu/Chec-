package com.vampire.browser.browser

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class VampireWebClient(
    private val tab: BrowserTab,
    private val onStarted: () -> Unit,
    private val onFinished: (String, String) -> Unit,
    private val onError: (String) -> Unit
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        tab.isLoading = true
        tab.lastError = null
        val u = url ?: ""
        tab.url = u
        tab.isSecure = u.startsWith("https://")
        onStarted()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        tab.isLoading = false
        val t = view?.title ?: "Untitled"
        val u = url ?: ""
        tab.title = t
        tab.canGoBack = view?.canGoBack() ?: false
        tab.canGoForward = view?.canGoForward() ?: false
        onFinished(t, u)
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            val msg = error?.description?.toString() ?: "Failed to load page"
            tab.lastError = msg
            tab.isLoading = false
            onError(msg)
        }
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        handler?.cancel() // Enforce high security
        val msg = "Untrusted or invalid SSL certificate"
        tab.lastError = msg
        tab.isLoading = false
        onError(msg)
    }
}