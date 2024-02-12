/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api

import com.saltedge.authenticator.sdk.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

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
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}
