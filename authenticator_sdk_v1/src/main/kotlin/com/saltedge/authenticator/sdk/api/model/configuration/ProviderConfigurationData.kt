/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.configuration

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import java.io.Serializable

/**
 * Model of provider configuration
 */
@Keep
data class ProviderConfigurationData(
    @SerializedName(KEY_CONNECT_URL) var connectUrl: String,
    @SerializedName(KEY_CODE) var code: String,
    @SerializedName(KEY_NAME) var name: String,
    @SerializedName(KEY_LOGO_URL) var logoUrl: String?,
    @SerializedName(KEY_VERSION) var version: String,
    @SerializedName(KEY_SUPPORT_EMAIL) var supportEmail: String?,
    @SerializedName(KEY_CONSENT_MANAGEMENT) var consentManagementSupported: Boolean? = false,
    @SerializedName(KEY_GEOLOCATION_REQUIRED) var geolocationRequired: Boolean?
) : Serializable
