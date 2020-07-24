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
package com.saltedge.authenticator.sdk.tools.biometric

import android.annotation.SuppressLint
import android.content.Context
import javax.crypto.Cipher

/**
 * Abstraction of BiometricTools
 *
 * @see BiometricTools
 */
interface BiometricToolsAbs {
//    /**
//     * Create AES key for biometric Cipher
//     */
//    fun activateFingerprint(): Boolean

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
