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
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.setVisible
import kotlinx.android.synthetic.main.view_pinpad.view.*

/**
 * The class contains button panel for entering a passcode
 */
class KeypadView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    View.OnClickListener {

    var clickListener: KeypadClickListener? = null
    var fingerprintActionClickListener: PasscodeInputViewListener? = null

    private var vibrator: Vibrator? = context.getSystemService(VIBRATOR_SERVICE) as? Vibrator?

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pinpad, this)
        for (i in 0..pinpadLayout.childCount) pinpadLayout.getChildAt(i)?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view == null || !isEnabled) return
        vibrateOnKeyClick()
        when (view.id) {
            R.id.fingerView -> fingerprintActionClickListener?.onBiometricInputSelected()
            R.id.deleteView -> clickListener?.onDeleteKeyClick()
            R.id.forgotView -> fingerprintActionClickListener?.onResetPasscode()
            else -> clickListener?.onDigitKeyClick((view as? TextView)?.text.toString())
        }
    }

    fun setupFingerAction(active: Boolean) {
        fingerView?.setVisible(active)
        forgotView?.setVisible(active)
        deleteView?.setVisible(!active)
    }

    @Suppress("DEPRECATION")
    private fun vibrateOnKeyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(40, 32))
        } else vibrator?.vibrate(10)
    }

    fun showDeleteView() {
        fingerView?.setVisible(show = false)
        deleteView?.setVisible(show = true)
    }

    fun showFingerView() {
        fingerView?.setVisible(show = true)
        deleteView?.setVisible(show = false)
    }

    interface KeypadClickListener {
        fun onDeleteKeyClick()
        fun onDigitKeyClick(value: String = "")
    }
}
