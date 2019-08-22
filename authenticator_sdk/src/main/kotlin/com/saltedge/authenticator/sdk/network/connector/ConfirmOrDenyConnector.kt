/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.sdk.network.connector

import com.saltedge.authenticator.sdk.constants.API_AUTHORIZATIONS
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_PUT
import com.saltedge.authenticator.sdk.contract.ConfirmAuthorizationResult
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.createInvalidResponseError
import com.saltedge.authenticator.sdk.model.request.ConfirmDenyData
import com.saltedge.authenticator.sdk.model.request.ConfirmDenyRequestData
import com.saltedge.authenticator.sdk.model.response.ConfirmDenyResponseData
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.network.ApiResponseInterceptor
import retrofit2.Call

internal class ConfirmOrDenyConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: ConfirmAuthorizationResult?
) : ApiResponseInterceptor<ConfirmDenyResponseData>() {

    fun updateAuthorization(
        connectionAndKey: ConnectionAndKey,
        authorizationId: String,
        payloadData: ConfirmDenyData
    ) {
        val requestBody = ConfirmDenyRequestData(payloadData)
        val requestData = createAuthenticatedRequestData(
            requestMethod = REQUEST_METHOD_PUT,
            baseUrl = connectionAndKey.connection.connectUrl,
            apiRoutePath = "$API_AUTHORIZATIONS/$authorizationId",
            accessToken = connectionAndKey.connection.accessToken,
            signPrivateKey = connectionAndKey.key,
            requestBodyObject = requestBody
        )
        apiInterface.updateAuthorization(
            requestUrl = requestData.requestUrl,
            headersMap = requestData.headersMap,
            requestBody = requestBody
        ).enqueue(this)
    }

    override fun onSuccessResponse(
        call: Call<ConfirmDenyResponseData>,
        response: ConfirmDenyResponseData
    ) {
        val data = response.data
        if (data == null) onFailureResponse(call, createInvalidResponseError())
        else resultCallback?.onConfirmDenySuccess(result = data)
    }

    override fun onFailureResponse(call: Call<ConfirmDenyResponseData>, error: ApiErrorData) {
        resultCallback?.onConfirmDenyFailure(error)
    }
}
