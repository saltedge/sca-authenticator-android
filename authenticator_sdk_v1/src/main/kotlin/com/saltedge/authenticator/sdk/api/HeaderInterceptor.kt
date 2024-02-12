/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api

import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.sdk.config.ApiV1Config
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

const val HEADER_KEY_EXPIRES_AT = "Expires-at"
const val HEADER_KEY_SIGNATURE = "Signature"

/**
 * Adds default headers as Content-type, Accept-Language, etc.
 */
internal class HeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header(HEADER_CONTENT_TYPE, HEADER_VALUE_JSON)
                .header(HEADER_KEY_ACCEPT_LANGUAGE, HEADER_VALUE_ACCEPT_LANGUAGE)
                .header(HEADER_KEY_USER_AGENT, ApiV1Config.userAgentInfo)
            chain.proceed(requestBuilder.build())
        } catch (e: Exception) {
            Timber.e(e)
            chain.proceed(chain.request())
        }
    }
}

fun Map<String, String>.addLocationHeader(geolocation: String?): Map<String, String> {
    return geolocation?.let {
        this.toMutableMap().apply { put(HEADER_KEY_GEOLOCATION, it) }
    } ?: this
}

fun Map<String, String>.addAuthorizationTypeHeader(authorizationType: String?): Map<String, String> {
    return authorizationType?.let {
        this.toMutableMap().apply { put(HEADER_KEY_AUTHORIZATION_TYPE, it) }
    } ?: this
}
