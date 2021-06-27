package com.ly.anki_assist_app.ui.print

import android.content.Context
import android.util.AttributeSet
import android.webkit.*
import timber.log.Timber

class MyWebView : WebView {

    constructor(context: Context) : super(context)

    constructor(context: Context , attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onScrollChanged(horiz: Int, vert: Int, oldHoriz: Int, oldVert: Int) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert)
    }

}

class AnkiDroidWebChromeClient : WebChromeClient() {
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        result?.confirm()
        return true
    }
}

class CardViewerWebClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: ""
        Timber.d("Obtained URL from card: '%s'", url)
        return filterUrl(url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return super.shouldInterceptRequest(view, request)
    }

    private fun filterUrl(url: String): Boolean {
        return false
    }
}