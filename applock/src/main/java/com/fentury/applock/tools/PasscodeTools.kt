/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools

import android.content.Context
import android.os.Build
import com.fentury.applock.repository.PreferencesRepository
import com.fentury.applock.tools.crypt.CryptoTools
import com.fentury.applock.tools.keystore.KeyStoreManager

private const val PASSCODE_SECURE_KEY_ALIAS = "base_alias_for_pin"

object PasscodeTools : PasscodeToolsAbs {

    /**
     * Replace KeyPair, destined for Passcode encryption, with new in KeyStoreManager
     */
    override fun replacePasscodeKey(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            KeyStoreManager.createOrReplaceRsaKeyPair(context, PASSCODE_SECURE_KEY_ALIAS)
        } else {
            KeyStoreManager.createOrReplaceAesKey(PASSCODE_SECURE_KEY_ALIAS)
        }
    }

    /**
     * Encrypts new passcode and save it in preferences
     *
     * @see PreferenceRepository.encryptedPasscode
     * @param passcode
     * @return boolean, true if PreferenceRepository.encryptedPasscode is equal encryptedPasscode
     */
    override fun savePasscode(passcode: String): Boolean {
        val encryptedPasscode = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            KeyStoreManager.getKeyPair(PASSCODE_SECURE_KEY_ALIAS)?.public?.let { key ->
                CryptoTools.rsaEncrypt(input = passcode, publicKey = key)
            }
        } else {
            KeyStoreManager.getSecretKey(PASSCODE_SECURE_KEY_ALIAS)?.let { key ->
                CryptoTools.aesEncrypt(input = passcode, key = key)
            }
        }
        PreferencesRepository.encryptedPasscode = encryptedPasscode ?: return false
        return true
    }

    /**
     * Read passcode from preferences and decrypt it
     *
     * @return decrypted passcode
     * @see PreferenceRepository.encryptedPasscode
     */
    override fun getPasscode(): String {
        val encryptedPasscode = PreferencesRepository.encryptedPasscode
        if (encryptedPasscode.isBlank()) return ""
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            KeyStoreManager.getKeyPair(PASSCODE_SECURE_KEY_ALIAS)?.private?.let { key ->
                String(CryptoTools.rsaDecrypt(encryptedPasscode, key) ?: byteArrayOf())
            }
        } else {
            KeyStoreManager.getSecretKey(PASSCODE_SECURE_KEY_ALIAS)?.let { key ->
                CryptoTools.aesDecrypt(encryptedPasscode, key)
            }
        } ?: return ""
    }
}
