/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.settings.passcode

import android.view.View
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent
import com.saltedge.authenticator.widget.passcode.PasscodeInputListener
import com.saltedge.authenticator.widget.passcode.PasscodeInputMode
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class PasscodeEditViewModel(
    private val passcodeTools: PasscodeToolsAbs,
    private val defaultDispatcher: CoroutineDispatcher
) : ViewModel(),
    LifecycleObserver,
    PasscodeInputListener,
    CoroutineScope
{
    private val savedPasscode: String
        get() = passcodeTools.getPasscode()
    val titleRes = MutableLiveData<ResId>(R.string.settings_passcode_input_current)
    val passcodeInputMode = MutableLiveData<PasscodeInputMode>(PasscodeInputMode.CHECK_PASSCODE)
    val initialPasscode = MutableLiveData<String>(savedPasscode)
    val loaderVisibility = MutableLiveData<Int>()
    val warningEvent = MutableLiveData<ViewModelEvent<ResId>>()
    val infoEvent = MutableLiveData<ViewModelEvent<ResId>>()
    val closeViewEvent = MutableLiveData<ViewModelEvent<Unit>>()
    override var coroutineContext = initCoroutine()

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
        }
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
        updateMode(newMode = PasscodeInputMode.NEW_PASSCODE)
    }

    override fun onInputInvalidPasscode(mode: PasscodeInputMode) {
        titleRes.postValue(getTitleTextResId(mode))
    }

    override fun onNewPasscodeEntered(mode: PasscodeInputMode, passcode: String) {
        titleRes.postValue(getTitleTextResId(mode))
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
        loaderVisibility.postValue(View.VISIBLE)
        viewModelScope.launch(defaultDispatcher) {
            if (savePasscode(passcodeTools, passcode)) {
                infoEvent.postValue(ViewModelEvent(R.string.settings_passcode_success))
                closeViewEvent.postUnitEvent()
            } else {
                warningEvent.postValue(ViewModelEvent(R.string.errors_contact_support))
            }
            loaderVisibility.postValue(View.GONE)
        }
    }

    override fun onPasscodeInputCanceledByUser() {
        closeViewEvent.postUnitEvent()
    }

    private fun initCoroutine(): CoroutineContext = Job() + Dispatchers.Main

    private suspend fun savePasscode(passcodeTools: PasscodeToolsAbs, param: String) =
        withContext(Dispatchers.Main) { passcodeTools.savePasscode(passcode = param) }

    private fun updateMode(newMode: PasscodeInputMode) {
        passcodeInputMode.postValue(newMode)
        titleRes.postValue(getTitleTextResId(newMode))
    }

    private fun getTitleTextResId(inputMode: PasscodeInputMode): ResId {
        return when (inputMode) {
            PasscodeInputMode.CHECK_PASSCODE -> R.string.settings_passcode_input_current
            PasscodeInputMode.NEW_PASSCODE -> R.string.settings_input_new_passcode
            PasscodeInputMode.CONFIRM_PASSCODE -> R.string.passcode_confirm_passcode
        }
    }
}
