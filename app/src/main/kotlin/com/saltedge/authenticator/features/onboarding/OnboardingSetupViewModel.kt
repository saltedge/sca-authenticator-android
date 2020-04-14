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
package com.saltedge.authenticator.features.onboarding

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.events.ViewModelEvent
import com.saltedge.authenticator.features.connections.create.ViewMode
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeInputView

class OnboardingSetupViewModel(
    val appContext: Context,
    val passcodeTools: PasscodeToolsAbs,
    val preferenceRepository: PreferenceRepositoryAbs,
    val biometricTools: BiometricToolsAbs
) : ViewModel() {

    var pageIndicator = MutableLiveData<ViewModelEvent<Int>>()
        private set

    var hideSkipViewAndShowProceedView = MutableLiveData<ViewModelEvent<Boolean>>()
        private set

    //passcode
    var hideOnboardingAndShowPasscodeSetupView = MutableLiveData<ViewModelEvent<Boolean>>()
        private set

    var setPasscodeInputMode = MutableLiveData<ViewModelEvent<PasscodeInputView.InputMode>>()
        private set

    var headerTitle = MutableLiveData<ViewModelEvent<Int>>()
        private set

    var headerDescription = MutableLiveData<ViewModelEvent<Int>>()
        private set

    var showPasscodeCancel = MutableLiveData<ViewModelEvent<Boolean>>()
        private set

    var passcodePositiveActionText = MutableLiveData<ViewModelEvent<Int>>()
        private set

    var hidePasscodeInputAndShowSetupView = MutableLiveData<ViewModelEvent<Boolean>>()
        private set

    val onboardingViewModels: List<OnboardingPageViewModel> = listOf(
        OnboardingPageViewModel(
            R.string.onboarding_carousel_one_title,
            R.string.onboarding_carousel_one_description,
            R.drawable.ic_onboarding_page_1
        ),
        OnboardingPageViewModel(
            R.string.onboarding_carousel_two_title,
            R.string.onboarding_carousel_two_description,
            R.drawable.ic_onboarding_page_2
        ),
        OnboardingPageViewModel(
            R.string.onboarding_carousel_three_title,
            R.string.onboarding_carousel_three_description,
            R.drawable.ic_onboarding_page_3
        )
    )

    fun onOnboardingPageSelected(position: Int) {
        if (onboardingViewModels.getOrNull(position) != null) {
            pageIndicator.postValue(ViewModelEvent(position))
            if (shouldShowProceedToSetupAction(position)) {
                hideSkipViewAndShowProceedView.postValue(ViewModelEvent(true))
            }
        }
    }

    private fun shouldShowProceedToSetupAction(position: Int): Boolean =
        position == onboardingViewModels.lastIndex

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.skipActionView, R.id.proceedToSetup -> {
                showPasscodeInput()
            }
        }
    }

    private fun showPasscodeInput() {
        val inputMode = PasscodeInputView.InputMode.NEW_PASSCODE
        hideOnboardingAndShowPasscodeSetupView.postValue(ViewModelEvent(true))
        setPasscodeInputMode.postValue(ViewModelEvent(inputMode))
        updateSetupViews(inputMode)
    }

    private fun updateSetupViews(inputMode: PasscodeInputView.InputMode) {
        headerTitle.postValue(ViewModelEvent(getSetupTitleResId(inputMode)))
        headerDescription.postValue(ViewModelEvent(getSetupSubtitleResId(inputMode)))
        showPasscodeCancel.postValue(
            ViewModelEvent(
                shouldShowPasscodeInputNegativeActionView(
                    inputMode
                )
            )
        )
        passcodePositiveActionText.postValue(
            ViewModelEvent(
                getPositivePasscodeActionViewText(
                    inputMode
                )
            )
        )
    }

    private fun getSetupTitleResId(
        passcodeInputMode: PasscodeInputView.InputMode?
    ): Int {
        return if (passcodeInputMode == PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE) {
            R.string.onboarding_secure_app_passcode_repeat
        } else R.string.onboarding_secure_app_passcode_create
    }

    private fun getSetupSubtitleResId(passcodeInputMode: PasscodeInputView.InputMode): Int? {
        return if (passcodeInputMode == PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE) {
            R.string.onboarding_secure_app_passcode_confirm
        } else R.string.onboarding_secure_app_passcode_description
    }

    private fun shouldShowPasscodeInputNegativeActionView(passcodeInputMode: PasscodeInputView.InputMode?): Boolean? {
        return passcodeInputMode?.let {
            it === PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE
        }
    }

    private fun getPositivePasscodeActionViewText(passcodeInputMode: PasscodeInputView.InputMode?): Int? {
        return passcodeInputMode?.let {
            if (it === PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE) android.R.string.ok
            else R.string.actions_next
        }
    }

    fun enteredNewPasscode(inputMode: PasscodeInputView.InputMode) {
        Log.d("some", "enteredNewPasscode")
        updateSetupViews(inputMode)
    }

    fun newPasscodeConfirmed(passcode: String) {
        Log.d("some", "newPasscodeConfirmed")

        if (passcodeTools.savePasscode(passcode)) {
//            goToNextSetupView()  TODO: check fun

                hidePasscodeInputAndShowSetupView.postValue(ViewModelEvent(true))
        }
        //        else {
        //            viewContract?.showWarningDialogWithMessage(
        //                appContext.getString(R.string.errors_cant_save_passcode)
        //            )
        //        }
    }

    fun passcodeInputCanceledByUser() {
        Log.d("some", "passcodeInputCanceledByUser")
        val inputMode = PasscodeInputView.InputMode.NEW_PASSCODE
        setPasscodeInputMode.postValue(ViewModelEvent(inputMode))
        updateSetupViews(inputMode)
    }
}
