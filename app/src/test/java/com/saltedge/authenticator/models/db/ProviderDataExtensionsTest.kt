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
package com.saltedge.authenticator.models.db

import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.features.connections.create.toConnection
import com.saltedge.authenticator.sdk.api.model.configuration.ProviderConfigurationData
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProviderDataExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun providerDataToConnectionTest() {
        var connection = ProviderConfigurationData(
            code = "demobank",
            name = "Demobank",
            connectUrl = "url",
            logoUrl = "url",
            version = "1",
            supportEmail = "exampple1@saltedge.com",
            consentManagementSupported = true,
            geolocationRequired = true
        ).toConnection()!!

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
        assertTrue(connection.consentManagementSupported!!)
        assertTrue(connection.geolocationRequired!!)

        connection = ProviderConfigurationData(
            connectUrl = "url1",
            code = "code2",
            name = "name3",
            logoUrl = "url4",
            version = "1",
            supportEmail = "example2@saltedge.com",
            geolocationRequired = false
        ).toConnection()!!

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
        assertFalse(connection.consentManagementSupported!!)
        assertFalse(connection.geolocationRequired!!)

        Assert.assertNull(
            ProviderConfigurationData(
                connectUrl = "",
                code = "",
                name = "",
                logoUrl = "",
                version = "0",
                supportEmail = "",
                geolocationRequired = null
            ).toConnection()
        )
    }
}
