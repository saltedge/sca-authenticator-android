/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.passcode.setup

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fentury.applock.R
import com.fentury.applock.models.ViewModelEvent
import com.fentury.applock.widget.passcode.PasscodeInputListener
import com.fentury.applock.widget.passcode.PasscodeInputMode
import com.fentury.applock.repository.PreferencesRepositoryAbs
import com.fentury.applock.tools.PasscodeToolsAbs
import com.fentury.applock.tools.ResId
import com.fentury.applock.tools.biometric.BiometricToolsAbs
import com.fentury.applock.tools.postUnitEvent

internal class PasscodeSetupViewModel(
    val appContext: Context,
    val passcodeTools: PasscodeToolsAbs,
    val preferencesRepository: PreferencesRepositoryAbs,
    val biometricTools: BiometricToolsAbs
) : ViewModel(),
    LifecycleObserver,
    PasscodeInputListener {

    val headerTitle: MutableLiveData<ResId> = MutableLiveData(R.string.secure_app_passcode_create)
    val passcodeInputMode = MutableLiveData(PasscodeInputMode.NEW_PASSCODE)
    val showWarningDialogWithMessage = MutableLiveData<ResId>()
    internal val onNewPasscodeSet = MutableLiveData<ViewModelEvent<Unit>>()

    override fun onPasscodeInputCanceledByUser() {
        val inputMode = PasscodeInputMode.NEW_PASSCODE
        passcodeInputMode.postValue(inputMode)
        headerTitle.postValue(getSetupTitleResId(inputMode))
    }

    override fun onInputValidPasscode() {}

    override fun onForgotActionSelected() {}

    override fun onClearApplicationDataSelected() {}

    override fun onInputInvalidPasscode(mode: PasscodeInputMode) {
        headerTitle.postValue(getSetupTitleResId(PasscodeInputMode.NEW_PASSCODE))
    }

    override fun onNewPasscodeEntered(mode: PasscodeInputMode, passcode: String) {
        headerTitle.postValue(getSetupTitleResId(mode))
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
        if (passcodeTools.savePasscode(passcode)) {
            activateFingerprint()
            onNewPasscodeSet.postUnitEvent()
        } else {
            showWarningDialogWithMessage.postValue(R.string.errors_cant_save_passcode)
        }
    }

    private fun getSetupTitleResId(passcodeInputMode: PasscodeInputMode?): Int {
        return if (passcodeInputMode == PasscodeInputMode.CONFIRM_PASSCODE) {
            R.string.passcode_confirm_passcode
        } else R.string.secure_app_passcode_create
    }

    private fun activateFingerprint() {
        try {
            biometricTools.activateFingerprint()
        } catch (e: Exception) {}
    }
}