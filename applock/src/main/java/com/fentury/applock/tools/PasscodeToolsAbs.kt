/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools

import android.content.Context

/**
 * Abstraction of PasscodeTools
 *
 * @see PasscodeTools
 */
interface PasscodeToolsAbs {
    fun replacePasscodeKey(context: Context)
    fun savePasscode(passcode: String): Boolean
    fun getPasscode(): String
}