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
package com.saltedge.authenticator.models.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.saltedge.authenticator.app.getDefaultSystemNightMode

const val KEY_DATABASE_KEY = "KEY_DATABASE_KEY"
const val KEY_LOCALE = "KEY_LOCALE"
const val KEY_PASSCODE = "KEY_PASSCODE"
const val KEY_NIGHT_MODE = "KEY_DARK_MODE"
const val KEY_NOTIFICATIONS = "KEY_NOTIFICATIONS"
const val KEY_SCREENSHOT_LOCK = "KEY_SCREENSHOT_LOCK"
const val KEY_PIN_INPUT_ATTEMPTS = "KEY_PIN_INPUT_ATTEMPTS"
const val KEY_PIN_INPUT_TIME = "KEY_PIN_INPUT_TIME"
const val KEY_CLOUD_MESSAGING_TOKEN = "KEY_CLOUD_MESSAGING_TOKEN"

object PreferenceRepository : PreferenceRepositoryAbs {

    private var preferences: SharedPreferences? = null

    /**
     * Init object: PreferenceRepository
     *
     * @param appContext - application context
     * @return preference repository
     */
    fun initObject(appContext: Context): PreferenceRepository {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        return this
    }

    /**
     * Computed property that read database key from preferences and saves to preferences.
     *
     * @return database key
     * @see saveValue
     */
    override var dbKey: String
        get() = preferences?.getString(KEY_DATABASE_KEY, "") ?: ""
        set(value) {
            preferences?.saveValue(KEY_DATABASE_KEY, value)
        }

    /**
     * Computed property that read/write the Dark mode state from preferences
     * Light — MODE_NIGHT_NO
     * Dark — MODE_NIGHT_YES
     * System default — MODE_NIGHT_FOLLOW_SYSTEM
     *
     * @return stored night mode or default one
     */
    override var nightMode: Int
        get() = preferences?.getInt(KEY_NIGHT_MODE, getDefaultSystemNightMode()) ?: getDefaultSystemNightMode()
        set(value) {
            preferences?.saveValue(KEY_NIGHT_MODE, value)
        }

    /**
     * Computed property that read the cloud messaging token from preferences and saves to preferences
     *
     * @return token
     * @see saveValue
     */
    override var cloudMessagingToken: String
        get() = preferences?.getString(KEY_CLOUD_MESSAGING_TOKEN, "") ?: ""
        set(value) {
            preferences?.saveValue(KEY_CLOUD_MESSAGING_TOKEN, value)
        }

    /**
     * Computed property that read the notifications state from preferences and saves to preferences
     *
     * @return boolean, true if notifications are enabled
     * @see saveValue
     */
    override var notificationsEnabled: Boolean
        get() = preferences?.getBoolean(KEY_NOTIFICATIONS, false) ?: false
        set(value) {
            preferences?.saveValue(KEY_NOTIFICATIONS, value)
        }

    /**
     * Computed property that read the required app locale from preferences and saves to preferences
     *
     * @return the locale as a string
     * @see saveValue
     */
    override var currentLocale: String?
        get() = preferences?.getString(KEY_LOCALE, null)
        set(value) {
            preferences?.saveValue(KEY_LOCALE, value)
        }

    /**
     * Computed property that read the number of pin input attempts from preferences
     * and saves to preferences
     *
     * @return the locale as a string
     * @see saveValue
     */
    override var pinInputAttempts: Int
        get() = preferences?.getInt(KEY_PIN_INPUT_ATTEMPTS, 0) ?: 0
        set(value) {
            preferences?.saveValue(KEY_PIN_INPUT_ATTEMPTS, value)
        }

    /**
     * Computed property that read the amount of block pin input from
     * preferences and saves to preferences
     *
     * @return time as a long
     * @see saveValue
     */
    override var blockPinInputTillTime: Long
        get() = preferences?.getLong(KEY_PIN_INPUT_TIME, 0L) ?: 0L
        set(value) {
            preferences?.saveValue(KEY_PIN_INPUT_TIME, value)
        }

    /**
     * Computed property that read screenshot lock state from preferences and saves to preferences.
     *
     * @return boolean, true if screenshot lock is enabled
     * @see saveValue
     */
    override var screenshotLockEnabled: Boolean
        get() = preferences?.getBoolean(KEY_SCREENSHOT_LOCK, true) ?: true
        set(value) {
            preferences?.saveValue(KEY_SCREENSHOT_LOCK, value)
        }

    /**
     * Computed property that read encrypted passcode from preferences and saves to preferences.
     *
     * @return encrypted key passcode
     * @see saveValue
     */
    override var encryptedPasscode: String
        get() = preferences?.getString(KEY_PASSCODE, "") ?: ""
        set(value) {
            preferences?.saveValue(KEY_PASSCODE, value)
        }

    /**
     * Check if passcode exist
     *
     * @return boolean, true if encryptedPasscode is not empty
     */
    override fun passcodeExist(): Boolean = encryptedPasscode.isNotEmpty()

    /**
     * Clear all preferences from app except database key
     */
    override fun clearUserPreferences() {
        preferences?.edit()
            ?.remove(KEY_PASSCODE)
            ?.remove(KEY_NIGHT_MODE)
            ?.remove(KEY_NOTIFICATIONS)
            ?.remove(KEY_LOCALE)
            ?.remove(KEY_PIN_INPUT_ATTEMPTS)
            ?.remove(KEY_PIN_INPUT_TIME)
            ?.apply()
    }

    /**
     * Put a String value in the preferences editor
     *
     * @receiver shared preferences - data storage
     * @param key - String value
     * @param value - String value
     */
    private fun SharedPreferences.saveValue(key: String, value: String?) {
        this.edit()?.putString(key, value)?.apply()
    }

    /**
     * Put a Boolean value in the preferences editor
     *
     * @receiver shared preferences - data storage
     * @param key - String value
     * @param value - Boolean value
     */
    private fun SharedPreferences.saveValue(key: String, value: Boolean) {
        this.edit()?.putBoolean(key, value)?.apply()
    }

    /**
     * Put a Int value in the preferences editor
     *
     * @receiver shared preferences - data storage
     * @param key - String value
     * @param value - Int value
     */
    private fun SharedPreferences.saveValue(key: String, value: Int) {
        this.edit()?.putInt(key, value)?.apply()
    }

    /**
     * Put a Long value in the preferences editor
     *
     * @receiver shared preferences - data storage
     * @param key - String value
     * @param value - Long value
     */
    private fun SharedPreferences.saveValue(key: String, value: Long) {
        this.edit()?.putLong(key, value)?.apply()
    }

}
