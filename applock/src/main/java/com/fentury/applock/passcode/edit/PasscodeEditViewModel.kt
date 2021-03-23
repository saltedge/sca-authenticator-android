/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.passcode.edit

import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.fentury.applock.R
import com.fentury.applock.models.ViewModelEvent
import com.fentury.applock.tools.PasscodeToolsAbs
import com.fentury.applock.tools.ResId
import com.fentury.applock.tools.postUnitEvent
import com.fentury.applock.widget.passcode.PasscodeInputListener
import com.fentury.applock.widget.passcode.PasscodeInputMode
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal class PasscodeEditViewModel(
    private val passcodeTools: PasscodeToolsAbs
) : ViewModel(),
    LifecycleObserver,
    PasscodeInputListener,
    CoroutineScope
{
    private val savedPasscode: String
        get() = passcodeTools.getPasscode()
    val titleRes: MutableLiveData<Int> = MutableLiveData(R.string.settings_passcode_input_current)
    val passcodeInputMode = MutableLiveData<PasscodeInputMode>(PasscodeInputMode.CHECK_PASSCODE)
    val initialPasscode = MutableLiveData<String>(savedPasscode)
    val loaderVisibility = MutableLiveData<Int>()
    val descriptionVisibility = MutableLiveData<Int>()
    internal val warningEvent = MutableLiveData<ViewModelEvent<ResId>>()
    internal val infoEvent = MutableLiveData<ViewModelEvent<ResId>>()
    internal val closeViewEvent = MutableLiveData<ViewModelEvent<Unit>>()
    private var isPasscodeConfirmationMode: Boolean = false
    internal val verifySuccessEvent = MutableLiveData<ViewModelEvent<Unit>>()
    internal val verifyFailEvent = MutableLiveData<ViewModelEvent<Unit>>()
    internal val showBiometricPromptEvent = MutableLiveData<ViewModelEvent<Unit>>()
    override var coroutineContext = initCoroutine()

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
        }
    }

    fun setInitialData(@StringRes titleTextRes: Int?, isConfirmationMode: Boolean?) {
        titleRes.value = titleTextRes ?: R.string.settings_passcode_input_current
        isPasscodeConfirmationMode = isConfirmationMode ?: false
        if (isPasscodeConfirmationMode) descriptionVisibility.postValue(View.VISIBLE)
        else descriptionVisibility.postValue(View.GONE)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onLifecycleStart() {
        coroutineContext = initCoroutine()
        updateMode(newMode = PasscodeInputMode.CHECK_PASSCODE)
        initialPasscode.postValue(savedPasscode)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifecycleDestroy() {
        coroutineContext.cancel()
    }

    override fun onInputValidPasscode() {
        if (isPasscodeConfirmationMode) verifySuccessEvent.postUnitEvent()
        else updateMode(newMode = PasscodeInputMode.NEW_PASSCODE)
    }

    override fun onInputInvalidPasscode(mode: PasscodeInputMode) {
        if (isPasscodeConfirmationMode) verifyFailEvent.postUnitEvent()
        else titleRes.postValue(getTitleTextResId(mode))
    }

    override fun onNewPasscodeEntered(mode: PasscodeInputMode, passcode: String) {
        titleRes.postValue(getTitleTextResId(mode))
    }

    override fun onBiometricActionSelected() {
        showBiometricPromptEvent.postUnitEvent()
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
        loaderVisibility.postValue(View.VISIBLE)
        launch {
            if (savePasscode(passcodeTools, passcode)) {
                infoEvent.postValue(ViewModelEvent(R.string.settings_passcode_success))
                closeViewEvent.postUnitEvent()
            } else {
                warningEvent.postValue(ViewModelEvent(R.string.errors_contact_support))
            }
            loaderVisibility.postValue(View.GONE)
        }
    }

    override fun onForgotActionSelected() {}

    override fun onClearApplicationDataSelected() {}

    override fun onPasscodeInputCanceledByUser() {
        closeViewEvent.postUnitEvent()
    }

    private suspend fun savePasscode(passcodeTools: PasscodeToolsAbs, param: String) =
        withContext(Dispatchers.IO) { passcodeTools.savePasscode(passcode = param) }

    private fun initCoroutine(): CoroutineContext = Job() + Dispatchers.Main

    private fun updateMode(newMode: PasscodeInputMode) {
        passcodeInputMode.postValue(newMode)
        if (!isPasscodeConfirmationMode) titleRes.postValue(getTitleTextResId(newMode))
    }

    private fun getTitleTextResId(inputMode: PasscodeInputMode): ResId {
        return when (inputMode) {
            PasscodeInputMode.CHECK_PASSCODE -> R.string.settings_passcode_input_current
            PasscodeInputMode.NEW_PASSCODE -> R.string.settings_input_new_passcode
            PasscodeInputMode.CONFIRM_PASSCODE -> R.string.passcode_confirm_passcode
        }
    }
}
