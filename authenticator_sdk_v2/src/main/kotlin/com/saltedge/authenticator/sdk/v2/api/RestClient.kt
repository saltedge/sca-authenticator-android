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

import com.google.gson.Gson
import com.saltedge.authenticator.sdk.v2.config.DEFAULT_HOST
import com.saltedge.authenticator.sdk.v2.tools.createDefaultGson
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
