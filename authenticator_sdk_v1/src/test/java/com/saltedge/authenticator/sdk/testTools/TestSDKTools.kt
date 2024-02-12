/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.testTools

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

object TestTools {

    val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Context>().applicationContext

    fun getString(resId: Int): String {
        return try {
            applicationContext.getString(resId)
        } catch (ignored: Exception) {
            ""
        }
    }
}

@Throws(Exception::class)
fun <T> get404Response(): Response<T> = Response.error(404, get404ResponseBody().toResponseBody(null)
)

@Throws(Exception::class)
private fun get404ResponseBody(): String = "{\"error_class\": \"NotFound\",\"error_message\": \"Resource not found\"}"
