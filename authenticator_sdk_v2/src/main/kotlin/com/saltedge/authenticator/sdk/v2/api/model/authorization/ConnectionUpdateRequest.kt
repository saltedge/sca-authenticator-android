/*
 * Copyright (c) 2024 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.authorization

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_EXP
import com.saltedge.authenticator.core.api.KEY_PUSH_TOKEN
import com.saltedge.authenticator.core.tools.createExpiresAtTime
import java.io.Serializable

@Keep
data class ConnectionUpdateRequest(
    @SerializedName(KEY_DATA) val data: ConnectionUpdateRequestData,
    @SerializedName(KEY_EXP) val requestExpirationTime: Int = createExpiresAtTime()
) : Serializable

@Keep
data class ConnectionUpdateRequestData(
    @SerializedName(KEY_PUSH_TOKEN) val pushToken: String
) : Serializable
