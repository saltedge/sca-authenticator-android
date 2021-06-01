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
package com.saltedge.authenticator.core.api.model

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import java.io.Serializable

/**
 * Authorization data bundle.
 *
 */
data class DescriptionData(
    val payment: DescriptionPaymentData? = null,
    val text: String? = null,
    val html: String? = null,
    val extra: ExtraData? = null
) : Serializable {
    val hasTextContent: Boolean
        get() = text != null
    val hasExtraContent: Boolean
        get() = extra != null
    val hasHtmlContent: Boolean
        get() = html != null
    val hasPaymentContent: Boolean
        get() = payment != null
}

data class DescriptionPaymentData(
    @SerializedName("payee") var payee: String? = null,
    @SerializedName("amount") var amount: String? = null,
    @SerializedName("account") var account: String? = null,
    @SerializedName("payment_date") var paymentDate: DateTime? = null,
    @SerializedName("fee") var fee: String? = null,
    @SerializedName("exchange_rate") var exchangeRate: String? = null,
    @SerializedName("reference") var reference: String? = null
)

data class ExtraData(
    @SerializedName("action_date") var actionDate: DateTime? = null,
    @SerializedName("device") var device: String? = null,
    @SerializedName("location") var location: String? = null,
    @SerializedName("ip") var ip: String? = null
)
