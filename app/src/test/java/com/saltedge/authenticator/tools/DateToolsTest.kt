/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.tools

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.CoreMatchers.equalTo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DateToolsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    @Throws(Exception::class)
    fun toDateFormatStringTest() {
        val testDate: DateTime = DateTime(2020, 9, 12, 10, 10, 10)

        assertThat(testDate.toDateFormatString(context), equalTo("12 September 2020"))
        assertThat(testDate.plusDays(1).toDateFormatString(context), equalTo("13 September 2020"))
    }

    @Test
    @Throws(Exception::class)
    fun toDateFormatStringWithUTCTest() {
        val testDate: DateTime = DateTime(2021, 9, 5, 14, 30, 30, DateTimeZone.UTC)

        assertThat(
            testDate.toDateFormatStringWithUTC(context),
            equalTo("14:30, 5 September 2021 UTC")
        )
    }
}
