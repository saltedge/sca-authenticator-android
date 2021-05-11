/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator.core.tools

import android.util.Log
import com.saltedge.authenticator.core.BuildConfig

private const val MAX_LOG_SIZE = 1000

/**
 * Show debug log/logs with given message and tag if message is not empty
 *
 * @param tag - the tag that appears in the log
 * @param message - the message that appears in the log
 */
internal fun printDebugLog(tag: String, message: String) {
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

/**
 * String split into substrings. The maximum length of the substring
 * is determined by a parameter @param lineLength
 *
 * @receiver String object
 * @param lineLength - maximum number of characters per line
 * @return String object
 */
internal fun String.splitToLines(lineLength: Int): String {
    return if (lineLength <= 0 || this.length <= lineLength) this
    else {
        var result = ""
        var index = 0
        while (index < length) {
            val line = substring(index, Math.min(index + lineLength, length))
            result += (if (result.isEmpty()) line else "\n$line")
            index += lineLength
        }
        result
    }
}

/**
 * Checks if string is not null or empty
 *
 * @receiver String object
 * @return boolean, true if string is not null or empty
 */
fun String?.isNotNullOrEmpty(): Boolean = !this.isNullOrEmpty()
