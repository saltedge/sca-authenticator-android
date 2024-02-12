/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.retrofit

import android.net.Uri
import com.google.gson.Gson
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.tools.json.createDefaultGson
import com.saltedge.authenticator.sdk.v2.api.*
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

fun String.toUpdateConnectionUrl(id: String): String {
    return createRequestUrl(this, "api/authenticator/v2/connections/$id/update")
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

fun String.toConsentsRevokeUrl(id: ID): String {
    return createRequestUrl(this, "$API_CONSENTS/$id/revoke")
}

fun String.toConnectionConfigurationShowUrl(id: String): String {
    return createRequestUrl(this, "$API_CONFIGURATIONS/$id")
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
