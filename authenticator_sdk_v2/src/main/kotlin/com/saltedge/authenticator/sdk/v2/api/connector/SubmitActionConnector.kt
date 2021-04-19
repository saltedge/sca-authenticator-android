/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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

import com.saltedge.authenticator.sdk.v2.config.API_ACTIONS
import com.saltedge.authenticator.sdk.v2.config.REQUEST_METHOD_PUT
import com.saltedge.authenticator.sdk.v2.api.contract.ActionSubmitListener
import com.saltedge.authenticator.sdk.v2.api.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.v2.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.model.error.createInvalidResponseError
import com.saltedge.authenticator.sdk.v2.api.model.response.SubmitActionResponse
import com.saltedge.authenticator.sdk.v2.api.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.ApiResponseInterceptor
import com.saltedge.authenticator.sdk.v2.api.createSignedRequestData
import retrofit2.Call

internal class SubmitActionConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: ActionSubmitListener?
): ApiResponseInterceptor<SubmitActionResponse>() {

    fun updateAction(
        actionUUID: String,
        connectionAndKey: ConnectionAndKey
    ) {
        val requestData = createSignedRequestData<Nothing>(
            requestMethod = REQUEST_METHOD_PUT,
            baseUrl = connectionAndKey.connection.connectUrl,
            apiRoutePath = "$API_ACTIONS/${actionUUID}",
            accessToken = connectionAndKey.connection.accessToken,
            signPrivateKey = connectionAndKey.key
        )

        apiInterface.updateAction(
            requestData.requestUrl,
            requestData.headersMap
        ).enqueue(this)
    }


    override fun onSuccessResponse(call: Call<SubmitActionResponse>, response: SubmitActionResponse) {
        val data = response.data
        if (data == null) {
            onFailureResponse(call, createInvalidResponseError())
        } else {
            resultCallback?.onActionInitSuccess(data)
        }
    }

    override fun onFailureResponse(call: Call<SubmitActionResponse>, error: ApiErrorData) {
        resultCallback?.onActionInitFailure(error)
    }
}
