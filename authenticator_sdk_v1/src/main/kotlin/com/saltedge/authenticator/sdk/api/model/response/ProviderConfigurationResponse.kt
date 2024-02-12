/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.sdk.api.model.configuration.ProviderConfigurationData

@Keep
data class ProviderConfigurationResponse(@SerializedName(KEY_DATA) var data: ProviderConfigurationData)
