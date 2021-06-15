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

import com.saltedge.authenticator.core.api.RequestQueueAbs
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionsRevokeListener
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionRequest
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.addSignatureHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.revokeConnectionsPath
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
) : RequestQueueAbs<RevokeConnectionResponse>() {

    private var errorResult: ApiErrorData? = null
    private var result = mutableListOf<String>()

    /**
     * Prepare request url, request models (AuthenticatedRequestData)
     * and sends requests queue to server (ApiInterface)
     *
     * @param forConnections - list of ConnectionAndKey (alias to Pairs of Connection and related PrivateKey)
     */
    fun revokeAccess(forConnections: List<RichConnection>) {//RevokeConnectionRequest
        if (super.queueIsEmpty()) {
            super.setQueueSize(forConnections.size)
            if (super.queueIsEmpty()) onQueueFinished()
            else {
                result = mutableListOf<String>()
                forConnections.forEach {
                    val request = RevokeConnectionRequest()
                    val headers = createAccessTokenHeader(it.connection.accessToken)
                        .addSignatureHeader(
                            it.private,
                            request.data,
                            request.requestExpirationTime
                        )
                    apiInterface.revokeConnection(
                        requestUrl = it.connection.connectUrl.revokeConnectionsPath(it.connection.id),
                        headersMap = headers,
                        requestBody = request
                    ).enqueue(this)
                }
            }
        }
    }

    /**
     * Pass result to resultCallback.onConnectionsRevokeResult(...)
     */
    public override fun onQueueFinished() {
        resultCallback?.onConnectionsV2RevokeResult(revokedConnections = result, apiError = errorResult)
    }

    /**
     * Adds successful revoked tokens to result list and call super.onResponseReceived()
     *
     * @param call - retrofit call
     * @param response - RevokeAccessTokenResponseData model
     */
    override fun onSuccessResponse(call: Call<RevokeConnectionResponse>, response: RevokeConnectionResponse) {
        result.add(response.data.revokedConnectionId)
        super.onResponseReceived()
    }

    /**
     * save received error and call super.onResponseReceived()
     *
     * @param call - retrofit call
     * @param error - ApiError
     */
    override fun onFailureResponse(call: Call<RevokeConnectionResponse>, error: ApiErrorData) {
        errorResult = error
        super.onResponseReceived()
    }
}
