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
package com.saltedge.authenticator.sdk.network.connector

import android.net.Uri
import com.saltedge.authenticator.sdk.constants.DEFAULT_EXPIRATION_MINUTES
import com.saltedge.authenticator.sdk.model.request.AuthenticatedRequestData
import com.saltedge.authenticator.sdk.network.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.sdk.network.HEADER_KEY_EXPIRES_AT
import com.saltedge.authenticator.sdk.network.HEADER_KEY_SIGNATURE
import com.saltedge.authenticator.sdk.network.RestClient
import com.saltedge.authenticator.sdk.tools.createSignatureHeader
import com.saltedge.authenticator.sdk.tools.printToLogcat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.security.PrivateKey

/**
 * Create Authenticated (with Signature) request data
 */
internal fun <T> createAuthenticatedRequestData(
    requestMethod: String,
    baseUrl: String,
    apiRoutePath: String,
    accessToken: String,
    signPrivateKey: PrivateKey,
    requestBodyObject: T? = null
): AuthenticatedRequestData {
    val requestUrl = createRequestUrl(baseUrl = baseUrl, routePath = apiRoutePath)
    val requestBodyString = requestBodyObject?.let { RestClient.defaultGson.toJson(it) } ?: ""
    return AuthenticatedRequestData(
        requestUrl = requestUrl,
        headersMap = createHeaders(
            signPrivateKey = signPrivateKey,
            accessToken = accessToken,
            url = requestUrl,
            requestMethod = requestMethod,
            requestBody = requestBodyString
        )
    )
}

internal fun createRequestUrl(baseUrl: String, routePath: String): String {
    val baseUri = Uri.parse(baseUrl)
    val baseUriPath = baseUri.encodedPath?.trimStart('/') ?: ""
    return try {
        Uri.Builder()
            .scheme(baseUri.scheme)
            .encodedAuthority(baseUri.authority ?: "")
            .appendEncodedPath(baseUriPath)
            .appendEncodedPath(routePath)
            .build()
            .toString()
    } catch (e: Exception) {
        "$baseUrl/$routePath"
    }
}

private fun createHeaders(
    signPrivateKey: PrivateKey,
    accessToken: String,
    url: String,
    requestMethod: String,
    requestBody: String
): Map<String, String> {
    val expiresAt = createExpiresAtTime().toString()
    val signature = createSignatureHeader(
        requestMethod = requestMethod,
        requestUrl = url,
        expiresAt = expiresAt,
        requestBody = requestBody,
        privateKey = signPrivateKey
    )
    return mapOf(
        HEADER_KEY_ACCESS_TOKEN to accessToken,
        HEADER_KEY_EXPIRES_AT to expiresAt,
        HEADER_KEY_SIGNATURE to signature
    )
}

/**
 * Return unix time (seconds) of current time plus timeout (by default 5 minutes)
 */
internal fun createExpiresAtTime(withMinutesTimeOut: Int = DEFAULT_EXPIRATION_MINUTES): Int =
    (DateTime.now(DateTimeZone.UTC).plusMinutes(withMinutesTimeOut).millis / 1000).toInt()
