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
package com.saltedge.authenticator.sdk.tools

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Build application and device info:
 * e.g.: Salt Edge Authenticator / 2.3.0(39); StandAloneInstall; (Xiaomi; Redmi Note 8; SDK 28; Android 9)
 */
class ApplicationAndDeviceInfoBuilder(val context: Context) {

    companion object {

        fun buildUserAgent(context: Context): String {
            with(context.packageManager) {
                val versionName = try {
                    getPackageInfo(context.packageName, 0).versionName
                } catch (e: PackageManager.NameNotFoundException) {
                    "nameNotFound"
                }
                val versionCode = try {
                    getPackageInfo(context.packageName, 0).versionCode.toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    "versionCodeNotFound"
                }

                val applicationInfo = context.applicationInfo
                val stringId = applicationInfo.labelRes
                val appName =
                    if (stringId == 0) applicationInfo.nonLocalizedLabel.toString()
                    else context.getString(stringId)

                val manufacturer = Build.MANUFACTURER
                val model = Build.MODEL
                val version = Build.VERSION.SDK_INT
                val versionRelease = Build.VERSION.RELEASE

                val installerName = getInstallerPackageName(context.packageName) ?: "StandAloneInstall"

                val userAgentValue = "$appName / $versionName($versionCode); $installerName;" +
                    " ($manufacturer; $model; SDK $version; Android $versionRelease)"

                return userAgentValue.map { c ->
                    if ((c <= '\u001f' && c != '\t') || c >= '\u007f') {
                        ' '
                    } else {
                        c
                    }
                }.joinToString("")
            }
        }
    }
}
