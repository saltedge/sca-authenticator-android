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
package com.saltedge.authenticator.tool

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.saltedge.authenticator.BuildConfig

private const val MAX_LOG_SIZE = 1000

/**
 * Show log/logs with given message
 *
 * @receiver Throwable
 * @param message - the message that appears in the log
 */
fun Throwable.log(message: String = "") {
    try {
        if (BuildConfig.DEBUG) {
            printToLogcat("${this.cause}", message)
            printStackTrace()
        } else Crashlytics.logException(this)
    } catch (e: Exception) {
        Crashlytics.logException(e)
    }
}

/**
 * Create an error collector for the application not for DEBUG version
 *
 * @return crashlytics
 */
fun createCrashlyticsKit(): Crashlytics =
        Crashlytics.Builder().core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()

/**
 * Show log/logs with given message and tag
 *
 * @param tag - the tag that appears in the log
 * @param message - the message that appears in the log
 */
fun printToLogcat(tag: String, message: String) {
    if (BuildConfig.DEBUG && message.isNotEmpty()) {
        var start = 0
        while (start < message.length) {
            var end = start + MAX_LOG_SIZE
            if (end > message.length) end = message.length
            Log.d(tag, message.substring(start, end).trim())
            start += MAX_LOG_SIZE
        }
    }
}
