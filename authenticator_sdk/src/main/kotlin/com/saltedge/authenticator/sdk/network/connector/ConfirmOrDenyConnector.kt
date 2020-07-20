/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.sdk.network.connector

import com.saltedge.authenticator.sdk.constants.API_AUTHORIZATIONS
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_PUT
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.model.*
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.error.createInvalidResponseError
import com.saltedge.authenticator.sdk.model.request.ConfirmDenyRequestData
import com.saltedge.authenticator.sdk.model.request.ConfirmDenyRequest
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResponse
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.network.ApiResponseInterceptor
import retrofit2.Call

internal class ConfirmOrDenyConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: ConfirmAuthorizationListener?
) : ApiResponseInterceptor<ConfirmDenyResponse>() {

    private var connectionId: ConnectionID = ""
    private var authorizationId: AuthorizationID = ""

    fun updateAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        payloadData: ConfirmDenyRequestData
    ) {
        this.connectionId = connectionAndKey.connection.id
        this.authorizationId = authorizationId
        val requestBody = ConfirmDenyRequest(payloadData)
        val requestData = createSignedRequestData(
            requestMethod = REQUEST_METHOD_PUT,
            baseUrl = connectionAndKey.connection.connectUrl,
            apiRoutePath = "$API_AUTHORIZATIONS/$authorizationId",
            accessToken = connectionAndKey.connection.accessToken,
            signPrivateKey = connectionAndKey.key,
            requestBodyObject = requestBody
        )
        apiInterface.updateAuthorization(
            requestUrl = requestData.requestUrl,
            headersMap = requestData.headersMap,
            requestBody = requestBody
        ).enqueue(this)
    }

    override fun onSuccessResponse(
        call: Call<ConfirmDenyResponse>,
        response: ConfirmDenyResponse
    ) {
        val data = response.data
        if (data == null) onFailureResponse(call, createInvalidResponseError())
        else resultCallback?.onConfirmDenySuccess(result = data, connectionID = this.connectionId)
    }

    override fun onFailureResponse(call: Call<ConfirmDenyResponse>, error: ApiErrorData) {
        resultCallback?.onConfirmDenyFailure(
            error = error,
            connectionID = this.connectionId,
            authorizationID = this.authorizationId
        )
    }
}
