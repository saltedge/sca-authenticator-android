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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.sdk.model.AuthorizationData
import com.saltedge.authenticator.sdk.model.EncryptedAuthorizationData
import com.saltedge.authenticator.sdk.testTools.toJsonString
import com.saltedge.authenticator.sdk.tools.CryptoTools.aesDecrypt
import com.saltedge.authenticator.sdk.tools.CryptoTools.aesEncrypt
import com.saltedge.authenticator.sdk.tools.CryptoTools.decryptAuthorizationData
import com.saltedge.authenticator.sdk.tools.CryptoTools.rsaDecrypt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.security.KeyPair
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@RunWith(AndroidJUnit4::class)
class CryptoToolsTest {

    @Test
    @Throws(Exception::class)
    fun rsaEncryptDecryptTest() {
        assertThat(aesKey.size, equalTo(32)) // AES-256
        assertThat(aesIV.size, equalTo(16))

        val encryptedKey = rsaEncrypt(aesKey, keyPair.public)!!
        val encryptedIV = rsaEncrypt(aesKey, keyPair.public)!!

        assertThat(rsaDecrypt(encryptedKey, keyPair.private), equalTo(aesKey))
        assertThat(rsaDecrypt(encryptedIV, keyPair.private), equalTo(aesKey))

        Assert.assertNull(rsaDecrypt("", keyPair.private))
        val invalidCertificate: PublicKey = object : PublicKey {
            override fun getAlgorithm(): String = ""
            override fun getEncoded(): ByteArray = byteArrayOf()
            override fun getFormat(): String = ""
        }
        Assert.assertNull(rsaEncrypt(byteArrayOf(), invalidCertificate))
    }

    /**
     * Check aesDecrypt of External messages encrypted with AES-256-CBC
     */
    @Test
    @Throws(Exception::class)
    fun aesDecryptTestCase1() {
        val errorMessage = "{\"name\":\"Andrey\", \"age\":27, \"car\":\"BMW\", \"mileage\":null}"
        val encryptedMessage = encryptAesCBCString(errorMessage, aesKey, aesIV)!!

        assertThat(encryptedMessage, not(equalTo(errorMessage)))
        assertThat(
            encryptedMessage,
            equalTo("MBrw7K2rCIKop50b2PmkmlAVO9Bulhl7yO8ZPw2ulVnh7MB9yI0vRJjum6xFnQMq\n9BR172WT/KAw78Zg4++EQQ==")
        )
        assertThat(aesDecrypt(encryptedMessage, aesKey, aesIV), equalTo(errorMessage))
        Assert.assertNull(aesDecrypt(encryptedMessage, aesKey, aesKey))
    }

    /**
     * Check aesDecrypt of internal strings (db key) encrypted with AES-256-GCM
     */
    @Test
    @Throws(Exception::class)
    fun aesDecryptTestCase2() {
        val initialTextValue = "test key"
        val testKey: SecretKey = KeyStoreManager.createOrReplaceAesKey("test")!!

        val encryptedMessage = aesEncrypt(initialTextValue, testKey)!!

        val decryptedMessage = aesDecrypt(encryptedMessage, testKey)!!

        assertThat(encryptedMessage, not(equalTo(initialTextValue)))
        assertThat(decryptedMessage, equalTo(initialTextValue))
    }

    @Test
    @Throws(Exception::class)
    fun decryptAuthorizationDataTest() {
        assertThat(aesKey.size, equalTo(32)) // AES-256
        assertThat(aesIV.size, equalTo(16))

        val encryptedData = EncryptedAuthorizationData(
            id = authData.id,
            connectionId = authData.connectionId,
            algorithm = "AES-256-CBC",
            key = rsaEncrypt(aesKey, keyPair.public)!!,
            iv = rsaEncrypt(aesIV, keyPair.public)!!,
            data = encryptAesCBCString(authData.toJsonString(), aesKey, aesIV)!!
        )

        assertThat(
            decryptAuthorizationData(
                encryptedData = encryptedData,
                rsaPrivateKey = keyPair.private!!
            ),
            equalTo(authData)
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData,
                rsaPrivateKey = null
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(key = null),
                rsaPrivateKey = keyPair.private!!
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(iv = null),
                rsaPrivateKey = keyPair.private!!
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(data = null),
                rsaPrivateKey = keyPair.private!!
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(key = ""),
                rsaPrivateKey = keyPair.private!!
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(iv = ""),
                rsaPrivateKey = keyPair.private!!
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(data = ""),
                rsaPrivateKey = keyPair.private!!
            )
        )
    }

    private val keyPair: KeyPair = KeyStoreManager.createOrReplaceRsaKeyPair("test")!!
    private val aesKey = byteArrayOf(65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45, 65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45)
    private val aesIV = byteArrayOf(65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45)
    private val authData = AuthorizationData(
        id = "444",
        title = "title",
        description = "description",
        connectionId = "333",
        expiresAt = DateTime(0),
        authorizationCode = "111"
    )

    private fun rsaEncrypt(input: ByteArray, publicKey: PublicKey): String? {
        try {
            val encryptCipher =
                Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround")
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)

            val outputStream = ByteArrayOutputStream()
            val cipherOutputStream = CipherOutputStream(outputStream, encryptCipher)
            cipherOutputStream.write(input)
            cipherOutputStream.close()
            return encodeToPemBase64String(outputStream.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun encryptAesCBCString(text: String, key: ByteArray, iv: ByteArray): String? {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return encodeToPemBase64String(cipher.doFinal(text.toByteArray()))
    }
}
