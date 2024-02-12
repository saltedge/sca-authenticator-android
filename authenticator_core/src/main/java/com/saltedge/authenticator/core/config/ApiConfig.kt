/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.config

import android.content.Context
import com.saltedge.authenticator.core.tools.buildUserAgent

const val DEFAULT_RETURN_URL = "authenticator://oauth/redirect"
const val DEFAULT_PLATFORM_NAME = "android"
const val DEFAULT_EXPIRATION_MINUTES = 5

abstract class ApiConfig {
    /**
     * Url where WebView will be redirected on enrollment finish
     */
    var authenticationReturnUrl: String = DEFAULT_RETURN_URL
    var userAgentInfo = ""
        private set

    /**
     * Initialize SDK
     *
     * @param context of Application
     */
    fun setupConfig(context: Context, authenticationReturnUrl: String  = DEFAULT_RETURN_URL) {
        this.userAgentInfo = buildUserAgent(context)
        this.authenticationReturnUrl = authenticationReturnUrl
    }

    fun isReturnToUrl(url: String): Boolean = url.startsWith(authenticationReturnUrl)
}

