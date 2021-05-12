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
package com.saltedge.authenticator.sdk.v2.api.model.authorization

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import java.io.Serializable

data class AuthorizationsListResponse(@SerializedName(KEY_DATA) var data: List<AuthorizationResponseData>)

data class AuthorizationResponse(@SerializedName(KEY_DATA) var data: AuthorizationResponseData)

data class AuthorizationResponseData(
    @SerializedName(KEY_ID) var id: String,
    @SerializedName(KEY_CONNECTION_ID) var connectionId: String,
    @SerializedName(KEY_STATUS) var status: String,
    @SerializedName(KEY_IV) var iv: String,
    @SerializedName(KEY_KEY) var key: String,
    @SerializedName(KEY_DATA) var data: String
) : Serializable

fun AuthorizationResponseData.isValid(): Boolean {
    return key.isNotEmpty() && iv.isNotEmpty() && data.isNotEmpty() && connectionId.isNotEmpty()
}
