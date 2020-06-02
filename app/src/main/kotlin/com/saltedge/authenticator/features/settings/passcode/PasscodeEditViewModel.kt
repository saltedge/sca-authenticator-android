/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.authenticator.features.settings.passcode

import android.view.View
import androidx.lifecycle.*
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.settings.passcode.saver.PasscodeSaveResultListener
import com.saltedge.authenticator.features.settings.passcode.saver.PasscodeSaver
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.widget.passcode.PasscodeInputListener
import com.saltedge.authenticator.widget.passcode.PasscodeInputMode

class PasscodeEditViewModel(
    private val passcodeTools: PasscodeToolsAbs
) : ViewModel(),
    LifecycleObserver,
    PasscodeInputListener,
    PasscodeSaveResultListener
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

    fun bindLifecycleObserver(lifecycle: Lifecycle) {
        lifecycle.let {
            it.removeObserver(this)
            it.addObserver(this)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onLifecycleStart() {
        updateMode(newMode = PasscodeInputMode.CHECK_PASSCODE)
        initialPasscode.postValue(savedPasscode)
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
        PasscodeSaver(passcodeTools, callback = this).runNewTask(passcode)
    }

    override fun passcodeSavedWithResult(result: Boolean) {
        loaderVisibility.postValue(View.GONE)
        if (result) {
            infoEvent.postValue(ViewModelEvent(R.string.settings_passcode_success))
            closeViewEvent.postValue(ViewModelEvent(Unit))
        } else {
            warningEvent.postValue(ViewModelEvent(R.string.errors_contact_support))
        }
    }

    override fun onPasscodeInputCanceledByUser() {
        closeViewEvent.postValue(ViewModelEvent(Unit))
    }

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
