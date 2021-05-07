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
package com.saltedge.authenticator.tools

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.ERROR_INVALID_AUTHENTICATION_DATA
import com.saltedge.authenticator.app.ERROR_INVALID_DEEPLINK
import com.saltedge.authenticator.sdk.v2.api.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.v2.api.ERROR_CLASS_HOST_UNREACHABLE
import com.saltedge.authenticator.sdk.v2.api.ERROR_CLASS_SSL_HANDSHAKE

/**
 * Creates localized description by known error class name or returns message field itself
 *
 * @receiver api error
 * @param context - application context
 * @return error message string
 */
fun com.saltedge.authenticator.sdk.api.model.error.ApiErrorData.getErrorMessage(context: Context): String {
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

fun com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData.getErrorMessage(context: Context): String {
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
