/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.api.model.configuration.ProviderConfigurationData

/**
 * Provider configuration request result
 */
interface FetchProviderConfigurationListener {
    fun onFetchProviderConfigurationSuccess(result: ProviderConfigurationData)
    fun onFetchProviderConfigurationFailure(error: ApiErrorData)
}
