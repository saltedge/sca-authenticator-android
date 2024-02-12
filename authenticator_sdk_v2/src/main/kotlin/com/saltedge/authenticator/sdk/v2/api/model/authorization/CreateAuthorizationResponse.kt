/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.authorization

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.core.api.KEY_CONNECTION_ID
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.model.ID

@Keep
data class CreateAuthorizationResponse(@SerializedName(KEY_DATA) var data: CreateAuthorizationResponseData)

@Keep
data class CreateAuthorizationResponseData(
    @SerializedName(KEY_CONNECTION_ID) var connectionID: ID,
    @SerializedName(KEY_AUTHORIZATION_ID) var authorizationID: ID
)
