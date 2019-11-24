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
import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricState
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.tool.log
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeInputView

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
    private var setupModesList: Array<SetupViewMode> =
        if (biometricTools.isBiometricSupported(appContext)) {
            arrayOf(
                SetupViewMode.INPUT_PASSCODE,
                SetupViewMode.ALLOW_BIOMETRICS,
                SetupViewMode.ALLOW_NOTIFICATIONS,
                SetupViewMode.COMPLETE
            )
        } else {
            arrayOf(
                SetupViewMode.INPUT_PASSCODE,
                SetupViewMode.ALLOW_NOTIFICATIONS,
                SetupViewMode.COMPLETE
            )
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
            R.id.actionView -> {
                when (setupViewMode) {
                    SetupViewMode.ALLOW_BIOMETRICS -> onAllowTouchIdClick()
                    SetupViewMode.ALLOW_NOTIFICATIONS -> {
                        preferenceRepository.notificationsEnabled = true
                        goToNextSetupView()
                    }
                    SetupViewMode.COMPLETE -> viewContract?.showMainActivity()
                }
            }
            R.id.skipSetupActionView -> {
                when (setupViewMode) {
                    SetupViewMode.ALLOW_BIOMETRICS -> {
                        preferenceRepository.fingerprintEnabled = false
                        goToNextSetupView()
                    }
                    SetupViewMode.ALLOW_NOTIFICATIONS -> {
                        preferenceRepository.notificationsEnabled = false
                        goToNextSetupView()
                    }
                }
            }
            R.id.skipActionView, R.id.proceedToSetup -> {
                showPasscodeInput()
            }
        }
    }

    fun newPasscodeConfirmed(passcode: String) {
        if (passcodeTools.savePasscode(passcode)) {
            goToNextSetupView()
        } else {
            viewContract?.showWarningDialogWithMessage(
                appContext.getString(R.string.errors_cant_save_passcode)
            )
        }
    }

    private fun showPasscodeInput() {
        val inputMode = PasscodeInputView.InputMode.NEW_PASSCODE
        viewContract?.hideOnboardingAndShowPasscodeSetupView()
        viewContract?.setPasscodeInputMode(inputMode)
        updateSetupViews(inputMode)
    }

    private fun updateSetupViews(inputMode: PasscodeInputView.InputMode?) {
        viewContract?.updateSetupViews(
            setupStepProgress = setupStepProgress,
            headerTitle = getSetupTitleResId(setupViewMode, inputMode),
            headerDescription = getSetupSubtitleResId(setupViewMode, inputMode),
            showPasscodeCancel = shouldShowPasscodeInputNegativeActionView(inputMode),
            passcodePositiveActionText = getPositivePasscodeActionViewText(inputMode),
            setupImageResId = getSetupImageResId(setupViewMode),
            actionText = getActionTextResId(setupViewMode)
        )
        if (setupViewMode == SetupViewMode.COMPLETE) viewContract?.hideSkipView()
    }

    private fun getActionTextResId(mode: SetupViewMode): Int {
        return when (mode) {
            SetupViewMode.ALLOW_BIOMETRICS -> R.string.onboarding_secure_app_touch_id_allow_android
            SetupViewMode.ALLOW_NOTIFICATIONS -> R.string.onboarding_allow_notifications_title
            SetupViewMode.COMPLETE -> R.string.actions_proceed
            else -> R.string.actions_proceed
        }
    }

    private fun getSetupImageResId(mode: SetupViewMode): Int {
        return when (mode) {
            SetupViewMode.ALLOW_BIOMETRICS -> R.drawable.ic_setup_fingerprint
            SetupViewMode.ALLOW_NOTIFICATIONS -> R.drawable.ic_setup_notifications
            SetupViewMode.COMPLETE -> R.drawable.ic_complete_ok_70
            else -> 0
        }
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

    private fun getSetupSubtitleResId(
        mode: SetupViewMode,
        passcodeInputMode: PasscodeInputView.InputMode?
    ): Int {
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
        val warningMessage = getCurrentFingerprintStateWarningMessage(appContext)
        when {
            warningMessage != null -> viewContract?.showWarningDialogWithMessage(warningMessage)
            biometricTools.activateFingerprint() -> {
                preferenceRepository.fingerprintEnabled = true
                goToNextSetupView()
            }
            else -> {
                viewContract?.showWarningDialogWithMessage(appContext.getString(R.string.errors_activate_touch_id))
            }
        }
    }

    private fun getCurrentFingerprintStateWarningMessage(context: Context): String? {
        return try {
            context.getString(
                when (biometricTools.getFingerprintState(context)) {
                    BiometricState.NOT_SUPPORTED -> R.string.errors_touch_id_not_supported
                    BiometricState.NOT_BLOCKED_DEVICE -> R.string.errors_activate_touch_id
                    BiometricState.NO_FINGERPRINTS -> R.string.errors_touch_id_not_enrolled
                    else -> return null
                }
            )
        } catch (e: Exception) {
            e.log()
            null
        }
    }

    private fun goToNextSetupView() {
        val index = setupModesList.indexOf(setupViewMode)
        if (index > -1) {
            setupViewMode = setupModesList.getOrNull(index + 1) ?: setupViewMode
        }
        updateSetupViews(inputMode = null)
        viewContract?.hidePasscodeInputAndShowSetupView()
    }
}
