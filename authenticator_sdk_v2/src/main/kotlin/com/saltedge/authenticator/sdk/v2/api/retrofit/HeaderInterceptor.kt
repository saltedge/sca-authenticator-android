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
