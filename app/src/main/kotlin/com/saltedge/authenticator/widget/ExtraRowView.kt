/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.saltedge.authenticator.databinding.ViewExtraBinding
import com.saltedge.authenticator.tools.setVisible

class ExtraRowView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding: ViewExtraBinding

    init {
        binding = ViewExtraBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setTitle(title: String) {
        binding.titleView.text = title
    }

    fun setTitle(titleResId: Int) {
        setTitle(context.getString(titleResId))
    }

    fun setDescription(text: String) {
        binding.descriptionView.text = text
    }

    fun setDescription(textResId: Int) {
        setDescription(context.getString(textResId))
    }

    fun setVisible(show: Boolean) {
        binding.contentView.setVisible(show = show)
    }
}
