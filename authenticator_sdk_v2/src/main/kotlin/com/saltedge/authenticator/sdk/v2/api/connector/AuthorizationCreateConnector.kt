/*
 * Copyright (c) 2021 Salt Edge Inc.
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
import com.saltedge.authenticator.sdk.v2.api.retrofit.toAuthorizationsCreateUrl
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
            requestUrl = richConnection.connection.connectUrl.toAuthorizationsCreateUrl(),
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
