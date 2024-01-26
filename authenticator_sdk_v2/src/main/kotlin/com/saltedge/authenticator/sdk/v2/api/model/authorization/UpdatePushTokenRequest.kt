/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2024 Salt Edge Inc.
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

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_EXP
import com.saltedge.authenticator.core.api.KEY_PUSH_TOKEN
import com.saltedge.authenticator.core.tools.createExpiresAtTime
import java.io.Serializable

@Keep
data class UpdatePushTokenRequest(
    @SerializedName(KEY_DATA) val data: UpdatePushTokenRequestData,
    @SerializedName(KEY_EXP) val requestExpirationTime: Int = createExpiresAtTime()
) : Serializable

@Keep
data class UpdatePushTokenRequestData(
    @SerializedName(KEY_PUSH_TOKEN) val pushToken: String
) : Serializable
