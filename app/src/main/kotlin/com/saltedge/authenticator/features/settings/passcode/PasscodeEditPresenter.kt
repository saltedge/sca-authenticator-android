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
package com.saltedge.authenticator.features.settings.passcode

import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.passcode.saver.PasscodeSaveResultListener
import com.saltedge.authenticator.features.settings.passcode.saver.PasscodeSaver
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeInputView
import javax.inject.Inject

class PasscodeEditPresenter @Inject constructor(
        private val passcodeTools: PasscodeToolsAbs
) : PasscodeEditContract.Presenter, PasscodeSaveResultListener {

    private val savedPasscode: String
        get() = passcodeTools.getPasscode()

    override var viewContract: PasscodeEditContract.View? = null

    override fun onViewCreated() {
        val newMode = PasscodeInputView.InputMode.CHECK_PASSCODE
        viewContract?.initInputMode(mode = newMode, passcode = savedPasscode)
        viewContract?.updateViewContent(
                titleTextResId = getTitleTextResId(newMode),
                positiveActionTextResId = getPositiveTextResId(newMode)
        )
    }

    override fun enteredCurrentPasscode() {
        val newMode = PasscodeInputView.InputMode.NEW_PASSCODE
        viewContract?.initInputMode(mode = newMode, passcode = "")
        viewContract?.updateViewContent(
                titleTextResId = getTitleTextResId(newMode),
                positiveActionTextResId = getPositiveTextResId(newMode)
        )
    }

    override fun enteredNewPasscode(mode: PasscodeInputView.InputMode) {
        viewContract?.updateViewContent(
                titleTextResId = getTitleTextResId(mode),
                positiveActionTextResId = getPositiveTextResId(mode)
        )
    }

    override fun newPasscodeConfirmed(passcode: String) {
        viewContract?.showProgress()
        PasscodeSaver(passcodeTools, callback = this).runNewTask(passcode)
    }

    override fun passcodeSavedWithResult(result: Boolean) {
        viewContract?.let {
            it.hideProgress()
            if (result) {
                it.showInfo(R.string.settings_passcode_success)
                it.closeView()
            } else {
                it.showWarning(R.string.errors_contact_support)
            }
        }
    }

    private fun getTitleTextResId(inputMode: PasscodeInputView.InputMode): Int {
        return when (inputMode) {
            PasscodeInputView.InputMode.CHECK_PASSCODE -> R.string.settings_passcode_input_current
            PasscodeInputView.InputMode.NEW_PASSCODE -> R.string.settings_passcode_input_new
            PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE -> R.string.settings_passcode_repeat_new
        }
    }

    private fun getPositiveTextResId(inputMode: PasscodeInputView.InputMode): Int {
        return when (inputMode) {
            PasscodeInputView.InputMode.CHECK_PASSCODE, PasscodeInputView.InputMode.NEW_PASSCODE -> {
                R.string.actions_next
            }
            PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE -> R.string.actions_ok
        }
    }
}
