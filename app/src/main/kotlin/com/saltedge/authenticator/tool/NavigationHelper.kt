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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.features.connections.qr.QrScannerActivity
import com.saltedge.authenticator.sdk.constants.DEFAULT_SUPPORT_EMAIL_LINK

/**
 * Check fragment navigation level
 *
 * @receiver fragment activity
 * @return boolean, true if backStackEntryCount == 0
 */
fun FragmentActivity?.isTopNavigationLevel(): Boolean =
    (this?.supportFragmentManager?.backStackEntryCount ?: 0) == 0

/**
 * Add fragment in back stack
 *
 * @receiver fragment activity
 */
fun FragmentActivity.addFragment(fragment: Fragment) {
    try {
        supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, fragment, fragment.createTagName())
            ?.addToBackStack(null)?.commit()
    } catch (ignored: IllegalStateException) {
    } catch (e: Exception) {
        e.log()
    }
}

/**
 * Replace fragment in container if backStackEntryCount > 0
 *
 * @receiver fragment activity
 */
fun FragmentActivity.replaceFragment(fragment: Fragment) {
    try {
        if (!isTopNavigationLevel()) {
            supportFragmentManager.popBackStackImmediate(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
        supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, fragment, fragment.createTagName())?.commit()
    } catch (ignored: IllegalStateException) {
    } catch (e: Exception) {
        e.log()
    }
}

/**
 * Get current fragment in container
 *
 * @receiver app compat activity
 * @return fragment object which is in the container
 */
fun AppCompatActivity.currentFragmentInContainer(): Fragment? {
    try {
        return supportFragmentManager?.findFragmentById(R.id.container)
    } catch (ignored: IllegalStateException) {
    } catch (e: Exception) {
        e.log()
    }
    return null
}

/**
 * Show dialog fragment
 *
 * @receiver fragment activity
 * @param dialog object
 */
fun FragmentActivity.showDialogFragment(dialog: DialogFragment) {
    try {
        dialog.show(supportFragmentManager, dialog.javaClass.simpleName)
    } catch (ignored: IllegalStateException) {
    } catch (e: Exception) {
        e.log()
    }
}

/**
 * Finish fragment
 *
 * @receiver fragment activity
 */
fun FragmentActivity.finishFragment() {
    try {
        supportFragmentManager?.popBackStack()
    } catch (ignored: IllegalStateException) {
    } catch (e: Exception) {
        e.log()
    }
}

/**
 * Show support mail
 *
 * @receiver fragment activity
 */
fun FragmentActivity.startMailApp(supportEmail: String? = DEFAULT_SUPPORT_EMAIL_LINK) {
    try {
        this.startActivityForResult(Intent(Intent.ACTION_SENDTO)
            .apply { data = Uri.parse("mailto:$supportEmail") }, 0
        )
    } catch (ignored: IllegalStateException) {
    } catch (ignored: ActivityNotFoundException) {
    } catch (e: Exception) {
        e.log()
    }
}

/**
 * Show system settings
 *
 * @receiver fragment activity
 */
fun FragmentActivity.startSystemSettings() {
    try {
        this.startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0)
    } catch (ignored: IllegalStateException) {
    } catch (ignored: ActivityNotFoundException) {
    } catch (e: Exception) {
        e.log()
    }
}

/**
 * Show QrScannerActivity
 *
 * @receiver fragment activity
 */
fun FragmentActivity.startQrScannerActivity() {
    try {
        this.startActivityForResult(
            Intent(this, QrScannerActivity::class.java),
            QR_SCAN_REQUEST_CODE
        )
    } catch (ignored: IllegalStateException) {
    } catch (ignored: ActivityNotFoundException) {
    } catch (e: Exception) {
        e.log()
    }
}

/**
 * Restart application
 *
 * @receiver fragment activity
 */
fun FragmentActivity.restartApp() {
    try {
        val intent = this.packageManager.getLaunchIntentForPackage(this.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    } catch (ignored: Exception) {
    }
}

/**
 * Create tag name for fragment
 *
 * @receiver fragment
 * @return fragment name
 */
private fun Fragment?.createTagName(): String? = this?.javaClass?.name
