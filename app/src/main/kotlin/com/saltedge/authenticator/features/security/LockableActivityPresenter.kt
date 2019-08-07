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
package com.saltedge.authenticator.features.security

import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.db.ConnectionsRepositoryAbs
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.tool.MILLIS_IN_MINUTE
import com.saltedge.authenticator.tool.log
import com.saltedge.authenticator.tool.millisToRemainedMinutes
import com.saltedge.authenticator.tool.secure.PasscodeTools

class LockableActivityPresenter(
        val viewContract: LockableActivityContract,
        val connectionsRepository: ConnectionsRepositoryAbs,
        val preferenceRepository: PreferenceRepositoryAbs
) {

    private var returnFromOwnActivity = false
    val savedPasscode: String
        get() = PasscodeTools.getPasscode()
    private var timer: CountDownTimer? = null

    fun onActivityCreate() {
        returnFromOwnActivity = false
    }

    fun onActivityResult() {
        returnFromOwnActivity = true
    }

    fun onActivityStart(intent: Intent?) {
        when {
            returnFromOwnActivity -> {
                returnFromOwnActivity = false
                viewContract.closeLockView()
            } // when app has result from started activity
            intent?.getBooleanExtra(KEY_SKIP_PIN, false) == true -> {
                intent.removeExtra(KEY_SKIP_PIN)
                viewContract.closeLockView()
            } // when we start with SKIP_PIN
            else -> lockScreen()
        }
    }

    fun onSuccessAuthentication() {
        preferenceRepository.pinInputAttempts = 0
        preferenceRepository.blockPinInputTillTime = 0L
        viewContract.vibrateAboutSuccess()
        viewContract.closeLockView()
    }

    fun onWrongPasscodeInput() {
        val inputAttempt = preferenceRepository.pinInputAttempts + 1
        preferenceRepository.pinInputAttempts = inputAttempt
        preferenceRepository.blockPinInputTillTime = SystemClock.elapsedRealtime() + calculateWrongAttemptWaitTime(inputAttempt)

        when {
            shouldBlockInput(inputAttempt) -> disableUnlockInput(inputAttempt)
            shouldWipeApplication(inputAttempt) -> {
                wipeApplication()
                viewContract.resetUser()
            }
            else -> viewContract.clearOutputAndShowErrorWarning(R.string.errors_wrong_passcode_long)
        }
    }

    private fun lockScreen() {
        viewContract.lockScreen()
        val inputAttempt = preferenceRepository.pinInputAttempts
        if (shouldBlockInput(inputAttempt)) disableUnlockInput(inputAttempt)
        else if (viewContract.isBiometricReady()) viewContract.displayBiometricPromptView()
    }

    private fun disableUnlockInput(inputAttempt: Int) {
        val blockTime = preferenceRepository.blockPinInputTillTime - SystemClock.elapsedRealtime()
        if (blockTime > 0) {
            viewContract.disableUnlockInput(inputAttempt, millisToRemainedMinutes(blockTime))
            startInactivityTimer(blockTime)
        }
    }

    private fun startInactivityTimer(blockTime: Long) {
        try {
            resetTimer()
            timer = object : CountDownTimer(blockTime, blockTime) {
                override fun onFinish() {
                    resetTimer()
                    viewContract.unBlockInput()
                }

                override fun onTick(millisUntilFinished: Long) {}
            }.start()
        } catch (e: Exception) {
            e.log()
        }
    }

    private fun resetTimer() {
        timer?.cancel()
        timer = null
    }

    private fun shouldBlockInput(inputAttempt: Int): Boolean = inputAttempt in 6..10

    private fun shouldWipeApplication(inputAttempt: Int): Boolean = inputAttempt >= 11

    private fun wipeApplication() {
        preferenceRepository.clearUserPreferences()
        connectionsRepository.deleteAllConnections()
    }

    private fun calculateWrongAttemptWaitTime(attemptNumber: Int): Long = when {
        attemptNumber < 6 -> 0L
        attemptNumber == 6 -> 1L * MILLIS_IN_MINUTE
        attemptNumber == 7 -> 5L * MILLIS_IN_MINUTE
        attemptNumber == 8 -> 15L * MILLIS_IN_MINUTE
        attemptNumber == 9 -> 60L * MILLIS_IN_MINUTE
        attemptNumber == 10 -> 60L * MILLIS_IN_MINUTE
        else -> Long.MAX_VALUE
    }
}