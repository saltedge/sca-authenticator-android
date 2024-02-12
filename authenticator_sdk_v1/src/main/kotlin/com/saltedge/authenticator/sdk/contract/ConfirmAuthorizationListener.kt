/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponseData

/**
 * Confirm SCA Authorization request result
 */
interface ConfirmAuthorizationListener {
    fun onConfirmDenySuccess(result: ConfirmDenyResponseData, connectionID: ID)
    fun onConfirmDenyFailure(error: ApiErrorData, connectionID: ID, authorizationID: ID)
}
