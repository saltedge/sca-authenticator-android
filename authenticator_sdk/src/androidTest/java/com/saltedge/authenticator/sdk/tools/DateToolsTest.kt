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
package com.saltedge.authenticator.sdk.tools

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DateToolsTest {

    @Test
    @Throws(Exception::class)
    fun remainedSecondsTillExpireTest() {
        assertThat(DateTime.now().remainedSecondsTillExpire(), equalTo(0))
        assertThat(DateTime.now().minusMinutes(1).remainedSecondsTillExpire(), equalTo(0))
        assertThat(
            DateTime.now().plusMinutes(1).remainedSecondsTillExpire(),
            anyOf(equalTo(59), equalTo(60))
        )
    }

    @Test
    @Throws(Exception::class)
    fun secondsFromDateTest() {
        assertThat(DateTime.now().secondsFromDate(), equalTo(0))
        assertThat(DateTime.now().plusMinutes(1).secondsFromDate(), equalTo(0))
        assertThat(DateTime.now().minusMinutes(1).secondsFromDate(), equalTo(60))
    }

    @Test
    @Throws(Exception::class)
    fun remainedExpirationTimeTest() {
        assertThat(DateTime.now().remainedExpirationTime(), equalTo("-:--"))
        assertThat(DateTime.now().minusMinutes(1).remainedExpirationTime(), equalTo("-:--"))
        assertThat(
            DateTime.now().plusMinutes(1).remainedExpirationTime(),
            anyOf(equalTo("0:59"), equalTo("1:00"))
        )
        assertThat(
            DateTime.now().plusMinutes(10).plusSeconds(59).remainedExpirationTime(),
            anyOf(equalTo("10:58"), equalTo("10:59"))
        )
        assertThat(
            DateTime.now().plusHours(1).remainedExpirationTime(),
            anyOf(equalTo("59:59"), equalTo("1:00:00"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun secondsBetweenDatesTest() {
        val baseDate = DateTime.now()

        assertThat(secondsBetweenDates(baseDate, baseDate.plusMillis(5999)), equalTo(5))
        assertThat(secondsBetweenDates(baseDate, baseDate.plusMinutes(1)), equalTo(60))
        assertThat(secondsBetweenDates(baseDate, baseDate), equalTo(0))
        assertThat(secondsBetweenDates(baseDate, baseDate.minusSeconds(1)), equalTo(0))
    }
}
