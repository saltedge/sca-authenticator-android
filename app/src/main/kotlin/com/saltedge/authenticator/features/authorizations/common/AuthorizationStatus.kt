/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.authorizations.common

import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_STATUS_CLOSED
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

    fun isFinal(): Boolean {
        return this == CONFIRMED
            || this == DENIED
            || this == ERROR
            || this == TIME_OUT
            || this == UNAVAILABLE
    }

    fun isProcessing(): Boolean {
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
        AuthorizationStatus.valueOf(this.uppercase(Locale.US))
    } catch (e: Exception) {
        null
    }
}
fun AuthorizationStatus.computeConfirmedStatus(): AuthorizationStatus {
    return when (this) {
        AuthorizationStatus.CONFIRM_PROCESSING -> AuthorizationStatus.CONFIRMED
        AuthorizationStatus.DENY_PROCESSING -> AuthorizationStatus.DENIED
        else -> AuthorizationStatus.ERROR
    }
}

/**
 * Check that STRING status is equal to final AuthorizationStatus
 *
 * @return true if is final status
 */
val String.isFinalStatus: Boolean
    get() = this.toAuthorizationStatus()?.isFinal() ?: false


/**
 * Check that STRING status is equal to closed authorization status
 *
 * @return true if is closed status
 */
val String.isClosed: Boolean
    get() = this == KEY_STATUS_CLOSED

