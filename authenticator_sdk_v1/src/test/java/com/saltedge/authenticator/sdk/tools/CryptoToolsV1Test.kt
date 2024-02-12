/*
 * Copyright (c) 2019 Salt Edge Inc.
 */
package com.saltedge.authenticator.sdk.tools

import com.saltedge.android.test_tools.*
import com.saltedge.authenticator.core.api.model.ConsentData
import com.saltedge.authenticator.core.api.model.ConsentSharedData
import com.saltedge.authenticator.core.api.model.EncryptedData
import com.saltedge.authenticator.core.model.ConnectionAbs
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.testTools.TestTools
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
import java.security.PublicKey
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
class CryptoToolsV1Test {

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

        assertThat(CryptoToolsV1.rsaDecrypt(encryptedKey, CommonTestTools.testPrivateKey), equalTo(CommonTestTools.aesKey))
        assertThat(CryptoToolsV1.rsaDecrypt(encryptedIV, CommonTestTools.testPrivateKey), equalTo(CommonTestTools.aesKey))
    }

    /**
     * Check encryption and decryption of byte array with rsa keys
     * when one param is invalid
     */
    @Test
    @Throws(Exception::class)
    fun rsaEncryptDecryptTestCase2() {
        Assert.assertNull(CryptoToolsV1.rsaDecrypt("", CommonTestTools.testPrivateKey)) // Empty encrypted text

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
        assertThat(CryptoToolsV1.aesDecrypt(encryptedMessage, CommonTestTools.aesKey, CommonTestTools.aesIV), equalTo(errorMessage))
    }

    /**
     * Check aesDecrypt of internal strings (db key) encrypted with AES-256-GCM
     */
    @Test
    @Throws(Exception::class)
    fun aesDecryptTestCase2() {
        val initialTextValue = "test key"
        val testKey: SecretKey = SecretKeySpec(CommonTestTools.aesKey, 0, CommonTestTools.aesKey.size, "AES")

        val encryptedMessage = CryptoToolsV1.aesEncrypt(initialTextValue, testKey)

        val decryptedMessage = CryptoToolsV1.aesDecrypt(encryptedMessage, testKey)!!

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

        Assert.assertNull(CryptoToolsV1.aesDecrypt(encryptedMessage, CommonTestTools.aesKey, CommonTestTools.aesKey)) // Invalid IV
    }

    @Test
    @Throws(Exception::class)
    fun decryptAuthorizationDataTest() {
        val encryptedData: EncryptedData = authData.encryptWithTestKey()

        assertThat(
            CryptoToolsV1.decryptAuthorizationData(
                encryptedData = encryptedData,
                rsaPrivateKey = CommonTestTools.testPrivateKey
            ),
            equalTo(authData)
        )
        Assert.assertNull(
            CryptoToolsV1.decryptAuthorizationData(
                encryptedData = encryptedData,
                rsaPrivateKey = null
            )
        )
        Assert.assertNull(
            CryptoToolsV1.decryptAuthorizationData(
                encryptedData = encryptedData.copy(key = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            CryptoToolsV1.decryptAuthorizationData(
                encryptedData = encryptedData.copy(iv = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            CryptoToolsV1.decryptAuthorizationData(
                encryptedData = encryptedData.copy(data = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun decryptConsentDataTest() {
        val encryptedData: EncryptedData = consentData.encryptWithTestKey()
        val requestConnection: ConnectionAbs =
            TestConnection(id = "333", guid = "test", connectUrl = "/", accessToken = "accessToken")

        assertThat(
            CryptoToolsV1.decryptConsentData(
                encryptedData = encryptedData,
                rsaPrivateKey = CommonTestTools.testPrivateKey,
                connectionGUID = requestConnection.guid,
                consentID = null
            ),
            equalTo(consentData)
        )
        Assert.assertNull(
            CryptoToolsV1.decryptConsentData(
                encryptedData = encryptedData.copy(key = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey,
                connectionGUID = requestConnection.guid,
                consentID = null
            )
        )
        Assert.assertNull(
            CryptoToolsV1.decryptAuthorizationData(
                encryptedData = encryptedData.copy(iv = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            CryptoToolsV1.decryptConsentData(
                encryptedData = encryptedData.copy(data = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey,
                connectionGUID = requestConnection.guid,
                consentID = null
            )
        )
    }

    private val authData = AuthorizationData(
        id = "444",
        title = "title",
        description = "description",
        connectionId = "333",
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        authorizationCode = "111"
    )
    private val consentData = ConsentData(
        id = "555",
        userId = "1",
        connectionId = "2",
        tppName = "title",
        consentTypeString = "aisp",
        accounts = emptyList(),
        sharedData = ConsentSharedData(balance = true, transactions = true),
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        createdAt = DateTime(0).withZone(DateTimeZone.UTC),
        connectionGuid = "test"
    )
}
