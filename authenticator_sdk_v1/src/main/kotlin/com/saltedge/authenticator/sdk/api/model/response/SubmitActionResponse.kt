/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.core.api.KEY_CONNECTION_ID
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_SUCCESS

@Keep
data class SubmitActionResponse(
    @SerializedName(KEY_DATA) var data: SubmitActionResponseData? = null
)

@Keep
data class SubmitActionResponseData(
    @SerializedName(KEY_SUCCESS) var success: Boolean? = null,
    @SerializedName(KEY_CONNECTION_ID) var connectionId: String? = null,
    @SerializedName(KEY_AUTHORIZATION_ID) var authorizationId: String? = null
)
