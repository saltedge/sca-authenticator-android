/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData

/**
 * Encrypted Data (Authorizations) request result
 */
interface FetchAuthorizationsListener {
    fun onFetchAuthorizationsResult(
        result: List<AuthorizationResponseData>,
        errors: List<ApiErrorData>
    )
}
