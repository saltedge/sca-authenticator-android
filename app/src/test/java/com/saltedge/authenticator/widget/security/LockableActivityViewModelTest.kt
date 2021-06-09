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
package com.saltedge.authenticator.widget.security

import android.content.Intent
import android.os.SystemClock
import android.view.View
import com.saltedge.android.test_tools.ViewModelTest
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.TestAppTools
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LockableActivityViewModelTest : ViewModelTest() {

    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockPreferenceRepository = Mockito.mock(PreferenceRepositoryAbs::class.java)
    private val mockPasscodeTools = Mockito.mock(PasscodeToolsAbs::class.java)
    private val mockKeyStoreManager = Mockito.mock(KeyManagerAbs::class.java)
    private val mockAuthenticatorApiManager = Mockito.mock(AuthenticatorApiManagerAbs::class.java)
    private val mockBiometricTools = Mockito.mock(BiometricToolsAbs::class.java)

    @Before
    fun setUp() {
        mockPreferenceRepository.pinInputAttempts = 0
    }

    @Test
    @Throws(Exception::class)
    fun getViewContractTest() {
        assertThat(createViewModel().connectionsRepository, equalTo(mockConnectionsRepository))
        assertThat(createViewModel().preferenceRepository, equalTo(mockPreferenceRepository))
        assertThat(createViewModel().passcodeTools, equalTo(mockPasscodeTools))
    }

    @Test
    @Throws(Exception::class)
    fun getSavedPasscodeTest() {
        val viewModel = createViewModel()

        Mockito.`when`(mockPasscodeTools.getPasscode()).thenReturn("1234")

        assertThat(viewModel.savedPasscode, equalTo("1234"))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityCreateTest() {
        val viewModel = createViewModel()
        viewModel.onActivityCreate()
        viewModel.onActivityStart(Intent())

        assertThat(viewModel.lockViewVisibility.value, equalTo(View.VISIBLE))
    }

    /**
     * test onActivityStart when returnFromOwnActivity is true
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase1() {
        val viewModel = createViewModel()
        viewModel.onActivityResult()
        viewModel.onActivityStart(Intent())

        assertThat(viewModel.lockViewVisibility.value, equalTo(View.GONE))
    }

    /**
     * test onActivityStart when returnFromOwnActivity is true
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase2() {
        val viewModel = createViewModel()
        viewModel.onActivityResult()
        viewModel.onActivityStart(Intent())

        assertThat(viewModel.lockViewVisibility.value, equalTo(View.GONE))
    }

    /**
     * test onActivityStart when returnFromOwnActivity is false
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase3() {
        val viewModel = createViewModel()
        viewModel.onActivityStart(Intent())

        assertThat(viewModel.lockViewVisibility.value, equalTo(View.VISIBLE))
    }

    /**
     * test onActivityStart when returnFromOwnActivity is false and getBooleanExtra true
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase4() {
        val viewModel = createViewModel()
        viewModel.onActivityStart(Intent().putExtra(KEY_SKIP_PIN, true))

        assertThat(viewModel.lockViewVisibility.value, equalTo(View.GONE))
    }

    /**
     * test onActivityStart when returnFromOwnActivity is false and getBooleanExtra false
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase5() {
        val viewModel = createViewModel()
        viewModel.onActivityStart(Intent().putExtra(KEY_SKIP_PIN, false))

        assertThat(viewModel.lockViewVisibility.value, equalTo(View.VISIBLE))
    }

    /**
     * test onActivityStart when returnFromOwnActivity
     * and isBiometricReady in LockableActivityContract is true
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase6() {
        given(mockBiometricTools.isBiometricReady(TestAppTools.applicationContext)).willReturn(true)

        val viewModel = createViewModel()
        viewModel.onActivityStart(Intent())

        assertThat(viewModel.showBiometricPromptEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    /**
     * test onActivityStart when shouldBlockInput is true and blockTime > 0
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase7() {
        Mockito.doReturn(6).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(999L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        val viewModel = createViewModel()
        viewModel.onActivityStart(Intent())

        assertThat(viewModel.disablePasscodeInputEvent.value, equalTo(ViewModelEvent(1)))
    }

    /**
     * test onActivityStart when shouldBlockInput is true and blockTime <= 0
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase8() {
        val viewModel = createViewModel()
        mockPreferenceRepository.pinInputAttempts = 7
        mockPreferenceRepository.blockPinInputTillTime = 0L
        viewModel.onActivityStart(Intent())

        assertThat(viewModel.disablePasscodeInputEvent.value, `is`(nullValue()))
    }

    @Test
    @Throws(Exception::class)
    fun onSuccessAuthenticationTest() {
        //given
        val viewModel = createViewModel()
        AppTools.lastUnlockType = ActivityUnlockType.PASSCODE
        mockPreferenceRepository.pinInputAttempts = 7
        mockPreferenceRepository.blockPinInputTillTime = 999L + SystemClock.elapsedRealtime()

        //when
        viewModel.onSuccessAuthentication(unlockType = ActivityUnlockType.BIOMETRICS)

        //then
        assertThat(mockPreferenceRepository.pinInputAttempts, equalTo(0))
        assertThat(mockPreferenceRepository.blockPinInputTillTime, equalTo(0L))
        assertThat(viewModel.successVibrateEvent.value, equalTo(ViewModelEvent(Unit)))
        assertThat(viewModel.lockViewVisibility.value, equalTo(View.GONE))
        assertThat(AppTools.lastUnlockType, equalTo(ActivityUnlockType.BIOMETRICS))
    }

    /**
     * test onWrongPasscodeInput when pinInputAttempts >= 11
     */
    @Test
    @Throws(Exception::class)
    fun onWrongPasscodeInputTestCase1() {
        Mockito.doReturn(11).`when`(mockPreferenceRepository).pinInputAttempts
        val viewModel = createViewModel()

        viewModel.onWrongPasscodeInput()

        assertThat(viewModel.showAppClearWarningEvent.value, equalTo(ViewModelEvent(Unit)))
    }

    /**
     * test onWrongPasscodeInput when shouldBlockInput is true and blockTime > 0
     */
    @Test
    @Throws(Exception::class)
    fun onWrongPasscodeInputTestCase2() {
        val viewModel = createViewModel()

        Mockito.doReturn(5).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(1000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        viewModel.onWrongPasscodeInput()

        assertThat(viewModel.disablePasscodeInputEvent.value, equalTo(ViewModelEvent(1)))
    }

    @Test
    @Throws(Exception::class)
    fun onWrongPasscodeInputTestCase3() {
        val viewModel = createViewModel()
        Mockito.doReturn(6).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(300000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        viewModel.onWrongPasscodeInput()

        assertThat(viewModel.disablePasscodeInputEvent.value, equalTo(ViewModelEvent(5)))
    }

    @Test
    @Throws(Exception::class)
    fun onWrongPasscodeInputTestCase4() {
        val viewModel = createViewModel()
        Mockito.doReturn(7).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(900000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        viewModel.onWrongPasscodeInput()

        assertThat(viewModel.disablePasscodeInputEvent.value, equalTo(ViewModelEvent(15)))
    }

    @Test
    @Throws(Exception::class)
    fun onWrongPasscodeInputTestCase5() {
        val viewModel = createViewModel()
        Mockito.doReturn(8).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(3600000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        viewModel.onWrongPasscodeInput()

        assertThat(viewModel.disablePasscodeInputEvent.value, equalTo(ViewModelEvent(60)))
    }

    @Test
    @Throws(Exception::class)
    fun onWrongPasscodeInputTestCase6() {
        val viewModel = createViewModel()
        Mockito.doReturn(9).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(3600000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        viewModel.onWrongPasscodeInput()

        assertThat(viewModel.disablePasscodeInputEvent.value, equalTo(ViewModelEvent(60)))
    }

    private fun createViewModel(): LockableActivityViewModel {
        return LockableActivityViewModel(
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository,
            passcodeTools = mockPasscodeTools,
            keyStoreManager = mockKeyStoreManager,
            apiManager =  mockAuthenticatorApiManager
        ).apply {
            appContext = TestAppTools.applicationContext
            biometricTools = mockBiometricTools
        }
    }
}
