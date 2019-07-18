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
package com.saltedge.authenticator.sdk.model

import android.content.Context
import com.saltedge.authenticator.sdk.R
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_API_RESPONSE
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_CONNECTION_NOT_FOUND
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_HOST_UNREACHABLE
import com.saltedge.authenticator.sdk.constants.ERROR_CLASS_SSL_HANDSHAKE

/**
 * Creates error description by known error class names or returns message field itself
 *
 * @receiver api error
 * @param context - application context
 * @return error message string
 */
fun ApiErrorData.getErrorMessage(context: Context): String {
    return if (errorMessage.isBlank()) {
        when (errorClassName) {
            ERROR_CLASS_HOST_UNREACHABLE -> context.getString(R.string.errors_no_internet_connection)
            ERROR_CLASS_SSL_HANDSHAKE -> context.getString(R.string.errors_update_security)
            ERROR_CLASS_API_RESPONSE -> context.getString(R.string.errors_request_error)
            else -> errorMessage
        }
    } else errorMessage
}

/**
 * Checks if errorClassName is equal to ERROR_CLASS_CONNECTION_NOT_FOUND
 *
 * @receiver api error
 * @return boolean. true if errorClassName == ERROR_CLASS_CONNECTION_NOT_FOUND
 */
fun ApiErrorData.isConnectionNotFound(): Boolean = errorClassName == ERROR_CLASS_CONNECTION_NOT_FOUND

/**
 * Creates Api Error for unknown network error
 * with class name `ERROR_CLASS_API_RESPONSE` and message `Request Error ($responseCode)`
 *
 * @param responseCode - network response code (e.g. 200, 400, etc.)
 * @return api error
 */
fun createRequestError(responseCode: Int): ApiErrorData {
    return ApiErrorData(
            errorMessage = "Request Error ($responseCode)",
            errorClassName = ERROR_CLASS_API_RESPONSE)
}

/**
 * Creates Api Error for invalid response format
 * with class name `ERROR_CLASS_API_RESPONSE` and message `Invalid response`
 *
 * @return api error
 */
fun createInvalidResponseError(): ApiErrorData {
    return ApiErrorData(
            errorMessage = "Invalid response",
            errorClassName = ERROR_CLASS_API_RESPONSE)
}
