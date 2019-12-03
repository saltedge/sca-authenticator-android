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
package com.saltedge.android.security

import android.content.Context
import com.saltedge.android.security.checker.*

/**
 * Class for checking possible breaches in application environment or application tempering
 *
 * Check next breaches:
 * 1. OS is working under Root privileges
 * 2. Current device is Emulator
 * 3. Application can be debugged
 * 4. Application installed from not verified installer (not from Google Play)
 * 5. Application signed with not verified signature
 * 6. OS has installed hooking framework
 */
object RaspChecker {
    /**
     * checking possible breaches in application environment or application tempering
     *
     * @param context of Application
     * @return check report or empty string
     */
    fun collectFailsReport(context: Context): String {
        val checkList = listOfNotNull(
            context.checkIfDeviceRooted(),
            checkIfDeviceEmulator(),
            context.checkIfAppDebuggable(),
            context.checkAppInstaller(),
            context.checkAppSignature(),
            context.checkHookingFrameworks()
        )
        return if (checkList.isEmpty()) "" else checkList.joinToString(separator = ", ")
    }
}
