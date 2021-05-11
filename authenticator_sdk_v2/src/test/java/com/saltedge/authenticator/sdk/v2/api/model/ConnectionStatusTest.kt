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
package com.saltedge.authenticator.sdk.v2.api.model

import com.saltedge.authenticator.core.model.ConnectionStatus
import com.saltedge.authenticator.core.model.toConnectionStatus
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Test

class ConnectionStatusTest {

    @Test
    @Throws(Exception::class)
    fun valuesTest() {
        assertThat(
            ConnectionStatus.values(),
            equalTo(arrayOf(ConnectionStatus.INACTIVE, ConnectionStatus.ACTIVE))
        )
    }

    @Test
    @Throws(Exception::class)
    fun valueOfTest() {
        assertThat(ConnectionStatus.valueOf("INACTIVE"), equalTo(ConnectionStatus.INACTIVE))
        assertThat(ConnectionStatus.valueOf("ACTIVE"), equalTo(ConnectionStatus.ACTIVE))
    }

    @Test
    @Throws(Exception::class)
    fun toConnectionStatusTest() {
        assertThat("INACTIVE".toConnectionStatus(), equalTo(ConnectionStatus.INACTIVE))
        assertThat("ACTIVE".toConnectionStatus(), equalTo(ConnectionStatus.ACTIVE))
        Assert.assertNull("UNKNOWN".toConnectionStatus())
    }
}
