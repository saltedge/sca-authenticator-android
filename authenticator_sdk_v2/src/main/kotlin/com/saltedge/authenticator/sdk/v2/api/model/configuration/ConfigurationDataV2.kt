/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.configuration

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import java.io.Serializable

data class ConfigurationResponse(@SerializedName(KEY_DATA) var data: ConfigurationDataV2)

/**
 * SCA Service configuration
 */
@Keep
data class ConfigurationDataV2(
    @SerializedName(KEY_SCA_SERVICE_URL) var scaServiceUrl: String,
    @SerializedName(KEY_API_VERSION) var apiVersion: String,
    @SerializedName(KEY_PROVIDER_ID) var providerId: String,
    @SerializedName(KEY_PROVIDER_NAME) var providerName: String,
    @SerializedName(KEY_PROVIDER_LOGO_URL) var providerLogoUrl: String?,
    @SerializedName(KEY_PROVIDER_SUPPORT_EMAIL) var providerSupportEmail: String,
    @SerializedName(KEY_PROVIDER_PUBLIC_KEY) var providerPublicKey: String,
    @SerializedName(KEY_GEOLOCATION_REQUIRED) var geolocationRequired: Boolean
) : Serializable
