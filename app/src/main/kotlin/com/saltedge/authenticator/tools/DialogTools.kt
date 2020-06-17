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
package com.saltedge.authenticator.tools

import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.saltedge.authenticator.R

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
        e.log()
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
 * Show error dialog with given message
 *
 * @receiver FragmentActivity
 * @param message - the message that appears in the dialog
 * @param listener - on dialog action click listener
 */
fun FragmentActivity.showErrorDialog(
    message: String?,
    listener: DialogInterface.OnClickListener? = null
): AlertDialog? {
    return showDialogWithTitleAndMessage(titleResId = R.string.errors_error, message = message, listener = listener)
}

/**
 * Show database init error
 *
 * @receiver FragmentActivity
 * @param listener - on dialog action click listener
 */
fun FragmentActivity.showDbErrorDialog(listener: DialogInterface.OnClickListener): AlertDialog? {
    return showWarningDialog(message = getString(R.string.errors_db_init), listener = listener)
}

/**
 * Show warning about reset of all user data
 *
 * @receiver FragmentActivity
 * @param listener - on dialog action click listener
 */
fun FragmentActivity.showDialogAboutUserReset(listener: DialogInterface.OnClickListener): AlertDialog? {
    return showWarningDialog(message = getString(R.string.errors_account_reset), listener = listener)
}

/**
 * Show reset data dialog
 *
 * @receiver FragmentActivity
 * @param listener - on dialog action click listener
 */
fun FragmentActivity.showResetDataDialog(listener: DialogInterface.OnClickListener): AlertDialog? {
    return try {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(R.string.forgot_passcode_clear_title)
            .setMessage(R.string.forgot_passcode_clear_message)
            .setPositiveButton(R.string.actions_clear, listener)
            .setNegativeButton(R.string.actions_cancel, listener)
            .show()
    } catch (e: java.lang.Exception) {
        e.log()
        null
    }
}

/**
 * Show reset data and settings dialog
 *
 * @receiver FragmentActivity
 * @param listener - on dialog action click listener
 */
fun FragmentActivity.showResetDataAndSettingsDialog(listener: DialogInterface.OnClickListener): AlertDialog? {
    return try {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(R.string.forgot_passcode_clear_title)
            .setMessage(R.string.settings_clear_message)
            .setPositiveButton(R.string.actions_clear, listener)
            .setNegativeButton(R.string.actions_cancel, listener)
            .show()
    } catch (e: java.lang.Exception) {
        e.log()
        null
    }
}

/**
 * Show dialog with given message
 *
 * @receiver FragmentActivity
 * @param message - the message that appears in the dialog
 */
fun FragmentActivity.showInfoDialog(message: String?): AlertDialog? {
    return try {
        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setMessage(message)
            .setCancelable(false)
            .show()
    } catch (e: java.lang.Exception) {
        e.log()
        null
    }
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
        e.log()
        null
    }
}
