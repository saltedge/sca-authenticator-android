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
package com.saltedge.authenticator.widget.passcode

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.getEnabledStateColorResId
import com.saltedge.authenticator.tool.validatePasscode
import com.saltedge.authenticator.tool.setTextColorResId
import com.saltedge.authenticator.tool.setVisible
import kotlinx.android.synthetic.main.view_passcode_input.view.*

class PasscodeInputView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
        PinpadInputHandlerContract {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_passcode_input, this)
        setupViews()
    }

    var inputMode = InputMode.CHECK_PASSCODE
        private set
    private var currentPasscode: String = ""
    var listener: PasscodeInputViewListener? = null

    override fun getPasscodeOutputText(): String? = passcodeTextInputView?.text?.toString()

    override fun setPasscodeOutputText(text: String) {
        passcodeTextInputLayout?.error = null
        passcodeTextInputView?.setText(text)
        updatePositiveActionEnabledState(isEnabled = text.isNotEmpty())
    }

    override fun onFingerprintClickAction() {
        listener?.onBiometricInputSelected()
    }

    fun initInputMode(inputMode: InputMode, currentPasscode: String = "") {
        this.inputMode = inputMode
        this.currentPasscode = if (inputMode == InputMode.NEW_PASSCODE) "" else currentPasscode
        passcodeTextInputView?.setText("")
    }

    fun setPositiveActionText(@StringRes textResId: Int) {
        positivePasscodeActionView?.setText(textResId)
    }

    var biometricsActionIsAvailable: Boolean = false
        set(value) {
            field = value
            pinpadView?.setupFingerAction(active = value)
        }

    var cancelActionIsAvailable: Boolean = true
        set(value) {
            field = value
            negativePasscodeActionView?.setVisible(show = value)
        }

    private fun setupViews() {
        passcodeTextInputLayout?.typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
        pinpadView?.setupFingerAction(active = biometricsActionIsAvailable)
        negativePasscodeActionView?.setVisible(show = cancelActionIsAvailable)

        negativePasscodeActionView?.setOnClickListener { onNegativeActionClick() }
        positivePasscodeActionView?.setOnClickListener { onPositiveActionClick() }
        pinpadView?.inputHandler = PinpadInputHandler(contract = this)
    }

    private fun onNegativeActionClick() {
        listener?.onPasscodeInputCanceledByUser()
    }

    private fun onPositiveActionClick() {
        val enteredPasscode = getPasscodeOutputText() ?: return
        if (enteredPasscode.isEmpty()) return
        when (inputMode) {
            InputMode.CHECK_PASSCODE -> {
                if (currentPasscode == enteredPasscode) {
                    listener?.onEnteredPasscodeIsValid()
                } else {
                    passcodeTextInputView?.setText("")
                    onInputError(context.getString(R.string.errors_passcode_not_match))
                    listener?.onEnteredPasscodeIsInvalid()
                }
            }
            InputMode.NEW_PASSCODE -> {
                validatePasscode(enteredPasscode, context)?.let {
                    onInputError(it)
                } ?: run {
                    initInputMode(inputMode = InputMode.REPEAT_NEW_PASSCODE, currentPasscode = enteredPasscode)
                    listener?.onNewPasscodeEntered(mode = InputMode.REPEAT_NEW_PASSCODE, passcode = currentPasscode)
                }
            }
            InputMode.REPEAT_NEW_PASSCODE -> {
                if (currentPasscode == enteredPasscode) {
                    listener?.onNewPasscodeConfirmed(passcode = currentPasscode)
                } else {
                    initInputMode(inputMode = InputMode.NEW_PASSCODE)
                    onInputError(context.getString(R.string.errors_passcode_not_match))
                }
            }
        }
    }

    private fun onInputError(errorName: String) {
        passcodeTextInputView?.setText("")
        passcodeTextInputLayout?.error = errorName
    }

    private fun updatePositiveActionEnabledState(isEnabled: Boolean) {
        positivePasscodeActionView?.isEnabled = isEnabled
        positivePasscodeActionView?.setTextColorResId(getEnabledStateColorResId(isEnabled))
    }

    enum class InputMode {
        CHECK_PASSCODE, NEW_PASSCODE, REPEAT_NEW_PASSCODE
    }
}
