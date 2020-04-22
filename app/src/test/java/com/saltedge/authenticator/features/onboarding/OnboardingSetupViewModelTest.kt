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

import android.view.View
import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.testTools.TestAppTools
import com.saltedge.authenticator.tool.secure.PasscodeToolsAbs
import com.saltedge.authenticator.widget.passcode.PasscodeInputView
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardingSetupViewModelTest {

    private lateinit var viewModel: OnboardingSetupViewModel
    private val mockPreferenceRepository = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private val mockPasscodeTools = Mockito.mock(PasscodeToolsAbs::class.java)
    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)

    @Before
    fun setUp() {
        viewModel = OnboardingSetupViewModel(
            appContext = TestAppTools.applicationContext,
            preferenceRepository = mockPreferenceRepository,
            passcodeTools = mockPasscodeTools,
            biometricTools = mockBiometricTools
        )
    }

    @Test
    @Throws(Exception::class)
    fun onboardingViewModelsTest() {
        assertThat(
            viewModel.onboardingViewModels,
            equalTo(
                listOf(
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
            )
        )
    }

    /**
     * Test onViewClick when click on skipActionView
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase1() {
        viewModel.onViewClick(R.id.skipActionView)

        assertThat(viewModel.setupLayoutVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.onboardingLayoutVisibility.value, equalTo(View.GONE))
        assertNotNull(viewModel.setPasscodeInputMode.value)

        assertThat(
            viewModel.headerTitle.value,
            equalTo(R.string.onboarding_secure_app_passcode_create)
        )
    }

    /**
     * Test onViewClick when click on proceedToSetup
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase2() {
        viewModel.onViewClick(R.id.proceedToSetup)

        assertThat(viewModel.setupLayoutVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.onboardingLayoutVisibility.value, equalTo(View.GONE))
        assertNotNull(viewModel.setPasscodeInputMode.value)

        assertThat(
            viewModel.headerTitle.value,
            equalTo(R.string.onboarding_secure_app_passcode_create)
        )
    }

    /**
     * Test onViewClick when click on nextActionView
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase3() {
        viewModel.onViewClick(R.id.nextActionView)

        assertNotNull(viewModel.moveNext.value)
    }

    /**
     * Test onViewClick when click on unknown id
     */
    @Test
    @Throws(Exception::class)
    fun onViewClickTestCase4() {
        viewModel.onViewClick(-1)

        assertNull(viewModel.moveNext.value)
        assertThat(viewModel.setupLayoutVisibility.value, equalTo(View.GONE))
        assertThat(viewModel.onboardingLayoutVisibility.value, equalTo(View.VISIBLE))
        assertNull(viewModel.setPasscodeInputMode.value)
    }

    @Test
    @Throws(Exception::class)
    fun enteredNewPasscodeTest() {
        viewModel.enteredNewPasscode(PasscodeInputView.InputMode.REPEAT_NEW_PASSCODE)

        assertThat(
            viewModel.headerTitle.value,
            equalTo(R.string.onboarding_secure_app_passcode_repeat)
        )
    }

    @Test
    @Throws(Exception::class)
    fun reEnterPasscodeTest() {
        viewModel.reEnterPasscode()

        assertThat(
            viewModel.headerTitle.value,
            equalTo(R.string.onboarding_secure_app_passcode_create)
        )
    }

    @Test
    @Throws(Exception::class)
    fun passcodeInputCanceledByUserTest() {
        viewModel.passcodeInputCanceledByUser()

        assertThat(
            viewModel.setPasscodeInputMode.value,
            equalTo(PasscodeInputView.InputMode.NEW_PASSCODE)
        )

        assertThat(
            viewModel.headerTitle.value,
            equalTo(R.string.onboarding_secure_app_passcode_create)
        )
    }

    /**
     * Test newPasscodeConfirmed when passcode was not saved
     */
    @Test
    @Throws(Exception::class)
    fun newPasscodeConfirmedTestCase1() {
        Mockito.doReturn(false).`when`(mockPasscodeTools).savePasscode(Mockito.anyString())
        viewModel.newPasscodeConfirmed(passcode = "1234")

        assertThat(
            viewModel.showWarningDialogWithMessage.value,
            equalTo(TestAppTools.applicationContext.getString(R.string.errors_cant_save_passcode))
        )
    }

    /**
     * Test newPasscodeConfirmed when passcode was saved
     */
    @Test
    @Throws(Exception::class)
    fun newPasscodeConfirmedTestCase2() {
        Mockito.doReturn(true).`when`(mockPasscodeTools).savePasscode(Mockito.anyString())
        viewModel.newPasscodeConfirmed(passcode = "1234")

        assertThat(viewModel.passcodeInputViewVisibility.value, equalTo(View.GONE))
        assertNotNull(viewModel.showMainActivity.value)
    }

    @Test
    @Throws(Exception::class)
    fun onOnboardingPageSelectedTest() {
        viewModel.onOnboardingPageSelected(position = -1)

        assertNull(viewModel.pageIndicator.value)

        viewModel.onOnboardingPageSelected(position = viewModel.onboardingViewModels.lastIndex + 1)

        assertNull(viewModel.pageIndicator.value)

        viewModel.onOnboardingPageSelected(position = 0)

        assertThat(viewModel.pageIndicator.value, equalTo(0))

        viewModel.onOnboardingPageSelected(position = 1)

        assertThat(viewModel.pageIndicator.value, equalTo(1))

        viewModel.onOnboardingPageSelected(position = viewModel.onboardingViewModels.lastIndex)

        assertThat(viewModel.proceedViewVisibility.value, equalTo(View.VISIBLE))
        assertThat(viewModel.skipViewVisibility.value, equalTo(View.GONE))
    }
}
