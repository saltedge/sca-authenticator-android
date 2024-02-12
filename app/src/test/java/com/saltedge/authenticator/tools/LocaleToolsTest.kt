/*
 * Copyright (c) 2019 Salt Edge Inc.
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
