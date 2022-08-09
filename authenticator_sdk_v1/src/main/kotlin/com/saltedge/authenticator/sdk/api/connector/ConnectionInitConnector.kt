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
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createInvalidResponseError
import com.saltedge.authenticator.sdk.constants.API_CONNECTIONS
import com.saltedge.authenticator.sdk.contract.ConnectionCreateListener
import com.saltedge.authenticator.sdk.api.model.request.CreateConnectionRequestData
import com.saltedge.authenticator.sdk.api.model.request.CreateConnectionRequest
import com.saltedge.authenticator.sdk.api.model.response.CreateConnectionResponse
import com.saltedge.authenticator.sdk.api.ApiInterface
import retrofit2.Call

/**
 * Connector sends new Connection data to server,
 * received response is redirecting to resultCallback.
 *
 * @param apiInterface - instance of ApiInterface
 * @param resultCallback - instance of ConnectionInitResult for returning query result
 */
internal class ConnectionInitConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: ConnectionCreateListener?
) : ApiResponseInterceptor<CreateConnectionResponse>() {

    /**
     * Prepare request url, request model (CreateConnectionRequestData)
     * and sends request to server (ApiInterface)
     *
     * @param baseUrl - provider base url
     * @param publicKey - new connection public key in pem format
     * @param pushToken - Firebase Cloud Messaging token of current app
     * @param connectQueryParam: String? - connect_query string extracted from deep-link
     */
    fun postConnectionData(
        baseUrl: String,
        publicKey: String,
        pushToken: String,
        providerCode: String,
        connectQueryParam: String?
    ) {
        val url = createRequestUrl(baseUrl = baseUrl, routePath = API_CONNECTIONS)
        val requestData = CreateConnectionRequest(
            data = CreateConnectionRequestData(
                publicKey = publicKey,
                pushToken = pushToken,
                providerCode = providerCode,
                connectQueryParam = connectQueryParam
            )
        )
        apiInterface.createConnection(url, requestData).enqueue(this)
    }

    /**
     * Retrofit callback
     * If response exist then pass response to resultCallback.onConnectionInitSuccess(...)
     * else create InvalidResponseError and pass error to resultCallback.onConnectionInitFailure(...)
     *
     * @param call - retrofit call
     * @param response - CreateConnectionResponse model
     */
    override fun onSuccessResponse(
        call: Call<CreateConnectionResponse>,
        response: CreateConnectionResponse
    ) {
        val data = response.data
        if (data == null) {
            onFailureResponse(call, createInvalidResponseError())
        } else {
            resultCallback?.onConnectionCreateSuccess(data)
        }
    }

    /**
     * Pass error to resultCallback.onConnectionInitFailure(...)
     *
     * @param call - retrofit call
     * @param error - ApiError
     */
    override fun onFailureResponse(call: Call<CreateConnectionResponse>, error: ApiErrorData) {
        resultCallback?.onConnectionCreateFailure(error)
    }
}
