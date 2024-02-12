/*
 * Copyright (c) 2019 Salt Edge Inc.
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
    FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    if (BuildConfig.DEBUG) {
        Timber.plant(DebugTree())
    } else {
        Timber.plant(CrashlyticsReportingTree())
    }
}

/**
 * Show log/logs with given message
 *
 * @param message - the message that appears in the log
 */
class CrashlyticsReportingTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
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
