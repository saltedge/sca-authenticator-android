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
package com.saltedge.authenticator.sdk.tools

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
