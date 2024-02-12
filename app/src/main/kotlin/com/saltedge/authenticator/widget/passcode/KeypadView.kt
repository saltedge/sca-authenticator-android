/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.passcode

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.databinding.ViewKeypadBinding
import com.saltedge.authenticator.tools.setVisible

/**
 * The class contains button panel for entering a passcode and extra actions
 */
class KeypadView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    View.OnClickListener {

    var clickListener: KeypadClickListener? = null
    private var binding: ViewKeypadBinding

    private var vibrator: Vibrator? = context.getSystemService(VIBRATOR_SERVICE) as? Vibrator?

    init {
        binding = ViewKeypadBinding.inflate(LayoutInflater.from(context), this, true)
        for (i in 0..binding.pinpadLayout.childCount) binding.pinpadLayout.getChildAt(i)?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (!isEnabled) return
        vibrateOnKeyClick()
        when (view?.id ?: return) {
            R.id.fingerActionView -> clickListener?.onFingerKeyClick()
            R.id.successActionView -> clickListener?.onSuccessKeyClick()
            R.id.forgotActionView -> clickListener?.onForgotKeyClick()
            else -> clickListener?.onDigitKeyClick((view as? TextView)?.text.toString())
        }
    }

    fun setupFingerAction(active: Boolean) {
        binding.fingerActionView.setVisible(active)
        binding.forgotActionView.setVisible(active)
        binding.successActionView.setVisible(!active)
    }

    @Suppress("DEPRECATION")
    private fun vibrateOnKeyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(30, 32))
        } else vibrator?.vibrate(10)
    }

    fun showSuccessView() {
        binding.fingerActionView.setVisible(show = false)
        binding.successActionView.setVisible(show = true)
    }

    fun showFingerView() {
        binding.fingerActionView.setVisible(show = true)
        binding.successActionView.setVisible(show = false)
    }

    interface KeypadClickListener {
        fun onDigitKeyClick(value: String = "")
        fun onFingerKeyClick()
        fun onForgotKeyClick()
        fun onSuccessKeyClick()
    }
}
