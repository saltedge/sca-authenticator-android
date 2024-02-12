/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.sdk.v2.api.contract.FetchAuthorizationListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.toAuthorizationsShowUrl
import retrofit2.Call

internal class AuthorizationShowConnector(
    private val apiInterface: ApiInterface,
    var callback: FetchAuthorizationListener?
) : ApiResponseInterceptor<AuthorizationResponse>() {

    fun showAuthorization(connection: ConnectionAbs, authorizationId: String) {
        apiInterface.showAuthorization(
            requestUrl = connection.connectUrl.toAuthorizationsShowUrl(authorizationId),
            headersMap = createAccessTokenHeader(connection.accessToken)
        ).enqueue(this)
    }

    override fun onSuccessResponse(call: Call<AuthorizationResponse>, response: AuthorizationResponse) {
        callback?.onFetchAuthorizationSuccess(result = response.data)
    }

    override fun onFailureResponse(call: Call<AuthorizationResponse>, error: ApiErrorData) {
        callback?.onFetchAuthorizationFailed(error = error)
    }
}
