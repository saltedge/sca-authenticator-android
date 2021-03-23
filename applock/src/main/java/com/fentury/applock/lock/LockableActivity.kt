/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.lock

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.fentury.applock.R
import com.fentury.applock.widget.passcode.PasscodeInputListener
import com.fentury.applock.widget.passcode.PasscodeInputMode
import com.fentury.applock.repository.PreferencesRepository
import com.fentury.applock.root.SEAppLock
import com.fentury.applock.tools.*
import com.fentury.applock.tools.biometric.BiometricTools
import com.fentury.applock.widget.biometric.BiometricPromptAbs
import com.fentury.applock.widget.biometric.BiometricPromptCallback
import com.fentury.applock.widget.security.UnlockAppInputView
import com.google.android.material.snackbar.Snackbar

const val KEY_SKIP_PIN = "KEY_SKIP_PIN"

@SuppressLint("Registered")
abstract class LockableActivity : AppCompatActivity(),
    PasscodeInputListener,
    BiometricPromptCallback
{
    private var inactivityWarningSnackbar: Snackbar? = null
    private var viewModel = LockableActivityViewModel(
        preferenceRepository = PreferencesRepository,
        passcodeTools = PasscodeTools,
        biometricTools = BiometricTools
    )

    private var biometricPrompt: BiometricPromptAbs? = null
    private var vibrator: Vibrator? = null
    private var alertDialog: AlertDialog? = null

    abstract fun getUnlockAppInputView(): UnlockAppInputView?

    fun restartLockableActivity() {
        finish()
        startActivity(Intent(this, this.javaClass).apply { putExtra(KEY_SKIP_PIN, true) })
    }

    /**
     * override if activity need to receive event when Activity is locked
     */
    open fun onLockActivity() {}

    /**
     * override if activity need to receive event when Activity is unlocked
     */
    open fun onUnlockActivity() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SEAppLock.passcodeComponent.inject(this)
        biometricPrompt = SEAppLock.passcodeComponent.biometricPrompt()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        setupViewModel()
        viewModel.onActivityCreate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult()
    }

    override fun onStart() {
        super.onStart()
        biometricPrompt?.resultCallback = this
        getUnlockAppInputView()?.let {
            it.biometricsActionIsAvailable = viewModel.isBiometricInputReady
            it.setSavedPasscode(viewModel.savedPasscode)
            it.passcodeInputViewListener = this
        }
        viewModel.onActivityStart(intent)
    }

    override fun onStop() {
        viewModel.destroyTimer()
        biometricPrompt?.resultCallback = null
        biometricPrompt?.dismissBiometricPrompt()
        getUnlockAppInputView()?.passcodeInputViewListener = null
        super.onStop()
    }

    override fun onBiometricActionSelected() {
        displayBiometricPrompt()
    }

    override fun onPasscodeInputCanceledByUser() {}

    override fun onInputValidPasscode() {
        viewModel.onSuccessAuthentication()
    }

    override fun onInputInvalidPasscode(mode: PasscodeInputMode) {
        viewModel.onWrongPasscodeInput()
    }

    override fun onNewPasscodeEntered(mode: PasscodeInputMode, passcode: String) {}

    override fun onNewPasscodeConfirmed(passcode: String) {}

    override fun onForgotActionSelected() {
        getUnlockAppInputView()?.let {
            it.setInputViewVisibility(show = false)
            it.setResetPasscodeViewVisibility(show = true)
        }
    }

    override fun biometricAuthFinished() {
        viewModel.onSuccessAuthentication()
    }
    override fun biometricsCanceledByUser() {}

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            viewModel.onTouch(lockViewIsNotVisible = getUnlockAppInputView()?.visibility != View.VISIBLE)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun setupViewModel() {
        viewModel.appContext = this.applicationContext

        viewModel.lockViewVisibility.observe(this, Observer {
            getUnlockAppInputView()?.visibility = it
        })
        viewModel.onLockEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { onLockActivity() }
        })
        viewModel.onUnlockEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { onUnlockActivity() }
        })
        viewModel.dismissLockWarningEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                inactivityWarningSnackbar?.dismiss()
                inactivityWarningSnackbar = null
            }
        })
        viewModel.showLockWarningEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { showLockWarningEvent() }
        })
        viewModel.enablePasscodeInputEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { showPasscodeInputView() }
        })
        viewModel.disablePasscodeInputEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { minutes -> showWarningAndHidePasscodeView(minutes) }
        })
        viewModel.successVibrateEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { successVibrate() }
        })
        viewModel.showBiometricPromptEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { displayBiometricPrompt() }
        })
    }

    private fun showLockWarningEvent() {
        inactivityWarningSnackbar = getUnlockAppInputView()?.buildSnackbar(
            message = getString(R.string.warning_application_was_locked),
            snackBarDuration = Snackbar.LENGTH_LONG,
            actionResId = R.string.actions_cancel
        )
        inactivityWarningSnackbar?.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (event == DISMISS_EVENT_TIMEOUT) viewModel.onLockWarningIgnored()
            }
        })
        inactivityWarningSnackbar?.show()
    }

    private fun showPasscodeInputView() {
        getUnlockAppInputView()?.let {
            it.biometricsActionIsAvailable = viewModel.isBiometricInputReady
            it.setInputViewVisibility(show = true)
            it.setResetPasscodeViewVisibility(show = false)
        }
        alertDialog?.dismiss()
    }

    private fun showWarningAndHidePasscodeView(remainedMinutes: Int) {
        val wrongPasscodeMessage = getString(R.string.errors_wrong_passcode)
        val retryMessage = resources.getQuantityString(
            R.plurals.errors_passcode_try_again,
            remainedMinutes,
            remainedMinutes
        )
        getUnlockAppInputView()?.let {
            it.hideWarning()
            it.setInputViewVisibility(show = true)
            it.setResetPasscodeViewVisibility(show = false)
        }
        alertDialog = showLockWarningDialog(message = "$wrongPasscodeMessage\n$retryMessage")
    }

    /**
     * Display biometric prompt if resultCallback is already set on Activity start
     */
    @TargetApi(Build.VERSION_CODES.P)
    private fun displayBiometricPrompt() {
        if (biometricPrompt?.resultCallback != null) {
            biometricPrompt?.showBiometricPrompt(
                context = this,
                title = SEAppLock.applicationName ?: "",
                descriptionResId = R.string.fingerprint_scan_unlock,
                negativeActionTextResId = R.string.actions_cancel
            )
        }
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    private fun successVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator?.vibrate(50)
    }
}