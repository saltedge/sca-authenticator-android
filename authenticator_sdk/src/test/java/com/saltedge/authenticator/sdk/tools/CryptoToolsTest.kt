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

import com.saltedge.authenticator.sdk.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.model.authorization.EncryptedAuthorizationData
import com.saltedge.authenticator.sdk.testTools.TestTools
import com.saltedge.authenticator.sdk.testTools.getTestPrivateKey
import com.saltedge.authenticator.sdk.testTools.getTestPublicKey
import com.saltedge.authenticator.sdk.testTools.toJsonString
import com.saltedge.authenticator.sdk.tools.crypt.CryptoTools.aesDecrypt
import com.saltedge.authenticator.sdk.tools.crypt.CryptoTools.aesEncrypt
import com.saltedge.authenticator.sdk.tools.crypt.CryptoTools.decryptAuthorizationData
import com.saltedge.authenticator.sdk.tools.crypt.CryptoTools.rsaDecrypt
import net.danlew.android.joda.JodaTimeAndroid
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
class CryptoToolsTest {

    @Before
    fun setUp() {
        JodaTimeAndroid.init(TestTools.applicationContext)
    }

    /**
     * Check encryption and decryption of byte array with rsa keys
     */
    @Test
    @Throws(Exception::class)
    fun rsaEncryptDecryptTestCase1() {
        assertThat(aesKey.size, equalTo(32)) // AES-256
        assertThat(aesIV.size, equalTo(16))

        val encryptedKey = rsaEncrypt(aesKey, publicKey)!!
        val encryptedIV = rsaEncrypt(aesKey, publicKey)!!

        assertThat(rsaDecrypt(encryptedKey, privateKey), equalTo(aesKey))
        assertThat(rsaDecrypt(encryptedIV, privateKey), equalTo(aesKey))
    }

    /**
     * Check encryption and decryption of byte array with rsa keys
     * when one param is invalid
     */
    @Test
    @Throws(Exception::class)
    fun rsaEncryptDecryptTestCase2() {
        Assert.assertNull(rsaDecrypt("", privateKey)) // Empty encrypted text

        val invalidCertificate: PublicKey = object : PublicKey {
            override fun getAlgorithm(): String = ""
            override fun getEncoded(): ByteArray = byteArrayOf()
            override fun getFormat(): String = ""
        }
        Assert.assertNull(rsaEncrypt(byteArrayOf(), invalidCertificate)) // Invalid public key
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
    }

    /**
     * Check aesDecrypt of internal strings (db key) encrypted with AES-256-GCM
     */
    @Test
    @Throws(Exception::class)
    fun aesDecryptTestCase2() {
        val initialTextValue = "test key"
        val testKey: SecretKey = SecretKeySpec(aesKey, 0, aesKey.size, "AES")

        val encryptedMessage = aesEncrypt(initialTextValue, testKey)!!

        val decryptedMessage = aesDecrypt(encryptedMessage, testKey)!!

        assertThat(encryptedMessage, not(equalTo(initialTextValue)))
        assertThat(decryptedMessage, equalTo(initialTextValue))
    }

    /**
     * Check aesDecrypt with invalid param
     */
    @Test
    @Throws(Exception::class)
    fun aesDecryptTestCase3() {
        val errorMessage = "{\"name\":\"Andrey\", \"age\":27, \"car\":\"BMW\", \"mileage\":null}"
        val encryptedMessage = encryptAesCBCString(errorMessage, aesKey, aesIV)!!

        Assert.assertNull(aesDecrypt(encryptedMessage, aesKey, aesKey)) // Invalid IV
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
            key = rsaEncrypt(aesKey, publicKey)!!,
            iv = rsaEncrypt(aesIV, publicKey)!!,
            data = encryptAesCBCString(authData.toJsonString(), aesKey, aesIV)!!
        )

        assertThat(
            decryptAuthorizationData(
                encryptedData = encryptedData,
                rsaPrivateKey = privateKey
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
                rsaPrivateKey = privateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(iv = null),
                rsaPrivateKey = privateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(data = null),
                rsaPrivateKey = privateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(key = ""),
                rsaPrivateKey = privateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(iv = ""),
                rsaPrivateKey = privateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(data = ""),
                rsaPrivateKey = privateKey
            )
        )
    }

    private val aesKey = byteArrayOf(
        65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45,
        65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45
    )
    private val aesIV = byteArrayOf(65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45)
    private val authData = AuthorizationData(
        id = "444",
        title = "title",
        description = "description",
        connectionId = "333",
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        authorizationCode = "111"
    )
    private var privateKey: PrivateKey = this.getTestPrivateKey()
    private var publicKey: PublicKey = this.getTestPublicKey()

    private fun rsaEncrypt(input: ByteArray, publicKey: PublicKey): String? {
        try {
            val encryptCipher =
                Cipher.getInstance("RSA/ECB/PKCS1Padding")
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
