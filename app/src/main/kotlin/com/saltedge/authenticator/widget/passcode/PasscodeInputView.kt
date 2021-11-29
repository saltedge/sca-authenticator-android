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

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.app.buildVersion26orGreater
import com.saltedge.authenticator.tools.setVisible
import kotlinx.android.synthetic.main.view_passcode_input.view.*

enum class PasscodeInputMode {
    CHECK_PASSCODE, NEW_PASSCODE, CONFIRM_PASSCODE
}

private const val PASSCODE_MIN_SIZE = 4
private const val PASSCODE_MAX_SIZE = 16

/**
 * Passcode input container
 *
 * @see KeypadView
 */
class PasscodeInputView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    KeypadView.KeypadClickListener
{
    private var vibrator: Vibrator? = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator?
    var initialPasscode: String = ""
    var title: String
        get() = titleView?.text?.toString() ?: ""
        set(value) {
            titleView?.text = value
        }
    var error: String
        get() = descriptionView?.text?.toString() ?: ""
        set(value) {
            showError(value)
        }
    var inputMode = PasscodeInputMode.CHECK_PASSCODE
        set(value) {
            field = value
            updatePasscodeOutput("")
        }
    var listener: PasscodeInputListener? = null
    var biometricsActionIsAvailable: Boolean = false
        set(value) {
            field = value
            keypadView?.setupFingerAction(active = value)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_passcode_input, this)
        setupViews()
    }

    override fun onDigitKeyClick(value: String) {
        val text: String = passcodeLabelView?.text?.toString() ?: return
        if (text.length < PASSCODE_MAX_SIZE) updatePasscodeOutput(text + value)
        else showError(R.string.errors_max_passcode)
    }

    override fun onFingerKeyClick() {
        listener?.onBiometricActionSelected()
    }

    override fun onForgotKeyClick() {
        listener?.onForgotActionSelected()
    }

    override fun onDeleteKeyClick() {
        val text: String = passcodeLabelView?.text?.toString() ?: return
        if (text.isNotEmpty()) updatePasscodeOutput(text.take(text.length - 1))
    }

    private fun setupViews() {
        descriptionView?.alpha = 0f

        updatePasscodeOutput("")

        keypadView?.setupFingerAction(active = biometricsActionIsAvailable)
        keypadView?.clickListener = this
        submitView?.setOnClickListener {
            onPasscodeInputFinished(passcode = passcodeLabelView?.text?.toString() ?: "")
        }
    }

    private fun updatePasscodeOutput(text: String) {
        passcodeLabelView?.setText(text)
        submitView?.setVisible(show = (PASSCODE_MIN_SIZE..PASSCODE_MAX_SIZE).contains(text.length))
        keypadView?.let {
            if (text.isEmpty() && biometricsActionIsAvailable) it.showFingerView() else it.showDeleteView()
        }
    }

    private fun onPasscodeInputFinished(passcode: String) {
        when (inputMode) {
            PasscodeInputMode.CHECK_PASSCODE -> {
                if (initialPasscode == passcode) {
                    listener?.onInputValidPasscode()
                    updatePasscodeOutput("")
                } else {
                    inputMode = PasscodeInputMode.CHECK_PASSCODE
                    showError(R.string.errors_wrong_passcode)
                    listener?.onInputInvalidPasscode(inputMode)
                }
            }
            PasscodeInputMode.NEW_PASSCODE -> {
                descriptionView?.alpha = 0f

                initialPasscode = passcode
                inputMode = PasscodeInputMode.CONFIRM_PASSCODE
                listener?.onNewPasscodeEntered(inputMode, passcode)
            }
            PasscodeInputMode.CONFIRM_PASSCODE -> {
                if (initialPasscode == passcode) {
                    listener?.onNewPasscodeConfirmed(passcode = passcode)
                    updatePasscodeOutput("")
                } else {
                    inputMode = PasscodeInputMode.NEW_PASSCODE
                    showError(R.string.errors_passcode_not_match)
                    listener?.onInputInvalidPasscode(inputMode)
                }
            }
        }
    }

    private fun showError(@StringRes errorRes: ResId) {
        showError(context.getString(errorRes))
    }

    private fun showError(error: String) {
        descriptionView?.text = error
        errorVibrate()

        descriptionView?.alpha = 1f
        descriptionView?.animate()?.setStartDelay(3000L)?.alpha(0f)?.setDuration(500L)?.start()
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    private fun errorVibrate() {
        if (buildVersion26orGreater) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
        } else vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
    }
}
