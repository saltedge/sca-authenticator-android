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

import android.security.keystore.KeyProperties
import android.util.Base64
import com.saltedge.authenticator.sdk.model.AuthorizationData
import com.saltedge.authenticator.sdk.model.EncryptedAuthorizationData
import java.security.Key
import java.security.PrivateKey
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoTools : CryptoToolsAbs {

    private const val supportedAlgorithm = "AES-256-CBC"
    private const val AES_INTERNAL_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val AES_EXTERNAL_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private val encryptionIv = byteArrayOf(65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11)

    override fun rsaDecrypt(encryptedText: String, privateKey: PrivateKey): ByteArray? {
        try {
            if (encryptedText.isBlank()) return null
            val decryptCipher = getRsaCipher() ?: return null
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decodedText = decodeFromPemBase64String(encryptedText)
            return decryptCipher.doFinal(decodedText)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun aesEncrypt(input: String, key: Key): String? {
        try {
            val encryptCipher = Cipher.getInstance(AES_INTERNAL_TRANSFORMATION) ?: return null
            encryptCipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, encryptionIv))
            val encryptedBytes = encryptCipher.doFinal(input.toByteArray())
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun aesDecrypt(encryptedText: String, key: Key): String? {
        try {
            val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val encryptCipher = Cipher.getInstance(AES_INTERNAL_TRANSFORMATION) ?: return null
            encryptCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, encryptionIv))
            val decodedBytes = encryptCipher.doFinal(encryptedBytes)
            return String(decodedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun aesDecrypt(encryptedText: String, key: ByteArray, iv: ByteArray): String? {
        try {
            val decryptCipher = Cipher.getInstance(AES_EXTERNAL_TRANSFORMATION) ?: return null
            decryptCipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, KeyProperties.KEY_ALGORITHM_AES),
                IvParameterSpec(iv)
            )
            val decryptedBytes = decryptCipher.doFinal(decodeFromPemBase64String(encryptedText))
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun decryptAuthorizationData(
        encryptedData: EncryptedAuthorizationData,
        rsaPrivateKey: PrivateKey?
    ): AuthorizationData? {
        if (encryptedData.algorithm != supportedAlgorithm) return null
        try {
            val privateKey = rsaPrivateKey ?: return null
            val encryptedKey = encryptedData.key ?: return null
            val encryptedIV = encryptedData.iv ?: return null
            val encryptedMessage = encryptedData.data ?: return null
            val key = rsaDecrypt(encryptedKey, privateKey) ?: return null
            val iv = rsaDecrypt(encryptedIV, privateKey) ?: return null
            val jsonString = aesDecrypt(encryptedMessage, key = key, iv = iv)
            return createDefaultGson().fromJson(jsonString, AuthorizationData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getRsaCipher(): Cipher? {
        try {
            // AndroidOpenSSL causes error in android 6: InvalidKeyException: Need RSA private or public key (AndroidKeyStoreBCWorkaround)
            // AndroidKeyStoreBCWorkaround causes error in android 5: NoSuchProviderException: Provider not available (AndroidOpenSSL)
            return Cipher.getInstance("RSA/ECB/PKCS1Padding")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
