/*
 * Copyright (c) 2019 Salt Edge Inc.
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
import com.saltedge.authenticator.app.guid
import com.saltedge.authenticator.core.api.KEY_NAME
import com.saltedge.authenticator.features.main.SharedViewModel
import com.saltedge.authenticator.tools.getEnabledStateColorResId
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
            return Bundle().apply {
                this.guid = guid
                this.putString(KEY_NAME, name)
            }
        }
    }
}
