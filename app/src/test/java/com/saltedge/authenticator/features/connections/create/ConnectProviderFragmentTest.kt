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
package com.saltedge.authenticator.features.connections.create

import com.saltedge.authenticator.app.KEY_GUID
import com.saltedge.authenticator.sdk.model.appLink.ConnectAppLinkData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectProviderFragmentTest {

    @Test
    @Throws(Exception::class)
    fun newInstanceTestCase1() {
        val arguments = ConnectProviderFragment.newInstance(connectionGuid = "guid1").arguments!!

        assertThat(arguments.getString(KEY_GUID), equalTo("guid1"))
        assertNull(arguments.getSerializable(ConnectProviderFragment.KEY_CONNECT_DATA))
    }

    @Test
    @Throws(Exception::class)
    fun newInstanceTestCase2() {
        val arguments = ConnectProviderFragment.newInstance(
            connectAppLinkData = ConnectAppLinkData("https://www.my_host.com/configuration")
        ).arguments!!

        assertNull(arguments.getString(KEY_GUID))
        assertThat(
            arguments.getSerializable(ConnectProviderFragment.KEY_CONNECT_DATA) as ConnectAppLinkData,
            equalTo(ConnectAppLinkData("https://www.my_host.com/configuration"))
        )
    }
}
