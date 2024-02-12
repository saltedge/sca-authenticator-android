/*
 * Copyright (c) 2019 Salt Edge Inc.
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
