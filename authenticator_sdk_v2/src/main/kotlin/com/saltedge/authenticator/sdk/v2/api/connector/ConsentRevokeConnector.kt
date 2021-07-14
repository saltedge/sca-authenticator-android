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
import com.saltedge.authenticator.core.api.model.error.createInvalidResponseError
import com.saltedge.authenticator.core.contract.ConsentRevokeListener
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.model.EmptyRequest
import com.saltedge.authenticator.sdk.v2.api.model.consent.ConsentRevokeResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.addSignatureHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.toConnectionsRevokeUrl
import retrofit2.Call

/**
 * Connector makes request to API to revoke Consent
 *
 * @param apiInterface - instance of ApiInterface
 * @param callback - instance of ConsentRevokeListener for returning query result
 */
internal class ConsentRevokeConnector(
    private val apiInterface: ApiInterface,
    var callback: ConsentRevokeListener? = null
) : ApiResponseInterceptor<ConsentRevokeResponse>() {

    /**
     * Prepare request url, request models
     * and sends requests queue to server (ApiInterface)
     *
     * @param richConnection - RichConnection (alias to Pairs of Connection and related PrivateKey)
     */
    fun revokeConsent(consentID: ID, richConnection: RichConnection) {
        val request = EmptyRequest()
        val headers = createAccessTokenHeader(richConnection.connection.accessToken)
            .addSignatureHeader(
                richConnection.private,
                request.data,
                request.requestExpirationTime
            )
        apiInterface.revokeConsent(
            requestUrl = richConnection.connection.connectUrl.toConnectionsRevokeUrl(consentID),
            headersMap = headers,
            request
        ).enqueue(this)
    }

    override fun onSuccessResponse(
        call: Call<ConsentRevokeResponse>,
        response: ConsentRevokeResponse
    ) {
        val consentId = response.data?.consentId
        if (consentId == null) {
            onFailureResponse(call, createInvalidResponseError())
        } else {
            callback?.onConsentRevokeSuccess(consentId)
        }
    }

    override fun onFailureResponse(call: Call<ConsentRevokeResponse>, error: ApiErrorData) {
        callback?.onConsentRevokeFailure(error = error)
    }
}
