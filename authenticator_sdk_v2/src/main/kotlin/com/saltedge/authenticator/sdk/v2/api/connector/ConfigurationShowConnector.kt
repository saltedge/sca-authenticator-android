/*
 * Copyright (c) 2022 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.connector

import com.saltedge.authenticator.core.api.ApiResponseInterceptor
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.sdk.v2.api.contract.ShowConnectionConfigurationListener
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationResponse
import com.saltedge.authenticator.sdk.v2.api.retrofit.ApiInterface
import com.saltedge.authenticator.sdk.v2.api.retrofit.toConnectionConfigurationShowUrl
import retrofit2.Call

class ConfigurationShowConnector(
    private val apiInterface: ApiInterface,
    var callback: ShowConnectionConfigurationListener?
) : ApiResponseInterceptor<ConfigurationResponse>() {

    fun showConnectionConfiguration(connection: ConnectionAbs, providerId: String) {
        apiInterface.showConnectionConfiguration(
            requestUrl = connection.connectUrl.toConnectionConfigurationShowUrl(providerId)
        ).enqueue(this)
    }

    override fun onSuccessResponse(
        call: Call<ConfigurationResponse>,
        response: ConfigurationResponse
    ) {
        callback?.onShowConnectionConfigurationSuccess(result = response.data)
    }

    override fun onFailureResponse(call: Call<ConfigurationResponse>, error: ApiErrorData) {
        callback?.onShowConnectionConfigurationFailed(error = error)
    }
}
