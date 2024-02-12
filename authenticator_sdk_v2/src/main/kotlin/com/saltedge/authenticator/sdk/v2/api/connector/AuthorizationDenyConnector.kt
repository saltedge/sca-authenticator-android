/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationDenyListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import retrofit2.Call

internal class AuthorizationDenyConnector(
    private val apiInterface: ApiInterface,
    authorizationId: String,
    var callback: AuthorizationDenyListener?
) : AuthorizationUpdateBaseConnector(authorizationId = authorizationId, isConfirmRequest = false) {

    private var connectionID: ID = ""

    fun denyAuthorization(richConnection: RichConnection, encryptedPayload: EncryptedBundle) {
        val request = super.body(encryptedPayload)
        this.connectionID = richConnection.connection.id
        apiInterface.denyAuthorization(
            requestUrl = super.url(richConnection),
            headersMap = super.headers(richConnection, request),
            requestBody = request
        ).enqueue(this)
    }

    override fun onSuccessResponse(call: Call<UpdateAuthorizationResponse>, response: UpdateAuthorizationResponse) {
        callback?.onAuthorizationDenySuccess(result = response.data, connectionID = connectionID)
    }

    override fun onFailureResponse(call: Call<UpdateAuthorizationResponse>, error: ApiErrorData) {
        callback?.onAuthorizationDenyFailure(
            error = error,
            connectionID = connectionID,
            authorizationID = authorizationId
        )
    }
}
