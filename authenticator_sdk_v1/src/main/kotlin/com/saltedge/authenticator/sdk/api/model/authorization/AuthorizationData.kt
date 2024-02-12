/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.model.authorization

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import org.joda.time.DateTime
import java.io.Serializable

/**
 * Plain (not encrypted) authorization model
 * with annotation for GSON parsing
 */
@Keep
data class AuthorizationData(
    @SerializedName(KEY_ID) var id: String,
    @SerializedName(KEY_CREATED_AT) var createdAt: DateTime? = null,
    @SerializedName(KEY_UPDATED_AT) var updatedAt: DateTime? = null,
    @SerializedName(KEY_TITLE) var title: String,
    @SerializedName(KEY_DESCRIPTION) var description: String,
    @SerializedName(KEY_AUTHORIZATION_CODE) var authorizationCode: String? = null,
    @SerializedName(KEY_CONNECTION_ID) var connectionId: String,
    @SerializedName(KEY_EXPIRES_AT) var expiresAt: DateTime
) : Serializable
