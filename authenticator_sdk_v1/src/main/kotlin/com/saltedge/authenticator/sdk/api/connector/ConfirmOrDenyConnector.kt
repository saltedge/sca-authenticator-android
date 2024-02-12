/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createInvalidResponseError
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.addAuthorizationTypeHeader
import com.saltedge.authenticator.sdk.api.addLocationHeader
import com.saltedge.authenticator.sdk.api.model.request.ConfirmDenyRequest
import com.saltedge.authenticator.sdk.api.model.request.ConfirmDenyRequestData
import com.saltedge.authenticator.sdk.api.model.response.ConfirmDenyResponse
import com.saltedge.authenticator.sdk.constants.API_AUTHORIZATIONS
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_PUT
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationListener
import retrofit2.Call

internal class ConfirmOrDenyConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: ConfirmAuthorizationListener?
) : ApiResponseInterceptor<ConfirmDenyResponse>() {

    private var connectionId: ID = ""
    private var authorizationId: ID = ""

    fun updateAuthorization(
        connectionAndKey: RichConnection,
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
            signPrivateKey = connectionAndKey.private,
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
