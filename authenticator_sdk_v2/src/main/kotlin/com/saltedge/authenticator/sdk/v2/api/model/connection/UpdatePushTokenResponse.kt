/*
 * Copyright (c) 2024 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.connection

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_CONNECTION_ID
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.model.ID

@Keep
data class UpdatePushTokenResponse(@SerializedName(KEY_DATA) var data: UpdatePushTokenResponseData)

@Keep
data class UpdatePushTokenResponseData(
    @SerializedName(KEY_CONNECTION_ID) var connectionID: ID
)
