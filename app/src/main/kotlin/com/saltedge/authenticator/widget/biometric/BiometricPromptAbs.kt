/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.biometric

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.saltedge.authenticator.tools.ResId

interface BiometricPromptAbs {

    var resultCallback: BiometricPromptCallback?
    fun showBiometricPrompt(
        context: FragmentActivity,
        @StringRes titleResId: ResId,
        @StringRes descriptionResId: ResId,
        @StringRes negativeActionTextResId: ResId
    )
    fun dismissBiometricPrompt()
}
