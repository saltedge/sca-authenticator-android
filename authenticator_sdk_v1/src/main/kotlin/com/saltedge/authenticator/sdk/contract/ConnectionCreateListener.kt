/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.api.model.response.CreateConnectionResponseData

/**
 * Create SCA Connection request result
 */
interface ConnectionCreateListener {
    fun onConnectionCreateSuccess(response: CreateConnectionResponseData)
    fun onConnectionCreateFailure(error: ApiErrorData)
}
