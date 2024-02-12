/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.model.authorization

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.model.ID
import org.joda.time.DateTime
import java.io.Serializable

/**
 * Plain (not encrypted) authorization model
 * with annotation for GSON parsing
 */
@Keep
data class AuthorizationV2Data(
    @SerializedName(KEY_TITLE) var title: String,
    @SerializedName(KEY_DESCRIPTION) var description: DescriptionData,
    @SerializedName(KEY_AUTHORIZATION_CODE) var authorizationCode: String? = null,
    @SerializedName(KEY_CREATED_AT) var createdAt: DateTime? = null,
    @SerializedName(KEY_EXPIRES_AT) var expiresAt: DateTime,
    var connectionID: ID? = null,
    var authorizationID: ID? = null,
    var status: String? = null,
    var finishedAt: DateTime? = null
) : Serializable

fun AuthorizationV2Data.isNotExpired(): Boolean = expiresAt.isAfterNow
