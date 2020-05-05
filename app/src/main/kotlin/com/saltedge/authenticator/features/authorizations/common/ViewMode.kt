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

enum class ViewMode {
    LOADING,
    DEFAULT,
    CONFIRM_PROCESSING,
    DENY_PROCESSING,
    CONFIRM_SUCCESS,
    DENY_SUCCESS,
    ERROR,
    TIME_OUT,
    UNAVAILABLE;

    fun isFinalMode(): Boolean {
        return this == CONFIRM_SUCCESS
            || this == DENY_SUCCESS
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
                CONFIRM_SUCCESS -> R.drawable.ic_status_success
                DENY_SUCCESS -> R.drawable.ic_status_denied
                ERROR -> R.drawable.ic_status_error
                TIME_OUT -> R.drawable.ic_status_timeout
                UNAVAILABLE -> R.drawable.ic_status_unavailable
                else -> null
            }
        }

    val statusTitleResId: ResId
        get() {
            return when(this) {
                LOADING, DEFAULT -> R.string.authorizations_loading
                CONFIRM_PROCESSING, DENY_PROCESSING -> R.string.authorizations_processing
                CONFIRM_SUCCESS -> R.string.authorizations_confirmed
                DENY_SUCCESS -> R.string.authorizations_denied
                ERROR -> R.string.authorizations_error
                TIME_OUT -> R.string.authorizations_time_out
                UNAVAILABLE -> R.string.authorizations_unavailable
            }
        }

    val statusDescriptionResId: ResId
        get() {
            return when(this) {
                LOADING, DEFAULT -> R.string.authorizations_loading_description
                CONFIRM_PROCESSING, DENY_PROCESSING -> R.string.authorizations_processing_description
                CONFIRM_SUCCESS -> R.string.authorizations_confirmed_description
                DENY_SUCCESS -> R.string.authorizations_denied_description
                ERROR -> R.string.authorizations_error_description
                TIME_OUT -> R.string.authorizations_time_out_description
                UNAVAILABLE -> R.string.authorizations_unavailable_description
            }
        }

    val showProgress: Boolean
        get() = this === LOADING || this === CONFIRM_PROCESSING || this === DENY_PROCESSING
}
