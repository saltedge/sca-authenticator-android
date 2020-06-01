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
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.setVisible
import kotlinx.android.synthetic.main.view_passcode_edit.view.*

enum class PasscodeInputMode {
    CHECK_PASSCODE, NEW_PASSCODE, CONFIRM_PASSCODE
}

/**
 * Passcode input container
 *
 * @see KeypadView
 */
class PasscodeEditView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    KeypadView.KeypadClickListener {
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
        LayoutInflater.from(context).inflate(R.layout.view_passcode_edit, this)
        setupViews()
    }

    override fun onDigitKeyClick(value: String) {
        val text: String = passcodeLabelView?.text?.toString() ?: return
        updatePasscodeOutput(text + value)
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
        descriptionView?.visibility = View.INVISIBLE
        updatePasscodeOutput("")
        keypadView?.setupFingerAction(active = biometricsActionIsAvailable)
        keypadView?.clickListener = this
        submitView?.setOnClickListener {
            onPasscodeInputFinished(passcode = passcodeLabelView?.text?.toString() ?: "")
        }
    }

    private fun updatePasscodeOutput(text: String) {
        passcodeLabelView?.setText(text)
        submitView?.setVisible(show = (4..16).contains(text.length))
        keypadView?.let {
            if (text.isEmpty() && biometricsActionIsAvailable) it.showFingerView() else it.showDeleteView()
        }
    }

    private fun onPasscodeInputFinished(passcode: String) {
        when (inputMode) {
            PasscodeInputMode.CHECK_PASSCODE -> {
                if (initialPasscode == passcode) listener?.onInputValidPasscode()
                else {
                    inputMode = PasscodeInputMode.CHECK_PASSCODE
                    showError(R.string.errors_wrong_passcode)
                    listener?.onInputInvalidPasscode(inputMode)
                }
            }
            PasscodeInputMode.NEW_PASSCODE -> {
                descriptionView?.visibility = View.INVISIBLE
                initialPasscode = passcode
                inputMode = PasscodeInputMode.CONFIRM_PASSCODE
                listener?.onNewPasscodeEntered(inputMode, passcode)
            }
            PasscodeInputMode.CONFIRM_PASSCODE -> {
                if (initialPasscode == passcode) listener?.onNewPasscodeConfirmed(passcode = passcode)
                else {
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
        descriptionView?.visibility = View.VISIBLE
        descriptionView?.text = error
        errorVibrate()
        descriptionView?.animate()?.setStartDelay(3000L)?.withEndAction {
            descriptionView?.visibility = View.INVISIBLE
        }?.start()
    }

    @Suppress("DEPRECATION")
    private fun errorVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
        } else vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
    }
}
