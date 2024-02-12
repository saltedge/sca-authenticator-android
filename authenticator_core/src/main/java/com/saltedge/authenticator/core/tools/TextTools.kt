/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import com.saltedge.authenticator.core.BuildConfig
import timber.log.Timber

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
            Timber.d(message.substring(start, end).trim())
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
