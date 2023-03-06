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

import com.saltedge.authenticator.core.api.DEFAULT_EXPIRATION_MINUTES
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import kotlin.math.ceil

const val MILLIS_IN_MINUTE = 60000L

/**
 * Convert milliseconds to DateTime object
 *
 * @receiver millis
 * @return date time object
 */
fun Long.toDateTime(): DateTime = DateTime(this)

/**
 * Checks whether specific conditions(isAfter or isEqual) are met
 *
 * @receiver millis
 * @return boolean, true if one of the conditions is true
 */
fun DateTime.isAfterOrEqual(time: DateTime) = isAfter(time) || isEqual(time)

/**
 * Convert remained milliseconds to remained minutes
 *
 * @param remainedMillis - number of minutes remaining in milliseconds
 * @return milliseconds
 */
fun millisToRemainedMinutes(remainedMillis: Long): Int =
    ceil((remainedMillis.toDouble() / MILLIS_IN_MINUTE)).toInt()

/**
 * Parse String to DateTime object in UTC time zone
 *
 * @receiver datetime string
 * @return DateTime object
 */
fun String.parseToUtcDateTime(): DateTime? {
    return try {
        DateTime.parse(this).withZone(DateTimeZone.UTC)
    } catch (e: Exception) {
        null
    }
}

/**
 * Calculates seconds between now and expiration time
 *
 * @receiver expiresAt datetime
 * @return seconds till expire time
 */
fun DateTime.remainedSeconds(): Int = secondsBetweenDates(DateTime.now(this.zone), this)

/**
 * Calculates seconds between receiver's value and now
 *
 * @receiver expiresAt datetime
 * @return seconds from date
 */
fun DateTime.secondsPassedFromDate(): Int = secondsBetweenDates(this, DateTime.now(this.zone))

fun secondsBetweenDates(startDate: DateTime, endDate: DateTime): Int =
    (millisBetweenDates(startDate, endDate) / 1000).toInt()

/**
 * Create description of remained time
 *
 * @receiver period of remained seconds
 * @return String timestamp in "minutes:seconds" format
 */
fun Int.remainedTimeDescription(): String {
    return if (this <= 0) return "-:--"
    else {
        val period = Period(this * 1000L)
        PeriodFormatterBuilder()
            .appendHours()
            .appendSeparatorIfFieldsBefore(":")
            .printZeroAlways().minimumPrintedDigits(if (period.hours > 0) 2 else 1).appendMinutes()
            .appendSeparator(":")
            .minimumPrintedDigits(2).appendSeconds()
            .toFormatter().print(period)
    }
}

/**
 * Calculates seconds between now and receiver's value
 *
 * @receiver expiresAt datetime
 * @return String timestamp in "minutes:seconds" format
 */
fun DateTime.remainedTimeDescription(): String {
    val remainedSeconds = this.remainedSeconds()
    return if (remainedSeconds <= 0) return "-:--"
    else {
        val period = Period(remainedSeconds * 1000L)
        PeriodFormatterBuilder()
            .appendHours()
            .appendSeparatorIfFieldsBefore(":")
            .printZeroAlways().minimumPrintedDigits(if (period.hours > 0) 2 else 1).appendMinutes()
            .appendSeparator(":")
            .minimumPrintedDigits(2).appendSeconds()
            .toFormatter().print(period)
    }
}

/**
 * Return unix time (seconds) of current time plus timeout (by default 5 minutes)
 */
fun createExpiresAtTime(withMinutesTimeOut: Int = DEFAULT_EXPIRATION_MINUTES): Int =
    (DateTime.now(DateTimeZone.UTC).plusMinutes(withMinutesTimeOut).millis / 1000).toInt()

private fun millisBetweenDates(startDate: DateTime, endDate: DateTime): Long {
    val duration = endDate.millis - startDate.millis
    return if (duration < 0) 0 else duration
}
