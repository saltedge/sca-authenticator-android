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
package com.saltedge.authenticator.sdk.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

const val HEADER_CONTENT_TYPE = "Content-Type"
const val HEADER_KEY_ACCEPT_LANGUAGE = "Accept-Language"
const val HEADER_KEY_ACCESS_TOKEN = "Access-Token"
const val HEADER_KEY_EXPIRES_AT = "Expires-at"
const val HEADER_KEY_SIGNATURE = "Signature"

const val HEADER_VALUE_JSON = "application/json"
const val HEADER_VALUE_ACCEPT_LANGUAGE = "en"

/**
 * Adds default headers as Content-type, Accept-Language, etc.
 */
internal class HeaderInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header(HEADER_CONTENT_TYPE, HEADER_VALUE_JSON)
            .header(HEADER_KEY_ACCEPT_LANGUAGE, HEADER_VALUE_ACCEPT_LANGUAGE)
        return chain.proceed(requestBuilder.build())
    }
}
