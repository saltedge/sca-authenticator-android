/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
@file:Suppress("DEPRECATION")

package com.fentury.applock.widget.biometric

import android.annotation.TargetApi
import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import androidx.annotation.StringRes
import com.fentury.applock.R
import com.fentury.applock.root.Constants.Companion.KEY_TITLE
import com.fentury.applock.tools.ResId
import com.fentury.applock.tools.biometric.BiometricToolsAbs
import com.fentury.applock.tools.biometric.getFingerprintManager

const val KEY_DESCRIPTION = "description"
const val KEY_ACTION = "action"

@TargetApi(Build.VERSION_CODES.M)
internal class BiometricsInputPresenter(
    val biometricTools: BiometricToolsAbs,
    val contract: BiometricsInputContract.View?,
    val context: Context?
) : FingerprintManager.AuthenticationCallback() {

    companion object {
        fun dataBundle(
            title: String,
            @StringRes descriptionResId: ResId,
            @StringRes negativeActionTextResId: ResId
        ): Bundle {
            return Bundle().apply {
                putString(KEY_TITLE, title)
                putInt(KEY_DESCRIPTION, descriptionResId)
                putInt(KEY_ACTION, negativeActionTextResId)
            }
        }
    }

    var titleRes: String = context?.getString(R.string.fingerprint_title) ?: ""
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
        titleRes = if (cryptoObject == null) context?.getString(R.string.errors_error) ?: ""
        else arguments?.getString(KEY_TITLE, context?.getString(R.string.fingerprint_title)) ?: ""

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
        } catch (e: Exception) { }
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
            null
        }
    }

    private fun biometricPromptIsNotCanceled(errorCode: Int): Boolean =
        errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED && errorCode != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED
}