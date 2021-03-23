/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.widget.biometric

import android.annotation.TargetApi
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.fentury.applock.tools.ResId

/**
 * A handler class which start and manage callbacks from BiometricPrompt.
 */
@TargetApi(Build.VERSION_CODES.P)
internal class BiometricPromptManagerV28 : BiometricPromptAbs, DialogInterface.OnClickListener {

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
        title: String,
        descriptionResId: ResId,
        @StringRes negativeActionTextResId: ResId
    ) {
        try {
            val signal = CancellationSignal()
            cancellationSignal = signal
            val builder = BiometricPrompt.Builder(context)
            builder.setTitle(title)
            builder.setSubtitle(context.getString(descriptionResId))
            builder.setNegativeButton(
                context.getString(negativeActionTextResId),
                context.mainExecutor,
                this
            )
            val prompt: BiometricPrompt = builder.build()
            prompt.authenticate(signal, context.mainExecutor, authenticationCallBack)
        } catch (e: Exception) {
            cancelPrompt()
        }
    }

    override fun dismissBiometricPrompt() {
        try {
            cancelPrompt()
        } catch (e: Exception) {}
    }

    private fun cancelPrompt() {
        try {
            cancellationSignal?.cancel()
        } catch (e: Exception) { }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        cancellationSignal?.cancel()
        cancellationSignal = null
        dialog?.dismiss()
        resultCallback?.biometricsCanceledByUser()
    }
}