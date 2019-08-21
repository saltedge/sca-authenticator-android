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
package com.saltedge.authenticator.unitTests.features.onboarding

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.onboarding.OnboardingPageViewModel
import com.saltedge.authenticator.features.onboarding.OnboardingSetupContract
import com.saltedge.authenticator.features.onboarding.OnboardingSetupPresenter
import com.saltedge.authenticator.features.onboarding.SetupViewMode
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.testTools.TestTools
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeInputView
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class OnboardingSetupPresenterTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        Assert.assertNull(createPresenter(viewContract = null).viewContract)

        val presenter = createPresenter(viewContract = mockView)

        Assert.assertNotNull(presenter.viewContract)
        MatcherAssert.assertThat(presenter.setupViewMode, equalTo(SetupViewMode.INPUT_PASSCODE))

        Mockito.doReturn(true).`when`(mockBiometricTools).isFingerprintSupported(TestTools.applicationContext)

        MatcherAssert.assertThat(
            createPresenter(viewContract = mockView).setupStepCount,
            equalTo(4)
        )

        Mockito.doReturn(false).`when`(mockBiometricTools).isFingerprintSupported(TestTools.applicationContext)

        MatcherAssert.assertThat(
            createPresenter(viewContract = mockView).setupStepCount,
            equalTo(3)
        )
    }

    @Test
    @Throws(Exception::class)
    fun onboardingViewModelsTest() {
        assertThat(
            createPresenter(viewContract = mockView).onboardingViewModels,
            equalTo(
                listOf(
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
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun onCreateTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onViewClick(R.id.skipActionView)

        Mockito.verify(mockView).hideOnboardingViewAndShowSetupView()
        Mockito.verify(mockView).setPasscodeInputMode(PasscodeInputView.InputMode.NEW_PASSCODE)
        Mockito.verify(mockView).updateSetupViews(
            setupStepProgress = 0f,
            headerTitle = R.string.onboarding_secure_app_passcode_create,
            headerDescription = R.string.onboarding_secure_app_passcode_description,
            showPasscodeCancel = false,
            passcodePositiveActionText = R.string.actions_next
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onCreateTest_noViewContract() {
        val presenter = createPresenter(viewContract = null)
        presenter.onViewClick(viewId = R.id.skipActionView) // setting onboardingPassed == true

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun enteredNewPasscodeTest() {
        createPresenter(viewContract = mockView).enteredNewPasscode(PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE)

        Mockito.verify(mockView).updateSetupViews(
            setupStepProgress = 0f,
            headerTitle = R.string.onboarding_secure_app_passcode_repeat,
            headerDescription = R.string.onboarding_secure_app_passcode_confirm,
            showPasscodeCancel = true,
            passcodePositiveActionText = R.string.actions_ok
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun enteredNewPasscodeTest_noViewContract() {
        createPresenter(viewContract = null).enteredNewPasscode(PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun passcodeInputCanceledByUserTest() {
        createPresenter(viewContract = mockView).passcodeInputCanceledByUser()

        Mockito.verify(mockView).setPasscodeInputMode(PasscodeInputView.InputMode.NEW_PASSCODE)
        Mockito.verify(mockView).updateSetupViews(
            setupStepProgress = 0f,
            headerTitle = R.string.onboarding_secure_app_passcode_create,
            headerDescription = R.string.onboarding_secure_app_passcode_description,
            showPasscodeCancel = false,
            passcodePositiveActionText = R.string.actions_next
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun passcodeInputCanceledByUserTest_noViewContract() {
        createPresenter(viewContract = null).passcodeInputCanceledByUser()

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onOnboardingPageSelectedTest() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.onOnboardingPageSelected(position = -1)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.onOnboardingPageSelected(position = presenter.onboardingViewModels.lastIndex + 1)

        Mockito.verifyNoMoreInteractions(mockView)

        presenter.onOnboardingPageSelected(position = 0)

        Mockito.verify(mockView).updatePageIndicator(position = 0)
        Mockito.verifyNoMoreInteractions(mockView)

        Mockito.clearInvocations(mockView)
        presenter.onOnboardingPageSelected(position = 1)

        Mockito.verify(mockView).updatePageIndicator(position = 1)
        Mockito.verifyNoMoreInteractions(mockView)

        Mockito.clearInvocations(mockView)
        presenter.onOnboardingPageSelected(position = presenter.onboardingViewModels.lastIndex)

        Mockito.verify(mockView).updatePageIndicator(position = presenter.onboardingViewModels.lastIndex)
        Mockito.verify(mockView).hideSkipViewAndShowProceedView()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_invalidParams() {
        createPresenter(viewContract = mockView).onViewClick(viewId = -1)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_skipActionView() {
        createPresenter(viewContract = mockView).onViewClick(viewId = R.id.skipActionView)

        Mockito.verify(mockView).hideOnboardingViewAndShowSetupView()
        Mockito.verify(mockView).setPasscodeInputMode(PasscodeInputView.InputMode.NEW_PASSCODE)
        Mockito.verify(mockView).updateSetupViews(
            setupStepProgress = 0f,
            headerTitle = R.string.onboarding_secure_app_passcode_create,
            headerDescription = R.string.onboarding_secure_app_passcode_description,
            showPasscodeCancel = false,
            passcodePositiveActionText = R.string.actions_next
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_skipActionView_noViewContract() {
        createPresenter(viewContract = null).onViewClick(viewId = R.id.skipActionView)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_proceedToSetup() {
        createPresenter(viewContract = mockView).onViewClick(viewId = R.id.proceedToSetup)

        Mockito.verify(mockView).hideOnboardingViewAndShowSetupView()
        Mockito.verify(mockView).setPasscodeInputMode(PasscodeInputView.InputMode.NEW_PASSCODE)
        Mockito.verify(mockView).updateSetupViews(
            setupStepProgress = 0f,
            headerTitle = R.string.onboarding_secure_app_passcode_create,
            headerDescription = R.string.onboarding_secure_app_passcode_description,
            showPasscodeCancel = false,
            passcodePositiveActionText = R.string.actions_next
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_proceedToSetup_noViewContract() {
        createPresenter(viewContract = null).onViewClick(viewId = R.id.proceedToSetup)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_allowTouchIdActionView() {
        val context = TestTools.applicationContext
        val presenter = createPresenter(viewContract = mockView)
        presenter.setupViewMode = SetupViewMode.ALLOW_BIOMETRICS
        Mockito.doReturn(context.getString(R.string.errors_internal_error)).`when`(
            mockBiometricTools
        )
            .getCurrentFingerprintStateWarningMessage(context)
        presenter.onViewClick(R.id.allowTouchIdActionView)

        Mockito.verify(mockView).showWarningDialogWithMessage(context.getString(R.string.errors_internal_error))
        MatcherAssert.assertThat(presenter.setupViewMode, equalTo(SetupViewMode.ALLOW_BIOMETRICS))

        Mockito.doReturn(null).`when`(mockBiometricTools)
            .getCurrentFingerprintStateWarningMessage(TestTools.applicationContext)
        Mockito.doReturn(true).`when`(mockBiometricTools).activateFingerprint()
        Mockito.clearInvocations(mockView)
        presenter.onViewClick(R.id.allowTouchIdActionView)

        MatcherAssert.assertThat(
            presenter.setupViewMode,
            equalTo(SetupViewMode.ALLOW_NOTIFICATIONS)
        )

        Mockito.doReturn(false).`when`(mockBiometricTools).activateFingerprint()
        Mockito.clearInvocations(mockView)
        presenter.onViewClick(R.id.allowTouchIdActionView)

        Mockito.verify(mockView).showWarningDialogWithMessage(context.getString(R.string.errors_activate_touch_id))
        MatcherAssert.assertThat(
            presenter.setupViewMode,
            equalTo(SetupViewMode.ALLOW_NOTIFICATIONS)
        )
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_allowTouchIdActionView_noViewContract() {
        val context = TestTools.applicationContext
        val presenter = createPresenter(viewContract = null)
        Mockito.doReturn(context.getString(R.string.errors_internal_error)).`when`(
            mockBiometricTools
        )
            .getCurrentFingerprintStateWarningMessage(context)
        presenter.onViewClick(R.id.allowTouchIdActionView)

        Mockito.verifyNoMoreInteractions(mockView)

        Mockito.doReturn(null).`when`(mockBiometricTools)
            .getCurrentFingerprintStateWarningMessage(context)
        Mockito.doReturn(false).`when`(mockBiometricTools)
            .activateFingerprint()
        presenter.onViewClick(R.id.allowTouchIdActionView)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_skipTouchIdActionView() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setupViewMode = SetupViewMode.ALLOW_BIOMETRICS
        presenter.onViewClick(viewId = R.id.skipTouchIdActionView)

        Mockito.verify(mockPreferenceRepository).fingerprintEnabled = false
        assertThat(presenter.setupViewMode, equalTo(SetupViewMode.ALLOW_NOTIFICATIONS))
        Mockito.verify(mockView).updateSetupViews(
            setupStepProgress = 2f,
            headerTitle = R.string.onboarding_allow_notifications_title,
            headerDescription = R.string.onboarding_allow_notifications_description,
            showPasscodeCancel = null,
            passcodePositiveActionText = null
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_skipTouchIdActionView_noViewContract() {
        val presenter = createPresenter(viewContract = null)
        presenter.setupViewMode = SetupViewMode.ALLOW_BIOMETRICS
        presenter.onViewClick(viewId = R.id.skipTouchIdActionView)

        Mockito.verify(mockPreferenceRepository).fingerprintEnabled = false
        assertThat(presenter.setupViewMode, equalTo(SetupViewMode.ALLOW_NOTIFICATIONS))
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @UiThreadTest
    @Throws(Exception::class)
    fun onViewClickTest_allowNotificationsActionView() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setupViewMode = SetupViewMode.ALLOW_NOTIFICATIONS
        presenter.onViewClick(viewId = R.id.allowNotificationsActionView)

        Mockito.verify(mockPreferenceRepository).notificationsEnabled = true
        assertThat(presenter.setupViewMode, equalTo(SetupViewMode.COMPLETE))
        Mockito.verify(mockView).updateSetupViews(
            setupStepProgress = 3f,
            headerTitle = R.string.onboarding_well_done_title,
            headerDescription = R.string.onboarding_completed_description,
            showPasscodeCancel = null,
            passcodePositiveActionText = null
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @UiThreadTest
    @Throws(Exception::class)
    fun onViewClickTest_allowNotificationsActionView_noViewContract() {
        val presenter = createPresenter(viewContract = null)
        presenter.setupViewMode = SetupViewMode.ALLOW_NOTIFICATIONS
        presenter.onViewClick(viewId = R.id.allowNotificationsActionView)

        Mockito.verify(mockPreferenceRepository).notificationsEnabled = true
        assertThat(presenter.setupViewMode, equalTo(SetupViewMode.COMPLETE))
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @UiThreadTest
    @Throws(Exception::class)
    fun onViewClickTest_skipNotificationsActionView() {
        val presenter = createPresenter(viewContract = mockView)
        presenter.setupViewMode = SetupViewMode.ALLOW_NOTIFICATIONS
        presenter.onViewClick(viewId = R.id.skipNotificationsActionView)

        Mockito.verify(mockPreferenceRepository).notificationsEnabled = false
        assertThat(presenter.setupViewMode, equalTo(SetupViewMode.COMPLETE))
        Mockito.verify(mockView).updateSetupViews(
            setupStepProgress = 3f,
            headerTitle = R.string.onboarding_well_done_title,
            headerDescription = R.string.onboarding_completed_description,
            showPasscodeCancel = null,
            passcodePositiveActionText = null
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @UiThreadTest
    @Throws(Exception::class)
    fun onViewClickTest_skipNotificationsActionView_noViewContract() {
        val presenter = createPresenter(viewContract = null)
        presenter.setupViewMode = SetupViewMode.ALLOW_NOTIFICATIONS
        presenter.onViewClick(viewId = R.id.skipNotificationsActionView)

        Mockito.verify(mockPreferenceRepository).notificationsEnabled = false
        assertThat(presenter.setupViewMode, equalTo(SetupViewMode.COMPLETE))
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_proceedToMainActivity() {
        createPresenter(viewContract = mockView).onViewClick(R.id.proceedToMainActivity)

        Mockito.verify(mockView).showMainActivity()
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_mainActionView_noViewContract() {
        createPresenter(viewContract = null).onViewClick(R.id.mainActionView)

        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_newPasscodeConfirmed() {
        val presenter = createPresenter(viewContract = mockView)
        Mockito.doReturn(false).`when`(mockPasscodeTools).savePasscode(Mockito.anyString())
        presenter.newPasscodeConfirmed(passcode = "1234")

        Mockito.verify(mockView)
            .showWarningDialogWithMessage(TestTools.applicationContext.getString(R.string.errors_internal_error))
        Mockito.verifyNoMoreInteractions(mockView)

        Mockito.clearInvocations(mockView)
        Mockito.doReturn(true).`when`(mockPasscodeTools).savePasscode(Mockito.anyString())
        presenter.newPasscodeConfirmed(passcode = "1234")

        assertThat(presenter.setupViewMode, equalTo(SetupViewMode.ALLOW_BIOMETRICS))
        Mockito.verify(mockView).updateSetupViews(
            setupStepProgress = 1f,
            headerTitle = R.string.onboarding_secure_app_touch_id_allow_android,
            headerDescription = R.string.onboarding_secure_app_touch_id_description_android,
            showPasscodeCancel = null,
            passcodePositiveActionText = null
        )
        Mockito.verifyNoMoreInteractions(mockView)
    }

    @Test
    @Throws(Exception::class)
    fun onViewClickTest_newPasscodeConfirmed_noViewContract() {
        val presenter = createPresenter(viewContract = null)
        Mockito.doReturn(false).`when`(mockPasscodeTools).savePasscode(Mockito.anyString())
        presenter.newPasscodeConfirmed(passcode = "1234")

        Mockito.verifyNoMoreInteractions(mockView)

        Mockito.doReturn(true).`when`(mockPasscodeTools).savePasscode(Mockito.anyString())
        presenter.newPasscodeConfirmed(passcode = "1234")

        assertThat(presenter.setupViewMode, equalTo(SetupViewMode.ALLOW_BIOMETRICS))
        Mockito.verifyNoMoreInteractions(mockView)
    }

    private val mockPreferenceRepository = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private val mockPasscodeTools = Mockito.mock(PasscodeToolsAbs::class.java)
    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)
    private val mockView = Mockito.mock(OnboardingSetupContract.View::class.java)

    @Before
    fun setUp() {
        Mockito.doReturn(true).`when`(mockBiometricTools).isFingerprintSupported(TestTools.applicationContext)
        Mockito.doReturn(true).`when`(mockPasscodeTools).savePasscode(Mockito.anyString())
    }

    private fun createPresenter(viewContract: OnboardingSetupContract.View? = null): OnboardingSetupPresenter {
        return OnboardingSetupPresenter(
            appContext = TestTools.applicationContext,
            preferenceRepository = mockPreferenceRepository,
            passcodeTools = mockPasscodeTools,
            biometricTools = mockBiometricTools
        ).apply { this.viewContract = viewContract }
    }
}
