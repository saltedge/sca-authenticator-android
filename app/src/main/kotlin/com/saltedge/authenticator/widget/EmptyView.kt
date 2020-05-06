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
package com.saltedge.authenticator.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.setFont
import com.saltedge.authenticator.tools.setInvisible
import kotlinx.android.synthetic.main.view_empty.view.*

/**
 * View show empty screen
 */
class EmptyView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_empty, this)
        initAttributes(context, attrs)
        actionView?.setFont(R.font.roboto_regular)
    }

    fun setTitle(title: String) {
        titleView?.text = title
    }

    fun setTitle(titleResId: Int) {
        setTitle(context.getString(titleResId))
    }

    fun setDescription(description: String) {
        descriptionView?.text = description
    }

    fun setDescription(descriptionResId: Int) {
        setDescription(context.getString(descriptionResId))
    }

    fun setActionText(@StringRes textId: Int) = setActionText(context.getString(textId))

    fun setActionText(text: String?) {
        actionView?.setInvisible(text == null)
        actionView?.isClickable = text != null
        actionView?.text = text
    }

    fun setImageResource(@DrawableRes resId: Int) {
        emptyImageView?.setImageResource(resId)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        actionView?.setOnClickListener(l)
    }

    private fun initAttributes(context: Context, attrs: AttributeSet) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.EmptyView)
        try {
            attributes.getString(R.styleable.EmptyView_title)?.let { setTitle(it) }
            attributes.getString(R.styleable.EmptyView_description)?.let { setDescription(it) }
            setActionText(attributes.getString(R.styleable.EmptyView_mainActionText))
            setImageResource(
                attributes.getResourceId(
                    R.styleable.EmptyView_iconSrc,
                    R.mipmap.ic_launcher
                )
            )
        } finally {
            attributes.recycle()
        }
    }
}
