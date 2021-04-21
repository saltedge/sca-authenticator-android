package com.saltedge.authenticator.sdk.v2.api.model.request

import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.sdk.v2.config.KEY_DATA
import com.saltedge.authenticator.sdk.v2.config.KEY_EXP

data class RevokeConnectionRequest(
    @SerializedName(KEY_DATA) val data: Any = Any(),
    @SerializedName(KEY_EXP) val exp: Int
)
