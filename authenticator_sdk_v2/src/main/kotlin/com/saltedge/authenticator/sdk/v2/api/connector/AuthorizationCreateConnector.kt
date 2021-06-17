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
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationCreateListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.CreateAuthorizationRequest
import com.saltedge.authenticator.sdk.v2.api.model.authorization.CreateAuthorizationRequestData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.CreateAuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.addSignatureHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.authorizationsCreatePath
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import retrofit2.Call

internal class AuthorizationCreateConnector(
    private val apiInterface: ApiInterface,
    var callback: AuthorizationCreateListener?
) : ApiResponseInterceptor<CreateAuthorizationResponse>() {

    fun createAuthorizationForAction(richConnection: RichConnection, actionID: ID) {
        val payload = CreateAuthorizationRequest(CreateAuthorizationRequestData(
            actionID = actionID,
            providerID = richConnection.connection.code,
            connectionID = richConnection.connection.id
        ))
        apiInterface.createAuthorizationForAction(
            requestUrl = richConnection.connection.connectUrl.authorizationsCreatePath(),
            headersMap = headers(richConnection, payload),
            requestBody = payload
        ).enqueue(this)
    }

    private fun headers(richConnection: RichConnection, payload: CreateAuthorizationRequest): Map<String, String> {
        return createAccessTokenHeader(richConnection.connection.accessToken).addSignatureHeader(
            richConnection.private,
            payload.data,
            payload.requestExpirationTime
        )
    }

    override fun onSuccessResponse(
        call: Call<CreateAuthorizationResponse>,
        response: CreateAuthorizationResponse
    ) {
        callback?.onAuthorizationCreateSuccess(
            connectionID = response.data.connectionID,
            authorizationID = response.data.authorizationID
        )
    }

    override fun onFailureResponse(call: Call<CreateAuthorizationResponse>, error: ApiErrorData) {
        callback?.onAuthorizationCreateFailure(error)
    }
}
