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

import com.saltedge.authenticator.sdk.model.ProviderResponseData
import com.saltedge.authenticator.sdk.model.request.ConfirmDenyRequestData
import com.saltedge.authenticator.sdk.model.request.CreateConnectionRequestData
import com.saltedge.authenticator.sdk.model.response.*
import retrofit2.Call
import retrofit2.http.*

/**
 * Describes Identity Service API. Verbs, request and response format
 */
interface ApiInterface {

    @GET
    fun getProviderData(@Url requestUrl: String): Call<ProviderResponseData>

    @POST
    fun postNewConnectionData(
            @Url requestUrl: String,
            @Body body: CreateConnectionRequestData): Call<CreateConnectionResponseData>

    @DELETE
    fun deleteAccessToken(
            @Url requestUrl: String,
            @HeaderMap headersMap: Map<String, String>): Call<RevokeAccessTokenResponseData>

    @GET
    fun getAuthorizations(
            @Url requestUrl: String,
            @HeaderMap headersMap: Map<String, String>): Call<AuthorizationsResponseData>

    @GET
    fun getAuthorization(
            @Url requestUrl: String,
            @HeaderMap headersMap: Map<String, String>): Call<AuthorizationShowResponseData>

    @PUT
    fun updateAuthorization(
            @Url requestUrl: String,
            @HeaderMap headersMap: Map<String, String>,
            @Body requestBody: ConfirmDenyRequestData): Call<ConfirmDenyResponseData>
}
