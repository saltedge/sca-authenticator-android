/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.widget.passcode

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
    fun onForgotActionSelected()
    fun onClearApplicationDataSelected()
}