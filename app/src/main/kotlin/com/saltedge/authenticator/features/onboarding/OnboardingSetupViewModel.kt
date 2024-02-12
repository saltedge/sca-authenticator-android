/*
 * Copyright (c) 2020 Salt Edge Inc.
 */
package com.saltedge.authenticator.features.onboarding

import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.postUnitEvent
import com.saltedge.authenticator.widget.passcode.PasscodeInputListener
import com.saltedge.authenticator.widget.passcode.PasscodeInputMode
import com.saltedge.authenticator.widget.security.ActivityUnlockType
import timber.log.Timber

class OnboardingSetupViewModel(
    val appContext: Context,
    val passcodeTools: PasscodeToolsAbs,
    val preferenceRepository: PreferenceRepositoryAbs,
    val biometricTools: BiometricToolsAbs
) : ViewModel(), LifecycleObserver, PasscodeInputListener {
    //onboarding frame
    val onboardingLayoutVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    val proceedViewVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    val skipViewVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    val pageIndicator = MutableLiveData<Int>()
    val moveNext = MutableLiveData<ViewModelEvent<Unit>>()

    //passcode frame
    val setupLayoutVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    val headerTitle: MutableLiveData<ResId> = MutableLiveData(R.string.onboarding_secure_app_passcode_create)
    val passcodeInputViewVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    val passcodeInputMode = MutableLiveData<PasscodeInputMode>()
    val showWarningDialogWithMessage = MutableLiveData<ResId>()
    val showMainActivity = MutableLiveData<ViewModelEvent<Unit>>()

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
                proceedViewVisibility.postValue(View.VISIBLE)
                skipViewVisibility.postValue(View.GONE)
            }
        }
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.skipActionView, R.id.proceedToSetup -> showPasscodeInput()
            R.id.nextActionView -> moveNext.postUnitEvent()
        }
    }

    override fun onPasscodeInputCanceledByUser() {
        val inputMode = PasscodeInputMode.NEW_PASSCODE
        passcodeInputMode.postValue(inputMode)
        headerTitle.postValue(getSetupTitleResId(inputMode))
    }

    override fun onInputValidPasscode() {}

    override fun onInputInvalidPasscode(mode: PasscodeInputMode) {
        headerTitle.postValue(getSetupTitleResId(PasscodeInputMode.NEW_PASSCODE))
    }

    override fun onNewPasscodeEntered(mode: PasscodeInputMode, passcode: String) {
        headerTitle.postValue(getSetupTitleResId(mode))
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
        if (passcodeTools.savePasscode(passcode)) {
            passcodeInputViewVisibility.postValue(View.GONE)
            activateFingerprint()
            AppTools.lastUnlockType = ActivityUnlockType.PASSCODE
            showMainActivity.postUnitEvent()
        } else {
            showWarningDialogWithMessage.postValue(R.string.errors_cant_save_passcode)
        }
    }

    private fun shouldShowProceedToSetupAction(position: Int): Boolean =
        position == onboardingViewModels.lastIndex

    private fun showPasscodeInput() {
        val inputMode = PasscodeInputMode.NEW_PASSCODE
        setupLayoutVisibility.postValue(View.VISIBLE)
        onboardingLayoutVisibility.postValue(View.GONE)
        passcodeInputMode.postValue(inputMode)
        headerTitle.postValue(getSetupTitleResId(inputMode))
    }

    private fun getSetupTitleResId(passcodeInputMode: PasscodeInputMode?): Int {
        return if (passcodeInputMode == PasscodeInputMode.CONFIRM_PASSCODE) {
            R.string.passcode_confirm_passcode
        } else R.string.onboarding_secure_app_passcode_create
    }

    private fun activateFingerprint() {
        try {
            biometricTools.activateFingerprint()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
