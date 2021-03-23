/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.widget.passcode

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
import com.fentury.applock.R
import com.fentury.applock.tools.setVisible
import kotlinx.android.synthetic.main.view_keypad.view.*

/**
 * The class contains button panel for entering a passcode and extra actions
 */
internal class KeypadView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    View.OnClickListener {

    var clickListener: KeypadClickListener? = null

    private var vibrator: Vibrator? = context.getSystemService(VIBRATOR_SERVICE) as? Vibrator?

    init {
        LayoutInflater.from(context).inflate(R.layout.view_keypad, this)
        for (i in 0..pinpadLayout.childCount) pinpadLayout.getChildAt(i)?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (!isEnabled) return
        vibrateOnKeyClick()
        when (view?.id ?: return) {
            R.id.fingerActionView -> clickListener?.onFingerKeyClick()
            R.id.deleteActionView -> clickListener?.onDeleteKeyClick()
            R.id.forgotActionView -> clickListener?.onForgotKeyClick()
            else -> clickListener?.onDigitKeyClick((view as? TextView)?.text.toString())
        }
    }

    fun setupFingerAction(active: Boolean) {
        fingerActionView?.setVisible(active)
        forgotActionView?.setVisible(active)
        deleteActionView?.setVisible(!active)
    }

    @Suppress("DEPRECATION")
    private fun vibrateOnKeyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(30, 32))
        } else vibrator?.vibrate(10)
    }

    fun showDeleteView() {
        fingerActionView?.setVisible(show = false)
        deleteActionView?.setVisible(show = true)
    }

    fun showFingerView() {
        fingerActionView?.setVisible(show = true)
        deleteActionView?.setVisible(show = false)
    }

    interface KeypadClickListener {
        fun onDigitKeyClick(value: String = "")
        fun onFingerKeyClick()
        fun onForgotKeyClick()
        fun onDeleteKeyClick()
    }
}