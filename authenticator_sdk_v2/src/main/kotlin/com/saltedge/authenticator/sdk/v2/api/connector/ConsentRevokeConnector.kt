/*
 * Copyright (c) 2021 Salt Edge Inc.
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
import com.saltedge.authenticator.sdk.v2.api.retrofit.*
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
            requestUrl = richConnection.connection.connectUrl.toConsentsRevokeUrl(consentID),
            headersMap = headers,
            requestBody = request
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
