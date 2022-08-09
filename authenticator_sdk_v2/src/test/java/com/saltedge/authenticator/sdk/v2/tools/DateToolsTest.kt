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
package com.saltedge.authenticator.sdk.v2.tools

import com.saltedge.authenticator.core.tools.*
import com.saltedge.authenticator.sdk.v2.TestTools
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DateToolsTest {

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestTools.applicationContext)
    }

    @Test
    @Throws(Exception::class)
    fun constTest() {
        assertThat(MILLIS_IN_MINUTE, equalTo(60000L))
    }

    @Test
    @Throws(Exception::class)
    fun isAfterOrEqualTest() {
        Assert.assertFalse(DateTime(100L).isAfterOrEqual(DateTime(101L)))
        Assert.assertTrue(DateTime(100L).isAfterOrEqual(DateTime(100L)))
        Assert.assertTrue(DateTime(100L).isAfterOrEqual(DateTime(99L)))
    }

    @Test
    @Throws(Exception::class)
    fun remainedPinWaitTimeTest() {
        assertThat(millisToRemainedMinutes(-MILLIS_IN_MINUTE), equalTo(-1))
        assertThat(millisToRemainedMinutes(0L), equalTo(0))
        assertThat(millisToRemainedMinutes(MILLIS_IN_MINUTE - 1L), equalTo(1))
        assertThat(millisToRemainedMinutes(119998), equalTo(2))
        assertThat(millisToRemainedMinutes(2 * MILLIS_IN_MINUTE), equalTo(2))
    }

    @Test
    @Throws(Exception::class)
    fun remainedSecondsTillExpireTest() {
        assertThat(DateTime.now().remainedSeconds(), equalTo(0))
        assertThat(DateTime.now().minusMinutes(1).remainedSeconds(), equalTo(0))
        assertThat(
            DateTime.now().plusMinutes(1).remainedSeconds(),
            anyOf(equalTo(59), equalTo(60))
        )
    }

    @Test
    @Throws(Exception::class)
    fun secondsFromDateTest() {
        assertThat(DateTime.now().secondsPassedFromDate(), equalTo(0))
        assertThat(DateTime.now().plusMinutes(1).secondsPassedFromDate(), equalTo(0))
        assertThat(DateTime.now().minusMinutes(1).secondsPassedFromDate(), equalTo(60))
    }

    @Test
    @Throws(Exception::class)
    fun remainedExpirationTimeTest() {
        assertThat(0.remainedTimeDescription(), equalTo("-:--"))
        assertThat((-60).remainedTimeDescription(), equalTo("-:--"))
        assertThat(60.remainedTimeDescription(), equalTo("1:00"))
        assertThat(659.remainedTimeDescription(), equalTo("10:59"))
        assertThat(3600.remainedTimeDescription(), equalTo("1:00:00"))
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
