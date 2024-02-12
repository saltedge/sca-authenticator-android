/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.saltedge.authenticator.tools.updateApplicationLocale
import java.util.*

object TestAppTools {

    val applicationContext: Context
        get() = ApplicationProvider.getApplicationContext<Context>().applicationContext

    fun getString(resId: Int): String {
        return try {
            applicationContext.getString(resId) ?: ""
        } catch (ignored: Exception) {
            ""
        }
    }

    @Throws(Exception::class)
    fun setLocale(language: String, country: String = "") {
        applicationContext.updateApplicationLocale(Locale(language, country))
    }
}
