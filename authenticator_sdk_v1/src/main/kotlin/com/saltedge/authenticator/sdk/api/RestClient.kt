/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api

import com.google.gson.Gson
import com.saltedge.authenticator.core.tools.json.createDefaultGson
import com.saltedge.authenticator.sdk.constants.DEFAULT_HOST
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
