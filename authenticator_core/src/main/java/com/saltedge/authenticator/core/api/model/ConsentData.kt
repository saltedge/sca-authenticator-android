/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.model.ConsentType
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.toConsentType
import org.joda.time.DateTime
import java.io.Serializable

/**
 * Consent POJO model with annotation for GSON parsing
 */
@Keep
data class ConsentData(
    @SerializedName(KEY_ID) var id: ID,
    @SerializedName(KEY_TPP_NAME) var tppName: String,
    @SerializedName(KEY_CONSENT_TYPE) var consentTypeString: String,
    @SerializedName(KEY_ACCOUNTS) var accounts: List<AccountData>,
    @SerializedName(KEY_SHARED_DATA) var sharedData: ConsentSharedData?,
    @SerializedName(KEY_CREATED_AT) var createdAt: DateTime,
    @SerializedName(KEY_EXPIRES_AT) var expiresAt: DateTime,
    @SerializedName(KEY_USER_ID) var userId: ID? = null,
    //Extra data
    var connectionId: ID? = null,
    var connectionGuid: GUID = "",
) : Serializable {
    val consentType: ConsentType?
        get() = consentTypeString.toConsentType()
}

data class AccountData(
    @SerializedName(KEY_NAME) var name: String,
    @SerializedName(KEY_IBAN) var iban: String? = null,
    @SerializedName(KEY_ACCOUNT_NUMBER) var accountNumber: String? = null,
    @SerializedName(KEY_SORT_CODE) var sortCode: String? = null
) : Serializable

data class ConsentSharedData(
    @SerializedName(KEY_BALANCE) var balance: Boolean?,
    @SerializedName(KEY_TRANSACTIONS) var transactions: Boolean?
) : Serializable
