/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.widget.biometric

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.animation.CycleInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import com.fentury.applock.R
import com.fentury.applock.root.showDialogFragment
import com.fentury.applock.tools.ResId
import com.fentury.applock.tools.biometric.BiometricToolsAbs
import com.fentury.applock.tools.setTextColorResId
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

internal class BiometricsInputDialog(
    val biometricTools: BiometricToolsAbs
) : BottomSheetDialogFragment(),
    BiometricPromptAbs,
    BiometricsInputContract.View
{
    private val presenter = BiometricsInputPresenter(contract = this, biometricTools = biometricTools, context = context)
    private val titleView: TextView?
        get() = dialog?.findViewById<TextView>(R.id.titleView)
    private val descriptionView: TextView?
        get() = dialog?.findViewById<TextView>(R.id.descriptionView)
    private val statusImageView: ImageView?
        get() = dialog?.findViewById<ImageView>(R.id.statusImageView)
    private val cancelActionView: TextView?
        get() = dialog?.findViewById<TextView>(R.id.cancelActionView)
    override var resultCallback: BiometricPromptCallback? = null
    private fun getDialogViewLayout(): Int = R.layout.dialog_fingerprint

    override fun showBiometricPrompt(
        context: FragmentActivity,
        title: String,
        @StringRes descriptionResId: ResId,
        @StringRes negativeActionTextResId: ResId
    ) {
        if (presenter.initialized) {
            arguments = BiometricsInputPresenter.dataBundle(title, descriptionResId, negativeActionTextResId)
            if (!isAdded) context.showDialogFragment(this)
        } else {
            Toast.makeText(context, R.string.errors_internal_error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun dismissBiometricPrompt() {
        try {
            dismiss()
        } catch (e: Exception) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        presenter.setInitialData(arguments)
    }

    override fun onStart() {
        super.onStart()
        titleView?.setText(presenter.titleRes)
        descriptionView?.setText(presenter.descriptionRes)
        cancelActionView?.setText(presenter.negativeActionTextRes)
        cancelActionView?.setOnClickListener { onNegativeActionClick() }
    }

    override fun onResume() {
        super.onResume()
        activity?.let { presenter.onDialogResume(context = it) }
    }

    override fun onPause() {
        super.onPause()
        presenter.onDialogPause()
    }

    override fun updateStatusView(
        imageResId: Int,
        textColorResId: Int,
        textResId: Int,
        animateText: Boolean
    ) {
        statusImageView?.setImageResourceAnimated(imageResId)
        descriptionView?.setTextColorResId(textColorResId)
        descriptionView?.setText(textResId)
        if (animateText) descriptionView?.shakeView()
    }

    override fun sendAuthSuccessResult() {
        dismiss()
        resultCallback?.biometricAuthFinished()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

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

    /**
     * Convert dp to px
     *
     * @param dp - number of density pixels
     * @return Int object - number of pixels
     */
    private fun convertDpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private val callback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (!slideOffset.isNaN()) dialog?.window?.setDimAmount(0.5f - ((slideOffset * -1) / 2))
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
        }
    }

    private fun onNegativeActionClick() {
        dismiss()
        resultCallback?.biometricsCanceledByUser()
    }

    private fun ImageView.setImageResourceAnimated(@DrawableRes imageResId: Int) {
        ViewCompat.animate(this).withLayer().setDuration(150)
            .alpha(0.1f).scaleX(0.1f).scaleY(0.1f)
            .withEndAction {
                this.setImageResource(imageResId)
                ViewCompat.animate(this).withLayer()
                    .alpha(1f).scaleX(1f).scaleY(1f)
                    .duration = 150
            }
    }

    private fun View.shakeView() = ViewCompat.animate(this).withLayer()
        .translationX(20f).setDuration(400).setInterpolator(CycleInterpolator(5f))
}