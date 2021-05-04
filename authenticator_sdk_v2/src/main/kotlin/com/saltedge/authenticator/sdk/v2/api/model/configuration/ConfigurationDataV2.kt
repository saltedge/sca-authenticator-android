/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.sdk.v2.api.model.configuration

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.sdk.v2.api.*
import java.io.Serializable

data class ConfigurationResponse(@SerializedName(KEY_DATA) var data: ConfigurationDataV2)

/**
 * SCA Service configuration
 */
data class ConfigurationDataV2(
    @SerializedName(KEY_SCA_SERVICE_URL) var scaServiceUrl: String,
    @SerializedName(KEY_API_VERSION) var apiVersion: String,
    @SerializedName(KEY_PROVIDER_ID) var providerId: String,
    @SerializedName(KEY_PROVIDER_NAME) var providerName: String,
    @SerializedName(KEY_PROVIDER_LOGO_URL) var providerLogoUrl: String,
    @SerializedName(KEY_PROVIDER_SUPPORT_EMAIL) var providerSupportEmail: String,
    @SerializedName(KEY_PROVIDER_PUBLIC_KEY) var providerPublicKey: String,
    @SerializedName(KEY_GEOLOCATION_REQUIRED) var geolocationRequired: Boolean
) : Serializable
