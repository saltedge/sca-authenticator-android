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
package com.saltedge.authenticator.sdk.network.connector

import com.saltedge.authenticator.sdk.constants.API_ACTION
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_POST
import com.saltedge.authenticator.sdk.contract.ActionSubmitListener
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.createInvalidResponseError
import com.saltedge.authenticator.sdk.model.response.SubmitActionResponseData
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.network.ApiResponseInterceptor
import retrofit2.Call

internal class SubmitActionConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: ActionSubmitListener?
): ApiResponseInterceptor<SubmitActionResponseData>() {

    fun postActionData(
        actionUUID: String,
        connectionAndKey: ConnectionAndKey
    ) {
        val requestData = createAuthenticatedRequestData<Nothing>(
            requestMethod = REQUEST_METHOD_POST,
            baseUrl = connectionAndKey.connection.connectUrl,
            apiRoutePath = "$API_ACTION/${actionUUID}",
            accessToken = connectionAndKey.connection.accessToken,
            signPrivateKey = connectionAndKey.key
        )

        apiInterface.postAction(
            requestData.requestUrl,
            requestData.headersMap
        ).enqueue(this)
    }


    override fun onSuccessResponse(call: Call<SubmitActionResponseData>, response: SubmitActionResponseData) {
        val data = response.data
        if (data == null) {
            onFailureResponse(call, createInvalidResponseError())
        } else {
            resultCallback?.onActionInitSuccess(data)
        }
    }

    override fun onFailureResponse(call: Call<SubmitActionResponseData>, error: ApiErrorData) {
        resultCallback?.onActionInitFailure(error)
    }
}
