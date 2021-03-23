/**
 * @author Fentury Team
 * Copyright (c) 2020 Salt Edge. All rights reserved.
 */
package com.fentury.applock.tools.crypt

import android.util.Base64
import java.security.Key
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoTools : CryptoToolsAbs {

    private const val AES_INTERNAL_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val AES_EXTERNAL_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private val encryptionIv = byteArrayOf(65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11)

    override fun rsaEncrypt(input: String, publicKey: PublicKey): String? {
        return try {
            val encryptCipher = getRsaCipher()
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
            val decryptCipher = getRsaCipher()
            if (decryptCipher == null || encryptedText.isBlank()) return null
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decodedText = decodeFromPemBase64String(encryptedText)
            decryptCipher.doFinal(decodedText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
        return try {
            val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val encryptCipher = Cipher.getInstance(AES_INTERNAL_TRANSFORMATION) ?: return null
            encryptCipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, encryptionIv))
            val decodedBytes = encryptCipher.doFinal(encryptedBytes)
            String(decodedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun aesDecrypt(encryptedText: String, key: ByteArray, iv: ByteArray): String? {
        return try {
            val decryptCipher = Cipher.getInstance(AES_EXTERNAL_TRANSFORMATION) ?: return null
            decryptCipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, "AES"),
                IvParameterSpec(iv)
            )
            val decryptedBytes = decryptCipher.doFinal(decodeFromPemBase64String(encryptedText))
            String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getRsaCipher(): Cipher? {
        return try {
            // AndroidOpenSSL causes error in android 6: InvalidKeyException: Need RSA private or public key (AndroidKeyStoreBCWorkaround)
            // AndroidKeyStoreBCWorkaround causes error in android 5: NoSuchProviderException: Provider not available (AndroidOpenSSL)
            Cipher.getInstance("RSA/ECB/PKCS1Padding")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun decodeFromPemBase64String(inputString: String): ByteArray? {
    return Base64.decode(inputString.replace("\n", ""), Base64.NO_WRAP)
}