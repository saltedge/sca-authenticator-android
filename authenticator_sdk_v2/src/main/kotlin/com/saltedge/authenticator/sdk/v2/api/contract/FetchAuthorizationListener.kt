/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData

/**
 * Get SCA Authorization request result
 */
interface FetchAuthorizationListener {
    fun onFetchAuthorizationSuccess(result: AuthorizationResponseData)
    fun onFetchAuthorizationFailed(error: ApiErrorData?)
}
