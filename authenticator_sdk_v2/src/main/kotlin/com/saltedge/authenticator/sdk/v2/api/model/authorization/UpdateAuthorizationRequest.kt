/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.authorization

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.tools.createExpiresAtTime

@Keep
data class UpdateAuthorizationRequest(
    @SerializedName(KEY_DATA) val data: EncryptedBundle,
    @SerializedName(KEY_EXP) val requestExpirationTime: Int = createExpiresAtTime()
)

@Keep
data class UpdateAuthorizationData(
    @SerializedName(KEY_AUTHORIZATION_CODE) val authorizationCode: String,
    @SerializedName(KEY_USER_AUTHORIZATION_TYPE) val userAuthorizationType: String,
    @SerializedName(KEY_GEOLOCATION) val geolocation: String
)
