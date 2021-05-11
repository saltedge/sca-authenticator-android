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

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createInvalidResponseError
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.model.response.ConsentRevokeResponse
import com.saltedge.authenticator.sdk.constants.API_CONSENTS
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_DELETE
import com.saltedge.authenticator.sdk.contract.ConsentRevokeListener
import retrofit2.Call

/**
 * Connector make request to API to get Consents list
 *
 * @param apiInterface - instance of ApiInterface
 * @param resultCallback - instance of FetchEncryptedDataResult for returning query result
 * @see QueueConnector
 */
internal class ConsentRevokeConnector(
    val apiInterface: ApiInterface,
    var resultCallback: ConsentRevokeListener?
) : ApiResponseInterceptor<ConsentRevokeResponse>() {

    fun revokeConsent(consentId: String, connectionAndKey: RichConnection) {
        val requestData = createSignedRequestData<Nothing>(
            requestMethod = REQUEST_METHOD_DELETE,
            baseUrl = connectionAndKey.connection.connectUrl,
            apiRoutePath = "$API_CONSENTS/${consentId}",
            accessToken = connectionAndKey.connection.accessToken,
            signPrivateKey = connectionAndKey.private
        )

        apiInterface.revokeConsent(
            requestData.requestUrl,
            requestData.headersMap
        ).enqueue(this)
    }

    override fun onSuccessResponse(call: Call<ConsentRevokeResponse>, response: ConsentRevokeResponse) {
        val data = response.data
        if (data == null) {
            onFailureResponse(call, createInvalidResponseError())
        } else {
            resultCallback?.onConsentRevokeSuccess(data)
        }
    }

    override fun onFailureResponse(call: Call<ConsentRevokeResponse>, error: ApiErrorData) {
        resultCallback?.onConsentRevokeFailure(error)
    }
}
