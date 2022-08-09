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
