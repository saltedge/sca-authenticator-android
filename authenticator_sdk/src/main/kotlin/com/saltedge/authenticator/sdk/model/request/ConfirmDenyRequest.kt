/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.sdk.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.sdk.constants.KEY_AUTHORIZATION_CODE
import com.saltedge.authenticator.sdk.constants.KEY_CONFIRM
import com.saltedge.authenticator.sdk.constants.KEY_DATA

@Keep
data class ConfirmDenyRequest(@SerializedName(KEY_DATA) val data: ConfirmDenyRequestData)

@Keep
data class ConfirmDenyRequestData(
    @SerializedName(KEY_CONFIRM) val confirm: Boolean,
    @SerializedName(KEY_AUTHORIZATION_CODE) val authorizationCode: String?
)
