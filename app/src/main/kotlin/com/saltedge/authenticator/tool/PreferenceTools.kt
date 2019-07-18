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

import android.content.SharedPreferences

/**
 * Put a String value in the preferences editor
 *
 * @receiver shared preferences - data storage
 * @param key - String value
 * @param value - String value
 */
fun SharedPreferences.saveValue(key: String, value: String?) {
    this.edit()?.putString(key, value)?.apply()
}

/**
 * Put a Boolean value in the preferences editor
 *
 * @receiver shared preferences - data storage
 * @param key - String value
 * @param value - Boolean value
 */
fun SharedPreferences.saveValue(key: String, value: Boolean) {
    this.edit()?.putBoolean(key, value)?.apply()
}

/**
 * Put a Int value in the preferences editor
 *
 * @receiver shared preferences - data storage
 * @param key - String value
 * @param value - Int value
 */
fun SharedPreferences.saveValue(key: String, value: Int) {
    this.edit()?.putInt(key, value)?.apply()
}

/**
 * Put a Long value in the preferences editor
 *
 * @receiver shared preferences - data storage
 * @param key - String value
 * @param value - Long value
 */
fun SharedPreferences.saveValue(key: String, value: Long) {
    this.edit()?.putLong(key, value)?.apply()
}
