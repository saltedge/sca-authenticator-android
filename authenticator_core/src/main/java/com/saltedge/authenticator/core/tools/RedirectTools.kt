/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import android.net.Uri
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.Token

fun parseRedirect(
    url: String,
    success: (connectionID: ID?, accessToken: Token) -> Unit,
    error: (errorClass: String, errorMessage: String?) -> Unit
) {
    val uri = Uri.parse(url)
    val connectionID = uri.getQueryParameter(KEY_ID)
    val accessToken = uri.getQueryParameter(KEY_ACCESS_TOKEN)
    val errorClass = uri.getQueryParameter(KEY_ERROR_CLASS)
    val errorMessage = uri.getQueryParameter(KEY_ERROR_MESSAGE)

    if (accessToken?.isNotEmpty() == true) {
        success(connectionID, accessToken)
    } else {
        error(errorClass ?: ERROR_CLASS_AUTHENTICATION_RESPONSE, errorMessage)
    }
}
