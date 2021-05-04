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
package com.saltedge.authenticator.sdk.v2.tools.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.saltedge.authenticator.sdk.v2.TestTools
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocalDateAdapterTest {

    private var gson: Gson =
        GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestTools.applicationContext)
    }

    @Test
    @Throws(Exception::class)
    fun testDeserializeLocalDate() {
        assertThat(
            gson.fromJson("\"2015-01-01\"", LocalDate::class.java),
            equalTo(LocalDate.parse("2015-01-01"))
        )
        assertThat(
            gson.fromJson("\"Z20150101\"", LocalDate::class.java),
            equalTo(LocalDate(0))
        )
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeZeroDateTime() {
        assertThat(gson.toJson(0L.toLocalDate()), equalTo("\"1970-01-01\""))
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeAnyDateTime() {
        assertThat(gson.toJson(1420074123000L.toLocalDate()), equalTo("\"2015-01-01\""))
    }

    private fun Long.toLocalDate(): LocalDate = LocalDate(this, DateTimeZone.UTC)
}
