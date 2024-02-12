/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import java.io.Serializable

/**
 * Authorization data bundle.
 *
 */
@Keep
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

@Keep
data class DescriptionPaymentData(
    @SerializedName("payee") var payee: String? = null,
    @SerializedName("amount") var amount: String? = null,
    @SerializedName("account") var account: String? = null,
    @SerializedName("payment_date") var paymentDate: DateTime? = null,
    @SerializedName("fee") var fee: String? = null,
    @SerializedName("exchange_rate") var exchangeRate: String? = null,
    @SerializedName("reference") var reference: String? = null
)

@Keep
data class ExtraData(
    @SerializedName("action_date") var actionDate: DateTime? = null,
    @SerializedName("device") var device: String? = null,
    @SerializedName("location") var location: String? = null,
    @SerializedName("ip") var ip: String? = null
)
