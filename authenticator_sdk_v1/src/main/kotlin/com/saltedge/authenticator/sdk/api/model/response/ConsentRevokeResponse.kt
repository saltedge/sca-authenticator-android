/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_CONSENT_ID
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_SUCCESS

@Keep
data class ConsentRevokeResponse(
    @SerializedName(KEY_DATA) var data: ConsentRevokeResponseData? = null
)

@Keep
data class ConsentRevokeResponseData(
    @SerializedName(KEY_SUCCESS) var success: Boolean? = null,
    @SerializedName(KEY_CONSENT_ID) var consentId: String? = null
)
