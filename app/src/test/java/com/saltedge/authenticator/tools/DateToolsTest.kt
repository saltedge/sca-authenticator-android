/*
 * Copyright (c) 2020 Salt Edge Inc.
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
