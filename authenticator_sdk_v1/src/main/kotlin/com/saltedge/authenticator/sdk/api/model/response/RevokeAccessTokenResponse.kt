/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_ACCESS_TOKEN
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_SUCCESS

@Keep
data class RevokeAccessTokenResponse(
    @SerializedName(KEY_DATA) var data: RevokeAccessTokenResponseData? = null
)

@Keep
data class RevokeAccessTokenResponseData(
    @SerializedName(KEY_SUCCESS) var success: Boolean? = null,
    @SerializedName(KEY_ACCESS_TOKEN) var accessToken: String? = null
)
