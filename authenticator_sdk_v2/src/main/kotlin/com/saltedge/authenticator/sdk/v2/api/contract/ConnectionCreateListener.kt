/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.core.api.ERROR_CLASS_API_REQUEST
import com.saltedge.authenticator.core.api.model.error.ApiErrorData

/**
 * Create SCA Connection request result
 */
interface ConnectionCreateListener {
    fun onConnectionCreateSuccess(authenticationUrl: String, connectionId: String)
    fun onConnectionCreateFailure(error: ApiErrorData)
}

fun ConnectionCreateListener.error(message: String) {
    this.onConnectionCreateFailure(
        error = ApiErrorData(errorClassName = ERROR_CLASS_API_REQUEST, errorMessage = message)
    )
}
