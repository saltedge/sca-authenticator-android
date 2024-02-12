/*
 * Copyright (c) 2019 Salt Edge Inc.
 */

package com.saltedge.authenticator.sdk.api.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_ID
import com.saltedge.authenticator.core.api.KEY_SUCCESS

@Keep
data class ConfirmDenyResponse(@SerializedName(KEY_DATA) var data: ConfirmDenyResponseData? = null)

@Keep
data class ConfirmDenyResponseData(
    @SerializedName(KEY_SUCCESS) var success: Boolean? = null,
    @SerializedName(KEY_ID) var authorizationID: String? = null
)

fun ConfirmDenyResponseData.isValid(): Boolean {
    return this.success != null && this.authorizationID?.isNotEmpty() == true
}
