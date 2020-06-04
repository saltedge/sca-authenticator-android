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
package com.saltedge.authenticator.widget.security

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.setVisible
import com.saltedge.authenticator.widget.passcode.PasscodeInputListener
import com.saltedge.authenticator.widget.passcode.PasscodeInputMode
import kotlinx.android.synthetic.main.view_unlock.view.*

class UnlockAppInputView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    var biometricsActionIsAvailable: Boolean
        get() = passcodeInputView?.biometricsActionIsAvailable ?: false
        set(value) {
            passcodeInputView?.biometricsActionIsAvailable = value
        }
    var passcodeInputViewListener: PasscodeInputListener?
        get() = passcodeInputView?.listener
        set(value) {
            passcodeInputView?.listener = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_unlock, this)
        resetContentView?.setActionOnClickListener(OnClickListener { passcodeInputViewListener?.onClearDataActionSelected() })
        resetBackActionImageView?.setOnClickListener {
            setInputViewVisibility(show = true)
            setResetPasscodeViewVisibility(show = false)
        }
        passcodeInputView?.title = context.getString(R.string.passcode_enter_passcode_title)
    }

    fun setSavedPasscode(currentPasscode: String) {
        passcodeInputView?.inputMode = PasscodeInputMode.CHECK_PASSCODE
        passcodeInputView?.initialPasscode = currentPasscode
    }

    fun setErrorText(text: String) {
        passcodeInputView?.error = text
    }

    fun setInputViewVisibility(show: Boolean) {
        passcodeInputView?.setVisible(show)
        appLogoView?.setVisible(show)
    }

    fun setResetPasscodeViewVisibility(show: Boolean) {
        appLogoView?.setVisible(!show)
        resetPasscodeLayout?.setVisible(show)
    }

    fun setWarningView(show: Boolean, message: String = "") {
        warningView?.setVisible(show)
        warningView?.text = message
    }
}
