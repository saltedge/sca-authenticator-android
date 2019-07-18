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

import android.content.Context
import android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_CANCELED
import android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.core.os.CancellationSignal
import com.saltedge.authenticator.R
import com.saltedge.authenticator.tool.log
import com.saltedge.authenticator.tool.secure.fingerprint.BiometricTools

class BiometricsInputPresenter(val contract: BiometricsInputContract.View?) : FingerprintManagerCompat.AuthenticationCallback() {

    private var fingerprintManager: FingerprintManagerCompat? = null
    private val cryptoObject: FingerprintManagerCompat.CryptoObject? = initCryptoObject()
    private var mCancellationSignal: CancellationSignal? = null
    private var isDialogVisible = false

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
        super.onAuthenticationError(errMsgId, errString)
        if (biometricPromptIsNotCanceled(errMsgId)) onAuthResult(success = false)
    }

    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        onAuthResult(success = true)
    }

    /**
     * This method remains empty, because we get helpMsgId that lead to incorrect behavior of the application
     * for some phone models
     */
    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
        super.onAuthenticationHelp(helpMsgId, helpString)
        //Empty
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        onAuthResult(success = false)
    }

    fun onDialogResume(context: Context) {
        isDialogVisible = true
        try {
            if (BiometricTools.isFingerprintAuthAvailable(context)
                    && cryptoObject != null) {
                fingerprintManager = FingerprintManagerCompat.from(context)
                mCancellationSignal = CancellationSignal()
                fingerprintManager?.authenticate(cryptoObject, 0/* flags */,  mCancellationSignal, this, null)

            }
        } catch (e: SecurityException) {
            e.log()
        }
    }

    fun onDialogPause() {
        mCancellationSignal?.cancel()
        isDialogVisible = false
    }

    private fun onAuthResult(success: Boolean) {
        if (isDialogVisible) {
            val image = if (success) R.drawable.ic_fingerprint_confirmed else R.drawable.ic_fingerprint_error
            val colorResId = if (success) R.color.colorPrimary else R.color.red
            val text = if (success) R.string.fingerprint_confirmed else R.string.error_fingerprint_not_recognized
            contract?.updateStatusView(
                    imageResId = image,
                    textColorResId = colorResId,
                    textResId = text,
                    animateText = !success
            )
            if (success) contract?.sendAuthSuccessResult()
        }
    }

    private fun initCryptoObject(): FingerprintManagerCompat.CryptoObject? {
        return BiometricTools.initFingerprintCipher()?.let { FingerprintManagerCompat.CryptoObject(it) }
    }

    private fun biometricPromptIsNotCanceled(errorCode: Int): Boolean =
            errorCode != BIOMETRIC_ERROR_CANCELED && errorCode != BIOMETRIC_ERROR_USER_CANCELED
}