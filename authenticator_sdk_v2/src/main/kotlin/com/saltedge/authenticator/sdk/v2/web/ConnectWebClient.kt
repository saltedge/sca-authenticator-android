/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.sdk.v2.web

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import com.saltedge.authenticator.sdk.v2.config.ClientConfig

private const val WEB_PAGE_Y_CORRECTION_OFFSET = 20

@Suppress("OverridingDeprecatedMember", "DEPRECATION")
class ConnectWebClient(val contract: ConnectWebClientContract) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return if (url != null && url.startsWith(ClientConfig.authenticationReturnUrl)) {
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
