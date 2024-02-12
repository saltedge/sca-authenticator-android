/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api

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
