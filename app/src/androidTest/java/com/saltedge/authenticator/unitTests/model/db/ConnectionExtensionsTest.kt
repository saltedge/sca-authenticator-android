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
package com.saltedge.authenticator.unitTests.model.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saltedge.authenticator.model.db.*
import com.saltedge.authenticator.sdk.model.ConnectionAndKey
import com.saltedge.authenticator.sdk.model.ConnectionStatus
import com.saltedge.authenticator.sdk.model.ProviderData
import com.saltedge.authenticator.sdk.tools.KeyStoreManager
import com.saltedge.authenticator.testTools.DatabaseTestCase
import com.saltedge.authenticator.testTools.save
import com.saltedge.authenticator.testTools.setGuid
import com.saltedge.authenticator.testTools.setStatus
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectionExtensionsTest : DatabaseTestCase() {

    @Test
    @Throws(Exception::class)
    fun connectionInitWithProviderDataTest() {
        var connection = Connection().initWithProviderData(ProviderData(
                code = "demobank",
                name = "Demobank",
                connectUrl = "url",
                logoUrl = "url",
                version = "1")
        )

        Assert.assertTrue(connection.guid.isNotEmpty())
        assertThat(connection.id, equalTo(""))
        assertThat(connection.createdAt, greaterThan(0L))
        assertThat(connection.updatedAt, greaterThan(0L))
        assertThat(connection.name, equalTo("Demobank"))
        assertThat(connection.code, equalTo("demobank"))
        assertThat(connection.connectUrl, equalTo("url"))
        assertThat(connection.logoUrl, equalTo("url"))
        assertThat(connection.accessToken, equalTo(""))
        assertThat(connection.status, equalTo(ConnectionStatus.INACTIVE.toString()))

        connection = connection.initWithProviderData(ProviderData(
                connectUrl = "url1",
                code = "code2",
                name = "name3",
                logoUrl = "url4",
                version = "1")
        )

        Assert.assertTrue(connection.guid.isNotEmpty())
        assertThat(connection.id, equalTo(""))
        assertThat(connection.createdAt, greaterThan(0L))
        assertThat(connection.updatedAt, greaterThan(0L))
        assertThat(connection.name, equalTo("name3"))
        assertThat(connection.code, equalTo("code2"))
        assertThat(connection.connectUrl, equalTo("url1"))
        assertThat(connection.logoUrl, equalTo("url4"))
        assertThat(connection.accessToken, equalTo(""))
        assertThat(connection.status, equalTo(ConnectionStatus.INACTIVE.toString()))
    }

    @Test
    @Throws(Exception::class)
    fun connectionGetStatusTest() {
        val connection = Connection().setStatus(ConnectionStatus.INACTIVE)

        assertThat(connection.status, equalTo(ConnectionStatus.INACTIVE.toString()))
        assertThat(connection.getStatus(), equalTo(ConnectionStatus.INACTIVE))
    }

    @Test
    @Throws(Exception::class)
    fun connectionToConnectionAndKeyTest() {
        KeyStoreManager.deleteKeyPair("guid1")
        val pair: ConnectionAndKey? = Connection().setGuid("guid1").toConnectionAndKey(KeyStoreManager)

        Assert.assertNull(pair)

        KeyStoreManager.createNewRsaKeyPair("guid1")

        Assert.assertNotNull(Connection().setGuid("guid1").toConnectionAndKey(KeyStoreManager)!!.key)
    }

    @Test
    @Throws(Exception::class)
    fun connectionDeleteTest() {
        val connection1 = Connection().setGuid("guid1").save()
        Connection().setGuid("guid2").save()

        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(2L))

        connection1?.delete()

        assertThat(ConnectionsRepository.getConnectionsCount(), equalTo(1L))
    }
}
