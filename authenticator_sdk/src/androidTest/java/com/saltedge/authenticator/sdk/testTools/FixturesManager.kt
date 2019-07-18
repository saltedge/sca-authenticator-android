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
package com.saltedge.authenticator.sdk.testTools

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.saltedge.authenticator.sdk.network.adapter.DateTimeAdapter
import com.saltedge.authenticator.sdk.network.adapter.LocalDateAdapter
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

@Throws(Exception::class)
fun getFixtureContent(fixtureFilename: String): String {
    return streamToString(TestTools.testContext.resources.assets.open(fixtureFilename))
}

@Throws(Exception::class)
fun get404Response(): String = getFixtureContent("${API_RESPONSE_PATH}404.json")

private const val API_RESPONSE_PATH = "api/response/"

private fun streamToString(input: InputStream): String {
    val streamReader = InputStreamReader(input)
    val stringBuilder = StringBuilder()
    val bufferedReader = BufferedReader(streamReader)
    var readiedLine: String?
    try {
        readiedLine = bufferedReader.readLine()
        while (readiedLine != null) {
            stringBuilder.append(readiedLine)
            readiedLine = bufferedReader.readLine()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return stringBuilder.toString()
}
