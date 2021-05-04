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
import org.joda.time.DateTime
import java.io.Serializable

/**
 * Plain (not encrypted) authorization model
 * with annotation for GSON parsing
 */
data class AuthorizationData(
    @SerializedName(KEY_TITLE) var title: String,
    @SerializedName(KEY_DESCRIPTION) var description: DescriptionData,
    @SerializedName(KEY_AUTHORIZATION_CODE) var authorizationCode: String? = null,
    @SerializedName(KEY_CREATED_AT) var createdAt: DateTime? = null,
    @SerializedName(KEY_EXPIRES_AT) var expiresAt: DateTime
) : Serializable

fun AuthorizationData.isNotExpired(): Boolean = expiresAt.isAfterNow

data class DescriptionData(
    val payment: DescriptionPaymentData? = null,
    val text: DescriptionTextData? = null,
    val html: DescriptionHTMLData? = null,
    val extra: ExtraData? = null
)

data class DescriptionPaymentData(
    @SerializedName("payee") var payee: String? = null,
    @SerializedName("amount") var amount: String? = null,
    @SerializedName("account") var account: String? = null,
    @SerializedName("payment_date") var paymentDate: DateTime? = null,
    @SerializedName("reference") var reference: String? = null,
    @SerializedName("fee") var fee: String? = null,
    @SerializedName("exchange_rate") var exchangeRate: String? = null
)

data class DescriptionTextData(@SerializedName("text") var text: String? = null)

data class DescriptionHTMLData(@SerializedName("html") var html: String? = null)

data class ExtraData(
    @SerializedName("action_date") var actionDate: String? = null,
    @SerializedName("device") var device: String? = null,
    @SerializedName("location") var location: String? = null,
    @SerializedName("ip") var ip: String? = null
)
