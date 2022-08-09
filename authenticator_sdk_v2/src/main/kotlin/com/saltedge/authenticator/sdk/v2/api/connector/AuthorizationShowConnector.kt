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
