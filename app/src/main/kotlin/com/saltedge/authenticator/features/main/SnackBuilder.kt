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
package com.saltedge.authenticator.features.main

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.setFont

fun AppCompatActivity.showWarning(text: String) = getSnackbarAnchorView()?.showWarning(text)

fun View.showWarning(text: String) = showSnackbar(
    messageText = text,
    bgColorResId = android.R.color.holo_orange_dark
)

private fun Activity.getSnackbarAnchorView(): View? {
    return if (this is SnackbarAnchorContainer) getSnackbarAnchorView() else null
}

private fun View.showSnackbar(
    messageText: String,
    @ColorRes bgColorResId: Int
) {
    val snackbar = Snackbar.make(this, messageText, Snackbar.LENGTH_INDEFINITE)
    val textView = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
    textView.minimumHeight = context.resources.getDimension(R.dimen.action_bar_size).toInt()
    textView.gravity = Gravity.CENTER_VERTICAL
    textView.setFont(R.font.roboto_regular)
    textView.maxLines = 7
    snackbar.view.setBackgroundColor(ContextCompat.getColor(context, bgColorResId))
    val fab = this.findViewById<FloatingActionButton>(R.id.actionButton)
    snackbar.view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {}

        override fun onViewDetachedFromWindow(v: View) {
            fab?.translationY = 0f
        }
    })
    snackbar.view.setOnTouchListener { _, _ ->
        snackbar.dismiss()
        true
    }
    snackbar.show()
}

interface SnackbarAnchorContainer {
    fun getSnackbarAnchorView(): View?
}
