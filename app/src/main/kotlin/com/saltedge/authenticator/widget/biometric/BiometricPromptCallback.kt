/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.biometric

interface BiometricPromptCallback {
    fun biometricAuthFinished()
    fun biometricsCanceledByUser()
}
