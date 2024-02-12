/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api

import com.saltedge.authenticator.core.api.model.EncryptedListResponse
import com.saltedge.authenticator.sdk.api.model.request.ConfirmDenyRequest
import com.saltedge.authenticator.sdk.api.model.request.CreateConnectionRequest
import com.saltedge.authenticator.sdk.api.model.response.*
import retrofit2.Call
import retrofit2.http.*

/**
 * Describes Identity Service API. Verbs, request and response format
 */
interface ApiInterface {

    @GET
    fun getProviderConfiguration(@Url requestUrl: String): Call<ProviderConfigurationResponse>

    @POST
    fun createConnection(
        @Url requestUrl: String,
        @Body body: CreateConnectionRequest
    ): Call<CreateConnectionResponse>

    @DELETE
    fun revokeConnection(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>
    ): Call<RevokeAccessTokenResponse>

    @GET
    fun getAuthorizations(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>
    ): Call<EncryptedListResponse>

    @GET
    fun getAuthorization(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>
    ): Call<AuthorizationShowResponse>

    @PUT
    fun updateAuthorization(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>,
        @Body requestBody: ConfirmDenyRequest
    ): Call<ConfirmDenyResponse>

    @PUT
    fun updateAction(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>
    ): Call<SubmitActionResponse>

    @GET
    fun getConsents(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>
    ): Call<EncryptedListResponse>

    @DELETE
    fun revokeConsent(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>
    ): Call<ConsentRevokeResponse>
}
