/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.api

import com.saltedge.authenticator.sdk.v2.api.retrofit.createOkHttpClient
import org.junit.Assert
import org.junit.Test

class OkHttpClientUtilTest {

    @Test
    @Throws(Exception::class)
    fun createOkHttpClientTest() {
        val client = createOkHttpClient()

        Assert.assertNotNull(client)
    }
}
