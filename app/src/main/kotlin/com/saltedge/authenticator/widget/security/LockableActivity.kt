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
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.saltedge.authenticator.R
import com.saltedge.authenticator.features.main.buildWarning
import com.saltedge.authenticator.features.onboarding.OnboardingSetupActivity
import com.saltedge.authenticator.models.repository.ConnectionsRepository
import com.saltedge.authenticator.models.repository.PreferenceRepository
import com.saltedge.authenticator.sdk.AuthenticatorApiManager
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManager
import com.saltedge.authenticator.tools.*
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptCallback
import com.saltedge.authenticator.widget.passcode.PasscodeEditView
import com.saltedge.authenticator.widget.passcode.PasscodeInputViewListener

const val KEY_SKIP_PIN = "KEY_SKIP_PIN"

@SuppressLint("Registered")
abstract class LockableActivity : AppCompatActivity(),
    PasscodeInputViewListener,
    BiometricPromptCallback,
    DialogInterface.OnClickListener {

    abstract fun getUnlockAppInputView(): UnlockAppInputView?
    abstract fun getAppBarLayout(): View?
    var snackbar: Snackbar? = null

    private val viewContract: LockableActivityContract = object : LockableActivityContract {

        override fun unBlockInput() {
            enablePasscodeInput()
        }

        override fun showLockWarning() {
            showLockWarningView()
        }

        override fun dismissSnackbar() {
            dismissLockWarningView()
        }

        override fun resetUser() {
            resetCurrentUser()
        }

        override fun clearOutputAndShowErrorWarning(errorTextResId: Int) {
            clearPasscodeAndShowError(errorTextResId)
        }

        override fun vibrateAboutSuccess() {
            successVibrate()
        }

        override fun closeLockView() {
            unlockScreen()
        }

        override fun displayBiometricPromptView() {
            displayBiometricPrompt()
        }

        override fun isBiometricReady(): Boolean = isBiometricInputReady()

        override fun lockScreen() {
            setupViewsAndLockScreen()
        }

        override fun disableUnlockInput(inputAttempt: Int, remainedMinutes: Int) {
            showWrongPasscodeErrorAndDisablePasscodeInput(remainedMinutes = remainedMinutes)
        }
    }

    private var presenter = LockableActivityPresenter(
        viewContract = viewContract,
        connectionsRepository = ConnectionsRepository,
        preferenceRepository = PreferenceRepository,
        passcodeTools = PasscodeTools,
        keyStoreManager = KeyStoreManager,
        apiManager = AuthenticatorApiManager
    )

    private var biometricTools: BiometricToolsAbs? = null
    private var biometricPrompt: BiometricPromptAbs? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authenticatorApp?.appComponent?.let {
            biometricTools = authenticatorApp?.appComponent?.biometricTools()
            biometricPrompt = authenticatorApp?.appComponent?.biometricPrompt()
        }
        presenter.onActivityCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult()
    }

    override fun onStart() {
        super.onStart()
        biometricPrompt?.resultCallback = this
        getUnlockAppInputView()?.passcodeInputViewListener = this
        presenter.onActivityStart(intent)
    }

    override fun onStop() {
        presenter.destroyTimer()
        biometricPrompt?.resultCallback = null
        getUnlockAppInputView()?.passcodeInputViewListener = null
        super.onStop()
    }

    override fun onBiometricInputSelected() {
        displayBiometricPrompt()
    }

    override fun onPasscodeInputCanceledByUser() {
        // REDUNDANT
    }

    override fun onEnteredPasscodeIsValid() {
        presenter.onSuccessAuthentication()
    }

    override fun onEnteredPasscodeIsInvalid() {
        presenter.onWrongPasscodeInput()
    }

    override fun onNewPasscodeEntered(mode: PasscodeEditView.InputMode, passcode: String) {
        // REDUNDANT
    }

    override fun onNewPasscodeConfirmed(passcode: String) {}

    override fun onForgotActionSelected() {
        getUnlockAppInputView()?.let {
            it.setInputViewVisibility(show = false)
            it.showErrorMessage(show = false)
        }
    }

    override fun onClearDataActionSelected() {
        showResetDataDialog(listener = this)
    }

    override fun onClick(listener: DialogInterface?, dialogActionId: Int) {
        when (dialogActionId) {
            DialogInterface.BUTTON_POSITIVE -> showOnboardingActivity()
            DialogInterface.BUTTON_NEGATIVE -> listener?.dismiss()
        }
    }

    override fun biometricAuthFinished() {
        presenter.onSuccessAuthentication()
    }

    override fun biometricsCanceledByUser() {}

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN
            && getUnlockAppInputView()?.visibility != View.VISIBLE) {
            presenter.restartLockTimer()
        }
        return super.dispatchTouchEvent(ev)
    }

    fun resetCurrentUser() {
        clearPasscodeAndShowError(R.string.errors_wrong_passcode)
        showResetUserDialog(DialogInterface.OnClickListener { _, _ -> this.restartApp() })
    }

    fun restartLockableActivity() {
        startActivity(Intent(this, this.javaClass).apply { putExtra(KEY_SKIP_PIN, true) })
        finish()
    }

    private fun isBiometricInputReady(): Boolean = biometricTools?.isBiometricReady(context = this) == true

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

    private fun showWrongPasscodeErrorAndDisablePasscodeInput(remainedMinutes: Int) {
        val wrongPasscodeMessage = getString(R.string.errors_wrong_passcode)
        val retryMessage = resources.getQuantityString(
            R.plurals.errors_passcode_try_again,
            remainedMinutes,
            remainedMinutes
        )
        getUnlockAppInputView()?.let {
            it.setDescriptionText("$wrongPasscodeMessage\n$retryMessage")
            it.setInputViewVisibility(show = false)
            it.setResetPasscodeViewVisibility(show = false)
            it.showErrorMessage(show = true)
        }
    }

    private fun showLockWarningView() {
        snackbar = this@LockableActivity.buildWarning(
            getString(R.string.warning_application_was_locked),
            snackBarDuration = 5000,
            actionResId = R.string.actions_cancel
        )
        snackbar?.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (event == DISMISS_EVENT_TIMEOUT) presenter.onSnackbarDismissed()
            }
        })
        snackbar?.show()
    }

    private fun dismissLockWarningView() {
        snackbar?.dismiss()
        snackbar = null
    }

    private fun enablePasscodeInput() {
        getUnlockAppInputView()?.let {
            it.biometricsActionIsAvailable = isBiometricInputReady()
            it.setInputViewVisibility(show = true)
            it.showErrorMessage(show = false)
        }
    }

    private fun unlockScreen() {
        getUnlockAppInputView()?.setVisible(show = false)
        getAppBarLayout()?.setVisible(show = true)
        presenter.restartLockTimer()
    }

    private fun setupViewsAndLockScreen() {
        getUnlockAppInputView()?.let {
            it.biometricsActionIsAvailable = isBiometricInputReady()
            it.setSavedPasscode(presenter.savedPasscode)
            it.setVisible(show = true)
        }
        getAppBarLayout()?.setVisible(show = false)
    }

    private fun clearPasscodeAndShowError(@StringRes messageResId: Int) {
        errorVibrate()
        showPasscodeErrorMessage(messageResId)
    }

    private fun showPasscodeErrorMessage(@StringRes messageResId: Int) {
        getUnlockAppInputView()?.setDescriptionText(messageResId)
    }

    private fun showOnboardingActivity() {
        presenter.clearAppData()
        finish()
        startActivity(Intent(this, OnboardingSetupActivity::class.java))
    }

    @Suppress("DEPRECATION")
    private fun successVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator?.vibrate(50)
    }

    @Suppress("DEPRECATION")
    private fun errorVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
        } else vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
    }
}
