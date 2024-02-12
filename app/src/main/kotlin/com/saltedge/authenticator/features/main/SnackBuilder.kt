/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.main

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.setFont

fun FragmentActivity.showWarningSnack(
    textResId: Int,
    snackBarDuration: Int = Snackbar.LENGTH_INDEFINITE,
    actionResId: Int? = null
) = showWarningSnack(this.getString(textResId), snackBarDuration, actionResId)

fun FragmentActivity.showWarningSnack(
    message: String,
    snackBarDuration: Int = Snackbar.LENGTH_INDEFINITE,
    actionResId: Int? = null
) = getSnackbarAnchorView()?.buildSnackbar(message, snackBarDuration, actionResId)?.apply { show() }

fun FragmentActivity.buildWarningSnack(
    messageRes: ResId,
    snackBarDuration: Int = Snackbar.LENGTH_INDEFINITE,
    actionResId: Int? = null
) = getSnackbarAnchorView()?.buildSnackbar(this.getString(messageRes), snackBarDuration, actionResId)

private fun Activity.getSnackbarAnchorView(): View? {
    return if (this is SnackbarAnchorContainer) getSnackbarAnchorView() else null
}

private fun View.buildSnackbar(
    message: String,
    snackBarDuration: Int,
    actionResId: Int? = null
): Snackbar {
    val snackbar = Snackbar
        .make(this, message, snackBarDuration)
        .setActionTextColor(ContextCompat.getColor(context, R.color.primary_light))
    if (actionResId != null) snackbar.setAction(actionResId) { snackbar.dismiss() }
    val textView = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
    textView.minimumHeight = context.resources.getDimension(R.dimen.action_bar_size).toInt()
    textView.gravity = Gravity.CENTER_VERTICAL
    textView.setFont(R.font.roboto_regular)
    textView.setTextColor(ContextCompat.getColor(context, R.color.grey_40))
    textView.maxLines = 7
    snackbar.view.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_black_and_dark_100))
    snackbar.view.setOnTouchListener { _, _ ->
        snackbar.dismiss()
        true
    }
    return snackbar
}

interface SnackbarAnchorContainer {
    fun getSnackbarAnchorView(): View?
}
