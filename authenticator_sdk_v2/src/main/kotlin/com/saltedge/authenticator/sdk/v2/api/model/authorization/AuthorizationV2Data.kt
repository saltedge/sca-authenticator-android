/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.sdk.v2.api.model.authorization

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
data class AuthorizationV2Data(
    @SerializedName(KEY_TITLE) var title: String,
    @SerializedName(KEY_DESCRIPTION) var description: DescriptionData,
    @SerializedName(KEY_AUTHORIZATION_CODE) var authorizationCode: String? = null,
    @SerializedName(KEY_CREATED_AT) var createdAt: DateTime? = null,
    @SerializedName(KEY_EXPIRES_AT) var expiresAt: DateTime,
    var connectionId: ID? = null,
    var authorizationId: ID? = null,
    var status: String? = null,
) : Serializable

fun AuthorizationV2Data.isNotExpired(): Boolean = expiresAt.isAfterNow
