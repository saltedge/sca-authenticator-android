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
@file:Suppress("DEPRECATION")

package com.saltedge.authenticator.widget.biometric

import android.annotation.TargetApi
import android.content.Context
import android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_CANCELED
import android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import androidx.annotation.StringRes
import com.saltedge.authenticator.R
import com.saltedge.authenticator.app.KEY_ACTION
import com.saltedge.authenticator.sdk.constants.KEY_DESCRIPTION
import com.saltedge.authenticator.sdk.constants.KEY_TITLE
import com.saltedge.authenticator.sdk.tools.biometric.BiometricToolsAbs
import com.saltedge.authenticator.sdk.tools.biometric.getFingerprintManager
import com.saltedge.authenticator.tools.ResId
import com.saltedge.authenticator.tools.log

@TargetApi(Build.VERSION_CODES.M)
class BiometricsInputPresenter(
    val biometricTools: BiometricToolsAbs,
    val contract: BiometricsInputContract.View?
) : FingerprintManager.AuthenticationCallback() {

    companion object {
        fun dataBundle(
            @StringRes titleResId: ResId,
            @StringRes descriptionResId: ResId,
            @StringRes negativeActionTextResId: ResId
        ): Bundle {
            return Bundle().apply {
                putInt(KEY_TITLE, titleResId)
                putInt(KEY_DESCRIPTION, descriptionResId)
                putInt(KEY_ACTION, negativeActionTextResId)
            }
        }
    }

    var titleRes: ResId = R.string.fingerprint_title
        private set
    var descriptionRes: ResId = R.string.fingerprint_touch_sensor
        private set
    var negativeActionTextRes: ResId = R.string.actions_cancel
        private set
    private var fingerprintManager: FingerprintManager? = null
    private val cryptoObject: FingerprintManager.CryptoObject? = createCryptoObject()
    private var mCancellationSignal: CancellationSignal? = null
    private var isDialogVisible = false

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
        super.onAuthenticationError(errMsgId, errString)
        if (biometricPromptIsNotCanceled(errMsgId)) onAuthResult(success = false)
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        onAuthResult(success = true)
    }

    /**
     * This method remains empty, because we get helpMsgId that lead to incorrect behavior of the application
     * for some phone models
     */
    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
        super.onAuthenticationHelp(helpMsgId, helpString)
        // Empty
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        onAuthResult(success = false)
    }

    val initialized: Boolean = cryptoObject != null

    fun setInitialData(arguments: Bundle?) {
        titleRes = if (cryptoObject == null) R.string.errors_error
        else arguments?.getInt(KEY_TITLE, R.string.fingerprint_title) ?: R.string.fingerprint_title

        descriptionRes = if (cryptoObject == null) R.string.errors_fingerprint_init
        else arguments?.getInt(KEY_DESCRIPTION, R.string.fingerprint_touch_sensor) ?: R.string.fingerprint_touch_sensor

        negativeActionTextRes = arguments?.getInt(KEY_ACTION, R.string.actions_cancel) ?: R.string.actions_cancel
    }

    fun onDialogResume(context: Context) {
        isDialogVisible = true
        try {
            if (biometricTools.isFingerprintAuthAvailable(context) && cryptoObject != null) {
                fingerprintManager = context.getFingerprintManager()
                mCancellationSignal = CancellationSignal()
                fingerprintManager?.authenticate(
                    cryptoObject,
                    mCancellationSignal,
                    0/* flags */,
                    this,
                    null
                )
            }
        } catch (e: Exception) {
            e.log()
        }
    }

    fun onDialogPause() {
        mCancellationSignal?.cancel()
        mCancellationSignal = null
        isDialogVisible = false
    }

    private fun onAuthResult(success: Boolean) {
        if (isDialogVisible) {
            val image = if (success) {
                R.drawable.ic_fingerprint_confirmed
            } else {
                R.drawable.ic_fingerprint_error
            }
            val colorResId = if (success) R.color.primary else R.color.red
            val text = if (success) {
                R.string.fingerprint_confirmed
            } else {
                R.string.errors_fingerprint_not_recognized
            }
            contract?.updateStatusView(
                imageResId = image,
                textColorResId = colorResId,
                textResId = text,
                animateText = !success
            )
            if (success) contract?.sendAuthSuccessResult()
        }
    }

    private fun createCryptoObject(): FingerprintManager.CryptoObject? {
        return try {
            biometricTools.createFingerprintCipher()?.let { FingerprintManager.CryptoObject(it) }
        } catch (e: Exception) {
            e.log()
            null
        }
    }

    private fun biometricPromptIsNotCanceled(errorCode: Int): Boolean =
        errorCode != BIOMETRIC_ERROR_CANCELED && errorCode != BIOMETRIC_ERROR_USER_CANCELED
}
