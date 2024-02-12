/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.connection

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_AUTHENTICATION_URL
import com.saltedge.authenticator.core.api.KEY_CONNECTION_ID
import com.saltedge.authenticator.core.api.KEY_DATA

@Keep
data class CreateConnectionResponse(@SerializedName(KEY_DATA) var data: CreateConnectionResponseData)

@Keep
data class CreateConnectionResponseData(
    @SerializedName(KEY_AUTHENTICATION_URL) var authenticationUrl: String,
    @SerializedName(KEY_CONNECTION_ID) var connectionId: String
)
