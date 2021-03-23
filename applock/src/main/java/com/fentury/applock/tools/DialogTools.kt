/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools

import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.fentury.applock.R

/**
 * Show warning dialog with given message
 *
 * @receiver FragmentActivity
 * @param messageId - the message that appears in the dialog
 * @see showWarningDialog
 */
fun FragmentActivity.showWarningDialog(@StringRes messageId: Int): AlertDialog? {
    return try {
        showWarningDialog(message = getString(messageId))
    } catch (e: Exception) {
        null
    }
}

/**
 * Show dialog for lock screen with warning message. Dialog will block user interaction.
 *
 * @receiver FragmentActivity
 * @param message - the message that appears in the dialog
 * @return AlertDialog object or null
 */
fun FragmentActivity.showLockWarningDialog(message: String?): AlertDialog? {
    return try {
        AlertDialog.Builder(this, R.style.LockAlertDialog)
            .setMessage(message)
            .setCancelable(false)
            .show()
    } catch (e: java.lang.Exception) {
        null
    }
}

/**
 * Show warning dialog with given message
 *
 * @receiver FragmentActivity
 * @param message - the message that appears in the dialog
 * @param listener - on dialog action click listener
 */
fun FragmentActivity.showWarningDialog(
    message: String?,
    listener: DialogInterface.OnClickListener? = null
): AlertDialog? {
    return showDialogWithTitleAndMessage(titleResId = R.string.errors_warning, message = message, listener = listener)
}

/**
 * Show dialog with given title and message
 *
 * @receiver FragmentActivity
 * @param titleResId - the title that appears in the dialog
 * @param message - the message that appears in the dialog
 * @param listener - on dialog action click listener
 */
private fun FragmentActivity.showDialogWithTitleAndMessage(
    titleResId: Int,
    message: String?,
    listener: DialogInterface.OnClickListener? = null
): AlertDialog? {
    if (message?.isBlank() != false) return null
    return try {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(titleResId)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, listener)
            .show()
    } catch (e: Exception) {
        null
    }
}