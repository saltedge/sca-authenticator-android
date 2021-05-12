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
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.model.AuthorizationID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.model.authorization.ConfirmDenyResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationRequest
import com.saltedge.authenticator.sdk.v2.api.retrofit.addSignatureHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.authorizationsConfirmPath
import com.saltedge.authenticator.sdk.v2.api.retrofit.authorizationsDenyPath
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader

internal abstract class AuthorizationUpdateBaseConnector(
    val authorizationId: AuthorizationID,
    val isConfirmRequest: Boolean
) : ApiResponseInterceptor<ConfirmDenyResponse>() {

    protected fun url(richConnection: RichConnection): String {
        val baseUrl = richConnection.connection.connectUrl
        return if (isConfirmRequest) baseUrl.authorizationsConfirmPath(authorizationId)
        else baseUrl.authorizationsDenyPath(authorizationId)
    }

    protected fun headers(richConnection: RichConnection, request: UpdateAuthorizationRequest): Map<String, String> {
        return createAccessTokenHeader(richConnection.connection.accessToken).addSignatureHeader(
            richConnection.private,
            request.data,
            request.requestExpirationTime
        )
    }

    protected fun body(encryptedPayload: EncryptedBundle) = UpdateAuthorizationRequest(data = encryptedPayload)
}
