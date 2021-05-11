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
import com.saltedge.authenticator.sdk.v2.api.contract.FetchConfigurationListener
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationResponse
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import retrofit2.Call

internal class ConfigurationConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: FetchConfigurationListener?
) : ApiResponseInterceptor<ConfigurationResponse>() {

    fun fetchProviderConfiguration(url: String) {
        apiInterface.getProviderConfiguration(requestUrl = url).enqueue(this)
    }

    override fun onSuccessResponse(call: Call<ConfigurationResponse>, response: ConfigurationResponse) {
        resultCallback?.onFetchProviderConfigurationSuccess(result = response.data)
    }

    override fun onFailureResponse(call: Call<ConfigurationResponse>, error: ApiErrorData) {
        resultCallback?.onFetchProviderConfigurationFailure(error = error)
    }
}
