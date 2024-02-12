/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.RequestQueueAbs
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.FetchAuthorizationsListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationsListResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.isValid
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.toAuthorizationsIndexUrl
import retrofit2.Call

/**
 * Connector make request to API to get Authorizations list
 *
 * @param apiInterface - instance of ApiInterface
 * @param callback - instance of AuthorizationResponseData for returning query result
 * @see RequestQueueAbs
 */
internal class AuthorizationsIndexConnector(
    val apiInterface: ApiInterface,
    var callback: FetchAuthorizationsListener?
) : RequestQueueAbs<AuthorizationsListResponse>() {

    private var result = mutableListOf<AuthorizationResponseData>()
    private var errors = mutableListOf<ApiErrorData>()

    fun fetchActiveAuthorizations(connections: List<RichConnection>) {
        if (super.queueIsEmpty()) {
            result.clear()
            errors.clear()
            super.setQueueSize(connections.size)

            if (super.queueIsEmpty()) super.onResponseReceived()
            else connections.forEach {
                apiInterface.activeAuthorizations(
                    requestUrl = it.connection.connectUrl.toAuthorizationsIndexUrl(),
                    headersMap = createAccessTokenHeader(it.connection.accessToken)
                ).enqueue(this)
            }
        }
    }

    override fun onQueueFinished() {
        callback?.onFetchAuthorizationsResult(result, errors)
    }

    override fun onSuccessResponse(
        call: Call<AuthorizationsListResponse>,
        response: AuthorizationsListResponse
    ) {
        response.data.filter { it.isValid() }.let { result.addAll(it) }
        super.onResponseReceived()
    }

    override fun onFailureResponse(call: Call<AuthorizationsListResponse>, error: ApiErrorData) {
        errors.add(error)
        super.onResponseReceived()
    }
}
