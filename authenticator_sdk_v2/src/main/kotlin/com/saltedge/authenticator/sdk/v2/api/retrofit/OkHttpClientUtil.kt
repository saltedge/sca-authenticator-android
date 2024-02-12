/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api.retrofit

import com.saltedge.authenticator.sdk.v2.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level

/**
 * Creates OkHttpClient
 * add header interceptor, add logging interceptor, add timeouts
 *
 * @return OkHttpClient
 */
internal fun createOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(HeaderInterceptor())
        .addInterceptor(createHttpLoggingInterceptor())
        .build()
}

/**
 * Creates header interceptor
 * with log level BODY for DEBUG build type or NONE for RELEASE build type
 *
 * @return retrofit interceptor
 */
private fun createHttpLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
    }
}
