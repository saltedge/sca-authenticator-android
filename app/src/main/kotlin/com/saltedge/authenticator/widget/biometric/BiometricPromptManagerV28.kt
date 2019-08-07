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

import android.annotation.TargetApi
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.ResId

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
        cancellationSignal = CancellationSignal().also {
            val builder = BiometricPrompt.Builder(context)
            builder.setTitle(context.getString(titleResId))
            builder.setSubtitle(context.getString(descriptionResId))
            builder.setNegativeButton(context.getString(negativeActionTextResId), context.mainExecutor, this)
            val prompt: BiometricPrompt = builder.build()
            prompt.authenticate(it, context.mainExecutor, authenticationCallBack)
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        cancellationSignal?.cancel()
        cancellationSignal = null
        dialog?.dismiss()
        resultCallback?.biometricsCanceledByUser()
    }
}

fun BiometricPromptAbs.showAuthorizationConfirm(context: FragmentActivity) {
    this.showBiometricPrompt(
            context = context,
            titleResId = R.string.actions_confirm,
            descriptionResId = R.string.fingerprint_touch_sensor,
            negativeActionTextResId = R.string.actions_enter_passcode
    )
}
