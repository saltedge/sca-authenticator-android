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
package com.saltedge.authenticator.sdk.v2.api

import com.saltedge.authenticator.sdk.v2.api.model.configuration.ProviderConfigurationResponse
import com.saltedge.authenticator.sdk.v2.api.model.request.ConfirmDenyRequest
import com.saltedge.authenticator.sdk.v2.api.model.request.CreateConnectionRequest
import com.saltedge.authenticator.sdk.v2.api.model.response.*
import retrofit2.Call
import retrofit2.http.*

/**
 * Describes SCA Service API v2. Verbs, request and response format
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
}
