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

import com.saltedge.authenticator.sdk.v2.api.ApiResponseInterceptor
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationConfirmListener
import com.saltedge.authenticator.sdk.v2.api.model.AuthorizationID
import com.saltedge.authenticator.sdk.v2.api.model.authorization.ConfirmDenyResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationRequest
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationRequestData
import com.saltedge.authenticator.sdk.v2.api.model.connection.RichConnection
import com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.addSignatureHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.authorizationsConfirmPath
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import retrofit2.Call

internal class AuthorizationConfirmConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: AuthorizationConfirmListener?
) : ApiResponseInterceptor<ConfirmDenyResponse>() {

    private var authorizationId: AuthorizationID = ""

    fun confirmAuthorization(
        richConnection: RichConnection,
        authorizationId: String,
        encryptedPayload: String,
    ) {
        this.authorizationId = authorizationId
        val request = UpdateAuthorizationRequest(data = UpdateAuthorizationRequestData(encryptedPayload))
        val headers = createAccessTokenHeader(richConnection.connection.accessToken)
            .addSignatureHeader(
                richConnection.rsaPrivate,
                request.data,
                request.requestExpirationTime
            )
        apiInterface.confirmAuthorization(
            requestUrl = richConnection.connection.connectUrl.authorizationsConfirmPath(authorizationId),
            headersMap = headers,
            requestBody = request
        ).enqueue(this)
    }

    override fun onSuccessResponse(
        call: Call<ConfirmDenyResponse>,
        response: ConfirmDenyResponse
    ) {
        val data = response.data
        resultCallback?.onAuthorizationConfirmSuccess(result = data)
    }

    override fun onFailureResponse(call: Call<ConfirmDenyResponse>, error: ApiErrorData) {
        resultCallback?.onAuthorizationConfirmFailure(
            error = error,
            authorizationID = this.authorizationId
        )
    }
}
