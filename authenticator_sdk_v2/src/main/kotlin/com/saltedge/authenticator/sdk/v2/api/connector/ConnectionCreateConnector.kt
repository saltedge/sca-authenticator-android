/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.sdk.v2.api.contract.ConnectionCreateListener
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionRequest
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionRequestData
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionResponse
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.toConnectionsCreateUrl
import com.saltedge.authenticator.sdk.v2.config.ApiV2Config
import retrofit2.Call

/**
 * Connector sends new Connection data to server,
 * received response is redirecting to resultCallback.
 *
 * @param apiInterface - instance of ApiInterface
 * @param callback - instance of ConnectionInitResult for returning query result
 */
internal class ConnectionCreateConnector(
    private val apiInterface: ApiInterface,
    var callback: ConnectionCreateListener?
) : ApiResponseInterceptor<CreateConnectionResponse>() {

    /**
     * Prepare request url, request model (CreateConnectionRequestData)
     * and sends request to server (ApiInterface)
     *
     * @param baseUrl - service base url
     * @param providerId - unique identifier of Provider in SCA Service
     * @param pushToken - Firebase Cloud Messaging token of current app
     * @param encryptedRsaPublicKey - newly generated RSA public key (wrapped in encryption bundle object)
     * @param connectQueryParam: String? - connect_query string extracted from deep-link
     */
    fun postConnectionData(
        baseUrl: String,
        providerId: String,
        pushToken: String?,
        encryptedRsaPublicKey: EncryptedBundle,
        connectQueryParam: String?
    ) {
        val requestUrl = baseUrl.toConnectionsCreateUrl()
        val requestData = CreateConnectionRequest(
            data = CreateConnectionRequestData(
                providerId = providerId,
                returnUrl = ApiV2Config.authenticationReturnUrl,
                pushToken = pushToken,
                connectQueryParam = connectQueryParam,
                encryptedAppRsaPublicKey = encryptedRsaPublicKey,
            )
        )
        apiInterface.createConnection(requestUrl = requestUrl, body = requestData).enqueue(this)
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
        callback?.onConnectionCreateSuccess(
            authenticationUrl = response.data.authenticationUrl,
            connectionId = response.data.connectionId
        )
    }

    /**
     * Pass error to resultCallback.onConnectionInitFailure(...)
     *
     * @param call - retrofit call
     * @param error - ApiError
     */
    override fun onFailureResponse(call: Call<CreateConnectionResponse>, error: ApiErrorData) {
        callback?.onConnectionCreateFailure(error)
    }
}
