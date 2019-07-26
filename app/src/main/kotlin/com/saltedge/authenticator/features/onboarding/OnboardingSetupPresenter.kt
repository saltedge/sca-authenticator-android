/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2019 Salt Edge Inc.
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
import android.os.Handler
import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeInputView

const val COMPLETE_SCREEN_DURATION = 3000L

/**
 * Presenter of OnboardingSetupFragment
 *
 * @see OnboardingSetupActivity
 */
class OnboardingSetupPresenter(
        private val appContext: Context,
        private val passcodeTools: PasscodeToolsAbs,
        private val preferenceRepository: PreferenceRepositoryAbs,
        private val biometricTools: BiometricToolsAbs
) {
    var viewContract: OnboardingSetupContract.View? = null
    var setupViewMode: SetupViewMode = SetupViewMode.INPUT_PASSCODE
    val setupStepCount: Int
        get() = setupModesList.count()
    private var handler: Handler? = null

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
    private val setupStepProgress: Float
        get() = setupViewMode.ordinal.toFloat()
    private var setupModesList: Array<SetupViewMode> = if (biometricTools.isFingerprintSupported(appContext)) {
        arrayOf(SetupViewMode.INPUT_PASSCODE,
                SetupViewMode.ALLOW_BIOMETRICS,
                SetupViewMode.ALLOW_NOTIFICATIONS,
                SetupViewMode.COMPLETE)
    } else {
        arrayOf(SetupViewMode.INPUT_PASSCODE,
                SetupViewMode.ALLOW_NOTIFICATIONS,
                SetupViewMode.COMPLETE)
    }

    fun enteredNewPasscode(inputMode: PasscodeInputView.InputMode) {
        updateSetupViews(inputMode)
    }

    fun passcodeInputCanceledByUser() {
        val inputMode = PasscodeInputView.InputMode.NEW_PASSCODE
        viewContract?.setPasscodeInputMode(inputMode = inputMode)
        updateSetupViews(inputMode)
    }

    fun onOnboardingPageSelected(position: Int) {
        if (onboardingViewModels.getOrNull(position) != null) {
            viewContract?.updatePageIndicator(position)
            if (shouldShowProceedToSetupAction(position)) {
                viewContract?.hideSkipViewAndShowProceedView()
            }
        }
    }

    fun onViewClick(viewId: Int) {
        when (viewId) {
            R.id.skipActionView, R.id.proceedToSetup -> {
                showPasscodeInput()
            }
            R.id.allowTouchIdActionView -> onAllowTouchIdClick()
            R.id.skipTouchIdActionView -> {
                preferenceRepository.fingerprintEnabled = false
                goToNextSetupView()
            }
            R.id.allowNotificationsActionView -> {
                preferenceRepository.notificationsEnabled = true
                goToNextSetupView()
                startNextActivityWithDelay()
            }
            R.id.skipNotificationsActionView -> {
                preferenceRepository.notificationsEnabled = false
                goToNextSetupView()
                startNextActivityWithDelay()
            }
            R.id.proceedToMainActivity -> {
                viewContract?.showMainActivity()
                stopDelayHandler()
            }
        }
    }

    fun newPasscodeConfirmed(passcode: String) {
        if (passcodeTools.savePasscode(passcode)) {
            goToNextSetupView()
        } else viewContract?.showWarningDialogWithMessage(appContext.getString(R.string.errors_internal_error))
    }

    fun stopDelayHandler() {
        handler?.removeCallbacksAndMessages(null)
    }

    private fun startNextActivityWithDelay() {
        handler = Handler()
        handler?.postDelayed({ viewContract?.showMainActivity() }, COMPLETE_SCREEN_DURATION)
    }

    private fun showPasscodeInput() {
        val inputMode = PasscodeInputView.InputMode.NEW_PASSCODE
        viewContract?.hideOnboardingViewAndShowSetupView()
        viewContract?.setPasscodeInputMode(inputMode)
        updateSetupViews(inputMode)
    }

    private fun updateSetupViews(inputMode: PasscodeInputView.InputMode?) {
        viewContract?.updateSetupViews(
                setupStepProgress = setupStepProgress,
                headerTitle = getSetupTitleResId(setupViewMode, inputMode),
                headerDescription = getSetupSubtitleResId(setupViewMode, inputMode),
                showPasscodeCancel = shouldShowPasscodeInputNegativeActionView(inputMode),
                passcodePositiveActionText = getPositivePasscodeActionViewText(inputMode)
        )
    }

    private fun getSetupTitleResId(
            mode: SetupViewMode,
            passcodeInputMode: PasscodeInputView.InputMode?
    ): Int {
        return when (mode) {
            SetupViewMode.INPUT_PASSCODE -> {
                if (passcodeInputMode == PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE) R.string.onboarding_secure_app_passcode_repeat
                else R.string.onboarding_secure_app_passcode_create
            }
            SetupViewMode.ALLOW_BIOMETRICS -> R.string.onboarding_secure_app_touch_id_allow_android
            SetupViewMode.ALLOW_NOTIFICATIONS -> R.string.onboarding_allow_notifications_title
            SetupViewMode.COMPLETE -> R.string.onboarding_well_done_title
        }
    }

    private fun getSetupSubtitleResId(mode: SetupViewMode, passcodeInputMode: PasscodeInputView.InputMode?): Int {
        return when (mode) {
            SetupViewMode.INPUT_PASSCODE -> {
                if (passcodeInputMode == PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE) R.string.onboarding_secure_app_passcode_confirm
                else R.string.onboarding_secure_app_passcode_description
            }
            SetupViewMode.ALLOW_BIOMETRICS -> R.string.onboarding_secure_app_touch_id_description_android
            SetupViewMode.ALLOW_NOTIFICATIONS -> R.string.onboarding_allow_notifications_description
            SetupViewMode.COMPLETE -> R.string.onboarding_completed_description
        }
    }

    private fun shouldShowPasscodeInputNegativeActionView(passcodeInputMode: PasscodeInputView.InputMode?): Boolean? {
        return passcodeInputMode?.let {
            it === PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE
        }
    }

    private fun getPositivePasscodeActionViewText(passcodeInputMode: PasscodeInputView.InputMode?): Int? {
        return passcodeInputMode?.let {
            if (it === PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE) R.string.actions_ok
            else R.string.actions_next
        }
    }

    private fun shouldShowProceedToSetupAction(position: Int): Boolean =
            position == onboardingViewModels.lastIndex

    private fun onAllowTouchIdClick() {
        val warning = biometricTools.getCurrentFingerprintStateWarningMessage(appContext)
        when {
            warning != null -> viewContract?.showWarningDialogWithMessage(warning)
            biometricTools.activateFingerprint() -> goToNextSetupView()
            else -> {
                viewContract?.showWarningDialogWithMessage(appContext.getString(R.string.errors_activate_touch_id))
            }
        }
    }

    private fun goToNextSetupView() {
        val index = setupModesList.indexOf(setupViewMode)
        if (index > -1) {
            setupViewMode = setupModesList.getOrNull(index + 1) ?: setupViewMode
        }
        updateSetupViews(inputMode = null)
    }
}
