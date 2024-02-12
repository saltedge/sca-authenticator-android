/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID

/**
 * Revoke SCA Connection request result
 */
interface ConnectionsV2RevokeListener {
    fun onConnectionsV2RevokeResult(revokedIDs: List<ID>, apiErrors: List<ApiErrorData>)
}
