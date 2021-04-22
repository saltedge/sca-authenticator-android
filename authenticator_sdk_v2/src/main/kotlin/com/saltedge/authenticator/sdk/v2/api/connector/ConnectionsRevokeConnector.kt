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

import com.saltedge.authenticator.sdk.v2.config.API_CONNECTIONS
import com.saltedge.authenticator.sdk.v2.config.REQUEST_METHOD_DELETE
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionsRevokeListener
import com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.v2.api.model.request.SignedRequest
import com.saltedge.authenticator.sdk.v2.api.model.response.RevokeAccessTokenResponse
import com.saltedge.authenticator.sdk.v2.api.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.createSignedRequestData
import com.saltedge.authenticator.sdk.v2.api.model.request.RevokeConnectionRequest
import retrofit2.Call

/**
 * Connector send revoke request.
 *
 * @param apiInterface - instance of ApiInterface
 * @param resultCallback - instance of ConnectionsRevokeResult for returning query result
 */
internal class ConnectionsRevokeConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: ConnectionsRevokeListener? = null
) : RequestQueueAbs<RevokeAccessTokenResponse>() {

    private var errorResult: ApiErrorData? = null

    /**
     * Prepare request url, request models (AuthenticatedRequestData)
     * and sends requests queue to server (ApiInterface)
     *
     * @param connections - list of ConnectionAndKey (alias to Pairs of Connection and related PrivateKey)
     */
    fun revokeTokensFor(connections: List<ConnectionAndKey>, validSeconds: Int) {
        if (super.queueIsEmpty()) {
            val requestData: List<SignedRequest> = connections.map { (connection, key) ->
                createSignedRequestData<Nothing>(
                    requestMethod = REQUEST_METHOD_DELETE,
                    baseUrl = connection.connectUrl,
                    apiRoutePath = API_CONNECTIONS,
                    accessToken = connection.accessToken,
                    signPrivateKey = key
                )
            }
            super.setQueueSize(requestData.size)

            if (super.queueIsEmpty()) onQueueFinished()
            else requestData.forEach { sendRequest(requestData = it, validSeconds = validSeconds) }
        }
    }

    /**
     * Pass result to resultCallback.onConnectionsRevokeResult(...)
     */
    public override fun onQueueFinished() {
        resultCallback?.onConnectionsRevokeResult(errorResult)
    }

    /**
     * Adds successful revoked tokens to result list and call super.onResponseReceived()
     *
     * @param call - retrofit call
     * @param response - RevokeAccessTokenResponseData model
     */
    override fun onSuccessResponse(
        call: Call<RevokeAccessTokenResponse>,
        response: RevokeAccessTokenResponse
    ) {
        super.onResponseReceived()
    }

    /**
     * save received error and call super.onResponseReceived()
     *
     * @param call - retrofit call
     * @param error - ApiError
     */
    override fun onFailureResponse(call: Call<RevokeAccessTokenResponse>, error: ApiErrorData) {
        errorResult = error
        super.onResponseReceived()
    }

    private fun sendRequest(requestData: SignedRequest, validSeconds: Int) {
        val revokeConnectionRequest = RevokeConnectionRequest(
            exp = validSeconds
        )
        apiInterface.revokeConnection(
            requestUrl = requestData.requestUrl,
            headersMap = requestData.headersMap,
            requestBody = revokeConnectionRequest
        ).enqueue(this)
    }
}
