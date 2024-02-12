/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createInvalidResponseError
import com.saltedge.authenticator.core.contract.ConsentRevokeListener
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.api.ApiInterface
import com.saltedge.authenticator.sdk.api.model.response.ConsentRevokeResponse
import com.saltedge.authenticator.sdk.constants.API_CONSENTS
import com.saltedge.authenticator.sdk.constants.REQUEST_METHOD_DELETE
import retrofit2.Call

/**
 * Connector makes request to API to revoke Consent
 *
 * @param apiInterface - instance of ApiInterface
 * @param resultCallback - instance of ConsentRevokeListener for returning query result
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
        val consentId = response.data?.consentId
        if (consentId == null) {
            onFailureResponse(call, createInvalidResponseError())
        } else {
            resultCallback?.onConsentRevokeSuccess(consentId)
        }
    }

    override fun onFailureResponse(call: Call<ConsentRevokeResponse>, error: ApiErrorData) {
        resultCallback?.onConsentRevokeFailure(error)
    }
}
