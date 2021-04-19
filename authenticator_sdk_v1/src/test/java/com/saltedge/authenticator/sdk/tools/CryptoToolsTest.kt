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

import com.saltedge.android.test_tools.CommonTestTools
import com.saltedge.android.test_tools.encryptAesCBCString
import com.saltedge.android.test_tools.encryptWithTestKey
import com.saltedge.android.test_tools.rsaEncrypt
import com.saltedge.authenticator.sdk.api.model.ConsentData
import com.saltedge.authenticator.sdk.api.model.ConsentSharedData
import com.saltedge.authenticator.sdk.api.model.authorization.AuthorizationData
import com.saltedge.authenticator.sdk.testTools.TestTools
import com.saltedge.authenticator.sdk.tools.crypt.CryptoTools.aesDecrypt
import com.saltedge.authenticator.sdk.tools.crypt.CryptoTools.aesEncrypt
import com.saltedge.authenticator.sdk.tools.crypt.CryptoTools.decryptAuthorizationData
import com.saltedge.authenticator.sdk.tools.crypt.CryptoTools.decryptConsentData
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
import java.security.PublicKey
import javax.crypto.SecretKey
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
        assertThat(CommonTestTools.aesKey.size, equalTo(32)) // AES-256
        assertThat(CommonTestTools.aesIV.size, equalTo(16))

        val encryptedKey = rsaEncrypt(CommonTestTools.aesKey, CommonTestTools.testPublicKey)!!
        val encryptedIV = rsaEncrypt(CommonTestTools.aesKey, CommonTestTools.testPublicKey)!!

        assertThat(rsaDecrypt(encryptedKey, CommonTestTools.testPrivateKey), equalTo(CommonTestTools.aesKey))
        assertThat(rsaDecrypt(encryptedIV, CommonTestTools.testPrivateKey), equalTo(CommonTestTools.aesKey))
    }

    /**
     * Check encryption and decryption of byte array with rsa keys
     * when one param is invalid
     */
    @Test
    @Throws(Exception::class)
    fun rsaEncryptDecryptTestCase2() {
        Assert.assertNull(rsaDecrypt("", CommonTestTools.testPrivateKey)) // Empty encrypted text

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
        assertThat(aesDecrypt(encryptedMessage, CommonTestTools.aesKey, CommonTestTools.aesIV), equalTo(errorMessage))
    }

    /**
     * Check aesDecrypt of internal strings (db key) encrypted with AES-256-GCM
     */
    @Test
    @Throws(Exception::class)
    fun aesDecryptTestCase2() {
        val initialTextValue = "test key"
        val testKey: SecretKey = SecretKeySpec(CommonTestTools.aesKey, 0, CommonTestTools.aesKey.size, "AES")

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
        val encryptedMessage = encryptAesCBCString(errorMessage, CommonTestTools.aesKey, CommonTestTools.aesIV)!!

        Assert.assertNull(aesDecrypt(encryptedMessage, CommonTestTools.aesKey, CommonTestTools.aesKey)) // Invalid IV
    }

    @Test
    @Throws(Exception::class)
    fun decryptAuthorizationDataTest() {
        val encryptedData = authData.encryptWithTestKey()

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
                encryptedData = encryptedData.copy(key = null),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(iv = null),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(data = null),
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
    fun decryptConsentDataTest() {
        val encryptedData = consentData.encryptWithTestKey()

        assertThat(
            decryptConsentData(
                encryptedData = encryptedData,
                rsaPrivateKey = CommonTestTools.testPrivateKey
            ),
            equalTo(consentData)
        )
        Assert.assertNull(
            decryptConsentData(
                encryptedData = encryptedData,
                rsaPrivateKey = null
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(key = null),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptConsentData(
                encryptedData = encryptedData.copy(iv = null),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptAuthorizationData(
                encryptedData = encryptedData.copy(data = null),
                rsaPrivateKey = CommonTestTools.testPrivateKey
            )
        )
        Assert.assertNull(
            decryptConsentData(
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
            decryptConsentData(
                encryptedData = encryptedData.copy(data = ""),
                rsaPrivateKey = CommonTestTools.testPrivateKey
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
        tppName = "title",
        consentTypeString = "aisp",
        accounts = emptyList(),
        sharedData = ConsentSharedData(balance = true, transactions = true),
        expiresAt = DateTime(0).withZone(DateTimeZone.UTC),
        createdAt = DateTime(0).withZone(DateTimeZone.UTC)
    )
}
