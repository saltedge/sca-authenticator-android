/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.tools

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.saltedge.authenticator.R

typealias ResId = Int

/**
 * Set alpha to color
 *
 * @receiver Int object
 * @param factor - option to change the primary color
 * @return Int object - resource color
 */
fun Int.applyAlphaToColor(factor: Float): Int {
    if (factor >= 1f || factor < 0) return this
    val alpha = Math.round(Color.alpha(this) * factor)
    return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
}

/**
 * Get enabled state. Depending of the state, returns the color.
 *
 * @param isEnabled - color status parameter
 * @return ResId (Int) object - color resource
 */
fun getEnabledStateColorResId(isEnabled: Boolean): ResId {
    return if (isEnabled) R.color.button_blue_default else R.color.button_blue_disabled
}

/**
 * Convert dp to px
 *
 * @param dp - number of density pixels
 * @return Int object - number of pixels
 */
fun convertDpToPx(dp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp,
        Resources.getSystem().displayMetrics
    ).toInt()
}

fun SwipeRefreshLayout.stopRefresh() {
    isRefreshing = false
    destroyDrawingCache()
    clearAnimation()
}

fun SpannableStringBuilder.appendColoredText(
    text: String,
    colorRes: ResId,
    context: Context
): SpannableStringBuilder {
    val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, colorRes))
    return this.append(text, colorSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}

val Context.mediumTypefaceSpan: CustomTypefaceSpan?
    get() = ResourcesCompat.getFont(this, R.font.roboto_medium)?.let { CustomTypefaceSpan(typeface = it) }

