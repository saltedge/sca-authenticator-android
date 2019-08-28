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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.saltedge.authenticator.sdk.model.ConnectionAbs
import okhttp3.ResponseBody
import retrofit2.Response

object TestTools {

    val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Context>()

    fun getString(resId: Int): String {
        return try {
            applicationContext.getString(resId) ?: ""
        } catch (ignored: Exception) {
            ""
        }
    }
}

fun Any.toJsonString(): String = Gson().toJson(this)

@Throws(Exception::class)
fun getDefaultTestConnection(): ConnectionAbs =
    TestConnection(
        id = "333",
        guid = "test",
        connectUrl = "https://localhost",
        accessToken = "accessToken"
    )

@Throws(Exception::class)
fun <T> get404Response(): Response<T> = Response.error(404, ResponseBody.create(null, get404ResponseBody()))

@Throws(Exception::class)
private fun get404ResponseBody(): String = "{\"error_class\": \"NotFound\",\"error_message\": \"Resource not found\"}"
