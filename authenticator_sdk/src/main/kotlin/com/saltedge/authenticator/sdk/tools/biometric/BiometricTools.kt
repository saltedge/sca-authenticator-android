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

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import java.security.KeyPair
import javax.crypto.Cipher

const val FINGERPRINT_ALIAS_FOR_PIN = "fingerprint_alias_for_pin"

@Suppress("DEPRECATION")
class BiometricTools(
    val appContext: Context,
    val keyStoreManager: KeyStoreManagerAbs
) : BiometricToolsAbs {

    override fun replaceFingerprintKey(): KeyPair? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        return keyStoreManager.createOrReplaceRsaKeyPair(appContext, FINGERPRINT_ALIAS_FOR_PIN)
    }

    override fun activateFingerprint(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        keyStoreManager.createOrReplaceAesBiometricKey(FINGERPRINT_ALIAS_FOR_PIN)
        return keyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN)
    }

    @Throws(Exception::class)
    override fun isBiometricNotConfigured(context: Context): Boolean = !isBiometricReady(context)

    @Throws(Exception::class)
    override fun isBiometricSupported(context: Context) =
        getFingerprintState(context) !== BiometricState.NOT_SUPPORTED

    @Throws(Exception::class)
    override fun isBiometricReady(context: Context) =
        getFingerprintState(context) === BiometricState.READY

    @Throws(Exception::class)
    @SuppressLint("NewApi")
    override fun createFingerprintCipher(): Cipher? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        val key = keyStoreManager.getSecretKey(FINGERPRINT_ALIAS_FOR_PIN) ?: return null
        val mCipher = Cipher.getInstance("AES/CBC/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
        mCipher?.init(Cipher.ENCRYPT_MODE, key)
        return mCipher
    }

    @Throws(Exception::class)
    override fun isFingerprintAuthAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        return if (isFingerprintPermissionGranted(context)) {
            val manager = context.getFingerprintManager() ?: return false
            return manager.isHardwareDetected && manager.hasEnrolledFingerprints()
        } else false
    }

    @Throws(Exception::class)
    override fun getFingerprintState(context: Context): BiometricState {
        return when {
            context.isFingerprintHardwareNotAvailable() -> BiometricState.NOT_SUPPORTED
            context.inNotSecuredDevice() -> BiometricState.NOT_BLOCKED_DEVICE
            context.noEnrolledFingerprints() -> BiometricState.NOT_BLOCKED_DEVICE
            else -> BiometricState.READY
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isFingerprintPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.USE_FINGERPRINT
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Throws(Exception::class)
    private fun Context.isFingerprintHardwareNotAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return !(this.getFingerprintManager()?.isHardwareDetected ?: false)
    }

    @Throws(Exception::class)
    private fun Context.inNotSecuredDevice(): Boolean {
        return !(this.getKeyguardManager()?.isKeyguardSecure ?: false)
    }

    @Throws(Exception::class)
    private fun Context.noEnrolledFingerprints(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return !(this.getFingerprintManager()?.hasEnrolledFingerprints() ?: false)
    }

    private fun Context.getKeyguardManager(): KeyguardManager? {
        return this.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }
}

fun Context.getFingerprintManager(): FingerprintManager? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
    return this.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
}

/**
 * Check if in the application can use new (from SDK28) biometric prompt
 *
 * @return boolean, true if version sdk is greater than or equal to VERSION_CODES.P (SDK28)
 */
fun isBiometricPromptV28Enabled(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
}
