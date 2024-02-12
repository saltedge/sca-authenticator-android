/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.sdk.config.ApiV1Config

@Keep
data class CreateConnectionRequest(@SerializedName(KEY_DATA) val data: CreateConnectionRequestData)

@Keep
data class CreateConnectionRequestData(
    @SerializedName(KEY_PUBLIC_KEY) val publicKey: String,
    @SerializedName(KEY_RETURN_URL) val returnUrl: String = ApiV1Config.authenticationReturnUrl,
    @SerializedName(KEY_PLATFORM) val platform: String = DEFAULT_PLATFORM_NAME,
    @SerializedName(KEY_PUSH_TOKEN) val pushToken: String,
    @SerializedName(KEY_PROVIDER_CODE) val providerCode: String,
    @SerializedName(KEY_CONNECT_QUERY) val connectQueryParam: String?
)
