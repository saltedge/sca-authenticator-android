/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.lock

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fentury.applock.models.ViewModelEvent
import com.fentury.applock.repository.PreferencesRepositoryAbs
import com.fentury.applock.tools.PasscodeToolsAbs
import com.fentury.applock.tools.biometric.BiometricToolsAbs
import com.fentury.applock.tools.postUnitEvent
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

internal class LockableActivityViewModel(
    val preferenceRepository: PreferencesRepositoryAbs,
    val passcodeTools: PasscodeToolsAbs,
    val biometricTools: BiometricToolsAbs
): ViewModel() {
    private var returnFromOwnActivity = false
    private var countDownTimer: CountDownTimer? = null // enabled when user set passcode incorrect several times
    private val inactivityTimerDuration = TimeUnit.MINUTES.toMillis(1)
    private var timer: Timer? = null // enabled when user does not interact with the app for 1 minute
    var appContext: Context? = null
    val savedPasscode: String
        get() = passcodeTools.getPasscode()
    val lockViewVisibility = MutableLiveData<Int>(View.GONE)
    internal val onLockEvent = MutableLiveData<ViewModelEvent<Unit>>()
    internal val onUnlockEvent = MutableLiveData<ViewModelEvent<Unit>>()
    internal val dismissLockWarningEvent = MutableLiveData<ViewModelEvent<Unit>>()
    internal val showLockWarningEvent = MutableLiveData<ViewModelEvent<Unit>>()
    internal val enablePasscodeInputEvent = MutableLiveData<ViewModelEvent<Unit>>()
    internal val disablePasscodeInputEvent = MutableLiveData<ViewModelEvent<Int>>()
    internal val successVibrateEvent = MutableLiveData<ViewModelEvent<Unit>>()
    internal val showBiometricPromptEvent = MutableLiveData<ViewModelEvent<Unit>>()
    internal val isBiometricInputReady: Boolean
        get() = appContext?.let { biometricTools?.isBiometricReady(context = it) == true } ?: false
    private val millisInMinute = 60000L

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
            savedPasscode.isEmpty() -> unlockScreen()
            else -> lockScreen()
        }
    }

    fun onSuccessAuthentication() {
        preferenceRepository.pinInputAttempts = 0
        preferenceRepository.blockPinInputTillTime = 0L
        successVibrateEvent.postUnitEvent()
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
                preferenceRepository.clearPasscodePreferences()
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
     * Convert remained milliseconds to remained minutes
     *
     * @param remainedMillis - number of minutes remaining in milliseconds
     * @return milliseconds
     */
    private fun millisToRemainedMinutes(remainedMillis: Long): Int =
        ceil((remainedMillis.toDouble() / millisInMinute)).toInt()

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
        } catch (e: Exception) {}
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
        attemptNumber == 5 -> 1L * millisInMinute
        attemptNumber == 6 -> 3L * millisInMinute
        else -> 5L * millisInMinute
    }
}
