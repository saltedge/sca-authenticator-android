/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.android.security

import android.content.Context
import com.saltedge.android.security.checkers.*
import com.saltedge.android.security.checkers.checkAppSignatures
import com.saltedge.android.security.checkers.checkIfAppDebuggable
import com.saltedge.android.security.checkers.checkIfDeviceEmulator
import com.saltedge.android.security.checkers.checkIfDeviceRooted

/**
 * Class for checking possible breaches in application environment or application tempering
 *
 * Check next breaches:
 * 1. OS is working under Root privileges
 * 2. Current device is Emulator
 * 3. Application can be debugged
 * 4. Application signed with not verified signature
 * 5. OS has installed hooking framework
 */
object RaspChecker {
    /**
     * check possible breaches in application environment or harmful applications
     *
     * @param context of Application
     * @return check report or empty string
     */
    fun collectFailsReport(context: Context): String {
        val appSignatures = context.resources.getStringArray(R.array.signatures)
        val checkList = listOfNotNull(
            context.checkIfDeviceRooted(),
            checkIfDeviceEmulator(),
            context.checkIfAppDebuggable(),
            context.checkAppSignatures(appSignatures.toList()),
            context.checkHookingFrameworks()
        )
        return if (checkList.isEmpty()) "" else checkList.joinToString(separator = ", ")
    }
}
