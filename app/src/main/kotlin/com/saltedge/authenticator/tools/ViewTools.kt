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
package com.saltedge.authenticator.tools

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

/**
 * Set view visible or gone
 *
 * @receiver View object
 * @param show - the parameter that indicates whether to show the view or not
 */
fun View?.setVisible(show: Boolean = true) {
    this?.visibility = if (show) View.VISIBLE else View.GONE
}

/**
 * Set view invisible or visible
 *
 * @receiver View object
 * @param invisible - the parameter that indicates whether to show the view or not
 */
fun View?.setInvisible(invisible: Boolean = true) {
    this?.visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}

/**
 * Hide system keyboard
 *
 * @receiver EditText object
 */
fun EditText.hideSystemKeyboard() {
    val context = this.context
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodManager?.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

/**
 * Set font for text view in application
 *
 * @receiver TextView object
 */
fun TextView.setFont(@FontRes fontId: Int) {
    typeface = ResourcesCompat.getFont(context, fontId)
}

/**
 * Checks if text in TextView is truncated
 *
 * @receiver TextView object
 * @return boolean, true if text can be truncated
 */
fun TextView.isTextTruncated(): Boolean? {
    if (layout != null) {
        val lines = layout.lineCount
        return lines > 0 && layout.getEllipsisCount(lines - 1) > 0
    }
    return null
}

/**
 * Inflate list item view
 *
 * @receiver view group
 * @param resId - the resource that need to inflate
 * @return View object
 */
fun ViewGroup.inflateListItemView(resId: Int): View =
    LayoutInflater.from(context).inflate(resId, this, false)

/**
 * Allows to set the color of the text
 *
 * @receiver TextView object
 * @param colorResId - resource color
 */
fun TextView.setTextColorResId(colorResId: Int) {
    ContextCompat.getColor(this.context, colorResId).also { this.setTextColor(it) }
}
