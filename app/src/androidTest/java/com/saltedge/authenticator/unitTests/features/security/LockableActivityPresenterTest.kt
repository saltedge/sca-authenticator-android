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
package com.saltedge.authenticator.unitTests.features.security

import android.content.Intent
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.security.KEY_SKIP_PIN
import com.saltedge.authenticator.features.security.LockableActivityContract
import com.saltedge.authenticator.features.security.LockableActivityPresenter
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tool.secure.PasscodeTools.replacePasscodeKey
import com.saltedge.authenticator.tool.secure.PasscodeTools.savePasscode
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class LockableActivityPresenterTest {

    private val mockView = Mockito.mock(LockableActivityContract::class.java)
    private val mockConnectionsRepository = Mockito.mock(ConnectionsRepositoryAbs::class.java)
    private val mockPreferenceRepository = Mockito.mock(PreferenceRepositoryAbs::class.java)

    @Before
    fun setUp() {
        mockPreferenceRepository.pinInputAttempts = 0
    }

    @Test
    @Throws(Exception::class)
    fun getViewContractTest() {
        assertThat(
            LockableActivityPresenter(
                viewContract = mockView,
                connectionsRepository = mockConnectionsRepository,
                preferenceRepository = mockPreferenceRepository
            ).viewContract, equalTo(mockView)
        )
        assertThat(
            LockableActivityPresenter(
                viewContract = mockView,
                connectionsRepository = mockConnectionsRepository,
                preferenceRepository = mockPreferenceRepository
            ).connectionsRepository,
            equalTo(mockConnectionsRepository)
        )
        assertThat(
            LockableActivityPresenter(
                viewContract = mockView,
                connectionsRepository = mockConnectionsRepository,
                preferenceRepository = mockPreferenceRepository
            ).preferenceRepository,
            equalTo(mockPreferenceRepository)
        )
    }

    @Test
    @Throws(Exception::class)
    fun getSavedPasscodeTest() {
        replacePasscodeKey()
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )

        assertThat(presenter.savedPasscode, equalTo(""))

        savePasscode("1234")

        assertThat(presenter.savedPasscode, equalTo("1234"))
    }

    @Test
    @Throws(Exception::class)
    fun onActivityCreateTest() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        presenter.onActivityCreate()
        presenter.onActivityStart(Intent())

        Mockito.verify(mockView).lockScreen()
    }

    /**
     * test onActivityStart when returnFromOwnActivity is true
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase1() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        presenter.onActivityResult()
        presenter.onActivityStart(Intent())

        Mockito.verify(mockView).closeLockView()
    }

    /**
     * test onActivityStart when returnFromOwnActivity is true
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase2() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        presenter.onActivityResult()
        presenter.onActivityStart(Intent())

        Mockito.verify(mockView).closeLockView()
    }

    /**
     * test onActivityStart when returnFromOwnActivity is false
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase3() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        presenter.onActivityStart(Intent())

        Mockito.verify(mockView).lockScreen()
    }

    /**
     * test onActivityStart when returnFromOwnActivity is false and getBooleanExtra true
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase4() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        presenter.onActivityStart(Intent().putExtra(KEY_SKIP_PIN, true))

        Mockito.verify(mockView).closeLockView()
    }

    /**
     * test onActivityStart when returnFromOwnActivity is false and getBooleanExtra false
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase5() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        presenter.onActivityStart(Intent().putExtra(KEY_SKIP_PIN, false))

        Mockito.verify(mockView).lockScreen()
    }

    /**
     * test onActivityStart when returnFromOwnActivity
     * and isBiometricReady in LockableActivityContract is true
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase6() {
        Mockito.doReturn(true).`when`(mockView).isBiometricReady()

        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        presenter.onActivityStart(Intent())

        Mockito.verify(mockView).isBiometricReady()
        Mockito.verify(mockView).displayBiometricPromptView()
    }

    /**
     * test onActivityStart when shouldBlockInput is true and blockTime > 0
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase7() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )

        Mockito.doReturn(6).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(999L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        presenter.onActivityStart(Intent())

        Mockito.verify(mockView).disableUnlockInput(inputAttempt = 6, remainedMinutes = 1)
    }

    /**
     * test onActivityStart when shouldBlockInput is true and blockTime <= 0
     */
    @Test
    @Throws(Exception::class)
    fun onActivityStartTestCase8() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        mockPreferenceRepository.pinInputAttempts = 7
        mockPreferenceRepository.blockPinInputTillTime = 0L
        presenter.onActivityStart(Intent())

        Mockito.never()
    }

    @Test
    @Throws(Exception::class)
    fun onSuccessAuthenticationTest() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        mockPreferenceRepository.pinInputAttempts = 7
        mockPreferenceRepository.blockPinInputTillTime = 999L + SystemClock.elapsedRealtime()
        presenter.onSuccessAuthentication()

        assertThat(mockPreferenceRepository.pinInputAttempts, equalTo(0))
        assertThat(mockPreferenceRepository.blockPinInputTillTime, equalTo(0L))
        Mockito.verify(mockView).vibrateAboutSuccess()
        Mockito.verify(mockView).closeLockView()
    }

    /**
     * test onWrongPasscodeInput when pinInputAttempts >= 11
     */
    @Test
    @Throws(Exception::class)
    fun onWrongPasscodeInputTestCase1() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )

        Mockito.doReturn(11).`when`(mockPreferenceRepository).pinInputAttempts

        presenter.onWrongPasscodeInput()

        Mockito.verify(mockView).resetUser()
    }

    /**
     * test onWrongPasscodeInput when pinInputAttempts < 6
     */
    @Test
    @Throws(Exception::class)
    fun onWrongPasscodeInputTestCase2() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )
        mockPreferenceRepository.pinInputAttempts = 3
        presenter.onWrongPasscodeInput()

        Mockito.verify(mockView).clearOutputAndShowErrorWarning(R.string.errors_wrong_passcode_long)
    }

    /**
     * test onWrongPasscodeInput when shouldBlockInput is true and blockTime > 0
     */
    @Test
    @Throws(Exception::class)
    fun onWrongPasscodeInputTestCase3() {
        val presenter = LockableActivityPresenter(
            viewContract = mockView,
            connectionsRepository = mockConnectionsRepository,
            preferenceRepository = mockPreferenceRepository
        )

        Mockito.doReturn(5).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(1000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        presenter.onWrongPasscodeInput()

        Mockito.verify(mockView).disableUnlockInput(inputAttempt = 6, remainedMinutes = 1)
        Mockito.clearInvocations(mockView)
        Mockito.doReturn(6).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(300000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        presenter.onWrongPasscodeInput()

        Mockito.verify(mockView).disableUnlockInput(inputAttempt = 7, remainedMinutes = 5)
        Mockito.clearInvocations(mockView)
        Mockito.doReturn(7).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(900000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        presenter.onWrongPasscodeInput()

        Mockito.verify(mockView).disableUnlockInput(inputAttempt = 8, remainedMinutes = 15)
        Mockito.clearInvocations(mockView)
        Mockito.doReturn(8).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(3600000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        presenter.onWrongPasscodeInput()

        Mockito.verify(mockView).disableUnlockInput(inputAttempt = 9, remainedMinutes = 60)
        Mockito.clearInvocations(mockView)
        Mockito.doReturn(9).`when`(mockPreferenceRepository).pinInputAttempts
        Mockito.doReturn(3600000L + SystemClock.elapsedRealtime()).`when`(mockPreferenceRepository).blockPinInputTillTime

        presenter.onWrongPasscodeInput()

        Mockito.verify(mockView).disableUnlockInput(inputAttempt = 10, remainedMinutes = 60)
        Mockito.clearInvocations(mockView)
    }
}
