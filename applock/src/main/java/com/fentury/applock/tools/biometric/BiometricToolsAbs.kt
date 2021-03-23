/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools.biometric

import android.annotation.SuppressLint
import android.content.Context
import javax.crypto.Cipher

/**
 * Abstraction of BiometricTools
 *
 * @see BiometricTools
 */
interface BiometricToolsAbs {
    /**
     * Create AES key for biometric Cipher
     */
    fun activateFingerprint(): Boolean

    /**
     * Detect if biometrics is not configured ib system settings
     */
    @Throws(Exception::class)
    fun isBiometricNotConfigured(context: Context): Boolean

    /**
     * Detect if biometrics hardware is present
     */
    @Throws(Exception::class)
    fun isBiometricSupported(context: Context): Boolean

    /**
     * Detect if biometrics system is ready for recognition
     */
    @Throws(Exception::class)
    fun isFingerprintAuthAvailable(context: Context): Boolean

    /**
     * Detect if biometrics system is ready for operations
     */
    @Throws(Exception::class)
    fun isBiometricReady(context: Context): Boolean

    /**
     * Check Biometrics availability
     *
     * @return status, where
     *   NOT_SUPPORTED - Fingerprint Hardware Not Available
     *   NOT_BLOCKED_DEVICE - Device Not Secured by PIN
     *   NO_FINGERPRINTS - No Fingerprints enrolled
     *   READY - available
     */
    @Throws(Exception::class)
    fun getFingerprintState(context: Context): BiometricState

    /**
     * Create secure cipher for biometric operations
     */
    @Throws(Exception::class)
    @SuppressLint("NewApi")
    fun createFingerprintCipher(): Cipher?
}