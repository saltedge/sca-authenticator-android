/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.QR_SCAN_REQUEST_CODE
import com.saltedge.authenticator.app.defaultTransition
import com.saltedge.authenticator.core.api.DEFAULT_SUPPORT_EMAIL_LINK
import com.saltedge.authenticator.features.qr.QrScannerActivity
import com.saltedge.authenticator.widget.security.KEY_SKIP_PIN
import timber.log.Timber

/**
 * Get current fragment in container
 *
 * @receiver app compat activity
 * @return fragment object which is in the container
 */
fun FragmentActivity.currentFragmentOnTop(): Fragment? {
    try {
        val host = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        return host.childFragmentManager.fragments.getOrNull(0)
    } catch (ignored: IllegalStateException) {
    } catch (e: Exception) {
        Timber.e(e)
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
        Timber.e(e)
    }
}

/**
 * Show support mail
 *
 * @receiver fragment activity
 */
fun FragmentActivity.startMailApp(supportEmail: String? = null) {
    try {
        this.startActivityForResult(Intent(Intent.ACTION_SENDTO)
            .apply { data = Uri.parse("mailto:${supportEmail ?: DEFAULT_SUPPORT_EMAIL_LINK}") }, 0
        )
    } catch (ignored: IllegalStateException) {
    } catch (ignored: ActivityNotFoundException) {
    } catch (e: Exception) {
        Timber.e(e)
    }
}

/**
 * Show QrScannerActivity
 *
 * @receiver fragment activity
 */
fun FragmentActivity.showQrScannerActivity() {
    try {
        this.startActivityForResult(
            Intent(this, QrScannerActivity::class.java).apply { putExtra(KEY_SKIP_PIN, true) },
            QR_SCAN_REQUEST_CODE
        )
    } catch (ignored: IllegalStateException) {
    } catch (ignored: ActivityNotFoundException) {
    } catch (e: Exception) {
        Timber.e(e)
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

fun Fragment.navigateTo(actionRes: ResId, bundle: Bundle? = null, transition: NavOptions = defaultTransition) {
    try {
        if (mayNavigate()) findNavController().navigate(actionRes, bundle, transition)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Fragment.navigateToDialog(actionRes: ResId, bundle: Bundle) {
    try {
        if (mayNavigate()) findNavController().navigate(actionRes, bundle)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Fragment.popBackStack() {
    try {
        if (mayNavigate()) findNavController().popBackStack()
    } catch (e: Exception) {
        Timber.e(e)
    }
}

/**
 * Returns true if the navigation controller is still pointing at 'this' fragment, or false if it already navigated away.
 */
fun Fragment.mayNavigate(): Boolean {
    val navController = findNavController()
    val destinationIdInNavController = navController.currentDestination?.id

    // add tag_navigation_destination_id to your ids.xml so that it's unique:
    val destinationIdOfThisFragment = view?.getTag(R.id.tag_navigation_destination_id) ?: destinationIdInNavController

    // check that the navigation graph is still in 'this' fragment, if not then the app already navigated:
    return if (destinationIdInNavController == destinationIdOfThisFragment) {
        view?.setTag(R.id.tag_navigation_destination_id, destinationIdOfThisFragment)
        true
    } else false
}
