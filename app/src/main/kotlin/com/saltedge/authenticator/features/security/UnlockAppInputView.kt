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
package com.saltedge.authenticator.features.security

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.DELETE_ALL_REQUEST_CODE
import com.saltedge.authenticator.tool.setVisible
import com.saltedge.authenticator.widget.passcode.PasscodeInputView
import com.saltedge.authenticator.widget.passcode.PasscodeInputViewListener
import kotlinx.android.synthetic.main.view_unlock.view.*

class UnlockAppInputView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    View.OnClickListener {

    var biometricsActionIsAvailable: Boolean
        get() = passcodeInputView?.biometricsActionIsAvailable ?: false
        set(value) {
            passcodeInputView?.biometricsActionIsAvailable = value
        }
    var listener: PasscodeInputViewListener?
        get() = passcodeInputView?.listener
        set(value) {
            passcodeInputView?.listener = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_unlock, this)
        clearView?.setOnClickListener(this)
        backImageView?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        val viewId = view?.id ?: return
        when (viewId) {
            R.id.clearView -> listener?.showDeleteConnectionView(requestCode = DELETE_ALL_REQUEST_CODE)
            R.id.backImageView -> setInputViewVisibility(show = true)
        }
    }

    fun setSavedPasscode(currentPasscode: String) {
        passcodeInputView?.initInputMode(
            PasscodeInputView.InputMode.CHECK_PASSCODE,
            currentPasscode
        )
    }

    fun setDescriptionText(text: String) {
        descriptionView?.text = text
    }

    fun setDescriptionText(textResId: Int) {
        descriptionView?.setText(textResId)
    }

    fun setInputViewVisibility(show: Boolean) {
        passcodeInputView?.setVisible(show)
        appLogoView?.setVisible(show)
    }

    fun setResetPasscodeView() {
        titleView?.text = context?.getString(R.string.fingerprint_forgot_passcode)
        subTitleView?.text = context?.getString(R.string.fingerprint_forgot_passcode_description)
    }

    fun setResetPasscodeViewVisibility(show: Boolean) {
        appLogoView?.setVisible(!show)
        resetPasscodeLayout?.setVisible(show)
        showErrorMessage(show)
    }

    fun showErrorMessage(show: Boolean) {
        descriptionView?.setVisible(show)
    }
}
