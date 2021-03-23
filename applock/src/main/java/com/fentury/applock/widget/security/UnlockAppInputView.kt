/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.widget.security

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.fentury.applock.R
import com.fentury.applock.root.SEAppLock
import com.fentury.applock.widget.passcode.PasscodeInputListener
import com.fentury.applock.widget.passcode.PasscodeInputMode
import com.fentury.applock.tools.setVisible
import kotlinx.android.synthetic.main.view_passcode_input.view.*
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
        actionView?.text = SEAppLock.actionButtonText
        actionView?.setOnClickListener { passcodeInputViewListener?.onClearApplicationDataSelected() }
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

    fun setInputViewVisibility(show: Boolean) {
        passcodeInputView?.setVisible(show)
        appLogoView?.setVisible(show)
    }

    fun setResetPasscodeViewVisibility(show: Boolean) {
        appLogoView?.setVisible(!show)
        resetPasscodeLayout?.setVisible(show)
    }

    fun hideWarning() {
        passcodeInputView?.descriptionView?.visibility = View.GONE
    }
}