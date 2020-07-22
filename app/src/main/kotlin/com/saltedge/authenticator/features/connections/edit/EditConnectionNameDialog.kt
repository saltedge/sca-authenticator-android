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
package com.saltedge.authenticator.features.connections.edit

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.sdk.constants.KEY_NAME
import com.saltedge.authenticator.tools.getEnabledStateColorResId
import com.saltedge.authenticator.tools.guid
import com.saltedge.authenticator.tools.hideSystemKeyboard
import com.saltedge.authenticator.tools.setTextColorResId

class EditConnectionNameDialog : DialogFragment(), DialogInterface.OnClickListener {

    private var inputView: EditText? = null
    private var positiveButton: Button? = null
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(requireActivity(), R.style.InfoDialogTheme)
            .setTitle(R.string.ui_dialog_rename_title)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(R.string.actions_cancel, this)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_name, null)
        val dialog = adb.setView(view).create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val text = arguments?.getString(KEY_NAME) ?: ""
        inputView = dialog?.findViewById(R.id.connectionNameView) as EditText?
        inputView?.addTextChangedListener {
            val isEnabled = it?.isNotEmpty() == true
            positiveButton?.isEnabled = isEnabled
            positiveButton?.setTextColorResId(getEnabledStateColorResId(isEnabled))
        }
        inputView?.requestFocus()
        inputView?.setText(text)
        inputView?.setSelection(text.length)
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> onOkClick()
            DialogInterface.BUTTON_NEGATIVE -> dismissDialog()
        }
    }

    private fun onOkClick() {
        val inputValue = inputView?.text?.toString() ?: return
        if (inputValue.isNotEmpty()) {
            dismissDialog()
            sharedViewModel.onNewConnectionNameEntered(dataBundle(arguments?.guid  ?: "", inputValue))
        }
    }

    private fun dismissDialog() {
        inputView?.hideSystemKeyboard()
        dismiss()
    }

    companion object {
        fun dataBundle(guid: String, name: String): Bundle {
            return Bundle()
                .apply {
                    this.guid = guid
                    this.putString(KEY_NAME, name)
                }
        }
    }
}
