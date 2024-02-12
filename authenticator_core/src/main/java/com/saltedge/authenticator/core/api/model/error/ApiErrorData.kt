/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.api.model.error

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.KEY_ERROR_CLASS
import com.saltedge.authenticator.core.api.KEY_ERROR_MESSAGE
import com.saltedge.authenticator.core.model.Token
import java.io.Serializable

/**
 * API Error model
 * with annotation for GSON parsing
 * contains accessToken field which is used for further connection invalidation
 */
@Keep
data class ApiErrorData(
    @SerializedName(KEY_ERROR_CLASS) var errorClassName: String,
    @SerializedName(KEY_ERROR_MESSAGE) var errorMessage: String = "",
    var accessToken: Token? = null
) : Serializable {

    init {
        this.errorClassName = errorClassName.replace("""@\S+""".toRegex(), "")
        this.errorMessage = errorMessage.replace("""@\S+""".toRegex(), "")
    }
}
