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
package com.saltedge.authenticator.features.authorizations.common

import com.saltedge.authenticator.model.db.Connection
import com.saltedge.authenticator.sdk.model.AuthorizationData
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthorizationViewModelTest {

    private val model = AuthorizationViewModel(
        authorizationId = "444",
        authorizationCode = "111",
        title = "title",
        description = "description",
        expiresAt = DateTime(),
        connectionId = "333",
        connectionName = "Demobank",
        connectionLogoUrl = "url",
        validSeconds = 300,
        isProcessing = false,
        createdAt = DateTime()
    )

    @Test
    @Throws(Exception::class)
    fun isExpiredTest() {
        val now = DateTime.now()

        Assert.assertFalse(model.copy(expiresAt = now.plusMinutes(1)).isExpired())
        Assert.assertTrue(model.copy(expiresAt = now.minusMinutes(1)).isExpired())
    }

    @Test
    @Throws(Exception::class)
    fun isNotExpiredTest() {
        val now = DateTime.now()

        Assert.assertTrue(model.copy(expiresAt = now.plusMinutes(1)).isNotExpired())
        Assert.assertFalse(model.copy(expiresAt = now.minusMinutes(1)).isNotExpired())
    }

    @Test
    @Throws(Exception::class)
    fun remainedTimeTillExpireTest() {
        val now = DateTime.now()

        assertThat(model.copy(expiresAt = now.plusMinutes(1)).remainedTimeStringTillExpire(),
                anyOf(equalTo("0:59"), equalTo("1:00")))
    }

    @Test
    @Throws(Exception::class)
    fun remainedSecondsTillExpireTest() {
        val now = DateTime.now()

        assertThat(
            model.copy(expiresAt = now.plusMinutes(1)).remainedSecondsTillExpire(),
            anyOf(equalTo(59), equalTo(60))
        )
    }

    @Test
    @Throws(Exception::class)
    fun authorizationDataToAuthorizationViewModelTest() {
        val data = AuthorizationData(
            id = "444",
            authorizationCode = "111",
            title = "title",
            description = "description",
            createdAt = DateTime(0L),
            expiresAt = DateTime(300000L),
            connectionId = "333"
        )
        val connection = Connection().apply {
            id = "333"
            name = "Demobank"
            logoUrl = "url"
        }

        assertThat(
            data.toAuthorizationViewModel(connection = connection),
            equalTo(
                AuthorizationViewModel(
                    authorizationId = "444",
                    authorizationCode = "111",
                    title = "title",
                    description = "description",
                    expiresAt = DateTime(300000L),
                    connectionId = "333",
                    connectionName = "Demobank",
                    connectionLogoUrl = "url",
                    validSeconds = 300,
                    isProcessing = false,
                    createdAt = DateTime(0L)
                )
            )
        )
    }
}
