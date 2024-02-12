/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.instrumentationTestTools

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.saltedge.authenticator.tools.updateApplicationLocale
import java.util.*

object TestTools {

    val applicationContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    fun getString(resId: Int): String {
        return try {
            applicationContext.getString(resId) ?: ""
        } catch (ignored: Exception) {
            ""
        }
    }

    @Throws(Exception::class)
    fun setLocale(language: String, country: String = "") {
        InstrumentationRegistry.getInstrumentation().targetContext.updateApplicationLocale(
            Locale(
                language,
                country
            )
        )
    }
}
