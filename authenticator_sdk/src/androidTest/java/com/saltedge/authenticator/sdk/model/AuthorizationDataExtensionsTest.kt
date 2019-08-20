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

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthorizationDataExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun isNotExpiredTest() {
        val authData = AuthorizationData(
            id = "444",
            title = "title",
            description = "description",
            connectionId = "333",
            expiresAt = DateTime(),
            authorizationCode = "111"
        )

        Assert.assertTrue(authData.copy(expiresAt = DateTime.now().plusMinutes(1)).isNotExpired())
        Assert.assertFalse(authData.copy(expiresAt = DateTime.now().minusMinutes(1)).isNotExpired())
    }
}
