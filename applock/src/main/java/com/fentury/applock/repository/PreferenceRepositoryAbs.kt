/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.repository

/**
 * Abstraction of PreferenceRepository
 * @see PreferencesRepository
 */
interface PreferencesRepositoryAbs {
    var encryptedPasscode: String
    var pinInputAttempts: Int
    var blockPinInputTillTime: Long
    fun passcodeExist(): Boolean
    fun clearPasscodePreferences()
}