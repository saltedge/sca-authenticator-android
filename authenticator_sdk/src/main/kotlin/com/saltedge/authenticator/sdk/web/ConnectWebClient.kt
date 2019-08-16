/* 
 * This file is part of the Salt Edge Authenticator distribution 
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.sdk.web

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import com.saltedge.authenticator.sdk.constants.*

private const val WEB_PAGE_Y_CORRECTION_OFFSET = 20

@Suppress("OverridingDeprecatedMember", "DEPRECATION")
class ConnectWebClient(val contract: ConnectWebClientContract) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return if (url != null && url.startsWith(DEFAULT_RETURN_URL)) {
            val uri = Uri.parse(url)
            val connectionId = uri.getQueryParameter(KEY_ID)
            val accessToken = uri.getQueryParameter(KEY_ACCESS_TOKEN)
            if (connectionId != null && accessToken != null && accessToken.isNotEmpty()) {
                contract.webAuthFinishSuccess(connectionId, accessToken)
            } else {
                val errorClass = uri.getQueryParameter(KEY_ERROR_CLASS)
                contract.webAuthFinishError(
                    errorClass = errorClass ?: ERROR_CLASS_AUTHENTICATION_RESPONSE,
                    errorMessage = uri.getQueryParameter(KEY_ERROR_MESSAGE)
                )
            }
            true
        } else {
            super.shouldOverrideUrlLoading(view, url)
        }
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
