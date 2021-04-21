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
import com.saltedge.authenticator.sdk.v2.config.*
import java.io.Serializable

data class ProviderConfigurationResponse(@SerializedName(KEY_DATA) var data: ProviderConfigurationData)

/**
 * SCA Service configuration
 */
data class ProviderConfigurationData(
    @SerializedName(KEY_SCA_SERVICE_URL) var scaServiceUrl: String,
    @SerializedName(KEY_API_VERSION) var apiVersion: String,
    @SerializedName(KEY_PROVIDER_ID) var providerId: String,
    @SerializedName(KEY_NAME) var name: String,
    @SerializedName(KEY_LOGO_URL) var logoUrl: String,
    @SerializedName(KEY_SUPPORT_EMAIL) var supportEmail: String,
    @SerializedName(DH_PUBLIC_KEY) var dhPublicKey: String,
    @SerializedName(KEY_GEOLOCATION_REQUIRED) var geolocationRequired: Boolean
) : Serializable
