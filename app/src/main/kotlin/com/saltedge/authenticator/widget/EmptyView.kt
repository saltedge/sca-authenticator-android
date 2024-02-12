/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.saltedge.authenticator.R
import com.saltedge.authenticator.databinding.ViewEmptyBinding
import com.saltedge.authenticator.tools.setInvisible

/**
 * View show empty screen
 */
class EmptyView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding: ViewEmptyBinding

    init {
        binding = ViewEmptyBinding.inflate(LayoutInflater.from(context), this, true)
        initAttributes(context, attrs)
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

    fun setActionText(@StringRes textId: Int?) {
        setActionText(textId?.let { context.getString(it) })
    }

    fun setActionText(text: String?) {
        binding.actionView.setInvisible(text == null)
        binding.actionView.isClickable = text != null
        binding.actionView.text = text
    }

    fun setImageResource(@DrawableRes resId: Int) {
        binding.emptyImageView.setImageResource(resId)
    }

    fun setActionOnClickListener(l: OnClickListener?) {
        binding.actionView.setOnClickListener(l)
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
