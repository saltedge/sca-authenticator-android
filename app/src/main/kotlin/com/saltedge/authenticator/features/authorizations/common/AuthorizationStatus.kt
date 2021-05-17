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
package com.saltedge.authenticator.features.authorizations.common

import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.ResId
import java.util.*

enum class AuthorizationStatus {//pending confirm_processing deny_processing confirmed denied closed
    LOADING,
    PENDING,
    CONFIRM_PROCESSING,
    DENY_PROCESSING,
    CONFIRMED,
    DENIED,
    ERROR,
    TIME_OUT,
    UNAVAILABLE;

    fun isFinalStatus(): Boolean {
        return this == CONFIRMED
            || this == DENIED
            || this == ERROR
            || this == TIME_OUT
            || this == UNAVAILABLE
    }

    fun isProcessingMode(): Boolean {
        return this == CONFIRM_PROCESSING || this == DENY_PROCESSING
    }

    val statusImageResId: ResId?
        get() {
            return when(this) {
                CONFIRMED -> R.drawable.ic_status_success
                DENIED -> R.drawable.ic_status_denied
                ERROR -> R.drawable.ic_status_error
                TIME_OUT -> R.drawable.ic_status_timeout
                UNAVAILABLE -> R.drawable.ic_status_unavailable
                else -> null
            }
        }

    val statusTitleResId: ResId
        get() {
            return when(this) {
                LOADING, PENDING -> R.string.authorizations_loading
                CONFIRM_PROCESSING, DENY_PROCESSING -> R.string.authorizations_processing
                CONFIRMED -> R.string.authorizations_confirmed
                DENIED -> R.string.authorizations_denied
                ERROR -> R.string.authorizations_error
                TIME_OUT -> R.string.authorizations_time_out
                UNAVAILABLE -> R.string.authorizations_unavailable
            }
        }

    val statusDescriptionResId: ResId
        get() {
            return when(this) {
                LOADING, PENDING -> R.string.authorizations_loading_description
                CONFIRM_PROCESSING, DENY_PROCESSING -> R.string.authorizations_processing_description
                CONFIRMED -> R.string.authorizations_confirmed_description
                DENIED -> R.string.authorizations_denied_description
                ERROR -> R.string.authorizations_error_description
                TIME_OUT -> R.string.authorizations_time_out_description
                UNAVAILABLE -> R.string.authorizations_unavailable_description
            }
        }

    val processingMode: Boolean
        get() = this === LOADING || this === CONFIRM_PROCESSING || this === DENY_PROCESSING
}

fun String.toAuthorizationStatus(): AuthorizationStatus? {
    return try {
        if ("closed" == this) AuthorizationStatus.ERROR
        else AuthorizationStatus.valueOf(toUpperCase(Locale.US))
    } catch (e: Exception) {
        null
    }
}
