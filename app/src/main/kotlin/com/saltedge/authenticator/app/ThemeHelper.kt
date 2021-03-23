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
package com.saltedge.authenticator.app

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import androidx.navigation.navOptions
import com.fentury.applock.lock.KEY_SKIP_PIN
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.buildVersion28orGreater

fun Context.isDarkThemeSet(): Boolean {
    return this.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun Context.switchDarkLightMode(currentMode: Int): Int {
    return when (currentMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
        else -> {
            if (isDarkThemeSet()) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        }
    }
}

fun getDefaultSystemNightMode(): Int {
    return if (buildVersion28orGreater) AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    else AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
}

fun isSystemNightModeSupported(sdkVersion: Int): Boolean {
    return sdkVersion >= Build.VERSION_CODES.Q
}

fun FragmentActivity.applyNightMode(nightMode: Int) {
    if (this.intent != null) this.intent.putExtra(KEY_SKIP_PIN, true)
    AppCompatDelegate.setDefaultNightMode(nightMode)
}

val defaultTransition = navOptions {
    anim {
        enter = R.anim.slide_in_right
        exit = R.anim.slide_out_left
        popEnter = R.anim.slide_in_left
        popExit = R.anim.slide_out_right
    }
}
