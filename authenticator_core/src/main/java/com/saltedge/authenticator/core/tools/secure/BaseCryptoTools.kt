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
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.model.GUID
import com.saltedge.authenticator.core.model.ID
import com.saltedge.authenticator.core.tools.decodeFromPemBase64String
import com.saltedge.authenticator.core.tools.json.createDefaultGson
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

const val SUPPORTED_AES_ALGORITHM = "AES-256-CBC"
private const val AES_INTERNAL_TRANSFORMATION = "AES/GCM/NoPadding"
private const val AES_EXTERNAL_TRANSFORMATION = "AES/CBC/PKCS5Padding"
private const val RSA_ECB = "RSA/ECB/PKCS1Padding"

open class BaseCryptoTools : BaseCryptoToolsAbs {

    override fun rsaEncrypt(inputText: String, publicKey: PublicKey): String? =
        rsaEncrypt(inputText.toByteArray(), publicKey)

    override fun rsaEncrypt(inputBytes: ByteArray, publicKey: PublicKey): String? {
        return try {
            val encryptCipher = rsaCipherInstance()
            if (encryptCipher == null || inputBytes.isEmpty()) return null
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            Base64.encodeToString(encryptCipher.doFinal(inputBytes), Base64.DEFAULT)
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
            e.printStackTrace()
            Timber.e(e)
            null
        }
    }

    override fun aesEncrypt(data: String, key: SecretKey): String {
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
    override fun aesEncrypt(data: String, key: ByteArray, iv: ByteArray): ByteArray? {
        val cipher = Cipher.getInstance(AES_EXTERNAL_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, KeyAlgorithm.AES), IvParameterSpec(iv))
        return cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    @Throws(java.lang.Exception::class)
    override fun aesDecrypt(encryptedText: String, key: SecretKey): String? {
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
        return try {
            val encryptCipher = Cipher.getInstance(AES_INTERNAL_TRANSFORMATION) ?: return null
            encryptCipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, gcmEncryptionIv))
            val encryptedBytes = encryptCipher.doFinal(input.toByteArray())
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }

    }

    override fun aesGcmDecrypt(encryptedText: String, key: Key): String? {
        return try {
            val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val encryptCipher = Cipher.getInstance(AES_INTERNAL_TRANSFORMATION) ?: return null
            encryptCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, gcmEncryptionIv))
            val decodedBytes = encryptCipher.doFinal(encryptedBytes)
            String(decodedBytes)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    override fun decryptConsentData(
        encryptedData: EncryptedData,
        rsaPrivateKey: PrivateKey,
        connectionGUID: GUID,
        consentID: ID?
    ): ConsentData? {
        val algorithm = encryptedData.algorithm
        if (algorithm != null && algorithm != SUPPORTED_AES_ALGORITHM) return null
        return try {
            val encryptedKey = encryptedData.key
            val encryptedIV = encryptedData.iv
            val encryptedMessage = encryptedData.data
            val key = rsaDecrypt(encryptedKey, rsaPrivateKey) ?: return null
            val iv = rsaDecrypt(encryptedIV, rsaPrivateKey) ?: return null
            val jsonString = aesDecrypt(encryptedMessage, key = key, iv = iv)
            createDefaultGson().fromJson(jsonString, ConsentData::class.java).apply {
                this.connectionId = encryptedData.connectionId
                this.connectionGuid = connectionGUID
                if (consentID != null) this.id = consentID
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    fun rsaDecrypt(encryptedBytes: ByteArray, privateKey: PrivateKey): ByteArray? {
        return try {
            val decryptCipher = rsaCipherInstance() ?: return null
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
            decryptCipher.doFinal(encryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e(e)
            null
        }
    }

    private val gcmEncryptionIv = byteArrayOf(65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11)

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
    fun rsaEncrypt(inputText: String, publicKey: PublicKey): String?
    fun rsaEncrypt(inputBytes: ByteArray, publicKey: PublicKey): String?
    fun rsaDecrypt(encryptedText: String, privateKey: PrivateKey): ByteArray?
    fun aesEncrypt(data: String, key: SecretKey): String
    fun aesEncrypt(data: String, key: ByteArray, iv: ByteArray): ByteArray?
    fun aesDecrypt(encryptedText: String, key: SecretKey): String?
    fun aesDecrypt(encryptedText: String, key: ByteArray, iv: ByteArray): String?
    fun aesGcmEncrypt(input: String, key: Key): String?
    fun aesGcmDecrypt(encryptedText: String, key: Key): String?
    fun decryptConsentData(
        encryptedData: EncryptedData,
        rsaPrivateKey: PrivateKey,
        connectionGUID: GUID,
        consentID: ID?
    ): ConsentData?
}
