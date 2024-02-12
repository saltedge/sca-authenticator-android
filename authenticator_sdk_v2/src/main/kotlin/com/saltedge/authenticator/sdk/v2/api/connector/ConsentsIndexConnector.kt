/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.RequestQueueAbs
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.api.model.EncryptedListResponse
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.isValid
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.FetchConsentsListener
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.toConsentsIndexUrl
import retrofit2.Call

/**
 * Connector make request to API to get Consents list
 *
 * @param apiInterface - instance of ApiInterface
 * @param callback - instance of FetchConsentsListener for returning query result
 * @see RequestQueueAbs
 */
internal class ConsentsIndexConnector(
    val apiInterface: ApiInterface,
    var callback: FetchConsentsListener?
) : RequestQueueAbs<EncryptedListResponse>() {

    private var result = mutableListOf<EncryptedData>()
    private var errors = mutableListOf<ApiErrorData>()

    fun fetchActiveConsents(connections: List<RichConnection>) {
        if (super.queueIsEmpty()) {
            result.clear()
            errors.clear()
            super.setQueueSize(connections.size)

            if (super.queueIsEmpty()) super.onResponseReceived()
            else connections.forEach {
                apiInterface.activeConsents(
                    requestUrl = it.connection.connectUrl.toConsentsIndexUrl(),
                    headersMap = createAccessTokenHeader(it.connection.accessToken)
                ).enqueue(this)
            }
        }
    }

    override fun onQueueFinished() {
        callback?.onFetchConsentsV2Result(result, errors)
    }

    override fun onSuccessResponse(
        call: Call<EncryptedListResponse>,
        response: EncryptedListResponse
    ) {
        response.data.filter { it.isValid() }.let { result.addAll(it) }
        super.onResponseReceived()
    }

    override fun onFailureResponse(call: Call<EncryptedListResponse>, error: ApiErrorData) {
        errors.add(error)
        super.onResponseReceived()
    }
}
