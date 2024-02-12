/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.api.connector

import com.saltedge.authenticator.core.tools.createExpiresAtTime
import com.saltedge.authenticator.sdk.constants.API_AUTHORIZATIONS
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectionToolsTest {

    @Test
    @Throws(Exception::class)
    fun createRequestUrlTest() {
        assertThat(
            createRequestUrl("https://example.com", API_AUTHORIZATIONS),
            equalTo("https://example.com/api/authenticator/v1/authorizations")
        )
        assertThat(
            createRequestUrl("http://example.com/", API_AUTHORIZATIONS),
            equalTo("http://example.com/api/authenticator/v1/authorizations")
        )
        assertThat(
            createRequestUrl("https://connector.example.com:8080/path/my_id", API_AUTHORIZATIONS),
            equalTo("https://connector.example.com:8080/path/my_id/api/authenticator/v1/authorizations")
        )
    }

    @Test
    @Throws(Exception::class)
    fun createExpiresAtTimeTest() {
        assertThat(
            createExpiresAtTime(),
            equalTo((DateTime.now(DateTimeZone.UTC).plusMinutes(5).millis / 1000).toInt())
        )
        assertThat(
            createExpiresAtTime(3),
            equalTo((DateTime.now(DateTimeZone.UTC).plusMinutes(3).millis / 1000).toInt())
        )
    }
}
