/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_DATA
import com.saltedge.authenticator.core.api.KEY_EXP
import com.saltedge.authenticator.core.tools.createExpiresAtTime
import java.io.Serializable

@Keep
data class EmptyRequest(
    @SerializedName(KEY_DATA) val data: Any = Any(),
    @SerializedName(KEY_EXP) val requestExpirationTime: Int = createExpiresAtTime()
) : Serializable
