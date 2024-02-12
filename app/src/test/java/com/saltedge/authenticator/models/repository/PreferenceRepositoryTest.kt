/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.models.repository

import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.app.getDefaultSystemNightMode
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PreferenceRepositoryTest {

    @Before
    fun setUp() {
        PreferenceRepository.initObject(TestAppTools.applicationContext)
    }

    @Test
    @Throws(Exception::class)
    fun encryptedPasscodeTest() {
        clearPreferences()

        assertTrue(PreferenceRepository.encryptedPasscode.isEmpty())

        PreferenceRepository.encryptedPasscode = "test"

        assertThat(PreferenceRepository.encryptedPasscode, equalTo("test"))
        assertThat(
            PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).getString(
                KEY_PASSCODE,
                ""
            )!!,
            equalTo("test")
        )
    }

    @Test
    @Throws(Exception::class)
    fun dbKeyTest() {
        clearPreferences()

        assertTrue(PreferenceRepository.dbKey.isEmpty())

        PreferenceRepository.dbKey = "dbKey"

        assertThat(PreferenceRepository.dbKey, equalTo("dbKey"))
        assertThat(
            PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).getString(
                KEY_DATABASE_KEY,
                ""
            )!!,
            equalTo("dbKey")
        )
    }

    @Test
    @Throws(Exception::class)
    fun nightModeTest() {
        PreferenceRepository.nightMode = AppCompatDelegate.MODE_NIGHT_NO

        assertThat(PreferenceRepository.nightMode, equalTo(AppCompatDelegate.MODE_NIGHT_NO))

        PreferenceRepository.nightMode = AppCompatDelegate.MODE_NIGHT_YES

        assertThat(PreferenceRepository.nightMode, equalTo(AppCompatDelegate.MODE_NIGHT_YES))
    }

    @Test
    @Throws(Exception::class)
    fun notificationsEnabledTest() {
        clearPreferences()

        assertFalse(PreferenceRepository.notificationsEnabled)

        PreferenceRepository.notificationsEnabled = true

        assertTrue(PreferenceRepository.notificationsEnabled)
        assertTrue(
            PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).getBoolean(
                KEY_NOTIFICATIONS,
                false
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun screenshotLockEnabledTest() {
        clearPreferences()

        assertTrue(PreferenceRepository.screenshotLockEnabled)

        PreferenceRepository.screenshotLockEnabled = false

        assertFalse(PreferenceRepository.screenshotLockEnabled)
        assertFalse(
            PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).getBoolean(
                KEY_SCREENSHOT_LOCK,
                true
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun currentLocaleTest() {
        clearPreferences()

        assertNull(PreferenceRepository.currentLocale)

        PreferenceRepository.currentLocale = "en"

        assertThat(PreferenceRepository.currentLocale, equalTo("en"))
        assertThat(
            PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).getString(
                KEY_LOCALE,
                ""
            )!!,
            equalTo("en")
        )
    }

    @Test
    @Throws(Exception::class)
    fun pinInputAttemptsTest() {
        clearPreferences()

        assertThat(PreferenceRepository.pinInputAttempts, equalTo(0))

        PreferenceRepository.pinInputAttempts = 9

        assertThat(PreferenceRepository.pinInputAttempts, equalTo(9))
        assertThat(
            PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).getInt(
                KEY_PIN_INPUT_ATTEMPTS,
                0
            ),
            equalTo(9)
        )
    }

    @Test
    @Throws(Exception::class)
    fun pinInputWaitTillTimeTest() {
        clearPreferences()

        assertThat(PreferenceRepository.blockPinInputTillTime, equalTo(0L))

        PreferenceRepository.blockPinInputTillTime = 9000L

        assertThat(PreferenceRepository.blockPinInputTillTime, equalTo(9000L))
        assertThat(
            PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).getLong(
                KEY_PIN_INPUT_TIME,
                0L
            ),
            equalTo(9000L)
        )
    }

    @Test
    @Throws(Exception::class)
    fun passcodeExistTest() {
        clearPreferences()

        assertFalse(PreferenceRepository.passcodeExist())

        PreferenceRepository.encryptedPasscode = "test"

        assertTrue(PreferenceRepository.passcodeExist())
    }

    @Test
    @Throws(Exception::class)
    fun clearUserPreferencesTest() {
        val expectedMode = getDefaultSystemNightMode()

        PreferenceRepository.encryptedPasscode = "test"
        PreferenceRepository.nightMode = 1
        PreferenceRepository.notificationsEnabled = true
        PreferenceRepository.currentLocale = "en"
        PreferenceRepository.pinInputAttempts = 9
        PreferenceRepository.blockPinInputTillTime = 9000L
        PreferenceRepository.dbKey = "dbKey"

        assertThat(PreferenceRepository.encryptedPasscode, equalTo("test"))
        assertThat(PreferenceRepository.nightMode, equalTo(1))
        assertTrue(PreferenceRepository.notificationsEnabled)
        assertThat(PreferenceRepository.currentLocale, equalTo("en"))
        assertThat(PreferenceRepository.pinInputAttempts, equalTo(9))
        assertThat(PreferenceRepository.blockPinInputTillTime, equalTo(9000L))
        assertThat(PreferenceRepository.dbKey, equalTo("dbKey"))

        PreferenceRepository.clearUserPreferences()

        assertTrue(PreferenceRepository.encryptedPasscode.isEmpty())
        assertThat(PreferenceRepository.nightMode, equalTo(expectedMode))
        assertFalse(PreferenceRepository.notificationsEnabled)
        assertNull(PreferenceRepository.currentLocale)
        assertThat(PreferenceRepository.pinInputAttempts, equalTo(0))
        assertThat(PreferenceRepository.blockPinInputTillTime, equalTo(0L))
        assertThat(PreferenceRepository.dbKey, equalTo("dbKey"))
    }

    @Test
    @Throws(Exception::class)
    fun cloudMessagingTokenTest() {
        clearPreferences()

        assertTrue(PreferenceRepository.cloudMessagingToken.isEmpty())

        PreferenceRepository.cloudMessagingToken = "token"

        assertThat(PreferenceRepository.cloudMessagingToken, equalTo("token"))
        assertThat(
            PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext)
                .getString("KEY_CLOUD_MESSAGING_TOKEN", "")!!, equalTo("token")
        )
    }

//    TODO: test on Android Q version http://robolectric.org/configuring/
//    @Test
//    @Throws(Exception::class)
//    fun systemNightModeTest() {
//        clearPreferences()
//
//        assertTrue(PreferenceRepository.systemNightMode)
//
//        PreferenceRepository.systemNightMode = false
//
//        assertFalse(PreferenceRepository.systemNightMode)
//        assertFalse(
//            PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).getBoolean(
//                KEY_SYSTEM_DARK_MODE,
//                true
//            )
//        )
//    }

        @Test
        @Throws(Exception::class)
        fun systemNightModeTest() {
            clearPreferences()
            PreferenceRepository.systemNightMode = false

            assertFalse(PreferenceRepository.systemNightMode)
            assertFalse(
                PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).getBoolean(
                    KEY_SYSTEM_NIGHT_MODE,
                    true
                )
            )
        }

    private fun clearPreferences() {
        PreferenceManager.getDefaultSharedPreferences(TestAppTools.applicationContext).edit().clear().apply()
    }
}
