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
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.saltedge.authenticator.R
import com.saltedge.authenticator.databinding.ViewCompleteBinding
import com.saltedge.authenticator.tools.setInvisible
import com.saltedge.authenticator.tools.setVisible

/**
 * View show final state (success, error)
 */
class CompleteView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding: ViewCompleteBinding

    init {
        binding = ViewCompleteBinding.inflate(LayoutInflater.from(context), this, true)
        initAttributes(context, attrs)
    }

    fun setTitleText(text: String) {
        binding.titleView.text = text
    }

    fun setTitleText(textResId: Int) {
        binding.titleView.text = context.getString(textResId)
    }

    fun setTitleText(spannable: SpannableString) {
        binding.titleView.text = spannable
    }

    fun setDescription(text: String) {
        binding.descriptionView.text = text
    }

    fun setDescription(textResId: Int) {
        binding.descriptionView.text = context.getString(textResId)
    }

    fun setMainActionText(@StringRes textId: Int) = setMainActionText(context.getString(textId))

    fun setAltActionText(@StringRes textId: Int?) {
        setAltActionText(textId?.let { context.getString(it) })
    }

    fun setIconResource(@DrawableRes resId: Int) {
        binding.iconView.setImageDrawable(ContextCompat.getDrawable(context, resId))
    }

    fun setClickListener(l: OnClickListener?) {
        binding.actionView.setOnClickListener(l)
        binding.altActionView.setOnClickListener(l)
    }

    private fun initAttributes(context: Context, attrs: AttributeSet) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CompleteView)
        try {
            attributes.getString(R.styleable.CompleteView_title)?.let { setTitleText(it) }
            attributes.getString(R.styleable.CompleteView_description)?.let { setDescription(it) }
            setMainActionText(attributes.getString(R.styleable.CompleteView_mainActionText))
            setAltActionText(attributes.getString(R.styleable.CompleteView_altActionText))
            setIconResource(
                attributes.getResourceId(
                    R.styleable.CompleteView_iconSrc,
                    R.mipmap.ic_launcher
                )
            )
        } finally {
            attributes.recycle()
        }
    }

    private fun setMainActionText(text: String?) {
        binding.actionView.setInvisible(text == null)
        binding.actionView.isClickable = text != null
        binding.actionView.text = text
    }

    private fun setAltActionText(text: String?) {
        binding.altActionView.setVisible(show = text != null)
        binding.altActionView.text = text
    }
}
