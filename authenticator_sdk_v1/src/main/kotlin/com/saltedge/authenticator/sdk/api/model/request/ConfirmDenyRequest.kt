/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_AUTHORIZATION_CODE
import com.saltedge.authenticator.core.api.KEY_CONFIRM
import com.saltedge.authenticator.core.api.KEY_DATA

@Keep
data class ConfirmDenyRequest(@SerializedName(KEY_DATA) val data: ConfirmDenyRequestData)

@Keep
data class ConfirmDenyRequestData(
    @SerializedName(KEY_CONFIRM) val confirm: Boolean,
    @SerializedName(KEY_AUTHORIZATION_CODE) val authorizationCode: String?
)
