/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import android.annotation.SuppressLint
import android.content.Context
import android.os.LocaleList
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.buildVersion24orGreater
import com.saltedge.authenticator.models.repository.PreferenceRepository
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
@SuppressLint("NewApi")
@Suppress("DEPRECATION")
fun Context.updateApplicationLocale(locale: Locale) {
    val resources = this.resources
    val configuration = resources?.configuration
    Locale.setDefault(locale)
    if (buildVersion24orGreater) configuration?.setLocales(LocaleList(locale))
    else configuration?.locale = locale

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
    return if (buildVersion24orGreater) resources.configuration?.locales?.get(0)
    else resources?.configuration?.locale
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
