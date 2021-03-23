/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.widget.biometric

interface BiometricPromptCallback {
    fun biometricAuthFinished()
    fun biometricsCanceledByUser()
}