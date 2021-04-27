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
package com.saltedge.authenticator.sdk.tools

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saltedge.authenticator.sdk.v2.tools.secure.*
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class KeyExchangeTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    @Test
    @Throws(Exception::class)
    fun createOrReplaceDhKeyPairTest() {
        //Init connection on application side
        val providerDhPublicKey = providerDhPublicKeyPem.pemToPublicKey(KeyAlgorithm.DIFFIE_HELLMAN)!!
        val appDhKeyPair = KeyTools.createDhKeyPair(providerDhPublicKey)!!
        val sharedSecretOfApp = KeyTools.computeSecretKey(appDhKeyPair.private, providerDhPublicKey)
        val rsaKeyPair = KeyManager.createOrReplaceRsaKeyPair(context, "testRSA")!!
        val rsaPublicPem = rsaKeyPair.publicKeyToPem()
        val encRsaPublicPem = CryptoTools.aesEncrypt(rsaPublicPem, sharedSecretOfApp)
        val appDhPublicKeyPem = appDhKeyPair.publicKeyToPem()

        //Init connection on provider side
        val providerDhPrivateKey = providerDhPrivateKeyPem.pemToPrivateKey(KeyAlgorithm.DIFFIE_HELLMAN)!!
        val appDhPublicKey = appDhPublicKeyPem.pemToPublicKey(KeyAlgorithm.DIFFIE_HELLMAN)!!
        val sharedSecretOfProvider = KeyTools.computeSecretKey(providerDhPrivateKey, appDhPublicKey)
        val decRsaPublicPem = CryptoTools.aesDecrypt(encRsaPublicPem, sharedSecretOfProvider)

        assertThat(rsaPublicPem, equalTo(decRsaPublicPem))
    }
}
