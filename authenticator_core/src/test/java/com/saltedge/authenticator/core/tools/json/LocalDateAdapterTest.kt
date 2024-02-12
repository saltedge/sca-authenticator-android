/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools.json

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

    private val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Context>().applicationContext
    private var gson: Gson =
        GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()

    @Before
    fun setUp() {
        JodaTimeAndroid.init(applicationContext)
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
