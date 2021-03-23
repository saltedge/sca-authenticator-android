/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools.biometric

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
import com.fentury.applock.tools.keystore.KeyStoreManager
import javax.crypto.Cipher

const val FINGERPRINT_ALIAS_FOR_PIN = "fingerprint_alias_for_pin"

@Suppress("DEPRECATION")
object BiometricTools: BiometricToolsAbs {

    @Throws(Exception::class)
    override fun activateFingerprint(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        KeyStoreManager.createOrReplaceAesBiometricKey(FINGERPRINT_ALIAS_FOR_PIN)
        return KeyStoreManager.keyEntryExist(FINGERPRINT_ALIAS_FOR_PIN)
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
        val key = KeyStoreManager.getSecretKey(FINGERPRINT_ALIAS_FOR_PIN)
            ?: throw Exception("Secret key not found in keystore")
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
            context.noEnrolledFingerprints() -> BiometricState.NO_FINGERPRINTS
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

    /**
     * Determine if fingerprint hardware is present and functional.
     */
    @Throws(Exception::class)
    private fun Context.isFingerprintHardwareNotAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return !(this.getFingerprintManager()?.isHardwareDetected ?: false)
    }

    /**
     * Determine if keyguard is secured by a PIN, pattern or password or a SIM card
     * is currently locked.
     */
    @Throws(Exception::class)
    private fun Context.inNotSecuredDevice(): Boolean {
        return !(this.getKeyguardManager()?.isKeyguardSecure ?: false)
    }

    /**
     * Determine if there is at least one fingerprint enrolled.
     */
    @Throws(Exception::class)
    private fun Context.noEnrolledFingerprints(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return !(this.getFingerprintManager()?.hasEnrolledFingerprints() ?: false)
    }

    /**
     * Return KeyguardManager system service
     */
    private fun Context.getKeyguardManager(): KeyguardManager? {
        return this.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    }
}

/**
 * Return FingerprintManager system service
 */
fun Context.getFingerprintManager(): FingerprintManager? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
    return this.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
}