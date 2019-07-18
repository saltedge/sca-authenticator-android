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
import android.graphics.Color
import android.os.Build
import android.os.LocaleList
import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import java.util.*

object TestTools {

    val applicationContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    fun getColor(resId: Int): Int {
        try {
            return ContextCompat.getColor(applicationContext, resId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Color.BLACK
    }

    fun getString(resId: Int): String {
        return try {
            applicationContext.getString(resId) ?: ""
        } catch (ignored: Exception) {
            ""
        }
    }

    @Throws(Exception::class)
    fun setLocale(language: String, country: String = "") {
        InstrumentationRegistry.getInstrumentation().targetContext.updateApplicationLocale(Locale(language, country))
    }
}

private fun Context.updateApplicationLocale(locale: Locale) {
    val resources = this.resources
    val configuration = resources?.configuration
    Locale.setDefault(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration?.locales = LocaleList(locale)
    } else {
        configuration?.locale = locale
    }
    resources?.updateConfiguration(configuration, resources.displayMetrics)
}

fun Any.toJsonString(): String = Gson().toJson(this)
