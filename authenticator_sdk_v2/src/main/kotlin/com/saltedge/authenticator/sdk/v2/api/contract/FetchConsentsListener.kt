/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.error.ApiErrorData

/**
 * Encrypted Data (Consents) request result and errors
 */
interface FetchConsentsListener {
    fun onFetchConsentsV2Result(result: List<EncryptedData>, errors: List<ApiErrorData>)
}
