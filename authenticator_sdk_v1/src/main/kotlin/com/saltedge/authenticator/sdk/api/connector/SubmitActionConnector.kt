/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createInvalidResponseError
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.model.response.SubmitActionResponse
import com.saltedge.authenticator.sdk.constants.API_ACTIONS
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_PUT
import com.saltedge.authenticator.sdk.contract.ActionSubmitListener
import retrofit2.Call

internal class SubmitActionConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: ActionSubmitListener?
): ApiResponseInterceptor<SubmitActionResponse>() {

    fun updateAction(actionUUID: String, connectionAndKey: RichConnection) {
        val requestData = createSignedRequestData<Nothing>(
            requestMethod = REQUEST_METHOD_PUT,
            baseUrl = connectionAndKey.connection.connectUrl,
            apiRoutePath = "$API_ACTIONS/${actionUUID}",
            accessToken = connectionAndKey.connection.accessToken,
            signPrivateKey = connectionAndKey.private
        )

        apiInterface.updateAction(requestData.requestUrl, requestData.headersMap).enqueue(this)
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
