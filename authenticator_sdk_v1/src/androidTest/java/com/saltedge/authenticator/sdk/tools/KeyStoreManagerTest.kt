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

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManager
import com.saltedge.authenticator.sdk.tools.keystore.publicKeyToPemEncodedString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyStoreManagerTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    @Test
    @Throws(Exception::class)
    fun createOrReplaceKeyPairTest() {
        cleanKeystore()
        val secretKey = KeyStoreManager.createOrReplaceAesBiometricKey("biometric")

        assertNotNull(KeyStoreManager.createOrReplaceRsaKeyPair(context, "test1"))
        assertNotNull(KeyStoreManager.createOrReplaceRsaKeyPair(context, "test1"))

        if (secretKey == null) {
            assertThat(KeyStoreManager.getKeyStoreAliases(), equalTo(listOf("test1")))
        } else assertThat(
            KeyStoreManager.getKeyStoreAliases(),
            equalTo(listOf("test1", "biometric"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun keyPairExistTest() {
        cleanKeystore()
        KeyStoreManager.createOrReplaceRsaKeyPair(context, "test1")

        Assert.assertTrue(KeyStoreManager.keyEntryExist("test1"))
        Assert.assertFalse(KeyStoreManager.keyEntryExist("test2"))
    }

    @Test
    @Throws(Exception::class)
    fun getKeyStoreAliasesTest() {
        cleanKeystore()
        KeyStoreManager.createNewRsaKeyPair(context, "test1")

        assertThat(KeyStoreManager.getKeyStoreAliases(), equalTo(listOf("test1")))
    }

    @Test
    @Throws(Exception::class)
    fun getKeyPairTest() {
        cleanKeystore()
        KeyStoreManager.createNewRsaKeyPair(context, "test1")
        val pair = KeyStoreManager.getKeyPair("test1")

        assertThat(KeyStoreManager.getKeyStoreAliases(), equalTo(listOf("test1")))
        assertNotNull(pair?.public)
        assertNotNull(pair?.private)

        Assert.assertNull(KeyStoreManager.getKeyPair("non-existent"))
        Assert.assertNull(KeyStoreManager.getKeyPair(null))
    }

    @Test
    @Throws(Exception::class)
    fun deleteKeyPairsTest() {
        cleanKeystore()
        KeyStoreManager.createNewRsaKeyPair(context, "test1")
        KeyStoreManager.createNewRsaKeyPair(context, "test2")

        assertThat(KeyStoreManager.getKeyStoreAliases(), equalTo(listOf("test1", "test2")))

        KeyStoreManager.deleteKeyPairs(listOf("test1", "test2"))

        Assert.assertTrue(KeyStoreManager.getKeyStoreAliases().isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun deleteKeyPairTest() {
        cleanKeystore()
        KeyStoreManager.createNewRsaKeyPair(context, "test1")
        KeyStoreManager.createNewRsaKeyPair(context, "test2")

        assertThat(KeyStoreManager.getKeyStoreAliases(), equalTo(listOf("test1", "test2")))

        KeyStoreManager.deleteKeyPair("test1")

        assertThat(KeyStoreManager.getKeyStoreAliases(), equalTo(listOf("test2")))

        KeyStoreManager.deleteKeyPair("test2")

        Assert.assertTrue(KeyStoreManager.getKeyStoreAliases().isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun createNewKeyPairTest() {
        cleanKeystore()
        KeyStoreManager.createNewRsaKeyPair(context, "test1")

        assertThat(KeyStoreManager.getKeyStoreAliases(), equalTo(listOf("test1")))

        KeyStoreManager.createNewRsaKeyPair(context, "test2")

        assertThat(KeyStoreManager.getKeyStoreAliases(), equalTo(listOf("test1", "test2")))
    }

    @Test
    @Throws(Exception::class)
    fun publicKeyToPemEncodedStringTest() {
        cleanKeystore()
        val kp = KeyStoreManager.createNewRsaKeyPair(context, "test1")
        val pemString = kp!!.publicKeyToPemEncodedString()

        Assert.assertTrue(pemString.isNotEmpty())
        Assert.assertTrue(pemString.startsWith("-----BEGIN PUBLIC KEY-----\n"))
        Assert.assertTrue(pemString.endsWith("\n-----END PUBLIC KEY-----\n"))
    }

    private fun cleanKeystore() {
        KeyStoreManager.getKeyStoreAliases().forEach { KeyStoreManager.deleteKeyPair(it) }
    }
}
