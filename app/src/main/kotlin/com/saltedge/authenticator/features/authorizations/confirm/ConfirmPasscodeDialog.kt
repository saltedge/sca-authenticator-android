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
package com.saltedge.authenticator.features.authorizations.confirm

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.PasscodeTools
import com.saltedge.authenticator.widget.fragment.BaseRoundedBottomDialogFragment
import com.saltedge.authenticator.widget.passcode.PasscodeInputView
import com.saltedge.authenticator.widget.passcode.PasscodeInputViewListener

class ConfirmPasscodeDialog : BaseRoundedBottomDialogFragment(), PasscodeInputViewListener {

    var callback: PasscodePromptCallback? = null

    private val presenter = ConfirmPasscodePresenter(passcodeTools = PasscodeTools)
    private val passcodeInputView: PasscodeInputView? by lazy {
        dialog?.findViewById<PasscodeInputView>(R.id.passcodeInputView)
    }
    private val descriptionView: TextView? by lazy {
        dialog?.findViewById<TextView>(R.id.descriptionView)
    }

    override fun getDialogViewLayout(): Int = R.layout.dialog_confirm_with_passcode

    override fun onStart() {
        super.onStart()
        setupDialogViews()
    }

    override fun onBiometricInputSelected() {}

    override fun onPasscodeInputCanceledByUser() {
        dismiss()
        callback?.passcodePromptCanceledByUser()
    }

    override fun onEnteredPasscodeIsValid() {
        dismiss()
        callback?.successAuthWithPasscode()
    }

    override fun onEnteredPasscodeIsInvalid() {
        descriptionView?.setText(R.string.errors_wrong_passcode)
        context?.let { descriptionView?.setTextColor(ContextCompat.getColor(it, R.color.red)) }
    }

    override fun onNewPasscodeEntered(mode: PasscodeInputView.InputMode, passcode: String) {}

    override fun onNewPasscodeConfirmed(passcode: String) {}

    private fun setupDialogViews() {
        passcodeInputView?.biometricsActionIsAvailable = false
        passcodeInputView?.listener = this
        passcodeInputView?.initInputMode(PasscodeInputView.InputMode.CHECK_PASSCODE, presenter.savedPasscode)
    }

    companion object {
        fun newInstance(resultCallback: PasscodePromptCallback): ConfirmPasscodeDialog {
            return ConfirmPasscodeDialog().apply { callback = resultCallback }
        }
    }
}
