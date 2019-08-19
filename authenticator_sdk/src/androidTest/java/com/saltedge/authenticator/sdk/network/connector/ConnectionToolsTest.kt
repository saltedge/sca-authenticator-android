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
package com.saltedge.authenticator.sdk.network.connector

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.sdk.constants.API_AUTHORIZATIONS
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionToolsTest {

    @Test
    @Throws(Exception::class)
    fun createRequestUrlTest() {
        assertThat(createRequestUrl("https://google.com", API_AUTHORIZATIONS),
                equalTo("https://google.com/api/authenticator/v1/authorizations"))
        assertThat(createRequestUrl("https://google.com/", API_AUTHORIZATIONS),
                equalTo("https://google.com/api/authenticator/v1/authorizations"))
        assertThat(createRequestUrl("https://google.com/", "my_route", API_AUTHORIZATIONS),
            equalTo("https://google.com/my_route/api/authenticator/v1/authorizations"))
        assertThat(createRequestUrl("https://google.com", "my_route", API_AUTHORIZATIONS),
            equalTo("https://google.com/my_route/api/authenticator/v1/authorizations"))
    }

    @Test
    @Throws(Exception::class)
    fun createExpiresAtTimeTest() {
        assertThat(createExpiresAtTime(),
                equalTo((DateTime.now(DateTimeZone.UTC).plusMinutes(5).millis / 1000).toInt()))
        assertThat(createExpiresAtTime(3),
                equalTo((DateTime.now(DateTimeZone.UTC).plusMinutes(3).millis / 1000).toInt()))
    }
}
