/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.widget.biometric

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.fentury.applock.tools.ResId

interface BiometricPromptAbs {

    var resultCallback: BiometricPromptCallback?
    fun showBiometricPrompt(
        context: FragmentActivity,
        title: String,
        @StringRes descriptionResId: ResId,
        @StringRes negativeActionTextResId: ResId
    )
    fun dismissBiometricPrompt()
}