/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.contract

import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData

/**
 * Get SCA Authorization request result
 */
interface FetchAuthorizationListener {
    fun onFetchAuthorizationResult(result: EncryptedData?, error: ApiErrorData?)
}
