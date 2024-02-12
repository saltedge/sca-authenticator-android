/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.sdk.contract.FetchProviderConfigurationListener
import com.saltedge.authenticator.sdk.api.model.response.ProviderConfigurationResponse
import com.saltedge.authenticator.sdk.api.ApiInterface
import retrofit2.Call

internal class ProviderConfigurationConnector(
    private val apiInterface: ApiInterface,
    var resultCallback: FetchProviderConfigurationListener?
) : ApiResponseInterceptor<ProviderConfigurationResponse>() {

    fun fetchProviderConfiguration(url: String) {
        apiInterface.getProviderConfiguration(requestUrl = url).enqueue(this)
    }

    override fun onSuccessResponse(
        call: Call<ProviderConfigurationResponse>,
        response: ProviderConfigurationResponse
    ) {
        resultCallback?.onFetchProviderConfigurationSuccess(result = response.data)
    }

    override fun onFailureResponse(call: Call<ProviderConfigurationResponse>, error: ApiErrorData) {
        resultCallback?.onFetchProviderConfigurationFailure(error = error)
    }
}
