/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.v2.api.contract.FetchConfigurationListener
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import retrofit2.Call

internal class ConfigurationConnector(
    private val apiInterface: ApiInterface,
    var callback: FetchConfigurationListener?
) : ApiResponseInterceptor<ConfigurationResponse>() {

    fun fetchProviderConfiguration(url: String) {
        apiInterface.getProviderConfiguration(requestUrl = url).enqueue(this)
    }

    override fun onSuccessResponse(call: Call<ConfigurationResponse>, response: ConfigurationResponse) {
        callback?.onFetchProviderConfigurationSuccess(result = response.data)
    }

    override fun onFailureResponse(call: Call<ConfigurationResponse>, error: ApiErrorData) {
        callback?.onFetchProviderConfigurationFailure(error = error)
    }
}
