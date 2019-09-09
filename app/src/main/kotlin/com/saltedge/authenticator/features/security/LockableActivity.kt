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

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.db.ConnectionsRepository
import com.saltedge.authenticator.model.repository.PreferenceRepository
import com.saltedge.authenticator.sdk.tools.KeyStoreManager
import com.saltedge.authenticator.tool.AppTools
import com.saltedge.authenticator.tool.restartApp
import com.saltedge.authenticator.tool.secure.PasscodeTools
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricTools
import com.saltedge.authenticator.tool.setVisible
import com.saltedge.authenticator.tool.showResetUserDialog
import com.saltedge.authenticator.widget.biometric.BiometricPromptAbs
import com.saltedge.authenticator.widget.biometric.BiometricPromptCallback
import com.saltedge.authenticator.widget.biometric.BiometricPromptManagerV28
import com.saltedge.authenticator.widget.biometric.BiometricsInputDialog
import com.saltedge.authenticator.widget.passcode.PasscodeInputView
import com.saltedge.authenticator.widget.passcode.PasscodeInputViewListener

const val KEY_SKIP_PIN = "KEY_SKIP_PIN"

@SuppressLint("Registered")
abstract class LockableActivity :
    AppCompatActivity(),
    PasscodeInputViewListener,
    BiometricPromptCallback {

    abstract fun getUnlockAppInputView(): UnlockAppInputView?
    abstract fun getAppBarLayout(): View?

    private val viewContract = object : LockableActivityContract {

        override fun unBlockInput() {
            enablePasscodeInput()
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
        passcodeTools = PasscodeTools
    )
    private var biometricPrompt: BiometricPromptAbs? = null
    private var vibrator: Vibrator? = null
    private var biometricTools = BiometricTools(KeyStoreManager, PreferenceRepository)

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
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
        getUnlockAppInputView()?.listener = this
        presenter.onActivityStart(intent)
    }

    override fun onStop() {
        biometricPrompt?.resultCallback = null
        getUnlockAppInputView()?.listener = null
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

    override fun onNewPasscodeEntered(mode: PasscodeInputView.InputMode, passcode: String) {
        // REDUNDANT
    }

    override fun onNewPasscodeConfirmed(passcode: String) {
    }

    override fun biometricAuthFinished() {
        presenter.onSuccessAuthentication()
    }

    override fun biometricsCanceledByUser() {
    }

    fun restartLockableActivity() {
        startActivity(Intent(this, this.javaClass).apply { putExtra(KEY_SKIP_PIN, true) })
        finish()
    }

    private fun isBiometricInputReady(): Boolean = biometricTools.isBiometricReady(context = this)

    @TargetApi(Build.VERSION_CODES.P)
    private fun displayBiometricPrompt() {
        biometricPrompt = if (AppTools.isBiometricPromptV28Enabled()) {
            BiometricPromptManagerV28()
        } else {
            BiometricsInputDialog()
        }
        biometricPrompt?.resultCallback = this
        biometricPrompt?.showBiometricPrompt(
            context = this,
            titleResId = R.string.app_name,
            descriptionResId = R.string.fingerprint_scan_unlock,
            negativeActionTextResId = R.string.actions_cancel
        )
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
        }
    }

    private fun enablePasscodeInput() {
        getAppBarLayout()?.setVisible(show = true)
        getUnlockAppInputView()?.biometricsActionIsAvailable = isBiometricInputReady()
    }

    private fun unlockScreen() {
        getUnlockAppInputView()?.setVisible(show = false)
        getAppBarLayout()?.setVisible(show = true)
    }

    private fun setupViewsAndLockScreen() {
        getUnlockAppInputView()?.let {
            it.biometricsActionIsAvailable = isBiometricInputReady()
            it.setSavedPasscode(presenter.savedPasscode)
            it.setVisible(show = true)
        }
        getAppBarLayout()?.setVisible(show = false)
    }

    fun resetCurrentUser() {
        clearPasscodeAndShowError(R.string.errors_wrong_passcode)
        showResetUserDialog(DialogInterface.OnClickListener { _, _ -> this.restartApp() })
    }

    private fun clearPasscodeAndShowError(@StringRes messageResId: Int) {
        errorVibrate()
        showPasscodeErrorMessage(messageResId)
    }

    private fun showPasscodeErrorMessage(@StringRes messageResId: Int) {
        getUnlockAppInputView()?.setDescriptionText(messageResId)
    }

    @Suppress("DEPRECATION")
    private fun successVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator?.vibrate(150)
    }

    @Suppress("DEPRECATION")
    private fun errorVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
        } else vibrator?.vibrate(longArrayOf(0, 100, 100, 100), -1)
    }
}
