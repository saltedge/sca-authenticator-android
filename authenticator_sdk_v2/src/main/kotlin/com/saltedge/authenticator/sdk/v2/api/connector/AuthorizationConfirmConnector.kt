/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationConfirmListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import retrofit2.Call

internal class AuthorizationConfirmConnector(
    private val apiInterface: ApiInterface,
    authorizationId: String,
    var callback: AuthorizationConfirmListener?
) : AuthorizationUpdateBaseConnector(authorizationId = authorizationId, isConfirmRequest = true) {

    private var connectionID: ID = ""

    fun confirmAuthorization(connection: RichConnection, encryptedPayload: EncryptedBundle) {
        val request = super.body(encryptedPayload)
        this.connectionID = connection.connection.id
        apiInterface.confirmAuthorization(
            requestUrl = super.url(connection),
            headersMap = super.headers(connection, request),
            requestBody = request
        ).enqueue(this)
    }

    override fun onSuccessResponse(call: Call<UpdateAuthorizationResponse>, response: UpdateAuthorizationResponse) {
        callback?.onAuthorizationConfirmSuccess(result = response.data, connectionID = connectionID)
    }

    override fun onFailureResponse(call: Call<UpdateAuthorizationResponse>, error: ApiErrorData) {
        callback?.onAuthorizationConfirmFailure(
            error = error,
            connectionID = connectionID,
            authorizationID = authorizationId
        )
    }
}
