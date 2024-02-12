/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.connection

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_CONNECTION_ID
import com.saltedge.authenticator.core.api.KEY_DATA

@Keep
data class RevokeConnectionResponse(@SerializedName(KEY_DATA) var data: RevokeConnectionResponseData)

@Keep
data class RevokeConnectionResponseData(
    @SerializedName(KEY_CONNECTION_ID) var revokedConnectionId: String
)
