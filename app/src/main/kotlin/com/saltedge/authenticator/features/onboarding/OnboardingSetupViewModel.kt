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
import android.view.View
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
    val proceedViewVisibility: MutableLiveData<Int> = MutableLiveData()
    val skipViewVisibility: MutableLiveData<Int> = MutableLiveData()
    val setupLayoutVisibility: MutableLiveData<Int> = MutableLiveData()
    val onboardingLayoutVisibility: MutableLiveData<Int> = MutableLiveData()
    val passcodeInputViewVisibility: MutableLiveData<Int> = MutableLiveData()

    init {
        headerTitle.value = R.string.onboarding_secure_app_passcode_create
        setupLayoutVisibility.value = View.GONE
        onboardingLayoutVisibility.value = View.VISIBLE
        proceedViewVisibility.value = View.GONE
        skipViewVisibility.value = View.VISIBLE
        passcodeInputViewVisibility.value = View.VISIBLE
    }

    var pageIndicator = MutableLiveData<Int>()
        private set

    var setPasscodeInputMode = MutableLiveData<PasscodeInputView.InputMode>()
        private set

    var showWarningDialogWithMessage = MutableLiveData<String>()
        private set

    var showMainActivity = MutableLiveData<ViewModelEvent<Unit>>()
        private set

    var moveNext = MutableLiveData<ViewModelEvent<Unit>>()
        private set

    val onboardingViewModels: List<OnboardingPageViewModel> = listOf(
        OnboardingPageViewModel(
            titleResId = R.string.onboarding_carousel_one_title,
            subTitleResId = R.string.onboarding_carousel_one_description,
            imageResId = R.drawable.ic_onboarding_page_1
        ),
        OnboardingPageViewModel(
            titleResId = R.string.onboarding_carousel_two_title,
            subTitleResId = R.string.onboarding_carousel_two_description,
            imageResId = R.drawable.ic_onboarding_page_2
        ),
        OnboardingPageViewModel(
            titleResId = R.string.onboarding_carousel_three_title,
            subTitleResId = R.string.onboarding_carousel_three_description,
            imageResId = R.drawable.ic_onboarding_page_3
        )
    )

    fun onOnboardingPageSelected(position: Int) {
        if (onboardingViewModels.getOrNull(position) != null) {
            pageIndicator.postValue(position)
            if (shouldShowProceedToSetupAction(position)) {
                proceedViewVisibility.value = View.VISIBLE
                skipViewVisibility.value = View.GONE
            }
        }
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.skipActionView, R.id.proceedToSetup -> {
                showPasscodeInput()
            }
            R.id.nextActionView -> moveNext.postValue(ViewModelEvent(Unit))
        }
    }

    fun reEnterPasscode() {
        updateSetupViews(inputMode = PasscodeInputView.InputMode.NEW_PASSCODE)
    }

    fun enteredNewPasscode(inputMode: PasscodeInputView.InputMode) {
        updateSetupViews(inputMode)
    }

    fun newPasscodeConfirmed(passcode: String) {
        if (passcodeTools.savePasscode(passcode)) {
            passcodeInputViewVisibility.value = View.GONE
            showMainActivity.postValue(ViewModelEvent(Unit))
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
        setupLayoutVisibility.value = View.VISIBLE
        onboardingLayoutVisibility.value = View.GONE
        setPasscodeInputMode.postValue(inputMode)
        updateSetupViews(inputMode)
    }

    private fun updateSetupViews(inputMode: PasscodeInputView.InputMode) {
        headerTitle.value = getSetupTitleResId(inputMode)
    }

    private fun getSetupTitleResId(
        passcodeInputMode: PasscodeInputView.InputMode?
    ): Int {
        return if (passcodeInputMode == PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE) {
            R.string.onboarding_secure_app_passcode_repeat
        } else R.string.onboarding_secure_app_passcode_create
    }
}
