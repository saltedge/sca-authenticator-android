/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.web

interface ConnectWebClientContract {
    fun onReturnToRedirect(url: String)
    fun onPageLoadStarted()
    fun onPageLoadFinished()
}
