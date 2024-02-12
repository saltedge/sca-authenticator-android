/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.core.api.ERROR_CLASS_API_REQUEST
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationResponseData

/**
 * Deny SCA Authorization request result
 */
interface AuthorizationDenyListener {
    fun onAuthorizationDenySuccess(result: UpdateAuthorizationResponseData, connectionID: ID)
    fun onAuthorizationDenyFailure(error: ApiErrorData, connectionID: ID, authorizationID: ID)
}

fun AuthorizationDenyListener.error(message: String, connectionID: ID, authorizationID: ID) {
    this.onAuthorizationDenyFailure(
        error = ApiErrorData(errorClassName = ERROR_CLASS_API_REQUEST, errorMessage = message),
        authorizationID = authorizationID,
        connectionID = connectionID
    )
}
