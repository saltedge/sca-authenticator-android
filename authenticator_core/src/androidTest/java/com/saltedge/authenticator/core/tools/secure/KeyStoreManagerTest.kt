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

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
        val secretKey = KeyManager.createOrReplaceAesBiometricKey("biometric")

        assertNotNull(KeyManager.createOrReplaceRsaKeyPair(context, "test1"))
        assertNotNull(KeyManager.createOrReplaceRsaKeyPair(context, "test1"))

        if (secretKey == null) {
            assertThat(KeyManager.getKeyStoreAliases(), equalTo(listOf("test1")))
        } else assertThat(
            KeyManager.getKeyStoreAliases(),
            equalTo(listOf("test1", "biometric"))
        )
    }

    @Test
    @Throws(Exception::class)
    fun keyPairExistTest() {
        cleanKeystore()
        KeyManager.createOrReplaceRsaKeyPair(context, "test1")

        Assert.assertTrue(KeyManager.keyEntryExist("test1"))
        Assert.assertFalse(KeyManager.keyEntryExist("test2"))
    }

    @Test
    @Throws(Exception::class)
    fun getKeyStoreAliasesTest() {
        cleanKeystore()
        KeyManager.generateRsaKeyPair(context, "test1")

        assertThat(KeyManager.getKeyStoreAliases(), equalTo(listOf("test1")))
    }

    @Test
    @Throws(Exception::class)
    fun getKeyPairTest() {
        cleanKeystore()
        KeyManager.generateRsaKeyPair(context, "test1")
        val pair = KeyManager.getKeyPair("test1")

        assertThat(KeyManager.getKeyStoreAliases(), equalTo(listOf("test1")))
        assertNotNull(pair?.public)
        assertNotNull(pair?.private)

        Assert.assertNull(KeyManager.getKeyPair("non-existent"))
    }

    @Test
    @Throws(Exception::class)
    fun deleteKeyPairsTest() {
        cleanKeystore()
        KeyManager.generateRsaKeyPair(context, "test1")
        KeyManager.generateRsaKeyPair(context, "test2")

        assertThat(KeyManager.getKeyStoreAliases(), equalTo(listOf("test1", "test2")))

        KeyManager.deleteKeyPairsIfExist(listOf("test1", "test2"))

        Assert.assertTrue(KeyManager.getKeyStoreAliases().isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun deleteKeyPairTest() {
        cleanKeystore()
        KeyManager.generateRsaKeyPair(context, "test1")
        KeyManager.generateRsaKeyPair(context, "test2")

        assertThat(KeyManager.getKeyStoreAliases(), equalTo(listOf("test1", "test2")))

        KeyManager.deleteKeyPairIfExist("test1")

        assertThat(KeyManager.getKeyStoreAliases(), equalTo(listOf("test2")))

        KeyManager.deleteKeyPairIfExist("test2")

        Assert.assertTrue(KeyManager.getKeyStoreAliases().isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun createNewKeyPairTest() {
        cleanKeystore()
        KeyManager.generateRsaKeyPair(context, "test1")

        assertThat(KeyManager.getKeyStoreAliases(), equalTo(listOf("test1")))

        KeyManager.generateRsaKeyPair(context, "test2")

        assertThat(KeyManager.getKeyStoreAliases(), equalTo(listOf("test1", "test2")))
    }

    @Test
    @Throws(Exception::class)
    fun publicKeyToPemEncodedStringTest() {
        cleanKeystore()
        val kp = KeyManager.generateRsaKeyPair(context, "test1")
        val pemString = kp!!.publicKeyToPem()

        Assert.assertTrue(pemString.isNotEmpty())
        Assert.assertTrue(pemString.startsWith("-----BEGIN PUBLIC KEY-----\n"))
        Assert.assertTrue(pemString.endsWith("\n-----END PUBLIC KEY-----\n"))
    }

    private fun cleanKeystore() {
        KeyManager.getKeyStoreAliases().forEach { KeyManager.deleteKeyPairIfExist(it) }
    }
}
