/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.saltedge.android.test_tools.TestConnection
import com.saltedge.authenticator.core.model.ConnectionAbs
import okhttp3.ResponseBody
import retrofit2.Response

object TestTools {

    val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Context>().applicationContext

    fun getString(resId: Int): String {
        return try {
            applicationContext.getString(resId) ?: ""
        } catch (ignored: Exception) {
            ""
        }
    }
}

val defaultTestConnection: ConnectionAbs
    get() = TestConnection(id = "333", guid = "test", connectUrl = "https://localhost", accessToken = "accessToken")

@Throws(Exception::class)
fun <T> get404Response(): Response<T> = Response.error(404, ResponseBody.create(null, get404ResponseBody()))

@Throws(Exception::class)
private fun get404ResponseBody(): String = "{\"error_class\": \"NotFound\",\"error_message\": \"Resource not found\"}"
