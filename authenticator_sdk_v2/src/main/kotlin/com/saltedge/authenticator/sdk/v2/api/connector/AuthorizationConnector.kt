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

import com.saltedge.authenticator.sdk.v2.api.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.ApiResponseInterceptor
import com.saltedge.authenticator.sdk.v2.api.contract.FetchAuthorizationListener
import com.saltedge.authenticator.sdk.v2.api.createSignedRequestData
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.model.response.AuthorizationShowResponse
import com.saltedge.authenticator.sdk.v2.config.API_AUTHORIZATIONS
import com.saltedge.authenticator.sdk.v2.config.REQUEST_METHOD_GET
import retrofit2.Call

internal class AuthorizationConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: FetchAuthorizationListener?
) : ApiResponseInterceptor<AuthorizationShowResponse>() {

    fun getAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String
    ) {
        val requestData = createSignedRequestData<Nothing>(
            requestMethod = REQUEST_METHOD_GET,
            baseUrl = connectionAndKey.connection.connectUrl,
            apiRoutePath = "$API_AUTHORIZATIONS/$authorizationId",
            accessToken = connectionAndKey.connection.accessToken,
            signPrivateKey = connectionAndKey.key
        )
        apiInterface.getAuthorization(
            requestUrl = requestData.requestUrl,
            headersMap = requestData.headersMap
        ).enqueue(this)
    }

    override fun onSuccessResponse(
        call: Call<AuthorizationShowResponse>,
        response: AuthorizationShowResponse
    ) {
        resultCallback?.onFetchAuthorizationResult(result = response.data, error = null)
    }

    override fun onFailureResponse(
        call: Call<AuthorizationShowResponse>,
        error: ApiErrorData
    ) {
        resultCallback?.onFetchAuthorizationResult(result = null, error = error)
    }
}
