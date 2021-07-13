package com.ly.anki_assist_app.ankidroid.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import com.ly.anki_assist_app.ankidroid.api.AnkiAppApi
import timber.log.Timber

class MyWebView : WebView {

    companion object {
        val BASE_URL by lazy { AnkiAppApi.getAnkiMediaUri() }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context , attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        this.settings.apply {
            this.displayZoomControls = false
            this.builtInZoomControls = true
            this.setSupportZoom(true)
            this.loadWithOverviewMode = true
            this.javaScriptEnabled = true
        }
        this.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        this.isScrollbarFadingEnabled = true
        this.setBackgroundColor(Color.argb(1, 0, 0, 0))
        this.webViewClient = CardViewerWebClient()
        this.webChromeClient = AnkiDroidWebChromeClient()
    }

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

    override fun onPageFinished(view: WebView?, url: String?) {
        Timber.d("onPageFinished triggered");
//        drawFlag();
//        drawMark();
        view?.loadUrl("javascript:onPageFinished();");
    }
}