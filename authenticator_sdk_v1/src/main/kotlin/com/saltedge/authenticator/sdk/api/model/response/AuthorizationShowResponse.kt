/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.model.EncryptedData

@Keep
data class AuthorizationShowResponse(@SerializedName(KEY_DATA) var data: EncryptedData? = null)
