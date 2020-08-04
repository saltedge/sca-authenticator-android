/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.consents.common

import android.content.Context
import com.saltedge.authenticator.R
import com.saltedge.authenticator.sdk.model.ConsentData

/**
 * Get size of collection and create string like `%count consents`
 *
 * @receiver collection of consents
 * @param appContext
 * @return spanned string
 */
fun List<ConsentData>.toCountString(appContext: Context): String {
    return if (this.isEmpty()) ""
    else appContext.resources.getQuantityString(
        R.plurals.count_of_consents,
        this.count(),
        this.count()
    )
}

/**
 * Create prefix string for each item of connections list.
 * Prefix has format `%count consents ·`
 *
 * @param count of consents
 * @param appContext
 * @return spanned string
 */
fun consentsCountPrefixForConnection(count: Int, appContext: Context): String {
    if (count < 1) return ""
    val quantityString = appContext.resources.getQuantityString(
        R.plurals.count_of_consents,
        count,
        count
    )
    return "$quantityString\u30FB"
}

/**
 * Create string with count of days like `@count day left`
 *
 * @param countOfDays
 * @param appContext
 * @return string
 */
fun countOfDaysLeft(countOfDays: Int, appContext: Context): String {
    val template = appContext.getString(R.string.days_left)
    return String.format(template, countOfDays(countOfDays, appContext))
}

/**
 * Create string with count of days like `@count day`
 *
 * @param countOfDays
 * @param appContext
 * @return string
 */
fun countOfDays(countOfDays: Int, appContext: Context): String {
    return appContext.resources.getQuantityString(
        R.plurals.count_of_days,
        countOfDays,
        countOfDays
    )
}
