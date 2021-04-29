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
package com.saltedge.authenticator.sdk.v2.api.model.connection

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.sdk.v2.api.*
import com.saltedge.authenticator.sdk.v2.api.model.EncryptedBundle
import com.saltedge.authenticator.sdk.v2.config.ClientConfig
import com.saltedge.authenticator.sdk.v2.config.DEFAULT_PLATFORM_NAME

data class CreateConnectionRequest(@SerializedName(KEY_DATA) val data: CreateConnectionRequestData)

data class CreateConnectionRequestData(
    @SerializedName(KEY_PROVIDER_ID) val providerId: String,
    @SerializedName(KEY_RETURN_URL) val returnUrl: String,
    @SerializedName(KEY_PLATFORM) val platform: String = DEFAULT_PLATFORM_NAME,
    @SerializedName(KEY_PUSH_TOKEN) val pushToken: String?,
    @SerializedName(KEY_CONNECT_QUERY) val connectQueryParam: String?,
    @SerializedName(KEY_ENC_RSA_PUBLIC) var encryptedAppRsaPublicKey: EncryptedBundle
)
