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
                val errorObject = Gson().fromJson<ApiErrorData>(it, ApiErrorData::class.java)
                if (errorObject.errorMessage.isEmpty()) null else errorObject
            } ?: createRequestError(response.code())
        } catch (e: Exception) {
            e.printStackTrace()
            createRequestError(response.code())
        }
    }

    private fun extractAccessToken(call: Call<T>): Token? {
        return call.request().header(HEADER_KEY_ACCESS_TOKEN)
    }
}
