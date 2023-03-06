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

import android.content.Context
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate

/**
 * Converts the current date time to a string presentation
 *
 * @receiver millis
 * @param appContext - application context
 * @return the date as a string
 */
fun DateTime.toDateFormatString(appContext: Context): String =
    this.toString("d MMMM yyyy", appContext.getCurrentAppLocale()) ?: ""

/**
 * Converts the current date time to a string presentation
 *
 * @receiver millis
 * @param appContext - application context
 * @return the date as a string with time and time zone
 */
fun DateTime.toDateFormatStringWithUTC(appContext: Context): String =
    this.toString("HH:mm, d MMMM yyyy z", appContext.getCurrentAppLocale()) ?: ""

/**
 * Calculates days count between now and expiresAt date
 *
 * @receiver expires at dateTime
 * @return days count
 */
fun DateTime.daysTillExpire() = Days.daysBetween(LocalDate.now(), this.toLocalDate()).days
