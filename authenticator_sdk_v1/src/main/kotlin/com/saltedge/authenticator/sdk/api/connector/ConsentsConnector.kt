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
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.model.EncryptedData
import com.saltedge.authenticator.sdk.api.model.isValid
import com.saltedge.authenticator.sdk.api.model.request.SignedRequest
import com.saltedge.authenticator.sdk.api.model.response.EncryptedListResponse
import com.saltedge.authenticator.sdk.constants.API_CONSENTS
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_GET
import com.saltedge.authenticator.sdk.contract.FetchEncryptedDataListener
import retrofit2.Call

/**
 * Connector make request to API to get Consents list
 *
 * @param apiInterface - instance of ApiInterface
 * @param resultCallback - instance of FetchEncryptedDataResult for returning query result
 * @see QueueConnector
 */
internal class ConsentsConnector(
    val apiInterface: ApiInterface,
    val connectionsAndKeys: List<RichConnection>,
    var resultCallback: FetchEncryptedDataListener?
) : QueueConnector<EncryptedListResponse>() {

    private var result = mutableListOf<EncryptedData>()
    private var errors = mutableListOf<ApiErrorData>()

    fun fetchConsents() {
        if (super.queueIsEmpty()) {
            val requestData: List<SignedRequest> =
                connectionsAndKeys.map { (connection, key) ->
                    createSignedRequestData<Nothing>(
                        requestMethod = REQUEST_METHOD_GET,
                        baseUrl = connection.connectUrl,
                        apiRoutePath = API_CONSENTS,
                        accessToken = connection.accessToken,
                        signPrivateKey = key
                    )
                }
            result.clear()
            errors.clear()
            super.setQueueSize(requestData.size)

            if (super.queueIsEmpty()) super.onResponseReceived()
            else requestData.forEach { sendRequest(it) }
        }
    }

    override fun onQueueFinished() {
        resultCallback?.onFetchEncryptedDataResult(result, errors)
    }

    override fun onSuccessResponse(
        call: Call<EncryptedListResponse>,
        response: EncryptedListResponse
    ) {
        response.data?.filter { it.isValid() }?.let { result.addAll(it) }
        super.onResponseReceived()
    }

    override fun onFailureResponse(call: Call<EncryptedListResponse>, error: ApiErrorData) {
        errors.add(error)
        super.onResponseReceived()
    }

    private fun sendRequest(requestData: SignedRequest) {
        apiInterface.getConsents(
            requestUrl = requestData.requestUrl,
            headersMap = requestData.headersMap
        ).enqueue(this)
    }
}
