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
package com.saltedge.authenticator.core.tools.secure

import android.util.Base64
import com.saltedge.authenticator.core.tools.decodeFromPemBase64String
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object BaseCryptoTools : BaseCryptoToolsAbs {

    private const val AES_INTERNAL_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val AES_EXTERNAL_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    const val RSA_ECB = "RSA/ECB/PKCS1Padding"
    private val passcodeEncryptionIv = byteArrayOf(65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11)

    override fun rsaEncrypt(input: ByteArray, publicKey: PublicKey): String? {
        return try {
            val encryptCipher = rsaCipherInstance()
            if (encryptCipher == null || input.isEmpty()) return null
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            Base64.encodeToString(encryptCipher.doFinal(input), Base64.DEFAULT)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override fun rsaDecrypt(encryptedText: String, privateKey: PrivateKey): ByteArray? {
        return try {
            val decryptCipher = rsaCipherInstance()
            if (decryptCipher == null || encryptedText.isBlank()) return null
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
            decryptCipher.doFinal(decodeFromPemBase64String(encryptedText))
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    fun aesEncrypt(data: String, key: SecretKey): String {
        try {
            val keyBytes: ByteArray = key.encoded
            val aesKeyHash: ByteArray = MessageDigest.getInstance("SHA-256").digest(keyBytes)
            val ivBytes: ByteArray = aesKeyHash.copyOfRange(0, 16)
            val encryptedBytes: ByteArray = aesEncrypt(data, key = keyBytes, iv = ivBytes) ?: return ""
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: java.lang.Exception) {
            Timber.e(e)
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
    fun aesDecrypt(encryptedText: String, key: SecretKey): String? {
        return try {
            val keyBytes: ByteArray = key.encoded
            val aesKeyHash: ByteArray = MessageDigest.getInstance("SHA-256").digest(keyBytes)
            val ivBytes: ByteArray = aesKeyHash.copyOfRange(0, 16)
            aesDecrypt(encryptedText, key = keyBytes, iv = ivBytes)
        } catch (e: java.lang.Exception) {
            Timber.e(e)
            ""
        }
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
            Timber.e(e)
            null
        }
    }

    override fun aesGcmEncrypt(input: String, key: Key): String? {
        try {
            val encryptCipher = Cipher.getInstance(AES_INTERNAL_TRANSFORMATION) ?: return null
            encryptCipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, passcodeEncryptionIv))
            val encryptedBytes = encryptCipher.doFinal(input.toByteArray())
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun aesGcmDecrypt(encryptedText: String, key: Key): String? {
        return try {
            val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val encryptCipher = Cipher.getInstance(AES_INTERNAL_TRANSFORMATION) ?: return null
            encryptCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, passcodeEncryptionIv))
            val decodedBytes = encryptCipher.doFinal(encryptedBytes)
            String(decodedBytes)
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
            Timber.e(e)
            null
        }
    }
}

interface BaseCryptoToolsAbs {
    fun rsaEncrypt(input: ByteArray, publicKey: PublicKey): String?
    fun rsaDecrypt(encryptedText: String, privateKey: PrivateKey): ByteArray?
    fun aesDecrypt(encryptedText: String, key: ByteArray, iv: ByteArray): String?
    fun aesGcmEncrypt(input: String, key: Key): String?
    fun aesGcmDecrypt(encryptedText: String, key: Key): String?
}
