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

package com.saltedge.authenticator.sdk.v2.api.model.response

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.sdk.v2.config.KEY_DATA
import com.saltedge.authenticator.sdk.v2.config.KEY_ID
import com.saltedge.authenticator.sdk.v2.config.KEY_STATUS
import com.saltedge.authenticator.sdk.v2.config.KEY_SUCCESS

data class ConfirmDenyResponse(@SerializedName(KEY_DATA) var data: ConfirmDenyResponseData? = null)

data class ConfirmDenyResponseData(
    @SerializedName(KEY_ID) var authorizationID: String,
    @SerializedName(KEY_STATUS) var status: String
)
