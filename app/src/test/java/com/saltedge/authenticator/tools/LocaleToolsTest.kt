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

import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.repository.PreferenceRepository
import com.saltedge.authenticator.TestAppTools
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.isEmptyString
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class LocaleToolsTest {

    @Test
    @Throws(Exception::class)
    fun constantsTest() {
        assertThat(DEFAULT_LOCALE_CODE, equalTo("en"))
    }

    @Test
    @Throws(Exception::class)
    fun availableLocalizationsTest() {
        assertThat(TestAppTools.applicationContext.getAvailableLocalizations(), equalTo(listOf("en")))
    }

    @Test
    @Throws(Exception::class)
    fun applyPreferenceLocaleTest() {
        PreferenceRepository.currentLocale = "en"
        TestAppTools.applicationContext.applyPreferenceLocale()

        assertEquals("en", TestAppTools.applicationContext.getCurrentAppLocale()?.language)
    }

    @Test
    @Throws(Exception::class)
    fun updateApplicationLocaleTest() {
        TestAppTools.applicationContext.updateApplicationLocale(Locale("en"))

        assertEquals("Cancel", TestAppTools.getString(R.string.actions_cancel))
    }

    @Test
    @Throws(Exception::class)
    fun currentAppLocaleTest() {
        TestAppTools.setLocale("en")

        assertEquals("en", TestAppTools.applicationContext.getCurrentAppLocale()?.language)

        TestAppTools.setLocale("ru")

        assertEquals("ru", TestAppTools.applicationContext.getCurrentAppLocale()?.language)
    }

    @Test
    @Throws(Exception::class)
    fun languageNameTest() {
        assertThat(null.languageName, isEmptyString())

        TestAppTools.setLocale("ru")

        assertEquals("Русский", TestAppTools.applicationContext.getCurrentAppLocale()?.languageName)
        assertThat(Locale.ENGLISH.languageName, equalTo("English"))
    }

    @Test
    @Throws(Exception::class)
    fun currentAppLocaleLanguageNameTest() {
        TestAppTools.setLocale("en")

        assertEquals("English", TestAppTools.applicationContext.currentAppLocaleName())

        TestAppTools.setLocale("ru")

        assertEquals("Русский", TestAppTools.applicationContext.currentAppLocaleName())
    }

    @Test
    @Throws(Exception::class)
    fun languageCodeToNameTest() {
        assertEquals("English", "en_US".localeCodeToName())
        assertEquals("Русский", "ru".localeCodeToName())
        assertEquals("Deutsch", "de".localeCodeToName())
        assertEquals("English", "en".localeCodeToName())
        assertEquals("Română", "ro".localeCodeToName())
        assertEquals("", "".localeCodeToName())
    }
}
