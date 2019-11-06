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
package com.saltedge.authenticator.tool.secure

import com.saltedge.authenticator.model.repository.PreferenceRepository
import com.saltedge.authenticator.sdk.tools.crypt.CryptoTools
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManager
import javax.crypto.SecretKey

private const val PASSCODE_SECURE_KEY_ALIAS = "base_alias_for_pin"

object PasscodeTools : PasscodeToolsAbs {

    /**
     * Replace KeyPair, destined for Passcode encryption, with new in KeyStoreManager
     *
     * @return key pair
     */
    override fun replacePasscodeKey(): SecretKey? =
        KeyStoreManager.createOrReplaceAesKey(PASSCODE_SECURE_KEY_ALIAS)

    /**
     * Encrypts new passcode and save it in preferences
     *
     * @see PreferenceRepository.encryptedPasscode
     * @param passcode
     * @return boolean, true if PreferenceRepository.encryptedPasscode is equal encryptedPasscode
     */
    override fun savePasscode(passcode: String): Boolean {
        val key = KeyStoreManager.getSecretKey(PASSCODE_SECURE_KEY_ALIAS) ?: return false
        val encryptedPasscode = CryptoTools.aesEncrypt(passcode, key) ?: return false
        PreferenceRepository.encryptedPasscode = encryptedPasscode
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
        val key = KeyStoreManager.getSecretKey(PASSCODE_SECURE_KEY_ALIAS) ?: return ""
        return CryptoTools.aesDecrypt(encryptedPasscode, key) ?: return ""
    }
}
