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

import com.saltedge.authenticator.sdk.v2.api.*
import com.saltedge.authenticator.sdk.v2.api.ApiResponseInterceptor
import com.saltedge.authenticator.sdk.v2.config.API_AUTHORIZATIONS
import com.saltedge.authenticator.sdk.v2.config.REQUEST_METHOD_PUT
import com.saltedge.authenticator.sdk.v2.api.contract.ConfirmAuthorizationListener
import com.saltedge.authenticator.sdk.v2.api.model.*
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.model.error.createInvalidResponseError
import com.saltedge.authenticator.sdk.v2.api.model.request.ConfirmDenyRequestData
import com.saltedge.authenticator.sdk.v2.api.model.request.ConfirmDenyRequest
import com.saltedge.authenticator.sdk.v2.api.model.response.ConfirmDenyResponse
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
        geolocationHeader: String?,
        authorizationTypeHeader: String?,
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
            headersMap = requestData.headersMap
                .addLocationHeader(geolocationHeader)
                .addAuthorizationTypeHeader(authorizationTypeHeader),
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
