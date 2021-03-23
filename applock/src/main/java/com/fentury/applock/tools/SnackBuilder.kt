/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.fentury.applock.R
import com.google.android.material.snackbar.Snackbar

fun View.showWarningSnack(
    textResId: Int,
    snackBarDuration: Int = Snackbar.LENGTH_INDEFINITE,
    actionResId: Int? = null
) = this.buildSnackbar(this.context.getString(textResId), snackBarDuration, actionResId).apply { show() }

fun View.buildSnackbar(
    message: String,
    snackBarDuration: Int,
    actionResId: Int? = null
): Snackbar {
    val snackbar = Snackbar
        .make(this, message, snackBarDuration)
        .setActionTextColor(ContextCompat.getColor(context, android.R.color.white))
    if (actionResId != null) snackbar.setAction(actionResId) { snackbar.dismiss() }
    val textView = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
    textView.minimumHeight = context.resources.getDimension(R.dimen.action_bar_size).toInt()
    textView.gravity = Gravity.CENTER_VERTICAL
    textView.setFont(R.font.roboto_regular)
    textView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
    textView.maxLines = 7
    snackbar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.violet))
    snackbar.view.setOnTouchListener { _, _ ->
        snackbar.dismiss()
        true
    }
    return snackbar
}