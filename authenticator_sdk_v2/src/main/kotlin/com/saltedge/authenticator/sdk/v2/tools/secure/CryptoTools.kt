/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-authenticator-android).
 * Copyright (c) 2021 Salt Edge Inc.
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
package com.saltedge.authenticator.sdk.v2.tools.secure

import android.util.Base64
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.tools.decodeFromPemBase64String
import com.saltedge.authenticator.sdk.v2.tools.json.createDefaultGson
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoTools : CryptoToolsAbs {

//    private const val AES_INTERNAL_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val AES_EXTERNAL_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val RSA_ECB = "RSA/ECB/PKCS1Padding"
    private val passcodeEncryptionIv = byteArrayOf(65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11)

    override fun rsaEncrypt(input: String, publicKey: PublicKey): String? {
        return try {
            val encryptCipher = rsaCipherInstance()
            if (encryptCipher == null || input.isBlank()) return null
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val encryptedBytes = encryptCipher.doFinal(input.toByteArray())
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun rsaDecrypt(encryptedText: String, privateKey: PrivateKey): ByteArray? {
        return try {
            val decryptCipher = rsaCipherInstance()
            if (decryptCipher == null || encryptedText.isBlank()) return null
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decodedText = decodeFromPemBase64String(encryptedText)
            decryptCipher.doFinal(decodedText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun aesEncrypt(data: String, key: SecretKey): String {
        try {
            val aesKey: ByteArray = key.encoded
            val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
            val aesKeyHash: ByteArray = digest.digest(aesKey)
            val keyBytes: ByteArray = key.encoded
            val ivBytes: ByteArray = aesKeyHash.copyOfRange(0, 16)
            val encryptedBytes: ByteArray = aesEncrypt(data, keyBytes, ivBytes) ?: return ""
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    @Throws(java.lang.Exception::class)
    fun aesEncrypt(data: String, key: ByteArray, iv: ByteArray): ByteArray? {
        val cipher = Cipher.getInstance(AES_EXTERNAL_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, KeyAlgorithm.AES), IvParameterSpec(iv))
        return cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    @Throws(java.lang.Exception::class)
    override fun aesDecrypt(encryptedText: String, key: ByteArray, iv: ByteArray): String? {
        return try {
            val decryptCipher = Cipher.getInstance(AES_EXTERNAL_TRANSFORMATION) ?: return null
            decryptCipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, KeyAlgorithm.AES),
                IvParameterSpec(iv)
            )
            val decryptedBytes = decryptCipher.doFinal(decodeFromPemBase64String(encryptedText))
            String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun decryptAuthorizationData(
        encryptedData: AuthorizationResponseData,
        rsaPrivateKey: PrivateKey?
    ): AuthorizationData? {
        return try {
            val privateKey = rsaPrivateKey ?: return null
            val encryptedKey = encryptedData.key
            val encryptedIV = encryptedData.iv
            val encryptedMessage = encryptedData.data
            val key = rsaDecrypt(encryptedKey, privateKey) ?: return null
            val iv = rsaDecrypt(encryptedIV, privateKey) ?: return null
            val jsonString = aesDecrypt(encryptedMessage, key = key, iv = iv)
            createDefaultGson().fromJson(jsonString, AuthorizationData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rsaCipherInstance(): Cipher? {
        return try {
            // AndroidOpenSSL causes error in android 6: InvalidKeyException: Need RSA private or public key (AndroidKeyStoreBCWorkaround)
            // AndroidKeyStoreBCWorkaround causes error in android 5: NoSuchProviderException: Provider not available (AndroidOpenSSL)
            Cipher.getInstance(RSA_ECB)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

interface CryptoToolsAbs {
    fun rsaEncrypt(input: String, publicKey: PublicKey): String?
    fun rsaDecrypt(encryptedText: String, privateKey: PrivateKey): ByteArray?
    fun aesDecrypt(encryptedText: String, key: ByteArray, iv: ByteArray): String?
    fun decryptAuthorizationData(
        encryptedData: AuthorizationResponseData,
        rsaPrivateKey: PrivateKey?
    ): AuthorizationData?
}
