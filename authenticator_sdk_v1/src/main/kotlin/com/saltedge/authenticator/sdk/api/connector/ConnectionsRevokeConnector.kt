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

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.core.model.Token
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.model.request.SignedRequest
import com.saltedge.authenticator.sdk.api.model.response.RevokeAccessTokenResponse
import com.saltedge.authenticator.sdk.constants.API_CONNECTIONS
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_DELETE
import com.saltedge.authenticator.sdk.contract.ConnectionsRevokeListener
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
) : QueueConnector<RevokeAccessTokenResponse>() {

    private var result = mutableListOf<Token>()
    private var errorResult: ApiErrorData? = null

    /**
     * Prepare request url, request models (AuthenticatedRequestData)
     * and sends requests queue to server (ApiInterface)
     *
     * @param connections - list of ConnectionAndKey (alias to Pairs of Connection and related PrivateKey)
     */
    fun revokeTokensFor(connections: List<RichConnection>) {
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
            this.result = ArrayList()
            super.setQueueSize(requestData.size)

            if (super.queueIsEmpty()) onQueueFinished()
            else requestData.forEach { sendRequest(it) }
        }
    }

    /**
     * Pass result to resultCallback.onConnectionsRevokeResult(...)
     */
    public override fun onQueueFinished() {
        resultCallback?.onConnectionsRevokeResult(result, errorResult)
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
        response.data?.accessToken?.let {
            if ((response.data?.success == true)) result.add(it)
        }
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

    private fun sendRequest(requestData: SignedRequest) {
        apiInterface.revokeConnection(
            requestUrl = requestData.requestUrl,
            headersMap = requestData.headersMap
        ).enqueue(this)
    }
}
