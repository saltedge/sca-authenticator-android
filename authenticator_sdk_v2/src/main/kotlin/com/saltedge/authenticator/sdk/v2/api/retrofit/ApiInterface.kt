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

import com.saltedge.authenticator.sdk.v2.api.API_AUTHORIZATIONS
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationsListResponse
import com.saltedge.authenticator.sdk.v2.api.model.authorization.UpdateAuthorizationRequest
import com.saltedge.authenticator.sdk.v2.api.model.authorization.ConfirmDenyResponse
import com.saltedge.authenticator.sdk.v2.api.model.configuration.ConfigurationResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionRequest
import com.saltedge.authenticator.sdk.v2.api.model.connection.CreateConnectionResponse
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionRequest
import com.saltedge.authenticator.sdk.v2.api.model.connection.RevokeConnectionResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * Describes SCA Service API v2. Verbs, request and response format
 */
interface ApiInterface {

    @GET
    fun getProviderConfiguration(@Url requestUrl: String): Call<ConfigurationResponse>

    @POST
    fun createConnection(
        @Url requestUrl: String,
        @Body body: CreateConnectionRequest
    ): Call<CreateConnectionResponse>

    @PUT
    fun revokeConnection(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>,
        @Body requestBody: RevokeConnectionRequest
    ): Call<RevokeConnectionResponse>

    @GET(API_AUTHORIZATIONS)
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
    ): Call<ConfirmDenyResponse>

    @PUT
    fun denyAuthorization(
        @Url requestUrl: String,
        @HeaderMap headersMap: Map<String, String>,
        @Body requestBody: UpdateAuthorizationRequest
    ): Call<ConfirmDenyResponse>
}
