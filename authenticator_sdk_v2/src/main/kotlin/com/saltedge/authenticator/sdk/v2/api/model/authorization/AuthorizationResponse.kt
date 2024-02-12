/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.authorization

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import org.joda.time.DateTime
import java.io.Serializable

@Keep
data class AuthorizationsListResponse(@SerializedName(KEY_DATA) var data: List<AuthorizationResponseData>)

@Keep
data class AuthorizationResponse(@SerializedName(KEY_DATA) var data: AuthorizationResponseData)

/**
 * @status - pending confirm_processing deny_processing confirmed denied closed
 */
@Keep
data class AuthorizationResponseData(
    @SerializedName(KEY_ID) var id: String,
    @SerializedName(KEY_CONNECTION_ID) var connectionId: String,
    @SerializedName(KEY_STATUS) var status: String,
    @SerializedName(KEY_IV) var iv: String,
    @SerializedName(KEY_KEY) var key: String,
    @SerializedName(KEY_DATA) var data: String,
    @SerializedName(KEY_FINISHED_AT) var finishedAt: DateTime? = null
) : Serializable

fun AuthorizationResponseData.isValid(): Boolean {
    return connectionId.isNotEmpty()
}
