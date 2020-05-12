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
package com.saltedge.authenticator.widget.security

import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import com.saltedge.authenticator.R
import com.saltedge.authenticator.models.Connection
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.sdk.model.connection.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.connection.isActive
import com.saltedge.authenticator.sdk.tools.MILLIS_IN_MINUTE
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import com.saltedge.authenticator.sdk.tools.millisToRemainedMinutes
import com.saltedge.authenticator.tools.log
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import java.util.*
import java.util.concurrent.TimeUnit

class LockableActivityPresenter(
    val viewContract: LockableActivityContract,
    val connectionsRepository: ConnectionsRepositoryAbs,
    val preferenceRepository: PreferenceRepositoryAbs,
    val passcodeTools: PasscodeToolsAbs,
    val keyStoreManager: KeyStoreManagerAbs,
    val apiManager: AuthenticatorApiManagerAbs
) {

    private var returnFromOwnActivity = false
    val savedPasscode: String
        get() = passcodeTools.getPasscode()
    private var countDownTimer: CountDownTimer? = null // enabled when user set passcode incorrect several times
    private val timerDuration = TimeUnit.MINUTES.toMillis(1)
    private var timer: Timer? = null // enabled when user does not interact with the app for 1 minute

    fun onActivityCreate() {
        returnFromOwnActivity = false
    }

    fun onActivityResult() {
        returnFromOwnActivity = true
    }

    fun onActivityStart(intent: Intent?) {
        when {
            returnFromOwnActivity -> {  // when app has result from started activity
                returnFromOwnActivity = false
                viewContract.closeLockView()
            }
            intent?.getBooleanExtra(KEY_SKIP_PIN, false) == true -> { // when we start with SKIP_PIN
                intent.removeExtra(KEY_SKIP_PIN)
                viewContract.closeLockView()
            }
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
        preferenceRepository.blockPinInputTillTime =
            SystemClock.elapsedRealtime() + calculateWrongAttemptWaitTime(inputAttempt)

        when {
            shouldBlockInput(inputAttempt) -> disableUnlockInput(inputAttempt)
            shouldWipeApplication(inputAttempt) -> {
                wipeApplication()
                viewContract.resetUser()
            }
            else -> viewContract.clearOutputAndShowErrorWarning(R.string.errors_wrong_passcode_long)
        }
    }

    fun restartLockTimer() {
        viewContract.dismissSnackbar()
        timer?.cancel()
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    viewContract.showLockWarning()
                }
            }, timerDuration)
        }
    }

    fun destroyTimer() {
        timer?.cancel()
        timer?.purge()
        timer = null
    }

    fun onSnackbarDismissed() {
        lockScreen()
    }

    fun clearAppData() {
        sendRevokeRequestForConnections(connectionsRepository.getAllActiveConnections())
        deleteAllConnectionsAndKeys()
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
            countDownTimer = object : CountDownTimer(blockTime, blockTime) {
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
        countDownTimer?.cancel()
        countDownTimer = null
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

    private fun sendRevokeRequestForConnections(connections: List<Connection>) {//TODO move connections interactor
        val connectionsAndKeys: List<ConnectionAndKey> = connections.filter { it.isActive() }
            .mapNotNull { keyStoreManager.createConnectionAndKeyModel(it) }
        apiManager.revokeConnections(connectionsAndKeys = connectionsAndKeys, resultCallback = null)
    }

    private fun deleteAllConnectionsAndKeys() {
        val connectionGuids = connectionsRepository.getAllConnections().map { it.guid }
        keyStoreManager.deleteKeyPairs(connectionGuids)
        connectionsRepository.deleteAllConnections()
    }
}
