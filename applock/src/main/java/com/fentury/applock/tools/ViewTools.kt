/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools

import android.view.View
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
 * Set font for text view in application
 *
 * @receiver TextView object
 */
fun TextView.setFont(@FontRes fontId: Int) {
    typeface = ResourcesCompat.getFont(context, fontId)
}

/**
 * Allows to set the color of the text
 *
 * @receiver TextView object
 * @param colorResId - resource color
 */
fun TextView.setTextColorResId(colorResId: Int) {
    ContextCompat.getColor(this.context, colorResId).also { this.setTextColor(it) }
}