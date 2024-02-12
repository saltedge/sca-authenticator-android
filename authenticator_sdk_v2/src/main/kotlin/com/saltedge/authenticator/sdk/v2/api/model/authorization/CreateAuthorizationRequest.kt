/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.authorization

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.tools.createExpiresAtTime
import java.io.Serializable

@Keep
data class CreateAuthorizationRequest(
    @SerializedName(KEY_DATA) val data: CreateAuthorizationRequestData,
    @SerializedName(KEY_EXP) val requestExpirationTime: Int = createExpiresAtTime()
) : Serializable

@Keep
data class CreateAuthorizationRequestData(
    @SerializedName(KEY_PROVIDER_ID) val providerID: ID,
    @SerializedName(KEY_CONNECTION_ID) val connectionID: ID,
    @SerializedName(KEY_ACTION_ID) val actionID: ID
) : Serializable
