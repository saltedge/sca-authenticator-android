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

import com.saltedge.authenticator.widget.passcode.PasscodeInputView

interface PasscodeEditContract {

    interface View {
        fun initInputMode(mode: PasscodeInputView.InputMode, passcode: String)
        fun updateViewContent(titleTextResId: Int, positiveActionTextResId: Int)
        fun showProgress()
        fun hideProgress()
        fun closeView()
        fun showWarning(messageResId: Int)
        fun showInfo(messageResId: Int)
    }

    interface Presenter {
        var viewContract: View?
        fun onViewCreated()
        fun newPasscodeConfirmed(passcode: String)
        fun enteredCurrentPasscode()
        fun enteredNewPasscode(mode: PasscodeInputView.InputMode)
    }
}
