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
import com.saltedge.authenticator.sdk.v2.api.*
import com.saltedge.authenticator.sdk.v2.api.model.EncryptedBundle
import com.saltedge.authenticator.sdk.v2.api.retrofit.createExpiresAtTime

data class UpdateAuthorizationRequest(
    @SerializedName(KEY_DATA) val data: EncryptedBundle,
    @SerializedName(KEY_EXP) val requestExpirationTime: Int = createExpiresAtTime()
)

data class UpdateAuthorizationData(
    @SerializedName(KEY_AUTHORIZATION_CODE) val authorizationCode: String,
    @SerializedName(KEY_USER_AUTHORIZATION_TYPE) val userAuthorizationType: String,
    @SerializedName(KEY_GEOLOCATION) val geolocation: String
)
