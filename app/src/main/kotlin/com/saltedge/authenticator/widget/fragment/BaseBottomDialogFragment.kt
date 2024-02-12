/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.saltedge.authenticator.tools.convertDpToPx

abstract class BaseBottomDialogFragment : BottomSheetDialogFragment() {

    abstract fun getDialogViewLayout(): Int

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, getDialogViewLayout(), null)
        dialog.setContentView(contentView)
        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        (params.behavior as? BottomSheetBehavior)?.let {
            it.setBottomSheetCallback(callback)
            it.peekHeight = convertDpToPx(dp = 540f)
        }
    }

    private val callback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (!slideOffset.isNaN()) dialog?.window?.setDimAmount(0.5f - ((slideOffset * -1) / 2))
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
        }
    }
}
