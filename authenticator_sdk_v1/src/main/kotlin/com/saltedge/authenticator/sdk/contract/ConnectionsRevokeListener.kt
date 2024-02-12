/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.Token

/**
 * Revoke SCA Connection request result
 */
interface ConnectionsRevokeListener {
    fun onConnectionsRevokeResult(revokedTokens: List<Token>, apiErrors: List<ApiErrorData>)
}
