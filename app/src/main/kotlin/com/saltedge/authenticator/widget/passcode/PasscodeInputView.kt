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
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.ResId
import com.saltedge.authenticator.tool.setVisible
import kotlinx.android.synthetic.main.view_passcode_input.view.*

class PasscodeInputView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    PincodeLabelView.PincodeInputResultListener {

    private var vibrator: Vibrator? = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator?

    init {
        LayoutInflater.from(context).inflate(R.layout.view_passcode_input, this)
        setupViews()
    }

    private var inputMode = InputMode.CHECK_PASSCODE
    private var currentPasscode: String = ""
    var listener: PasscodeInputViewListener? = null

    fun initInputMode(inputMode: InputMode, currentPasscode: String = "") {
        this.inputMode = inputMode
        this.currentPasscode = if (inputMode == InputMode.NEW_PASSCODE) "" else currentPasscode
        passcodeLabelView?.clearAll()
    }

    var biometricsActionIsAvailable: Boolean = false
        set(value) {
            field = value
            pinpadView?.setupFingerAction(active = value)
        }

    override fun onPincodeInputFinished(passcode: String) {
        if (passcode.isEmpty()) return
        when (inputMode) {
            InputMode.CHECK_PASSCODE -> {
                if (currentPasscode == passcode) {
                    listener?.onEnteredPasscodeIsValid()
                } else {
                    passcodeLabelView?.clearAll()
                    onInputError(R.string.errors_passcode_not_match)
                    listener?.onEnteredPasscodeIsInvalid()
                }
            }
            InputMode.NEW_PASSCODE -> {
                infoMessageView?.setVisible(show = false)
                run {
                    initInputMode(
                        inputMode = InputMode.REPEAT_NEW_PASSCODE,
                        currentPasscode = passcode
                    )
                    listener?.onNewPasscodeEntered(
                        mode = InputMode.REPEAT_NEW_PASSCODE,
                        passcode = currentPasscode
                    )
                }
            }
            InputMode.REPEAT_NEW_PASSCODE -> {
                if (currentPasscode == passcode) {
                    listener?.onNewPasscodeConfirmed(passcode = currentPasscode)
                } else {
                    errorVibrate()
                    initInputMode(inputMode = InputMode.NEW_PASSCODE)
                    listener?.onEnteredPasscodeIsInvalid()
                    onInputError(R.string.errors_passcode_not_match)
                }
            }
        }
    }

    private fun setupViews() {
        infoMessageView?.setVisible(show = false)
        passcodeLabelView?.clearAll()
        pinpadView?.setupFingerAction(active = biometricsActionIsAvailable)
        passcodeLabelView?.resultListener = this
        pinpadView?.clickListener = passcodeLabelView
    }

    private fun onInputError(@StringRes errorName: ResId) {
        infoMessageView?.setVisible(show = true)
        infoMessageView?.setText(errorName)
    }

    @Suppress("DEPRECATION")
    private fun errorVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
        } else vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
    }

    enum class InputMode {
        CHECK_PASSCODE, NEW_PASSCODE, REPEAT_NEW_PASSCODE
    }
}
