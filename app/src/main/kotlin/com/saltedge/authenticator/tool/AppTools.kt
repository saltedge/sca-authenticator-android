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
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import com.crashlytics.android.Crashlytics

object AppTools : AppToolsAbs {

    /**
     * Checks if the application is running in test environment
     *
     * @param appContext - application context
     * @return boolean, true if application context contains test
     */
    override fun isTestsSuite(appContext: Context): Boolean {
        return appContext.classLoader?.toString()?.contains("test") ?: false
    }

    /**
     * Check if in the application can use new (from SDK28) biometric prompt
     *
     * @return boolean, true if version sdk is greater than or equal to VERSION_CODES.P (SDK28)
     */
    override fun isBiometricPromptV28Enabled(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    /**
     * Get display height
     *
     * @param appContext - application context
     * @return height size
     */
    override fun getDisplayHeight(appContext: Context): Int {
        val display =
            (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display?.getSize(size)
        return size.y
    }

    /**
     * Get display width
     *
     * @param appContext - application context
     * @return width size
     */
    override fun getDisplayWidth(appContext: Context): Int {
        val display =
            (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display?.getSize(size)
        return size.x
    }

    /**
     * Get application version name
     *
     * @param appContext - application context
     * @return version name
     */
    override fun getAppVersionName(appContext: Context): String {
        try {
            return appContext.packageManager?.getPackageInfo(appContext.packageName, 0)?.versionName
                ?: "unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            Crashlytics.logException(e)
        } catch (ignored: Exception) {
        }
        return "unknown"
    }
}
