/*
 * Copyright (c) 2021 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.v2.tools.secure

import android.util.Base64
import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.encryptAesCBCString
import com.saltedge.android.test_tools.rsaEncrypt
import com.saltedge.android.test_tools.toJsonString
import com.saltedge.authenticator.core.api.model.DescriptionData
import com.saltedge.authenticator.core.tools.encodeToPemBase64String
import com.saltedge.authenticator.sdk.v2.TestTools
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationResponseData
import com.saltedge.authenticator.sdk.v2.api.model.authorization.AuthorizationV2Data
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2
import com.saltedge.authenticator.sdk.v2.tools.CryptoToolsV2.decryptAuthorizationData
import com.saltedge.authenticator.sdk.v2.tools.WrappedAccessToken
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
import java.security.PublicKey
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
class CryptoToolsV2Test {

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
        assertThat(CommonTestTools.aesKey.size, equalTo(32)) // AES-256
        assertThat(CommonTestTools.aesIV.size, equalTo(16))

        val encryptedKey = rsaEncrypt(CommonTestTools.aesKey, CommonTestTools.testPublicKey)!!
        val encryptedIV = rsaEncrypt(CommonTestTools.aesKey, CommonTestTools.testPublicKey)!!

        assertThat(CryptoToolsV2.rsaDecrypt(encryptedKey, CommonTestTools.testPrivateKey), equalTo(CommonTestTools.aesKey))
        assertThat(CryptoToolsV2.rsaDecrypt(encryptedIV, CommonTestTools.testPrivateKey), equalTo(CommonTestTools.aesKey))
    }

    /**
     * Check encryption and decryption of byte array with rsa keys
     * when one param is invalid
     */
    @Test
    @Throws(Exception::class)
    fun rsaEncryptDecryptTestCase2() {
        Assert.assertNull(CryptoToolsV2.rsaDecrypt("", CommonTestTools.testPrivateKey)) // Empty encrypted text

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
        val encryptedMessage = encryptAesCBCString(errorMessage, CommonTestTools.aesKey, CommonTestTools.aesIV)!!

        assertThat(encryptedMessage, not(equalTo(errorMessage)))
        assertThat(
            encryptedMessage,
            equalTo("MBrw7K2rCIKop50b2PmkmlAVO9Bulhl7yO8ZPw2ulVnh7MB9yI0vRJjum6xFnQMq\n9BR172WT/KAw78Zg4++EQQ==")
        )
        assertThat(CryptoToolsV2.aesDecrypt(encryptedMessage, CommonTestTools.aesKey, CommonTestTools.aesIV), equalTo(errorMessage))
    }

    /**
     * Check aesDecrypt of internal strings (db key) encrypted with AES-256-GCM
     */
    @Test
    @Throws(Exception::class)
    fun aesDecryptTestCase2() {
        val initialTextValue = "test key"
        val testKey: SecretKey = SecretKeySpec(CommonTestTools.aesKey, 0, CommonTestTools.aesKey.size, "AES")

        val encryptedMessage = CryptoToolsV2.aesEncrypt(initialTextValue, testKey)!!

        val decryptedMessage = CryptoToolsV2.aesDecrypt(encryptedMessage, testKey)!!

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
        val encryptedMessage = encryptAesCBCString(errorMessage, CommonTestTools.aesKey, CommonTestTools.aesIV)!!

        Assert.assertNull(CryptoToolsV2.aesDecrypt(encryptedMessage, CommonTestTools.aesKey, CommonTestTools.aesKey)) // Invalid IV
    }

    @Test
    @Throws(Exception::class)
    fun decryptAuthorizationDataTest() {
        val encryptedData: AuthorizationResponseData = authData.encryptWithTestKey()

        assertThat(
            decryptAuthorizationData(
                encryptedData = encryptedData,
                rsaPrivateKey = CommonTestTools.testPrivateKey
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
                encryptedData = encryptedData.copy(key = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(iv = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(data = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(key = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(iv = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(data = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun decryptAccessTokenTest() {
        val testToken = getRandomString(32)
        assertThat(testToken.length, equalTo(32))
        val json = WrappedAccessToken(testToken).toJsonString()
        assertThat(json, equalTo("{\"access_token\":\"$testToken\"}"))

        val encrypted = rsaEncryptToken(json.toByteArray(), CommonTestTools.testPublicKey)!!
        val decryptedToken = CryptoToolsV2.decryptAccessToken(encrypted, CommonTestTools.testPrivateKey)

        assertThat(decryptedToken, equalTo(testToken))
    }

    private fun rsaEncryptToken(input: ByteArray, publicKey: PublicKey): String? {
        try {
            val encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)

            val outputStream = ByteArrayOutputStream()
            val cipherOutputStream = CipherOutputStream(outputStream, encryptCipher)
            cipherOutputStream.write(input)
            cipherOutputStream.close()
            return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getRandomString(size: Int): String {
        val rand = Random() //instance of random class
        val totalCharacters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var randomString = ""
        for (i in 0 until size) {
            randomString += totalCharacters[rand.nextInt(totalCharacters.length - 1)]
        }
        return randomString
    }

    private val authData = AuthorizationV2Data(
        title = "title",
        description = DescriptionData(),
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        authorizationCode = "111",
        connectionID = "333",
        authorizationID = "444",
        status = "pending"
    )

    private fun AuthorizationV2Data.encryptWithTestKey(): AuthorizationResponseData {
        return encryptWithTestKey(
            id = "444",
            connectionId = "333",
            jsonString = this.toJsonString(),
            publicKey = CommonTestTools.testPublicKey
        )
    }

    private fun encryptWithTestKey(
        id: String,
        connectionId: String,
        jsonString: String,
        publicKey: PublicKey
    ): AuthorizationResponseData {
        return AuthorizationResponseData(
            id = id,
            connectionId = connectionId,
            status = "pending",
            key = rsaEncrypt(CommonTestTools.aesKey, publicKey)!!,
            iv = rsaEncrypt(CommonTestTools.aesIV, publicKey)!!,
            data = encryptAesCBCString(jsonString, CommonTestTools.aesKey, CommonTestTools.aesIV)!!
        )
    }
}
