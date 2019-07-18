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
import android.content.pm.ApplicationInfo
import com.saltedge.android.security.emu.isRunOnEmulator
import com.saltedge.android.security.hooks.isHookingFrameworkDetected
import com.saltedge.android.security.root.isDeviceRooted
import com.saltedge.android.security.signature.isVerifiedAppSignature

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
     * @param context - Application Context
     * @return list of Fail Class names
     */
    fun collectFailsResult(context: Context): List<String> {
        val isDeviceRooted = isDeviceRooted(context)
        val isDeviceEmulator = isDeviceEmulator()
        val isAppDebuggable = isAppDebuggable(context)
        val isAppInstalledByNotVerifiedInstaller = isAppInstalledByNotVerifiedInstaller(context)
        val isAppSignatureInvalid = isAppSignatureInvalid(context)
        val isHookingFrameworkInstalled = isHookingFrameworkInstalled(context)
        return mapOf(
                "DeviceRooted" to isDeviceRooted,
                "DeviceEmulator" to isDeviceEmulator,
                "AppDebuggable" to isAppDebuggable,
                "AppInstalledByNotVerifiedInstaller" to isAppInstalledByNotVerifiedInstaller,
                "AppSignatureInvalid" to isAppSignatureInvalid,
                "HookingFrameworkInstalled" to isHookingFrameworkInstalled
        ).filter { it.value }.keys.toList()
    }

    /**
     * A simple root checker that gives an *indication* if the device is rooted or not.
     * Disclaimer: **root==god**, so there's no 100% way to check for root.
     *
     * @param context - Application Context
     * @return true, we think there's a good *indication* of root | false good *indication* of no root (could still be cloaked)
     */
    private fun isDeviceRooted(context: Context): Boolean = context.isDeviceRooted()

    /**
     * Device emulator checker
     *
     * @return true, if app started on emulator
     */
    private fun isDeviceEmulator(): Boolean = isRunOnEmulator()

    /**
     * Attached debugger checker
     *
     * @param context - Application Context
     * @return true, if DEBUG mode is true
     */
    private fun isAppDebuggable(context: Context): Boolean {
        return context.applicationInfo.flags.and(ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /**
     * Installer source checker
     *
     * @param context - Application Context
     * @return true, if app installed from Play Market
     */
    private fun isAppInstalledByNotVerifiedInstaller(context: Context): Boolean {
        val installer = context.packageManager.getInstallerPackageName(context.packageName)
        return installer == null || !installer.startsWith("com.android.vending")
    }

    /**
     * App Signature checker
     *
     * @param context - Application Context
     * @return true, if app signed with release certificate
     */
    private fun isAppSignatureInvalid(context: Context): Boolean = !isVerifiedAppSignature(context)

    /**
     * Checker for installed hooking frameworks
     *
     * @param context - Application Context
     * @return true, if detected installed hooking frameworks
     */
    private fun isHookingFrameworkInstalled(context: Context) = isHookingFrameworkDetected(context)
}
