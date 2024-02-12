/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.widget.security.ActivityUnlockType
import timber.log.Timber

interface AppToolsAbs {
    var lastUnlockType: ActivityUnlockType
    fun isTestsSuite(appContext: Context): Boolean
    fun getAppVersionName(appContext: Context): String
    fun getSDKVersion(): Int
}

object AppTools : AppToolsAbs {

    override var lastUnlockType: ActivityUnlockType = ActivityUnlockType.PASSCODE

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
            Timber.e(e)
        } catch (ignored: Exception) {
        }
        return "unknown"
    }

    override fun getSDKVersion(): Int = Build.VERSION.SDK_INT
}

/**
 * Cast current application to AuthenticatorApplication
 *
 * @receiver fragment activity
 */
val FragmentActivity.authenticatorApp: AuthenticatorApplication?
    get() = this.application as? AuthenticatorApplication

/**
 * Cast current activity to AuthenticatorApplication
 *
 * @receiver Fragment object
 */
val Fragment.authenticatorApp: AuthenticatorApplication?
    get() = this.activity?.authenticatorApp

/**
 * extract guid string value from bundle
 *
 * @receiver bundle of data
 * @return guid string or null if not exist
 */
var Bundle.guid: GUID?
    get() = getString(KEY_GUID)
    set(value) {
        putString(KEY_GUID, value)
    }

/**
 * Check if system sdk version is 28 or greater
 *
 * @return boolean, true if version sdk is greater than or equal to VERSION_CODES.P (SDK28)
 */
val buildVersion28orGreater: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

/**
 * Check if system sdk version is 26 or greater
 *
 * @return boolean, true if version sdk is greater than or equal to VERSION_CODES.P (SDK26)
 */
val buildVersion26orGreater: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/**
 * Check if system sdk version is 24 or greater
 *
 * @return boolean, true if version sdk is greater than or equal to VERSION_CODES.P (SDK24)
 */
val buildVersion24orGreater: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

/**
 * Check if system sdk version is 23 or greater
 *
 * @return boolean, true if version sdk is greater than or equal to VERSION_CODES.M (SDK23)
 */
val buildVersion23orGreater: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

/**
 * Check if system sdk version is less than 23
 *
 * @return boolean, true if version sdk is less than VERSION_CODES.M (SDK23)
 */
val buildVersionLessThan23: Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
