/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_DATA

@Keep
data class EncryptedListResponse(@SerializedName(KEY_DATA) var data: List<EncryptedData>)
