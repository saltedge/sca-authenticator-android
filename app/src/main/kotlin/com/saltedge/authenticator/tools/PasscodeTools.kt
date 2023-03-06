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
package com.saltedge.authenticator.tools

import android.content.Context
import com.saltedge.authenticator.app.buildVersionLessThan23
import com.saltedge.authenticator.core.tools.secure.KeyManager
import com.saltedge.authenticator.models.repository.PreferenceRepository
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2

private const val PASSCODE_SECURE_KEY_ALIAS = "base_alias_for_pin"

interface PasscodeToolsAbs {
    fun replacePasscodeKey(context: Context)
    fun savePasscode(passcode: String): Boolean
    fun getPasscode(): String
}

object PasscodeTools : PasscodeToolsAbs {

    /**
     * Replace KeyPair, destined for Passcode encryption, with new in KeyStoreManager
     */
    override fun replacePasscodeKey(context: Context) {
        if (buildVersionLessThan23) {
            KeyManager.createOrReplaceRsaKeyPair(context, PASSCODE_SECURE_KEY_ALIAS)
        } else {
            KeyManager.createOrReplaceAesKey(PASSCODE_SECURE_KEY_ALIAS)
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
        val encryptedPasscode = if (buildVersionLessThan23) {
            KeyManager.getKeyPair(PASSCODE_SECURE_KEY_ALIAS)?.public?.let { key ->
                CryptoToolsV2.rsaEncrypt(inputText = passcode, publicKey = key)
            }
        } else {
            KeyManager.getSecretKey(PASSCODE_SECURE_KEY_ALIAS)?.let { key ->
                CryptoToolsV2.aesGcmEncrypt(input = passcode, key = key)
            }
        }
        PreferenceRepository.encryptedPasscode = encryptedPasscode ?: return false
        return true
    }

    /**
     * Read passcode from preferences and decrypt it
     *
     * @return decrypted passcode
     * @see PreferenceRepository.encryptedPasscode
     */
    override fun getPasscode(): String {
        val encryptedPasscode = PreferenceRepository.encryptedPasscode
        if (encryptedPasscode.isBlank()) return ""
        return if (buildVersionLessThan23) {
            KeyManager.getKeyPair(PASSCODE_SECURE_KEY_ALIAS)?.private?.let { key ->
                String(CryptoToolsV2.rsaDecrypt(encryptedPasscode, key) ?: byteArrayOf())
            }
        } else {
            KeyManager.getSecretKey(PASSCODE_SECURE_KEY_ALIAS)?.let { key ->
                CryptoToolsV2.aesGcmDecrypt(encryptedPasscode, key)
            }
        } ?: return ""
    }
}
