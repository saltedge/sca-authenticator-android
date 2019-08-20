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

import com.saltedge.authenticator.sdk.contract.FetchProviderDataResult
import com.saltedge.authenticator.sdk.model.ApiErrorData
import com.saltedge.authenticator.sdk.model.ProviderResponseData
import com.saltedge.authenticator.sdk.network.ApiInterface
import com.saltedge.authenticator.sdk.network.ApiResponseInterceptor
import retrofit2.Call

internal class ProviderDataConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: FetchProviderDataResult?
) : ApiResponseInterceptor<ProviderResponseData>() {

    fun fetchProviderData(requestUrl: String) {
        apiInterface.getProviderData(requestUrl = requestUrl).enqueue(this)
    }

    override fun onSuccessResponse(
        call: Call<ProviderResponseData>,
        response: ProviderResponseData
    ) {
        resultCallback?.fetchProviderResult(result = response.data, error = null)
    }

    override fun onFailureResponse(
        call: Call<ProviderResponseData>,
        error: ApiErrorData
    ) {
        resultCallback?.fetchProviderResult(result = null, error = error)
    }
}
