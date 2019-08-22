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

import android.content.Context
import org.joda.time.DateTime

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
    Math.ceil((remainedMillis.toDouble() / MILLIS_IN_MINUTE)).toInt()

/**
 * Converts the current date time to a string presentation
 *
 * @receiver millis
 * @param appContext - application context
 * @return the date as a string
 */
fun DateTime.toLongDateString(appContext: Context): String =
    this.toString("d MMM yyyy, HH:mm", appContext.getCurrentAppLocale()) ?: ""
