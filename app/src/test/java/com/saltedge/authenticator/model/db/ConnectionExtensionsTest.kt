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
package com.saltedge.authenticator.model.db

import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.ConnectionStatus
import com.saltedge.authenticator.sdk.model.ProviderData
import com.saltedge.authenticator.sdk.tools.KeyStoreManagerAbs
import com.saltedge.authenticator.testTools.getTestPrivateKey
import com.saltedge.authenticator.testTools.getTestPublicKey
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

@RunWith(RobolectricTestRunner::class)
class ConnectionExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun connectionInitWithProviderDataTest() {
        var connection = Connection().apply {
            initWithProviderData(
                ProviderData(
                    code = "demobank",
                    name = "Demobank",
                    connectUrl = "url",
                    logoUrl = "url",
                    version = "1",
                    supportEmail = "exampple1@saltedge.com"
                )
            )
        }

        Assert.assertTrue(connection.guid.isNotEmpty())
        assertThat(connection.id, equalTo(""))
        assertThat(connection.createdAt, greaterThan(0L))
        assertThat(connection.updatedAt, greaterThan(0L))
        assertThat(connection.name, equalTo("Demobank"))
        assertThat(connection.code, equalTo("demobank"))
        assertThat(connection.connectUrl, equalTo("url"))
        assertThat(connection.logoUrl, equalTo("url"))
        assertThat(connection.accessToken, equalTo(""))
        assertThat(connection.status, equalTo("${ConnectionStatus.INACTIVE}"))
        assertThat(connection.supportEmail, equalTo("exampple1@saltedge.com"))

        connection = connection.apply {
            initWithProviderData(
                ProviderData(
                    connectUrl = "url1",
                    code = "code2",
                    name = "name3",
                    logoUrl = "url4",
                    version = "1",
                    supportEmail = "example2@saltedge.com"
                )
            )
        }

        Assert.assertTrue(connection.guid.isNotEmpty())
        assertThat(connection.id, equalTo(""))
        assertThat(connection.createdAt, greaterThan(0L))
        assertThat(connection.updatedAt, greaterThan(0L))
        assertThat(connection.name, equalTo("name3"))
        assertThat(connection.code, equalTo("code2"))
        assertThat(connection.connectUrl, equalTo("url1"))
        assertThat(connection.logoUrl, equalTo("url4"))
        assertThat(connection.accessToken, equalTo(""))
        assertThat(connection.status, equalTo("${ConnectionStatus.INACTIVE}"))
        assertThat(connection.supportEmail, equalTo("example2@saltedge.com"))
    }

    @Test
    @Throws(Exception::class)
    fun connectionGetStatusTest() {
        val connection = Connection().apply { status = "${ConnectionStatus.INACTIVE}" }

        assertThat(connection.status, equalTo("${ConnectionStatus.INACTIVE}"))
        assertThat(connection.getStatus(), equalTo(ConnectionStatus.INACTIVE))
    }

    @Test
    @Throws(Exception::class)
    fun connectionToConnectionAndKeyTest() {
        val pair: ConnectionAndKey? =
            Connection().apply { guid = "guid1" }.toConnectionAndKey(mockKeyStoreManager)

        Assert.assertNull(pair)

        Mockito.`when`(mockKeyStoreManager.getKeyPair("guid1")).thenReturn(
            KeyPair(
                publicKey,
                privateKey
            )
        )

        Assert.assertNotNull(
            Connection().apply { guid = "guid1" }.toConnectionAndKey(
                mockKeyStoreManager
            )!!.key
        )
    }

    private val mockKeyStoreManager = Mockito.mock(KeyStoreManagerAbs::class.java)
    private var privateKey: PrivateKey = this.getTestPrivateKey()
    private var publicKey: PublicKey = this.getTestPublicKey()
}
