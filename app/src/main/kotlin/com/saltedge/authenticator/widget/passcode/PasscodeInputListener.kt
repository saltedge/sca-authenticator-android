/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.widget.passcode

/**
 * Used for working with passcode
 *
 * @see PasscodeInputView
 */
interface PasscodeInputListener {
    fun onBiometricActionSelected() {}
    fun onPasscodeInputCanceledByUser()
    fun onInputValidPasscode()
    fun onInputInvalidPasscode(mode: PasscodeInputMode)
    fun onNewPasscodeEntered(mode: PasscodeInputMode, passcode: String)
    fun onNewPasscodeConfirmed(passcode: String)
    fun onForgotActionSelected() {}
    fun onClearDataActionSelected() {}
}
