/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.tools.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.saltedge.authenticator.core.tools.json.DateTimeAdapter
import com.saltedge.authenticator.sdk.v2.TestTools
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DateTimeAdapterTest {

    private var gson: Gson =
        GsonBuilder().registerTypeAdapter(DateTime::class.java, DateTimeAdapter()).create()

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestTools.applicationContext)
    }

    @Test
    @Throws(Exception::class)
    fun testDeserializeDateTime() {
        var jsonString = "\"2015-01-01T01:02:03.123Z\""

        assertThat(
            gson.fromJson(jsonString, DateTime::class.java),
            equalTo(DateTime(1420074123123).withZone(DateTimeZone.UTC))
        )

        jsonString = "\"20150101T010203123Z\""

        assertThat(
            gson.fromJson(jsonString, DateTime::class.java),
            equalTo(DateTime(0).withZone(DateTimeZone.UTC))
        )
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeZeroDateTime() {
        assertThat(gson.toJson(DateTime(0)), equalTo("\"1970-01-01T00:00:00.000Z\""))
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeAnyDateTime() {
        assertThat(gson.toJson(DateTime(1420074123123)), equalTo("\"2015-01-01T01:02:03.123Z\""))
    }
}
