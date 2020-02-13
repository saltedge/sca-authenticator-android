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
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_GET
import com.saltedge.authenticator.sdk.contract.FetchAuthorizationsResult
import com.saltedge.authenticator.sdk.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.authorization.EncryptedAuthorizationData
import com.saltedge.authenticator.sdk.model.authorization.isValid
import com.saltedge.authenticator.sdk.model.request.AuthenticatedRequestData
import com.saltedge.authenticator.sdk.model.response.AuthorizationsResponseData
import com.saltedge.authenticator.sdk.network.ApiInterface
import retrofit2.Call

/**
 * Connector query server for Authorizations list
 *
 * @param apiInterface - instance of ApiInterface
 * @param resultCallback - instance of ConnectionsRevokeResult for returning query result
 * @see QueueConnector
 */
internal class AuthorizationsConnector(
    val apiInterface: ApiInterface,
    var resultCallback: FetchAuthorizationsResult?
) : QueueConnector<AuthorizationsResponseData>() {

    private var result = mutableListOf<EncryptedAuthorizationData>()
    private var errors = mutableListOf<ApiErrorData>()

    fun fetchAuthorizations(connectionsAndKeys: List<ConnectionAndKey>) {
        if (super.queueIsEmpty()) {
            val requestData: List<AuthenticatedRequestData> =
                connectionsAndKeys.map { (connection, key) ->
                    createAuthenticatedRequestData<Nothing>(
                        requestMethod = REQUEST_METHOD_GET,
                        baseUrl = connection.connectUrl,
                        apiRoutePath = API_AUTHORIZATIONS,
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
        resultCallback?.onFetchAuthorizationsResult(result, errors)
    }

    override fun onSuccessResponse(
        call: Call<AuthorizationsResponseData>,
        response: AuthorizationsResponseData
    ) {
        response.data?.filter { it.isValid() }?.let { result.addAll(it) }
        super.onResponseReceived()
    }

    override fun onFailureResponse(call: Call<AuthorizationsResponseData>, error: ApiErrorData) {
        errors.add(error)
        super.onResponseReceived()
    }

    private fun sendRequest(requestData: AuthenticatedRequestData) {
        apiInterface.getAuthorizations(
            requestUrl = requestData.requestUrl,
            headersMap = requestData.headersMap
        ).enqueue(this)
    }
}
