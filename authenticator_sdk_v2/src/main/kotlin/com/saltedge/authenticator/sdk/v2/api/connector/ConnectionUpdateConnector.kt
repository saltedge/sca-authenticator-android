/*
 * Copyright (c) 2024 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionUpdateListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.ConnectionUpdateRequest
import com.saltedge.authenticator.sdk.v2.api.model.authorization.ConnectionUpdateRequestData
import com.saltedge.authenticator.sdk.v2.api.model.connection.UpdatePushTokenResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.addSignatureHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.toUpdateConnectionUrl
import retrofit2.Call

internal class ConnectionUpdateConnector(
    private val apiInterface: ApiInterface,
    var callback: ConnectionUpdateListener?
) : ApiResponseInterceptor<UpdatePushTokenResponse>() {

    fun updatePushToken(richConnection: RichConnection, currentPushToken: String?) {
        val payload = ConnectionUpdateRequest(ConnectionUpdateRequestData(pushToken = currentPushToken ?: ""))
        apiInterface.updateConnection(
            requestUrl = richConnection.connection.connectUrl.toUpdateConnectionUrl(richConnection.connection.id),
            headersMap = headers(richConnection, payload),
            requestBody = payload
        ).enqueue(this)
    }

    private fun headers(richConnection: RichConnection, payload: ConnectionUpdateRequest): Map<String, String> {
        return createAccessTokenHeader(richConnection.connection.accessToken).addSignatureHeader(
            richConnection.private,
            payload.data,
            payload.requestExpirationTime
        )
    }

    override fun onSuccessResponse(
        call: Call<UpdatePushTokenResponse>,
        response: UpdatePushTokenResponse
    ) {
        callback?.onUpdatePushTokenSuccess(connectionID = response.data.connectionID)
    }

    override fun onFailureResponse(call: Call<UpdatePushTokenResponse>, error: ApiErrorData) {
        callback?.onUpdatePushTokenFailed(error)
    }
}
