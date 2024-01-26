/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2024 Salt Edge Inc.
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
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.PushTokenUpdateListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdatePushTokenRequest
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdatePushTokenRequestData
import com.saltedge.authenticator.sdk.v2.api.model.connection.UpdatePushTokenResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.addSignatureHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.createAccessTokenHeader
import com.saltedge.authenticator.sdk.v2.api.retrofit.toUpdateConnectionUrl
import retrofit2.Call

internal class PushTokenUpdateConnector(
    private val apiInterface: ApiInterface,
    var callback: PushTokenUpdateListener?
) : ApiResponseInterceptor<UpdatePushTokenResponse>() {

    fun updatePushTokenForConnection(richConnection: RichConnection, currentPushToken: String?) {
        val payload = UpdatePushTokenRequest(UpdatePushTokenRequestData(pushToken = currentPushToken ?: ""))
        apiInterface.updatePushTokenForConnection(
            requestUrl = richConnection.connection.connectUrl.toUpdateConnectionUrl(richConnection.connection.id),
            headersMap = headers(richConnection, payload),
            requestBody = payload
        ).enqueue(this)
    }

    private fun headers(richConnection: RichConnection, payload: UpdatePushTokenRequest): Map<String, String> {
        return createAccessTokenHeader(richConnection.connection.accessToken).addSignatureHeader(
            richConnection.private,
            payload.data,
            payload.requestExpirationTime
        )
    }

    override fun onSuccessResponse(
        call: Call<UpdatePushTokenResponse>,
        response: UpdatePushTokenResponse
    ) {
        callback?.onUpdatePushTokenSuccess(connectionID = response.data.connectionID)
    }

    override fun onFailureResponse(call: Call<UpdatePushTokenResponse>, error: ApiErrorData) {
        callback?.onUpdatePushTokenFailed(error)
    }
}
