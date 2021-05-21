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

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saltedge.authenticator.app.AppTools
import com.saltedge.authenticator.core.tools.MILLIS_IN_MINUTE
import com.saltedge.authenticator.core.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.core.tools.millisToRemainedMinutes
import com.saltedge.authenticator.core.tools.secure.KeyManagerAbs
import com.saltedge.authenticator.models.ViewModelEvent
import com.saltedge.authenticator.models.repository.ConnectionsRepositoryAbs
import com.saltedge.authenticator.models.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.AuthenticatorApiManagerAbs
import com.saltedge.authenticator.tools.PasscodeToolsAbs
import com.saltedge.authenticator.tools.postUnitEvent
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class LockableActivityViewModel(
    val connectionsRepository: ConnectionsRepositoryAbs,
    val preferenceRepository: PreferenceRepositoryAbs,
    val passcodeTools: PasscodeToolsAbs,
    val keyStoreManager: KeyManagerAbs,
    val apiManager: AuthenticatorApiManagerAbs
): ViewModel() {
    private var returnFromOwnActivity = false
    private var countDownTimer: CountDownTimer? = null // enabled when user set passcode incorrect several times
    private val inactivityTimerDuration = TimeUnit.MINUTES.toMillis(1)
    private var timer: Timer? = null // enabled when user does not interact with the app for 1 minute
    var appContext: Context? = null
    var biometricTools: BiometricToolsAbs? = null
    val savedPasscode: String
        get() = passcodeTools.getPasscode()
    val lockViewVisibility = MutableLiveData<Int>(View.GONE)
    val onLockEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onUnlockEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val dismissLockWarningEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val showLockWarningEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val enablePasscodeInputEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val disablePasscodeInputEvent = MutableLiveData<ViewModelEvent<Int>>()
    val showAppClearWarningEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val onWipeApplicationEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val successVibrateEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val showBiometricPromptEvent = MutableLiveData<ViewModelEvent<Unit>>()
    val isBiometricInputReady: Boolean
        get() = appContext?.let { biometricTools?.isBiometricReady(context = it) == true } ?: false

    fun onActivityCreate() {
        returnFromOwnActivity = false
    }

    fun onActivityResult() {
        returnFromOwnActivity = true
    }

    fun onActivityStart(intent: Intent?) {
        lockViewVisibility
        when {
            returnFromOwnActivity -> {  // when app has result from started activity
                returnFromOwnActivity = false
                unlockScreen()

            }
            intent?.getBooleanExtra(KEY_SKIP_PIN, false) == true -> { // when we start with SKIP_PIN
                intent.removeExtra(KEY_SKIP_PIN)
                unlockScreen()
            }
            else -> lockScreen()
        }
    }

    fun onSuccessAuthentication(unlockType: ActivityUnlockType) {
        preferenceRepository.pinInputAttempts = 0
        preferenceRepository.blockPinInputTillTime = 0L
        successVibrateEvent.postUnitEvent()
        AppTools.lastUnlockType = unlockType
        unlockScreen()
    }

    fun onWrongPasscodeInput() {
        val inputAttempt = preferenceRepository.pinInputAttempts + 1
        preferenceRepository.pinInputAttempts = inputAttempt
        preferenceRepository.blockPinInputTillTime =
            SystemClock.elapsedRealtime() + calculateWrongAttemptWaitTime(inputAttempt)

        when {
            shouldBlockInput(inputAttempt) -> disableUnlockInput()
            shouldWipeApplication(inputAttempt) -> {
                onWipeApplicationEvent.postUnitEvent()
                showAppClearWarningEvent.postUnitEvent()
            }
        }
    }

    fun destroyTimer() {
        timer?.cancel()
        timer?.purge()
        timer = null
    }

    fun onLockWarningIgnored() {
        lockScreen()
    }

    fun onTouch(lockViewIsNotVisible: Boolean) {
        if (lockViewIsNotVisible) restartInactivityTimer()
    }

    private fun lockScreen() {
        lockViewVisibility.postValue(View.VISIBLE)
        onLockEvent.postUnitEvent()
        val inputAttempt = preferenceRepository.pinInputAttempts
        if (shouldBlockInput(inputAttempt)) disableUnlockInput()
        else if (isBiometricInputReady) showBiometricPromptEvent.postUnitEvent()
    }

    private fun unlockScreen() {
        lockViewVisibility.postValue(View.GONE)
        onUnlockEvent.postUnitEvent()
        restartInactivityTimer()
    }

    private fun restartInactivityTimer() {
        dismissLockWarningEvent.postUnitEvent()
        timer?.cancel()
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() { showLockWarningEvent.postUnitEvent() }
            }, inactivityTimerDuration)
        }
    }

    private fun disableUnlockInput() {
        val blockTime = preferenceRepository.blockPinInputTillTime - SystemClock.elapsedRealtime()
        if (blockTime > 0) {
            disablePasscodeInputEvent.postValue(ViewModelEvent(millisToRemainedMinutes(blockTime)))
            startDisableUnlockInputTimer(blockTime)
        }
    }

    /**
     * Start timer which counts time while passcode input is inactive
     */
    private fun startDisableUnlockInputTimer(blockTime: Long) {
        try {
            resetTimer()
            countDownTimer = object : CountDownTimer(blockTime, blockTime) {
                override fun onFinish() {
                    resetTimer()
                    enablePasscodeInputEvent.postUnitEvent()
                }

                override fun onTick(millisUntilFinished: Long) {}
            }.start()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun shouldBlockInput(inputAttempt: Int): Boolean = inputAttempt in 6..10

    //User exceeded passcode input attempts
    private fun shouldWipeApplication(inputAttempt: Int): Boolean = inputAttempt >= 11

    private fun calculateWrongAttemptWaitTime(attemptNumber: Int): Long = when {
        attemptNumber < 4 -> 0L
        attemptNumber == 5 -> 1L * MILLIS_IN_MINUTE
        attemptNumber == 6 -> 3L * MILLIS_IN_MINUTE
        else -> 5L * MILLIS_IN_MINUTE
    }
}
