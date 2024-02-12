/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.authorization

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_ID
import com.saltedge.authenticator.core.api.KEY_STATUS

@Keep
data class UpdateAuthorizationResponse(@SerializedName(KEY_DATA) var data: UpdateAuthorizationResponseData)

@Keep
data class UpdateAuthorizationResponseData(
    @SerializedName(KEY_ID) var authorizationID: String,
    @SerializedName(KEY_STATUS) var status: String
)
