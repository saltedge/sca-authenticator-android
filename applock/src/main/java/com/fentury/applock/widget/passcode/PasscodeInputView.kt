/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.widget.passcode

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.fentury.applock.R
import com.fentury.applock.tools.ResId
import com.fentury.applock.tools.setVisible
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
internal class PasscodeInputView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
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
        get() = errorView?.text?.toString() ?: ""
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
        errorView?.alpha = 0f

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
                errorView?.alpha = 0f

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
        errorView?.text = error
        errorVibrate()

        errorView?.alpha = 1f
        errorView?.animate()?.setStartDelay(3000L)?.alpha(0f)?.setDuration(500L)?.start()
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    private fun errorVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
        } else vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
    }
}