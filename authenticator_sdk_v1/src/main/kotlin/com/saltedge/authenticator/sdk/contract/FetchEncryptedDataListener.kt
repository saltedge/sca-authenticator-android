/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.contract

import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData

/**
 * Encrypted Data (Authorizations, Consents) request result
 */
interface FetchEncryptedDataListener {
    fun onFetchEncryptedDataResult(result: List<EncryptedData>, errors: List<ApiErrorData>)
}
