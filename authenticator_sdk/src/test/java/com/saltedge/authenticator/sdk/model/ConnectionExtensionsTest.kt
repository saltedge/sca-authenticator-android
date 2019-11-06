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
package com.saltedge.authenticator.sdk.model

import com.saltedge.authenticator.sdk.testTools.TestConnection
import com.saltedge.authenticator.sdk.testTools.getTestPrivateKey
import com.saltedge.authenticator.sdk.testTools.getTestPublicKey
import com.saltedge.authenticator.sdk.tools.keystore.KeyStoreManagerAbs
import io.mockk.every
import io.mockk.mockkClass
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class ConnectionExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun connectionGetStatusTest() {
        val connection = TestConnection().apply { status = "${ConnectionStatus.INACTIVE}" }

        assertThat(connection.status, equalTo("${ConnectionStatus.INACTIVE}"))
        assertThat(connection.getStatus(), equalTo(ConnectionStatus.INACTIVE))
    }

    @Test
    @Throws(Exception::class)
    fun connectionToConnectionAndKeyTest() {
        every { mockKeyStoreManager.getKeyPair("guid1") } returns null

        Assert.assertNull(TestConnection().apply { guid = "guid1" }.toConnectionAndKey(mockKeyStoreManager))

        every { mockKeyStoreManager.getKeyPair("guid1") } returns KeyPair(publicKey, privateKey)

        Assert.assertNotNull(TestConnection().apply { guid = "guid1" }.toConnectionAndKey(mockKeyStoreManager)!!.key)
    }

    private val mockKeyStoreManager = mockkClass(KeyStoreManagerAbs::class)
    private var privateKey: PrivateKey = this.getTestPrivateKey()
    private var publicKey: PublicKey = this.getTestPublicKey()
}
