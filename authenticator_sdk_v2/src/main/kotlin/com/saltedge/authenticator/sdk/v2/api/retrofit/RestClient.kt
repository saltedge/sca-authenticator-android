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

import android.net.Uri
import com.google.gson.Gson
import com.saltedge.authenticator.core.tools.json.createDefaultGson
import com.saltedge.authenticator.sdk.v2.api.API_AUTHORIZATIONS
import com.saltedge.authenticator.sdk.v2.api.API_CONNECTIONS
import com.saltedge.authenticator.sdk.v2.api.API_CONSENTS
import com.saltedge.authenticator.sdk.v2.api.DEFAULT_HOST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Initiates ApiInterface and default GSON
 */
internal object RestClient {

    var apiInterface: ApiInterface
    val defaultGson: Gson = createDefaultGson()

    init {
        apiInterface = Retrofit.Builder()
            .baseUrl(DEFAULT_HOST)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(defaultGson))
            .build()
            .create(ApiInterface::class.java)
    }
}

fun String.toConnectionsCreateUrl(): String {
    return createRequestUrl(this, API_CONNECTIONS)
}

fun String.toConnectionsRevokeUrl(id: String): String {
    return createRequestUrl(this, "$API_CONNECTIONS/$id/revoke")
}

fun String.toAuthorizationsIndexUrl(): String {
    return createRequestUrl(this, API_AUTHORIZATIONS)
}

fun String.toAuthorizationsShowUrl(id: String): String {
    return createRequestUrl(this, "$API_AUTHORIZATIONS/$id")
}

fun String.toAuthorizationsConfirmUrl(id: String): String {
    return createRequestUrl(this, "$API_AUTHORIZATIONS/$id/confirm")
}

fun String.toAuthorizationsDenyUrl(id: String): String {
    return createRequestUrl(this, "$API_AUTHORIZATIONS/$id/deny")
}

fun String.toAuthorizationsCreateUrl(): String {
    return createRequestUrl(this, API_AUTHORIZATIONS)
}

fun String.toConsentsIndexUrl(): String {
    return createRequestUrl(this, API_CONSENTS)
}

fun String.toConsentsRevokeUrl(id: String): String {
    return createRequestUrl(this, "$API_CONSENTS/$id/revoke")
}

private fun createRequestUrl(baseUrl: String, routePath: String): String {
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
