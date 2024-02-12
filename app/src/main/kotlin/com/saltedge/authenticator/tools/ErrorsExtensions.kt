/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ERROR_INVALID_AUTHENTICATION_DATA
import com.saltedge.authenticator.app.ERROR_INVALID_DEEPLINK
import com.saltedge.authenticator.core.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.core.api.ERROR_CLASS_HOST_UNREACHABLE
import com.saltedge.authenticator.core.api.ERROR_CLASS_SSL_HANDSHAKE
import com.saltedge.authenticator.core.api.model.error.ApiErrorData

/**
 * Creates localized description by known error class name or returns message field itself
 *
 * @receiver api error
 * @param context - application context
 * @return error message string
 */
fun ApiErrorData.getErrorMessage(context: Context): String {
    return if (errorMessage.isBlank()) {
        when (errorClassName) {
            ERROR_CLASS_HOST_UNREACHABLE -> context.getString(R.string.errors_no_connection)
            ERROR_CLASS_SSL_HANDSHAKE -> context.getString(R.string.errors_secure_connection)
            ERROR_CLASS_API_RESPONSE -> context.getString(R.string.errors_request_error)
            ERROR_INVALID_DEEPLINK -> context.getString(R.string.errors_invalid_qr)
            ERROR_INVALID_AUTHENTICATION_DATA -> context.getString(R.string.errors_invalid_qr)
            else -> errorMessage
        }
    } else errorMessage
}
