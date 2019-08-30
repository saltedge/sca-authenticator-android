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
import com.saltedge.authenticator.R
import com.saltedge.authenticator.model.repository.PreferenceRepositoryAbs
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.tool.log
import javax.crypto.Cipher

const val FINGERPRINT_ALIAS_FOR_PIN = "fingerprint_alias_for_pin"

@Suppress("DEPRECATION")
class BiometricTools(
    val keyStoreManager: KeyStoreManagerAbs,
    val preferenceRepository: PreferenceRepositoryAbs
) : BiometricToolsAbs {

    override fun replaceFingerprintKey() =
        keyStoreManager.createOrReplaceRsaKeyPair(FINGERPRINT_ALIAS_FOR_PIN)

    override fun activateFingerprint(): Boolean {
        keyStoreManager.createOrReplaceAesBiometricKey(FINGERPRINT_ALIAS_FOR_PIN)
        return if (keyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN)) {
            preferenceRepository.fingerprintEnabled = true
            true
        } else false
    }

    override fun isFingerprintNotConfigured(context: Context): Boolean = !isBiometricReady(context)

    override fun isFingerprintSupported(context: Context) =
        context.getFingerprintState() !== FingerprintState.NOT_SUPPORTED

    override fun isBiometricReady(context: Context) =
        context.getFingerprintState() === FingerprintState.READY

    override fun getCurrentFingerprintStateWarningMessage(context: Context): String? {
        return context.getString(
            when (context.getFingerprintState()) {
                FingerprintState.NOT_SUPPORTED -> R.string.errors_touch_id_not_supported
                FingerprintState.NOT_BLOCKED_DEVICE -> R.string.errors_activate_touch_id
                FingerprintState.NO_FINGERPRINTS -> R.string.errors_touch_id_not_enrolled
                else -> return null
            }
        )
    }

    @SuppressLint("NewApi")
    override fun createFingerprintCipher(): Cipher? {
        try {
            val key = keyStoreManager.getSecretKey(FINGERPRINT_ALIAS_FOR_PIN) ?: return null
            val mCipher = Cipher.getInstance("AES/CBC/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
            mCipher?.init(Cipher.ENCRYPT_MODE, key)
            return mCipher
        } catch (e: Exception) {
            e.log()
        }
        return null
    }

    override fun isFingerprintAuthAvailable(context: Context): Boolean {
        try {
            if (isFingerprintPermissionGranted(context)) {
                val manager = context.getFingerprintManager() ?: return false
                return manager.isHardwareDetected && manager.hasEnrolledFingerprints()
            }
        } catch (e: Exception) {
            e.log()
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isFingerprintPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.USE_FINGERPRINT
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Context.getFingerprintState(): FingerprintState {
        return when {
            this.isFingerprintHardwareNotAvailable() -> FingerprintState.NOT_SUPPORTED
            this.inNotSecuredDevice() -> FingerprintState.NOT_BLOCKED_DEVICE
            this.noEnrolledFingerprints() -> FingerprintState.NOT_BLOCKED_DEVICE
            else -> FingerprintState.READY
        }
    }

    private fun Context.isFingerprintHardwareNotAvailable(): Boolean {
        return !try {
            this.getFingerprintManager()?.isHardwareDetected ?: false
        } catch (e: Exception) {
            e.log()
            false
        }
    }

    private fun Context.inNotSecuredDevice(): Boolean {
        return !try {
            this.getKeyguardManager()?.isKeyguardSecure ?: false
        } catch (e: Exception) {
            e.log()
            false
        }
    }

    private fun Context.noEnrolledFingerprints(): Boolean {
        return !try {
            this.getFingerprintManager()?.hasEnrolledFingerprints() ?: false
        } catch (e: Exception) {
            e.log()
            false
        }
    }

    private fun Context.getKeyguardManager(): KeyguardManager? {
        return this.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }
}

fun Context.getFingerprintManager(): FingerprintManager? {
    return this.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
}
