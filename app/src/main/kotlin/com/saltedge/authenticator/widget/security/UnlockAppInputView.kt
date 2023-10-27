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
import android.widget.LinearLayout
import com.saltedge.authenticator.R
import com.saltedge.authenticator.databinding.ViewUnlockBinding
import com.saltedge.authenticator.tools.setVisible
import com.saltedge.authenticator.widget.passcode.PasscodeInputListener
import com.saltedge.authenticator.widget.passcode.PasscodeInputMode

class UnlockAppInputView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding: ViewUnlockBinding

    var biometricsActionIsAvailable: Boolean
        get() = binding.passcodeInputView.biometricsActionIsAvailable ?: false
        set(value) {
            binding.passcodeInputView.biometricsActionIsAvailable = value
        }
    var passcodeInputViewListener: PasscodeInputListener?
        get() = binding.passcodeInputView.listener
        set(value) {
            binding.passcodeInputView.listener = value
        }

    init {
        binding = ViewUnlockBinding.inflate(LayoutInflater.from(context), this, true)
        binding.resetContentView.setActionOnClickListener(OnClickListener { passcodeInputViewListener?.onClearDataActionSelected() })
        binding.resetBackActionImageView.setOnClickListener {
            setInputViewVisibility(show = true)
            setResetPasscodeViewVisibility(show = false)
        }
        binding.passcodeInputView.title = context.getString(R.string.passcode_enter_passcode_title)
    }

    fun setSavedPasscode(currentPasscode: String) {
        binding.passcodeInputView.inputMode = PasscodeInputMode.CHECK_PASSCODE
        binding.passcodeInputView.initialPasscode = currentPasscode
    }

    fun setInputViewVisibility(show: Boolean) {
        binding.passcodeInputView.setVisible(show)
        binding.appLogoView.setVisible(show)
    }

    fun setResetPasscodeViewVisibility(show: Boolean) {
        binding.appLogoView.setVisible(!show)
        binding.resetPasscodeLayout.setVisible(show)
    }

    fun hideWarning() {
        binding.passcodeInputView.hideDescription()
    }
}
