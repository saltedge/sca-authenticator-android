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

import com.saltedge.authenticator.core.api.model.EncryptedBundle
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.model.RichConnection
import com.saltedge.authenticator.sdk.v2.api.contract.AuthorizationDenyListener
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import retrofit2.Call

internal class AuthorizationDenyConnector(
    private val apiInterface: ApiInterface,
    authorizationId: String,
    var callback: AuthorizationDenyListener?
) : AuthorizationUpdateBaseConnector(authorizationId = authorizationId, isConfirmRequest = false) {

    private var connectionID: ID = ""

    fun denyAuthorization(richConnection: RichConnection, encryptedPayload: EncryptedBundle) {
        val request = super.body(encryptedPayload)
        this.connectionID = richConnection.connection.id
        apiInterface.denyAuthorization(
            requestUrl = super.url(richConnection),
            headersMap = super.headers(richConnection, request),
            requestBody = request
        ).enqueue(this)
    }

    override fun onSuccessResponse(call: Call<UpdateAuthorizationResponse>, response: UpdateAuthorizationResponse) {
        callback?.onAuthorizationDenySuccess(result = response.data, connectionID = connectionID)
    }

    override fun onFailureResponse(call: Call<UpdateAuthorizationResponse>, error: ApiErrorData) {
        callback?.onAuthorizationDenyFailure(
            error = error,
            connectionID = connectionID,
            authorizationID = authorizationId
        )
    }
}
