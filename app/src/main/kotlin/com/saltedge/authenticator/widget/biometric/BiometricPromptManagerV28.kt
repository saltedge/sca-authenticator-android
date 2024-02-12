/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.biometric

import android.annotation.TargetApi
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.saltedge.authenticator.tools.ResId
import timber.log.Timber

/**
 * A handler class which start and manage callbacks from BiometricPrompt.
 */
@TargetApi(Build.VERSION_CODES.P)
class BiometricPromptManagerV28 : BiometricPromptAbs, DialogInterface.OnClickListener {

    override var resultCallback: BiometricPromptCallback? = null
    private var authenticationCallBack = object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            resultCallback?.biometricAuthFinished()
        }
    }
    private var cancellationSignal: CancellationSignal? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun showBiometricPrompt(
        context: FragmentActivity,
        titleResId: ResId,
        descriptionResId: ResId,
        @StringRes negativeActionTextResId: ResId
    ) {
        try {
            val signal = CancellationSignal()
            cancellationSignal = signal
            val builder = BiometricPrompt.Builder(context)
            builder.setTitle(context.getString(titleResId))
            builder.setSubtitle(context.getString(descriptionResId))
            builder.setNegativeButton(
                context.getString(negativeActionTextResId),
                context.mainExecutor,
                this
            )
            val prompt: BiometricPrompt = builder.build()
            prompt.authenticate(signal, context.mainExecutor, authenticationCallBack)
        } catch (e: Exception) {
            Timber.e(e)
            cancelPrompt()
        }
    }

    override fun dismissBiometricPrompt() {
        try {
            cancelPrompt()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun cancelPrompt() {
        try {
            cancellationSignal?.cancel()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        cancellationSignal?.cancel()
        cancellationSignal = null
        dialog?.dismiss()
        resultCallback?.biometricsCanceledByUser()
    }
}
