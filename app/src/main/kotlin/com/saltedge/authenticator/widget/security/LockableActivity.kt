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

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.main.buildWarning
import com.saltedge.authenticator.features.onboarding.OnboardingSetupActivity
import com.saltedge.authenticator.models.repository.ConnectionsRepository
import com.saltedge.authenticator.models.repository.PreferenceRepository
import com.saltedge.authenticator.sdk.AuthenticatorApiManager
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManager
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptCallback
import com.saltedge.authenticator.widget.passcode.PasscodeInputListener
import com.saltedge.authenticator.widget.passcode.PasscodeInputMode

const val KEY_SKIP_PIN = "KEY_SKIP_PIN"

@SuppressLint("Registered")
abstract class LockableActivity : AppCompatActivity(),
    PasscodeInputListener,
    BiometricPromptCallback,
    DialogInterface.OnClickListener
{
    private var inactivityWarningSnackbar: Snackbar? = null
    private var viewModel = LockableActivityViewModel(
        connectionsRepository = ConnectionsRepository,
        preferenceRepository = PreferenceRepository,
        passcodeTools = PasscodeTools,
        keyStoreManager = KeyStoreManager,
        apiManager = AuthenticatorApiManager
    )
    private var biometricPrompt: BiometricPromptAbs? = null
    private var vibrator: Vibrator? = null

    abstract fun getUnlockAppInputView(): UnlockAppInputView?

    fun restartLockableActivity() {
        startActivity(Intent(this, this.javaClass).apply { putExtra(KEY_SKIP_PIN, true) })
        finish()
    }

    /**
     * override if activity need to receive event when Activity is unlocked
     */
    open fun onUnlockActivity() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.let {
            viewModel.biometricTools = authenticatorApp?.appComponent?.biometricTools()
            biometricPrompt = authenticatorApp?.appComponent?.biometricPrompt()
        }
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
        showResetView()
    }

    override fun onClearDataActionSelected() {
        showResetDataDialog(listener = this)
    }

    override fun onClick(listener: DialogInterface?, dialogActionId: Int) {
        when (dialogActionId) {
            DialogInterface.BUTTON_POSITIVE -> {
                viewModel.clearAppData()
                showOnboardingActivity()
            }
            DialogInterface.BUTTON_NEGATIVE -> listener?.dismiss()
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

        viewModel.onUnlockEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { onUnlockActivity() }
        })
        viewModel.dismissLockWarningEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                inactivityWarningSnackbar?.dismiss()
                inactivityWarningSnackbar = null
            }
        })
        viewModel.showAppClearWarningEvent.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                showDialogAboutUserReset(DialogInterface.OnClickListener { _, _ -> this.restartApp() })
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
        inactivityWarningSnackbar = this@LockableActivity.buildWarning(
            textResId = R.string.warning_application_was_locked,
            snackBarDuration = 5000,
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
            it.enableInput()
        }
    }

    private fun showWarningAndHidePasscodeView(remainedMinutes: Int) {
        val wrongPasscodeMessage = getString(R.string.errors_wrong_passcode)
        val retryMessage = resources.getQuantityString(
            R.plurals.errors_passcode_try_again,
            remainedMinutes,
            remainedMinutes
        )
        getUnlockAppInputView()?.let {
            it.showWarning("$wrongPasscodeMessage\n$retryMessage")
            it.setInputViewVisibility(show = true)
            it.setResetPasscodeViewVisibility(show = false)
        }
    }

    /**
     * Display biometric prompt if resultCallback is already set on Activity start
     */
    @TargetApi(Build.VERSION_CODES.P)
    private fun displayBiometricPrompt() {
        if (biometricPrompt?.resultCallback != null) {
            biometricPrompt?.showBiometricPrompt(
                context = this,
                titleResId = R.string.app_name,
                descriptionResId = R.string.fingerprint_scan_unlock,
                negativeActionTextResId = R.string.actions_cancel
            )
        }
    }

    private fun showOnboardingActivity() {
        finish()
        startActivity(Intent(this, OnboardingSetupActivity::class.java))
    }

    private fun showResetView() {
        getUnlockAppInputView()?.let {
            it.setInputViewVisibility(show = false)
            it.setResetPasscodeViewVisibility(show = true)
            it.showWarning()
        }
    }

    @Suppress("DEPRECATION")
    private fun successVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator?.vibrate(50)
    }
}
