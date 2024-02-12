/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.consent

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_ID

@Keep
data class ConsentRevokeResponse(
    @SerializedName(KEY_DATA) var data: ConsentRevokeResponseData? = null
)

@Keep
data class ConsentRevokeResponseData(
    @SerializedName(KEY_ID) var consentId: String? = null
)
