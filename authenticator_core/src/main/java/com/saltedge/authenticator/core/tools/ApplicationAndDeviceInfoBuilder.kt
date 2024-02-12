/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.core.tools

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Build application and device info:
 * e.g.: Salt Edge Authenticator / 2.3.0(39); StandaloneInstall; (Xiaomi; Redmi Note 8; SDK 28)
 */
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
        val appNameResId = applicationInfo.labelRes
        val appName =
            if (appNameResId == 0) applicationInfo.nonLocalizedLabel.toString()
            else context.getString(appNameResId)

        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val version = Build.VERSION.RELEASE

        val installerName = getInstallerPackageName(context.packageName) ?: "StandaloneInstall"

        val userAgentValue = "$appName; $versionName($versionCode); $installerName;" +
            " $manufacturer; $model; Android $version"

        return userAgentValue.map { c ->
            if ((c <= '\u001f' && c != '\t') || c >= '\u007f') {
                ' '
            } else {
                c
            }
        }.joinToString("")
    }
}
