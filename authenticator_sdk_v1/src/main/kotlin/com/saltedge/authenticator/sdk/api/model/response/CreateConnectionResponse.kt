/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_ACCESS_TOKEN
import com.saltedge.authenticator.core.api.KEY_CONNECT_URL
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_ID

@Keep
data class CreateConnectionResponse(
    @SerializedName(KEY_DATA) var data: CreateConnectionResponseData? = null
)

@Keep
data class CreateConnectionResponseData(
    @SerializedName(KEY_CONNECT_URL) var redirectUrl: String? = null,
    @SerializedName(KEY_ID) var connectionId: String? = null,
    @SerializedName(KEY_ACCESS_TOKEN) var accessToken: String? = null
)
