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

import android.content.Context
import com.saltedge.authenticator.R

const val MIN_LENGTH_FOR_THE_PASSCODE = 4
const val MAX_LENGTH_FOR_THE_PASSCODE = 16
private val passcodeValidationRange = MIN_LENGTH_FOR_THE_PASSCODE..MAX_LENGTH_FOR_THE_PASSCODE

/**
 * Checks if passcode is valid
 *
 * @param passcode - entered passcode
 * @return boolean, true if
 * @see validatePasscode
 */
fun isPasscodeValid(passcode: String): Boolean = passcode.length in passcodeValidationRange

/**
 * Get passcode validation error if passcode is empty or length is
 * less than MIN_LENGTH_FOR_THE_PASSCODE or more than MAX_LENGTH_FOR_THE_PASSCODE
 *
 * @param passcode - entered passcode
 * @return Int object - string resource
 */
fun validatePasscode(passcode: String, context: Context): String? {
    return when {
        passcode.isEmpty() -> context.getString(R.string.errors_empty_passcode)
        !isPasscodeValid(passcode) ->
            context.getString(
                R.string.errors_passcode_info, MIN_LENGTH_FOR_THE_PASSCODE,
                MAX_LENGTH_FOR_THE_PASSCODE
            )
        else -> null
    }
}
