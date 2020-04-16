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
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.events.ViewModelEvent
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeInputView

class OnboardingSetupViewModel(
    val appContext: Context,
    val passcodeTools: PasscodeToolsAbs,
    val preferenceRepository: PreferenceRepositoryAbs,
    val biometricTools: BiometricToolsAbs
) : ViewModel(), LifecycleObserver {

    val headerTitle: MutableLiveData<Int> = MutableLiveData()
    val headerDescription: MutableLiveData<Int> = MutableLiveData()
    val hideSkipViewAndShowProceedView: MutableLiveData<Boolean> = MutableLiveData()
    val hideOnboardingAndShowPasscodeSetupView: MutableLiveData<Boolean> = MutableLiveData()
    val hidePasscodeInputAndShowSetupView: MutableLiveData<Boolean> = MutableLiveData()

    init {
        headerTitle.value = R.string.onboarding_secure_app_passcode_create
        headerDescription.value = R.string.onboarding_secure_app_passcode_description
        hideSkipViewAndShowProceedView.value = false
        hideOnboardingAndShowPasscodeSetupView.value = false
        hidePasscodeInputAndShowSetupView.value = false
    }

    var pageIndicator = MutableLiveData<Int>()
        private set

    var setPasscodeInputMode = MutableLiveData<PasscodeInputView.InputMode>()
        private set

    var showPasscodeCancel = MutableLiveData<Boolean>()
        private set

    var passcodePositiveActionText = MutableLiveData<Int>()
        private set

    var showWarningDialogWithMessage = MutableLiveData<String>()
        private set

    var showMainActivity = MutableLiveData<ViewModelEvent<Unit>>()
        private set

    var moveNext = MutableLiveData<ViewModelEvent<Unit>>()
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
            pageIndicator.postValue(position)
            if (shouldShowProceedToSetupAction(position)) {
                hideSkipViewAndShowProceedView.value = true
            }
        }
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.skipActionView, R.id.proceedToSetup -> {
                showPasscodeInput()
            }
            R.id.nextActionView -> moveNext.postValue(ViewModelEvent())
        }
    }

    fun enteredNewPasscode(inputMode: PasscodeInputView.InputMode) {
        updateSetupViews(inputMode)
    }

    fun newPasscodeConfirmed(passcode: String) {
        if (passcodeTools.savePasscode(passcode)) {
            hidePasscodeInputAndShowSetupView.value = true
            showMainActivity.postValue(ViewModelEvent())
        } else {
            showWarningDialogWithMessage.postValue(appContext.getString(R.string.errors_cant_save_passcode))
        }
    }

    fun passcodeInputCanceledByUser() {
        val inputMode = PasscodeInputView.InputMode.NEW_PASSCODE
        setPasscodeInputMode.postValue(inputMode)
        updateSetupViews(inputMode)
    }

    private fun shouldShowProceedToSetupAction(position: Int): Boolean =
        position == onboardingViewModels.lastIndex

    private fun showPasscodeInput() {
        val inputMode = PasscodeInputView.InputMode.NEW_PASSCODE
        hideOnboardingAndShowPasscodeSetupView.value = true
        setPasscodeInputMode.postValue(inputMode)
        updateSetupViews(inputMode)
    }

    private fun updateSetupViews(inputMode: PasscodeInputView.InputMode) {
        headerTitle.value = getSetupTitleResId(inputMode)
        headerDescription.value = getSetupSubtitleResId(inputMode)
        showPasscodeCancel.value = shouldShowPasscodeInputNegativeActionView(inputMode)
        passcodePositiveActionText.postValue(getPositivePasscodeActionViewText(inputMode))
    }

    private fun getSetupTitleResId(
        passcodeInputMode: PasscodeInputView.InputMode?
    ): Int {
        return if (passcodeInputMode == PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE) {
            R.string.onboarding_secure_app_passcode_repeat
        } else R.string.onboarding_secure_app_passcode_create
    }

    private fun getSetupSubtitleResId(passcodeInputMode: PasscodeInputView.InputMode): Int {
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
}
