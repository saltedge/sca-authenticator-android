/*
 * Copyright (c) 2022 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.contract

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationDataV2

interface ShowConnectionConfigurationListener {
    fun onShowConnectionConfigurationSuccess(result: ConfigurationDataV2)
    fun onShowConnectionConfigurationFailed(error: ApiErrorData)
}
