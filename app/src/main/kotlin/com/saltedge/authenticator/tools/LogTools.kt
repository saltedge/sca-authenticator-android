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
package com.saltedge.authenticator.tools

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.saltedge.authenticator.BuildConfig
import timber.log.Timber
import timber.log.Timber.DebugTree

/**
 * Create an error collector for the application not for DEBUG version
 *
 * @return crashlytics
 */
fun createCrashlyticsKit() {
    if (BuildConfig.DEBUG) {
        Timber.plant(DebugTree())
    } else {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        Timber.plant(CrashlyticsReportingTree())
    }
}

/**
 * Show log/logs with given message
 *
 * @param message - the message that appears in the log
 */
class CrashlyticsReportingTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, throwable : Throwable?) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log(message)
        if (throwable != null) {
            if (priority == Log.ERROR) {
                crashlytics.recordException(throwable)
            } else {
                crashlytics.log("Exception: ${throwable.stackTraceToString()}")
            }
        }
    }
}
