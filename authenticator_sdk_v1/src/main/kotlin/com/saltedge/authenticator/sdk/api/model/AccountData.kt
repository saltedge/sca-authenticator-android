/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.sdk.api.model

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_ACCOUNT_NUMBER
import com.saltedge.authenticator.core.api.KEY_IBAN
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.core.api.KEY_SORT_CODE
import java.io.Serializable

/**
 * Account POJO model with annotation for GSON parsing
 */
data class AccountData(
    @SerializedName(KEY_NAME) var name: String,
    @SerializedName(KEY_ACCOUNT_NUMBER) var accountNumber: String? = null,
    @SerializedName(KEY_SORT_CODE) var sortCode: String? = null,
    @SerializedName(KEY_IBAN) var iban: String? = null
) : Serializable
