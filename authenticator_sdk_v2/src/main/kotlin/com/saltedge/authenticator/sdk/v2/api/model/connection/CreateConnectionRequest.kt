/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.connection

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.api.model.EncryptedBundle

data class CreateConnectionRequest(@SerializedName(KEY_DATA) val data: CreateConnectionRequestData)

data class CreateConnectionRequestData(
    @SerializedName(KEY_PROVIDER_ID) val providerId: String,
    @SerializedName(KEY_RETURN_URL) val returnUrl: String,
    @SerializedName(KEY_PLATFORM) val platform: String = DEFAULT_PLATFORM_NAME,
    @SerializedName(KEY_PUSH_TOKEN) val pushToken: String?,
    @SerializedName(KEY_CONNECT_QUERY) val connectQueryParam: String?,
    @SerializedName(KEY_ENC_RSA_PUBLIC) var encryptedAppRsaPublicKey: EncryptedBundle
)
