/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.retrofit

import com.saltedge.authenticator.core.api.*
import com.saltedge.authenticator.sdk.v2.config.ApiV2Config
import com.saltedge.authenticator.sdk.v2.tools.JwsTools
import okhttp3.Interceptor
import okhttp3.Response
import java.security.PrivateKey

const val HEADER_KEY_X_JWS_SIGNATURE = "x-jws-signature"

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
                .header(HEADER_KEY_USER_AGENT, ApiV2Config.userAgentInfo)
            chain.proceed(requestBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
            chain.proceed(chain.request())
        }
    }
}

fun createAccessTokenHeader(accessToken: String): Map<String, String> {
    return mapOf(HEADER_KEY_ACCESS_TOKEN to accessToken)
}

fun Map<String, String>.addSignatureHeader(
    signPrivateKey: PrivateKey,
    requestDataObject: Any,
    expiresAt: Int
): Map<String, String> {
    val map = this.toMutableMap()
    map[HEADER_KEY_X_JWS_SIGNATURE] = JwsTools.createSignature(requestDataObject, expiresAt, signPrivateKey)
    return map
}
