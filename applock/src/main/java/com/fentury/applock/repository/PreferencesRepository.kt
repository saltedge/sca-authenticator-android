/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.repository

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

const val KEY_PASSCODE = "KEY_PASSCODE"
const val KEY_PIN_INPUT_ATTEMPTS = "KEY_PIN_INPUT_ATTEMPTS"
const val KEY_PIN_INPUT_TIME = "KEY_PIN_INPUT_TIME"

internal object PreferencesRepository : PreferencesRepositoryAbs {

    private var preferences: SharedPreferences? = null

    /**
     * Init object: PreferenceRepository
     *
     * @param appContext - application context
     * @return preference repository
     */
    fun initObject(appContext: Context): PreferencesRepository {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        return this
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
    override fun clearPasscodePreferences() {
        preferences?.edit()
            ?.remove(KEY_PASSCODE)
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