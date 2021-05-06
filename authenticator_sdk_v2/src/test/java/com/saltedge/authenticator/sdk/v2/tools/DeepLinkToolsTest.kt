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

package com.saltedge.authenticator.sdk.v2.tools

import com.saltedge.authenticator.sdk.v2.api.model.appLink.ConnectAppLinkDataV2
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeepLinkToolsTest {

    @Test
    @Throws(Exception::class)
    fun isValidDeeplinkTest() {
        Assert.assertFalse("".isValidDeeplink())
        Assert.assertFalse("test".isValidDeeplink())
        Assert.assertFalse("https://google.com".isValidDeeplink())
        Assert.assertFalse("authenticator://saltedge.com/connect?configuration=https://localhost/configuration".isValidDeeplink())
        Assert.assertTrue("authenticator://saltedge.com/connect?configuration=https://example.com/configuration".isValidDeeplink())
        Assert.assertTrue("authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890".isValidDeeplink())
    }

    @Test
    @Throws(Exception::class)
    fun extractConnectAppLinkDataTest() {
        assertNull("".extractConnectAppLinkDataV2())
        assertNull("test".extractConnectAppLinkDataV2())
        assertNull("....".extractConnectAppLinkDataV2())
        assertNull("////".extractConnectAppLinkDataV2())
        assertNull("https://google.com".extractConnectAppLinkDataV2())
        assertNull("authenticator://saltedge.com/connect?configuration=https://localhost/configuration".extractConnectAppLinkDataV2())
        assertNull("authenticator://saltedge.com/connect?configuration=https://backend/api/authenticator/v1/configuration".extractConnectAppLinkDataV2())
        assertThat(
            "authenticator://saltedge.com/connect?configuration=https://example.com/configuration".extractConnectAppLinkDataV2(),
            equalTo(ConnectAppLinkDataV2(configurationUrl = "https://example.com/configuration", connectQuery = null))
        )
        assertThat(
            "authenticator://saltedge.com/connect?configuration=https://example.com/configuration&connect_query=1234567890".extractConnectAppLinkDataV2(),
            equalTo(ConnectAppLinkDataV2(configurationUrl = "https://example.com/configuration", connectQuery = "1234567890"))
        )
    }
}
