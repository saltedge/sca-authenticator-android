/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.RequestQueueAbs
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionsV2RevokeListener
import com.saltedge.authenticator.sdk.v2.api.model.EmptyRequest
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.addSignatureHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.toConnectionsRevokeUrl
import retrofit2.Call

/**
 * Connector send revoke request.
 *
 * @param apiInterface - instance of ApiInterface
 * @param callback - instance of ConnectionsRevokeResult for returning query result
 */
internal class ConnectionsRevokeConnector(
    private val apiInterface: ApiInterface,
    var callback: ConnectionsV2RevokeListener? = null
) : RequestQueueAbs<RevokeConnectionResponse>() {

    private var revokedIDs = mutableListOf<ID>()
    private var revokeErrors = mutableListOf<ApiErrorData>()

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
                this.revokeErrors = mutableListOf()
                this.revokedIDs = mutableListOf()
                forConnections.forEach {
                    val request = EmptyRequest()
                    val headers = createAccessTokenHeader(it.connection.accessToken)
                        .addSignatureHeader(
                            it.private,
                            request.data,
                            request.requestExpirationTime
                        )
                    apiInterface.revokeConnection(
                        requestUrl = it.connection.connectUrl.toConnectionsRevokeUrl(it.connection.id),
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
        callback?.onConnectionsV2RevokeResult(revokedIDs = revokedIDs, apiErrors = revokeErrors)
    }

    /**
     * Adds successful revoked tokens to result list and call super.onResponseReceived()
     *
     * @param call - retrofit call
     * @param response - RevokeAccessTokenResponseData model
     */
    override fun onSuccessResponse(call: Call<RevokeConnectionResponse>, response: RevokeConnectionResponse) {
        revokedIDs.add(response.data.revokedConnectionId)
        super.onResponseReceived()
    }

    /**
     * save received error and call super.onResponseReceived()
     *
     * @param call - retrofit call
     * @param error - ApiError
     */
    override fun onFailureResponse(call: Call<RevokeConnectionResponse>, error: ApiErrorData) {
        revokeErrors.add(error)
        super.onResponseReceived()
    }
}
