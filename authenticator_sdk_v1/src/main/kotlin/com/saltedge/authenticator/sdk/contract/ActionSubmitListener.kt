/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.api.model.response.SubmitActionResponseData

interface ActionSubmitListener {
    fun onActionInitSuccess(response: SubmitActionResponseData)
    fun onActionInitFailure(error: ApiErrorData)
}
