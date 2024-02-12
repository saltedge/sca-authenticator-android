/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationDataV2
import com.saltedge.authenticator.core.api.model.error.ApiErrorData

/**
 * Provider configuration request result
 */
interface FetchConfigurationListener {
    fun onFetchProviderConfigurationSuccess(result: ConfigurationDataV2)
    fun onFetchProviderConfigurationFailure(error: ApiErrorData)
}
