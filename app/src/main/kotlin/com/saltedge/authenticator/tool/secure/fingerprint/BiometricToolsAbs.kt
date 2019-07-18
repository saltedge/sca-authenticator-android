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
package com.saltedge.authenticator.tool.secure.fingerprint

import android.annotation.SuppressLint
import android.content.Context
import java.security.KeyPair
import javax.crypto.Cipher

/**
 * Abstraction of BiometricTools
 *
 * @see BiometricTools
 */
interface BiometricToolsAbs {
    fun replaceFingerprintKey(): KeyPair?
    fun activateFingerprint(): Boolean
    fun isFingerprintNotConfigured(context: Context): Boolean
    fun isFingerprintSupported(context: Context): Boolean
    fun isBiometricReady(context: Context): Boolean
    fun getCurrentFingerprintStateWarningMessage(context: Context): String?
    @SuppressLint("NewApi") fun initFingerprintCipher(): Cipher?
}
