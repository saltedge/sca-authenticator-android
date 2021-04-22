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
package com.saltedge.authenticator.sdk.v2.api.model

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.sdk.v2.config.*
import java.io.Serializable

/**
 * Encrypted model
 * with annotation for GSON parsing
 * contains:
 * - encrypted data (any data) with symmetric algorithm,
 * - algorithm code (now is supported only AES-CBC-256),
 * - encryption key (KEY), encrypted with asymmetric RSA key attached to specific connection (connectionId)
 * - initialization vector (IV), encrypted with asymmetric RSA key attached to specific connection (connectionId)
 */
data class EncryptedData(
    @SerializedName(KEY_ID) var id: String,
    @SerializedName(KEY_CONNECTION_ID) var connectionId: String,
    @SerializedName(KEY_STATUS) var status: String,
    @SerializedName(KEY_IV) var iv: String,
    @SerializedName(KEY_KEY) var key: String,
    @SerializedName(KEY_DATA) var data: String
) : Serializable
