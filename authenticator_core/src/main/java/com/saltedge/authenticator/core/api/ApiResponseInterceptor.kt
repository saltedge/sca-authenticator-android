/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.api

import com.google.gson.Gson
import com.saltedge.authenticator.core.BuildConfig
import com.saltedge.authenticator.core.api.model.error.ApiErrorData
import com.saltedge.authenticator.core.api.model.error.createRequestError
import com.saltedge.authenticator.core.api.model.error.exceptionToApiError
import com.saltedge.authenticator.core.model.Token
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

/**
 * Intercept Retrofit response and parse error or process exceptions
 */
abstract class ApiResponseInterceptor<T> : Callback<T> {

    final override fun onResponse(call: Call<T>, response: Response<T>) {
        val responseBody = response.body()
        if (response.isSuccessful && responseBody != null) {
            onSuccessResponse(call, responseBody)
        } else {
            onFailureResponse(call, responseToApiError(response)
                .apply { accessToken = extractAccessToken(call) })
        }
    }

    final override fun onFailure(call: Call<T>, t: Throwable) {
        if (BuildConfig.DEBUG) t.printStackTrace()
        onFailureResponse(call, t.exceptionToApiError()
            .apply { accessToken = extractAccessToken(call) })
    }

    abstract fun onSuccessResponse(call: Call<T>, response: T)

    abstract fun onFailureResponse(call: Call<T>, error: ApiErrorData)

    /**
     * Creates Api Error from response
     * errorBody is parsed as ApiErrorData with GSON
     * if cannot parse or errorMessage.isEmpty then creates default RequestError
     *
     * @param response - Retrofit response
     * @return api error
     */
    private fun responseToApiError(response: Response<T>): ApiErrorData {
        return try {
            response.errorBody()?.string()?.let {
                if (it.startsWith("{")) {
                    val errorObject = Gson().fromJson<ApiErrorData>(it, ApiErrorData::class.java)
                    if (errorObject.errorMessage.isNullOrEmpty()) null else errorObject
                } else {
                    Timber.e("Unexpected error format: $it")
                    createRequestError(response.code())
                }
            } ?: createRequestError(response.code())
        } catch (e: Exception) {
            Timber.e(e)
            createRequestError(response.code())
        }
    }

    private fun extractAccessToken(call: Call<T>): Token? {
        return call.request().header(HEADER_KEY_ACCESS_TOKEN)
    }
}
