/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Base64CompatTest {

    private val testString =
        "Salt Edge Solution for PISP is designed to help your business embrace the open source technology opportunities."
    private val base64String =
        "U2FsdCBFZGdlIFNvbHV0aW9uIGZvciBQSVNQIGlzIGRlc2lnbmVkIHRvIGhlbHAg\n" +
            "eW91ciBidXNpbmVzcyBlbWJyYWNlIHRoZSBvcGVuIHNvdXJjZSB0ZWNobm9sb2d5\n" +
            "IG9wcG9ydHVuaXRpZXMu"

    @Test
    @Throws(Exception::class)
    fun decodeFromPemBase64StringTest() {
        assertThat(String(decodeFromPemBase64String(base64String)!!), equalTo(testString))
    }

    @Test
    @Throws(Exception::class)
    fun encodeToPemBase64StringTest() {
        assertThat(encodeToPemBase64String(testString.toByteArray())!!, equalTo(base64String))
    }
}
