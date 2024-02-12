/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.web

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient

private const val WEB_PAGE_Y_CORRECTION_OFFSET = 20

@Suppress("OverridingDeprecatedMember", "DEPRECATION")
class ConnectWebClient(val authenticationReturnUrl: String, val contract: ConnectWebClientContract) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return if (url != null && url.startsWith(authenticationReturnUrl)) {
            contract.onReturnToRedirect(url)
            true
        } else super.shouldOverrideUrlLoading(view, url)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        contract.onPageLoadStarted()
        view?.scrollBy(0, WEB_PAGE_Y_CORRECTION_OFFSET)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        contract.onPageLoadFinished()
    }
}
