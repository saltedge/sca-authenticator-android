/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID

/**
 * Create SCA Authorization for an Action request result
 */
interface AuthorizationCreateListener {
    fun onAuthorizationCreateSuccess(connectionID: ID, authorizationID: ID)
    fun onAuthorizationCreateFailure(error: ApiErrorData)
}
