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
package com.saltedge.authenticator.features.connections.edit.name

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.sdk.constants.KEY_NAME
import com.saltedge.authenticator.tools.getEnabledStateColorResId
import com.saltedge.authenticator.tools.hideSystemKeyboard
import com.saltedge.authenticator.tools.setButtonsColor
import com.saltedge.authenticator.tools.setTextColorResId

class EditConnectionNameDialog : DialogFragment(), DialogInterface.OnClickListener, TextWatcher {

    private var inputView: EditText? = null
    private var positiveButton: Button? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb = AlertDialog.Builder(activity!!)
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
        inputView?.addTextChangedListener(this)
        (dialog as AlertDialog).setButtonsColor(R.color.blue)
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

    override fun afterTextChanged(s: Editable?) {
        val isEnabled = s?.isNotEmpty() == true
        positiveButton?.isEnabled = isEnabled
        positiveButton?.setTextColorResId(getEnabledStateColorResId(isEnabled))
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    private fun onOkClick() {
        val inputValue = inputView?.text?.toString() ?: return
        if (inputValue.isNotEmpty()) {
            dismissDialog()
            targetFragment?.onActivityResult(
                targetRequestCode,
                Activity.RESULT_OK,
                Intent().putExtra(KEY_GUID, arguments?.getString(KEY_GUID))
                    .putExtra(KEY_NAME, inputValue)
            )
        }
    }

    private fun dismissDialog() {
        inputView?.hideSystemKeyboard()
        dismiss()
    }

    companion object {
        fun newInstance(guid: String, name: String): EditConnectionNameDialog =
            newInstance(Bundle()
                .apply { putString(KEY_GUID, guid) }
                .apply { putString(KEY_NAME, name) }
            )

        fun newInstance(bundle: Bundle): EditConnectionNameDialog =
            EditConnectionNameDialog().apply { arguments = bundle }
    }
}
