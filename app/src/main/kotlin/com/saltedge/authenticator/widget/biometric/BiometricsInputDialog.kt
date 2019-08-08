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
package com.saltedge.authenticator.widget.biometric

import android.os.Bundle
import android.view.View
import android.view.animation.CycleInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_ACTION
import com.saltedge.authenticator.sdk.constants.KEY_DESCRIPTION
import com.saltedge.authenticator.sdk.constants.KEY_TITLE
import com.saltedge.authenticator.tool.ResId
import com.saltedge.authenticator.tool.setTextColorResId
import com.saltedge.authenticator.tool.showDialogFragment
import com.saltedge.authenticator.widget.fragment.BaseRoundedBottomDialogFragment

class BiometricsInputDialog : BaseRoundedBottomDialogFragment(),
    BiometricPromptAbs,
    BiometricsInputContract.View {

    private val presenter = BiometricsInputPresenter(contract = this)
    private val titleView: TextView? by lazy {
        dialog?.findViewById<TextView>(R.id.titleView)
    }
    private val descriptionView: TextView? by lazy {
        dialog?.findViewById<TextView>(R.id.descriptionView)
    }
    private val statusImageView: ImageView? by lazy {
        dialog?.findViewById<ImageView>(R.id.statusImageView)
    }
    private val cancelActionView: TextView? by lazy {
        dialog?.findViewById<TextView>(R.id.cancelActionView)
    }
    override var resultCallback: BiometricPromptCallback? = null
    override fun showBiometricPrompt(
        context: FragmentActivity,
        @StringRes titleResId: ResId,
        @StringRes descriptionResId: ResId,
        @StringRes negativeActionTextResId: ResId
    ) {
        arguments = Bundle().apply {
            putInt(KEY_TITLE, titleResId)
            putInt(KEY_DESCRIPTION, descriptionResId)
            putInt(KEY_ACTION, negativeActionTextResId)
        }
        if (!isAdded) context.showDialogFragment(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun getDialogViewLayout(): Int = R.layout.dialog_fingerprint

    override fun onStart() {
        super.onStart()
        arguments?.getInt(KEY_TITLE, R.string.settings_fingerprint)?.let {
            titleView?.text = getString(it)
        }
        arguments?.getInt(KEY_DESCRIPTION, R.string.fingerprint_touch_sensor)?.let {
            descriptionView?.text = getString(it)
        }
        arguments?.getInt(KEY_ACTION, R.string.actions_cancel)?.let {
            cancelActionView?.text = getString(it)
        }
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
