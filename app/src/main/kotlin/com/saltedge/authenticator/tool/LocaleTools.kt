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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.LocaleList
import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.repository.PreferenceRepository
import java.util.*

const val DEFAULT_LOCALE_CODE = "en"

/**
 * Read language setting from preferences and apply it as application locale
 *
 * @receiver context - application context
 * @see PreferenceRepository.currentLocale
 */
fun Context.applyPreferenceLocale() {
    val localeCode = PreferenceRepository.currentLocale ?: DEFAULT_LOCALE_CODE
    val settingsLocale = Locale(localeCode)
    val appLocale = getCurrentAppLocale() ?: return
    if (appLocale != settingsLocale) this.updateApplicationLocale(settingsLocale)
}

/**
 * Update application locale
 *
 * @receiver context - application context
 * @param locale - application locale
 */
@Suppress("DEPRECATION")
fun Context.updateApplicationLocale(locale: Locale) {
    val resources = this.resources
    val configuration = resources?.configuration
    Locale.setDefault(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration?.setLocales(LocaleList(locale))
    } else {
        configuration?.locale = locale
    }
    resources?.updateConfiguration(configuration, resources.displayMetrics)
}

/**
 * Get available localization are stored in file with path == LOCALES_FILE_PATH for application
 *
 * @receiver context - application context
 * @return list of available localizations
 */
fun Context.getAvailableLocalizations(): List<String> {
    val availableLocales = resources.getStringArray(R.array.available_locales)
    return if (availableLocales.isEmpty()) listOf(DEFAULT_LOCALE_CODE)
    else availableLocales.toList()
}

/**
 * Get current application locale name
 *
 * @receiver context - application context
 * @return current application locale code
 * @see languageName
 */
fun Context.currentAppLocaleName(): String? = getCurrentAppLocale()?.languageName

/**
 * Get current application locale
 *
 * @receiver context - application context
 * @return current application locale object
 */
@Suppress("DEPRECATION")
@SuppressLint("NewApi")
fun Context.getCurrentAppLocale(): Locale? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        resources.configuration?.locales?.get(0) else resources?.configuration?.locale
}

/**
 * Get language name from the given Local object
 *
 * @receiver locale
 * @return language name
 */
val Locale?.languageName: String get() = this?.getDisplayLanguage(this)?.capitalize() ?: ""

/**
 * Convert locale code to language name
 *
 * @receiver string language code
 * @return language name
 */
fun String.localeCodeToName() = Locale(this.take(2)).languageName
