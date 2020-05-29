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
import com.saltedge.authenticator.widget.passcode.PasscodeEditView
import com.saltedge.authenticator.widget.passcode.PasscodeInputViewListener

class PasscodeEditViewModel(
    private val passcodeTools: PasscodeToolsAbs
) : ViewModel(),
    LifecycleObserver,
    PasscodeInputViewListener,
    PasscodeSaveResultListener
{
    private val savedPasscode: String
        get() = passcodeTools.getPasscode()
    val titleRes = MutableLiveData<ResId>(R.string.settings_passcode_input_current)
//    val positiveActionTextRes = MutableLiveData<ResId>(R.string.actions_next)//TODO check usage
    val passcodeInputMode = MutableLiveData<PasscodeEditView.InputMode>(PasscodeEditView.InputMode.CHECK_PASSCODE)
    val currentPasscode = MutableLiveData<String>("")
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
        updateModeAndPasscode(newMode = PasscodeEditView.InputMode.CHECK_PASSCODE, passcode = savedPasscode)
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

    override fun onEnteredPasscodeIsValid() {
        updateModeAndPasscode(newMode = PasscodeEditView.InputMode.NEW_PASSCODE, passcode = "")
    }

    override fun onEnteredPasscodeIsInvalid() {}

    override fun onNewPasscodeEntered(mode: PasscodeEditView.InputMode, passcode: String) {
        titleRes.postValue(getTitleTextResId(mode))
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
        loaderVisibility.postValue(View.VISIBLE)
        PasscodeSaver(passcodeTools, callback = this).runNewTask(passcode)
    }

    private fun updateModeAndPasscode(newMode: PasscodeEditView.InputMode, passcode: String) {
        passcodeInputMode.postValue(newMode)
        currentPasscode.postValue(passcode)
        titleRes.postValue(getTitleTextResId(newMode))
    }

    private fun getTitleTextResId(inputMode: PasscodeEditView.InputMode): ResId {
        return when (inputMode) {
            PasscodeEditView.InputMode.CHECK_PASSCODE -> R.string.settings_passcode_input_current
            PasscodeEditView.InputMode.NEW_PASSCODE -> R.string.settings_passcode_input_new
            PasscodeEditView.InputMode.REPEAT_NEW_PASSCODE -> R.string.settings_passcode_repeat_new
        }
    }

//    private fun getPositiveTextResId(inputMode: PasscodeEditView.InputMode): Int {
//        return when (inputMode) {
//            PasscodeEditView.InputMode.CHECK_PASSCODE, PasscodeEditView.InputMode.NEW_PASSCODE -> {
//                R.string.actions_next
//            }
//            PasscodeEditView.InputMode.REPEAT_NEW_PASSCODE -> android.R.string.ok
//        }
//    }
}
