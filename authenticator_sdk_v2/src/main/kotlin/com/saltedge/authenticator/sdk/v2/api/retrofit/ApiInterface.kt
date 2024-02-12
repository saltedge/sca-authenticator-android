/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.retrofit

import com.saltedge.authenticator.core.api.model.EncryptedListResponse
import com.saltedge.authenticator.sdk.v2.api.model.EmptyRequest
import com.saltedge.authenticator.sdk.v2.api.model.authorization.*
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionRequest
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.UpdatePushTokenResponse
import com.saltedge.authenticator.sdk.v2.api.model.consent.ConsentRevokeResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * Describes SCA Service API v2. Verbs, request and response format
 */
interface ApiInterface {

    @GET
    fun getProviderConfiguration(@Url requestUrl: String): Call<ConfigurationResponse>

    @GET
    fun showConnectionConfiguration(@Url requestUrl: String): Call<ConfigurationResponse>

    @POST
    fun createConnection(
        @Url requestUrl: String,
        @Body body: CreateConnectionRequest
    ): Call<CreateConnectionResponse>

    @PUT
    fun revokeConnection(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>,
        @Body requestBody: EmptyRequest
    ): Call<RevokeConnectionResponse>

    @GET
    fun activeAuthorizations(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>
    ): Call<AuthorizationsListResponse>

    @GET
    fun showAuthorization(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>
    ): Call<AuthorizationResponse>

    @PUT
    fun confirmAuthorization(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>,
        @Body requestBody: UpdateAuthorizationRequest
    ): Call<UpdateAuthorizationResponse>

    @PUT
    fun denyAuthorization(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>,
        @Body requestBody: UpdateAuthorizationRequest
    ): Call<UpdateAuthorizationResponse>

    @POST
    fun createAuthorizationForAction(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>,
        @Body requestBody: CreateAuthorizationRequest
    ): Call<CreateAuthorizationResponse>

    @PATCH
    fun updateConnection(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>,
        @Body requestBody: ConnectionUpdateRequest
    ): Call<UpdatePushTokenResponse>

    @GET
    fun activeConsents(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>
    ): Call<EncryptedListResponse>

    @PUT
    fun revokeConsent(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>,
        @Body requestBody: EmptyRequest
    ): Call<ConsentRevokeResponse>
}
