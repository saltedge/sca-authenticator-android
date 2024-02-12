/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.android.security.checkers

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * Attached debugger checker. Checks if DEBUG mode is true
 *
 * @receiver context of Application
 * @return null if nothing to report or non-empty report string
 */
internal fun Context.checkIfAppDebuggable(): String? {
    val result = this.applicationInfo.flags.and(ApplicationInfo.FLAG_DEBUGGABLE) != 0
    return if (result) "AppIsDebuggable" else null
}
