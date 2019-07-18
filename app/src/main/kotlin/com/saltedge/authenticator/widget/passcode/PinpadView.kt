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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.setVisible
import kotlinx.android.synthetic.main.view_pinpad.view.*

class PinpadView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), View.OnClickListener {

    var inputHandler: PinpadInputHandler? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pinpad, this)
        for (i in 0..pinpadLayout.childCount) pinpadLayout.getChildAt(i)?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (!isEnabled) return
        val viewId = view?.id ?: return
        when (viewId) {
            R.id.fingerView -> inputHandler?.onKeyClick(Control.FINGER)
            R.id.deleteView -> inputHandler?.onKeyClick(Control.DELETE)
            else -> (view as? TextView)?.text
                    ?.let { inputHandler?.onKeyClick(Control.NUMBER, it.toString()) }
        }
    }

    fun setupFingerAction(active: Boolean) {
        fingerView?.setVisible(active)
    }

    enum class Control {
        DELETE, FINGER, NUMBER
    }
}
