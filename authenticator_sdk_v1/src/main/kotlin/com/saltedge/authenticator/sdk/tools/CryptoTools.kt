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
package com.saltedge.authenticator.sdk.tools

import com.saltedge.authenticator.core.tools.json.createDefaultGson
import com.saltedge.authenticator.core.tools.secure.BaseCryptoTools
import com.saltedge.authenticator.core.tools.secure.BaseCryptoToolsAbs
import com.saltedge.authenticator.core.tools.secure.SUPPORTED_AES_ALGORITHM
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.EncryptedData
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import java.security.PrivateKey

object CryptoTools : BaseCryptoTools(), CryptoToolsAbs {

    override fun decryptAuthorizationData(
        encryptedData: EncryptedData,
        rsaPrivateKey: PrivateKey?
    ): AuthorizationData? {
        if (encryptedData.algorithm != SUPPORTED_AES_ALGORITHM) return null
        return try {
            val privateKey = rsaPrivateKey ?: return null
            val encryptedKey = encryptedData.key ?: return null
            val encryptedIV = encryptedData.iv ?: return null
            val encryptedMessage = encryptedData.data ?: return null
            val key = rsaDecrypt(encryptedKey, privateKey) ?: return null
            val iv = rsaDecrypt(encryptedIV, privateKey) ?: return null
            val jsonString = aesDecrypt(encryptedMessage, key = key, iv = iv)
            createDefaultGson().fromJson(jsonString, AuthorizationData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun decryptConsentData(
        encryptedData: EncryptedData,
        rsaPrivateKey: PrivateKey?
    ): ConsentData? {
        if (encryptedData.algorithm != SUPPORTED_AES_ALGORITHM) return null
        return try {
            val privateKey = rsaPrivateKey ?: return null
            val encryptedKey = encryptedData.key ?: return null
            val encryptedIV = encryptedData.iv ?: return null
            val encryptedMessage = encryptedData.data ?: return null
            val key = rsaDecrypt(encryptedKey, privateKey) ?: return null
            val iv = rsaDecrypt(encryptedIV, privateKey) ?: return null
            val jsonString = aesDecrypt(encryptedMessage, key = key, iv = iv)
            createDefaultGson().fromJson(jsonString, ConsentData::class.java).apply {
                this.connectionId = encryptedData.connectionId
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

interface CryptoToolsAbs : BaseCryptoToolsAbs {
    fun decryptAuthorizationData(encryptedData: EncryptedData, rsaPrivateKey: PrivateKey?): AuthorizationData?
    fun decryptConsentData(encryptedData: EncryptedData, rsaPrivateKey: PrivateKey?): ConsentData?
}
