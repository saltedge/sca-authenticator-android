/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.saltedge.authenticator.R
import kotlinx.android.synthetic.main.view_passcode_label.view.*

private const val PASSCODE_SIZE = 4

/**
 * The class helps to display the passcode that is entered from the keyboard
 *
 * @see PinpadView
 */
class PasscodeLabelView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs),
    PinpadClickListener { //TODO: Move business logic from class to view model

    var resultListener: PasscodeInputResultListener? = null
    private var text: String = ""
    private val views: Array<ImageView>

    init {
        LayoutInflater.from(context).inflate(R.layout.view_passcode_label, this)
        views = arrayOf(firstItemView, secondItemView, thirdItemView, fourthItemView)
    }

    override fun onDeleteKeyClick() {
        if (text.isNotEmpty()) {
            text = text.take(text.length - 1)
            views[text.length].setImageResource(R.drawable.shape_passcode_item_off)
        }
    }

    override fun onDigitKeyClick(value: String) {
        if (text.length < PASSCODE_SIZE) {
            text = text.plus(value)
            views[text.length - 1].setImageResource(R.drawable.shape_passcode_item_on)
            if (text.length == PASSCODE_SIZE) {
                resultListener?.onPasscodeInputFinished(text)
            }
        }
    }

    fun clearAll() {
        text = ""
        views.forEach { it.setImageResource(R.drawable.shape_passcode_item_off) }
    }

    interface PasscodeInputResultListener {
        fun onPasscodeInputFinished(passcode: String)
    }
}
