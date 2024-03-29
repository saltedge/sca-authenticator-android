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
package com.saltedge.authenticator.sdk.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.sdk.constants.*
import org.joda.time.DateTime
import java.io.Serializable

/**
 * Consent POJO model with annotation for GSON parsing
 */
@Keep
data class ConsentData(
    @SerializedName(KEY_ID) var id: String,
    @SerializedName(KEY_USER_ID) var userId: String,
    @SerializedName(KEY_CREATED_AT) var createdAt: DateTime,
    @SerializedName(KEY_EXPIRES_AT) var expiresAt: DateTime,
    @SerializedName(KEY_TPP_NAME) var tppName: String,
    @SerializedName(KEY_CONSENT_TYPE) var consentTypeString: String,
    @SerializedName(KEY_ACCOUNTS) var accounts: List<AccountData>,
    @SerializedName(KEY_SHARED_DATA) var sharedData: ConsentSharedData?,
    @SerializedName(KEY_CONNECTION_ID) var connectionId: String? = null
) : Serializable {
    val consentType: ConsentType?
        get() = consentTypeString.toConsentType()
}
