/*
 * Copyright (c) 2024 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID


interface ConnectionUpdateListener {
    fun onUpdatePushTokenSuccess(connectionID: ID)
    fun onUpdatePushTokenFailed(error: ApiErrorData)
}
