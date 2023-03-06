/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.setVisible
import kotlinx.android.synthetic.main.view_extra.view.*

class ExtraRowView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_extra, this)
    }

    fun setTitle(title: String) {
        titleView?.text = title
    }

    fun setTitle(titleResId: Int) {
        setTitle(context.getString(titleResId))
    }

    fun setDescription(text: String) {
        descriptionView?.text = text
    }

    fun setDescription(textResId: Int) {
        setDescription(context.getString(textResId))
    }

    fun setVisible(show: Boolean) {
        contentView?.setVisible(show = show)
    }
}
